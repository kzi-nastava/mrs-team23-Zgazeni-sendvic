package ZgazeniSendvic.Server_Back_ISS.service;

import ZgazeniSendvic.Server_Back_ISS.dto.*;
import ZgazeniSendvic.Server_Back_ISS.exception.RideNotFoundException;
import ZgazeniSendvic.Server_Back_ISS.model.Account;
import ZgazeniSendvic.Server_Back_ISS.model.Ride;
import ZgazeniSendvic.Server_Back_ISS.repository.AccountRepository;
import ZgazeniSendvic.Server_Back_ISS.model.*;
import ZgazeniSendvic.Server_Back_ISS.repository.RideRepository;
import ZgazeniSendvic.Server_Back_ISS.repository.PanicNotificationRepository;
import ZgazeniSendvic.Server_Back_ISS.security.CustomUserDetails;
import ZgazeniSendvic.Server_Back_ISS.security.jwt.JwtUtils;
import jakarta.transaction.Transactional;
import ZgazeniSendvic.Server_Back_ISS.security.EmailDetails;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class RideServiceImpl implements IRideService {

    @Autowired
    RideRepository allRides;
    @Autowired
    AccountRepository allAccounts;
    @Autowired
    RideRepository rideRepo;
    @Autowired
    DriverMatchingService matcher;
    @Autowired
    private EmailService emailService;
    @Autowired
    OrsRoutingService orsRoutingService;
    @Autowired
    PanicNotificationRepository panicNotificationRepository;
    @Autowired
    JwtUtils jwtUtils;

    @Override
    public Collection<Ride> getAll() {
        return List.of();
    }

    @Override
    public Ride findRide(Long rideId) {
        Optional<Ride> found = allRides.findById(rideId);
        if (found.isEmpty()) {
            String value = "Ride was not found";
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, value);
        }
        return found.get();
    }

    public Ride createRide(RideRequest req) {

        List<Driver> activeDrivers = allAccounts.findAvailableDrivers();

        if (activeDrivers.isEmpty()) {
            sendNoDriversEmail(req.getCreator());
            throw new RuntimeException("No drivers available");
        }

        Driver selected = matcher.findBestDriver(
                activeDrivers,
                req.getLocations().get(0)
        );

        if (selected == null) {
            sendNoDriversEmail(req.getCreator());
            throw new RuntimeException("No suitable drivers");
        }

        Ride ride = new Ride();
        ride.setDriver(selected);
        ride.setCreator(req.getCreator());
        ride.setPassengers(req.getInvitedPassengers());
        ride.setLocations(req.getLocations());
        ride.setPrice(req.getEstimatedPrice());
        ride.setStatus(RideStatus.SCHEDULED);

        rideRepo.save(ride);

        sendRideAcceptedEmail(req.getCreator(), ride);
        sendNewRideForDriverEmail(selected, ride);

        return ride;
    }

    private void sendNoDriversEmail(Account user) {
        EmailDetails details = new EmailDetails();
        details.setRecipient(user.getEmail());
        details.setSubject("Ride request failed");
        details.setMsgBody(
                "Unfortunately, there are currently no available drivers. " +
                        "Please try again later."
        );

        emailService.sendSimpleMail(details);
    }

    private void sendRideAcceptedEmail(Account user, Ride ride) {
        EmailDetails details = new EmailDetails();
        details.setRecipient(user.getEmail());
        details.setSubject("Your ride has been accepted");
        details.setMsgBody(
                "Your ride has been successfully scheduled.\n\n" +
                        "Driver: " + ride.getDriver().getName() + "\n" +
                        "Estimated price: " + ride.getPrice()
        );

        emailService.sendSimpleMail(details);
    }

    private void sendNewRideForDriverEmail(Driver driver, Ride ride) {
        EmailDetails details = new EmailDetails();
        details.setRecipient(driver.getEmail());
        details.setSubject("New ride assigned");
        details.setMsgBody(
                "You have been assigned a new ride.\n\n" +
                        "Pickup location: " + ride.getLocations().get(0) + "\n" +
                        "Scheduled time: " + ride.getStartTime()
        );

        emailService.sendSimpleMail(details);
    }

    @Override
    public Ride insert(Ride ride) {
        //Do not need to check if it exists already
        Ride ret = allRides.save(ride);
        allRides.flush();
        return ret;
    }

    @Override
    public Ride update(Long rideID, DriveCancelDTO rideDTO) {
        return null;
    }

    @Override
    public DriveCancelledDTO updateCancel(Long rideID, DriveCancelDTO rideDTO) {
        Optional<Ride> found = allRides.findById(rideID);
        if(found.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ride not found");
        }

        Ride ride = found.get();
        canCancelRide(ride, rideDTO); //will throw if can't otherwise goes through

        ride.setStatus(RideStatus.CANCELED);
        setCanceler(ride);
        allRides.save(ride);
        allRides.flush();

        //some basic info to be showcased to everyone who needs to see it, if anyone except the one who ordered the rid
        DriveCancelledDTO cancelled = new DriveCancelledDTO();
        cancelled.setCancelled(true);
        cancelled.setRideID(rideID);
        cancelled.setReason(rideDTO.getReason());
        cancelled.setTime(LocalDateTime.now());

        return cancelled;

    }

    //Sets canceler if there is one
    public void setCanceler(Ride ride){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof AnonymousAuthenticationToken) {
            ride.setCanceler(null);
            return;
        }
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        Account canceler = userDetails.getAccount();
        ride.setCanceler(canceler);
    }

    public void canCancelRide(Ride ride, DriveCancelDTO rideDTO) {
        //If not scheduled, then it is at least active, already canceled etc., so it can't be canceled

        //for testing purposes, make a token and print it out
        //String testingToken = jwtUtils.generateRideToken(ride);
        //System.out.println("Generated ride token for testing: " + testingToken);

        if (ride.getStatus() != RideStatus.SCHEDULED) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Ride is not scheduled");
        }

        LocalDateTime now = LocalDateTime.now();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        //If not logged in
        if (auth instanceof AnonymousAuthenticationToken) {
            validateAndCancelAsAnonymous(ride, rideDTO, now);
            return;
            }

        //else if logged in
        try{
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        Account requester = userDetails.getAccount();

        //check if it is the driver of the ride
        if(Objects.equals(ride.getDriver().getId(), requester.getId())){
            //if it is, allow unless no reason
            if(rideDTO.getReason() == null || rideDTO.getReason().isBlank()){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reason for cancellation must be provided by the driver");
            }
            return;

        }
        //now check if passenger, if it is compare dates using the the 10 min func
            boolean isPassenger = false;
            for(Account passenger : ride.getPassengers()){
                if(Objects.equals(passenger.getId(), requester.getId())){
                    isPassenger = true;
                    break;
                }
            }
            if(isPassenger){
                if(isAtLeastTenMinutesBeforeRide(ride, now)){
                    return;
                }
                else{
                    throw new AccessDeniedException("Too late to cancel");
                }
            }
            throw new AccessDeniedException("Not passenger or Driver");

            //JUST NULLPOINTER
        }catch ( NullPointerException ex){
            //shouldn't really occur, but if it does, token somewhere failed, send access denied
            throw new AccessDeniedException("Failed Authentication");
        }
    }

    private void validateAndCancelAsAnonymous(Ride ride, DriveCancelDTO rideDTO, LocalDateTime now) {
        String token = rideDTO.getRideToken();
        jwtUtils.validateRideToken(token);

        Long rideIdFromToken = jwtUtils.getRideIdFromToken(token);
        if (!Objects.equals(rideIdFromToken, ride.getId())) {
            throw new AccessDeniedException("Invalid ride token for this ride");
        }

        if (!isAtLeastTenMinutesBeforeRide(ride, now)) {
            throw new AccessDeniedException("Too late to cancel for unauthenticated user");
        }
    }

    private boolean isAtLeastTenMinutesBeforeRide(Ride ride, LocalDateTime timeOfRequest) {
        LocalDateTime rideStartTime = ride.getStartTime();
        if (rideStartTime == null) {
            throw new IllegalStateException("Ride start time is not set");
        }
        return timeOfRequest.isBefore(rideStartTime.minusMinutes(10));

    }



    public void DummyRideInit(){

        Ride dummyRide = new Ride(
                1L,
                new Driver(),
                new Account(),
                new ArrayList<Account>(),
                new ArrayList<Location>(),
                1000.00,
                LocalDateTime.now(),
                LocalDateTime.now(),
                RideStatus.SCHEDULED,
                false
        );

        insert(dummyRide);

    }

    public RouteEstimationDTO routeEstimate(String destinationsStr){
        //would calculate based on route, however still not implemented so return is fixed for now
        //google maps has API for this
        //replace any whitespace, + = all instances
        destinationsStr = destinationsStr.replaceAll("\\s+", "");

        List<String> locations = List.of(destinationsStr.split(","));


        RouteEstimationDTO route = new RouteEstimationDTO(locations, locations.size()*10);
        return route;


    }

    public void startRide(Long rideId) {
        Ride ride = allRides.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));

        if (ride.isCanceled()) {
            throw new RuntimeException("Cannot start a canceled ride");
        }

        if (ride.isStarted()) {
            throw new RuntimeException("Ride already started");
        }

        ride.setStatus(RideStatus.ACTIVE);
        ride.setStartTime(LocalDateTime.now()); // optional but realistic
        allRides.save(ride);
    }

    public RideStoppedDTO stopRide(Long rideID, RideStopDTO stopReq){

        Optional<Ride> found = allRides.findById(rideID);
        if (found.isEmpty()) {
            String value = "Ride was not found";
            throw new RideNotFoundException(value);
        }
        //validation for possible nulls
        orsRoutingService.validateCoordinatesLocations(stopReq.getPassedLocations());


        Ride ride = getRideActiveAndDriver(found);

        ArrayList<Location> passedLocations = new ArrayList<Location>(stopReq.getPassedLocations());
        ride.changeLocations(passedLocations);
        ride.setEndTime(stopReq.getCurrentTime());

        List<List<Double>> coordinates = new ArrayList<>();

        for (Location loc : passedLocations) {
            coordinates.add(Arrays.asList(
                    loc.getLongitude(),
                    loc.getLatitude()
            ));
        }
        OrsRouteResult result = orsRoutingService.getFastestRouteWithPath(coordinates);
        ride.setPrice(result.getPrice());
        ride.setStatus(RideStatus.FINISHED);
        ride.setEndTime(stopReq.getCurrentTime());
        allRides.save(ride);
        allRides.flush();

        //should I call end ride here too?

        RideStoppedDTO stopped = new RideStoppedDTO(rideID, ride.getPrice(),  ride.getLocations());
        return stopped;
    }

    private static @NonNull Ride getRideActiveAndDriver(Optional<Ride> found) {
        Ride ride = found.get();
        //ride should be active, so that check ought to exist as well
        if(ride.getStatus() != RideStatus.ACTIVE){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ride is not active");
        }

        //finally, the one who ordered the stoppage should be THE DRIVER, which also means AUTHENTICATED
        //so I check if the authenticated user is the driver OF THE RIDE ITSELF, if not, throw
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();



        //assert auth != null;
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        //assert userDetails != null;
        Account driver = userDetails.getAccount();
        if(!Objects.equals(ride.getDriver().getId(), driver.getId())){
            throw new AccessDeniedException("Only the driver can stop the ride");
        }
        return ride;
    }

    public void endRide(RideEndDTO rideEndDTO) {
        if (rideEndDTO == null || rideEndDTO.getRideId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "rideId must be provided");
        }
        Optional<Ride> found = allRides.findById(rideEndDTO.getRideId());
        if (found.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ride not found");
        }
        Ride ride = found.get();
        if (rideEndDTO.getPrice() != null) {
            ride.setPrice(rideEndDTO.getPrice());
        }
        ride.setStatus(RideStatus.FINISHED);

        allRides.save(ride);
        allRides.flush();

        List<Account> passengers = ride.getPassengers();
        if (passengers != null && !passengers.isEmpty()) {
            for (Account passenger : passengers) {
                if (passenger == null || passenger.getEmail() == null) continue;
                try {
                    EmailDetails details = new EmailDetails();
                    details.setRecipient(passenger.getEmail());
                    details.setSubject("Ride ended");
                    details.setMsgBody("Your ride (ID: " + ride.getId() + ") has ended. Final price: " + ride.getPrice());
                    emailService.sendSimpleMail(details);
                } catch (Exception ex) {
                    System.err.println("Failed to send ride-ended email to " + passenger.getEmail() + ": " + ex.getMessage());
                }
            }
        }
    }

    @Override
    public Ride delete(Long rideId) {
        return null;
    }

    @Override
    public void deleteAll() {

    }

    @Transactional
    public PanicNotificationDTO PanicRide(Long rideID) {
        //pull out account from auth
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof AnonymousAuthenticationToken) {
            throw new AccessDeniedException("Unauthenticated user can't panic the ride");
        }
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        Account account = userDetails.getAccount();


        Optional<Ride> foundRide = allRides.findById(rideID);

        if(foundRide.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ride not found");
        }

        if(account == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found");
        }

        Ride ride = getRideForPanic(foundRide, account);

        // Create and save panic notification
        PanicNotification panicNotification = new PanicNotification(account, ride, LocalDateTime.now());
        panicNotificationRepository.save(panicNotification);
        panicNotificationRepository.flush();

        ride.setPanic(true);
        allRides.save(ride);
        allRides.flush();

        PanicNotificationDTO notificationDTO = new PanicNotificationDTO(
                panicNotification.getId(),
                account.getId(),
                account.getName() + " " + account.getLastName(),
                ride.getId(),
                panicNotification.getCreatedAt(),
                panicNotification.isResolved()
        );

        return notificationDTO;

    }

    private static @NonNull Ride getRideForPanic(Optional<Ride> foundRide, Account account) {
        Ride ride = (Ride) foundRide.get();

        //ride must be currently Active, and the presser must be either a passenger or the driver, otherwise, throw
        if(ride.getStatus() != RideStatus.ACTIVE){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only active rides can be panicked");
        }
        boolean isDriver = Objects.equals(ride.getDriver().getId(), account.getId());
        boolean isPassenger = false;
        for(Account passenger : ride.getPassengers()){
            if(Objects.equals(passenger.getId(), account.getId())){
                isPassenger = true;
                break;
            }
        }
        if(!isDriver && !isPassenger){
            throw new AccessDeniedException("Only passengers or driver can panic the ride");
        }

        // Check if panic notification already exists for this ride (prevent spamming)
        if(ride.isPanic()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Panic has already been activated for this ride");
        }
        return ride;
    }
}
