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
import ZgazeniSendvic.Server_Back_ISS.websocket.RideTrackingWebSocketService;
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
    @Autowired
    RideTrackingWebSocketService rideTrackingWebSocketService;

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

    @Override
    public Ride convertToRide(RideRequest request, Driver driver) {

        Ride ride = new Ride();

        ride.setDriver(driver);
        ride.setCreator(request.getCreator());
        ride.setPassengers(request.getInvitedPassengers());
        ride.setLocations(request.getLocations());
        ride.setScheduledTime(request.getScheduledTime());
        ride.setTotalPrice(request.getEstimatedPrice());
        ride.setTotalPrice(request.getEstimatedPrice());
        ride.setStatus(RideStatus.SCHEDULED);

        return allRides.save(ride);
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
        ride.setStartTime(LocalDateTime.now());
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
        ride.setTotalPrice(result.getPrice());
        ride.setStatus(RideStatus.FINISHED);
        ride.setEndTime(stopReq.getCurrentTime());
        allRides.save(ride);
        allRides.flush();

        //should I call end ride here too?

        RideStoppedDTO stopped = new RideStoppedDTO(rideID, ride.getTotalPrice(),  ride.getLocations());
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
            ride.setTotalPrice(rideEndDTO.getPrice());
        }
        ride.setStatus(RideStatus.FINISHED);

        allRides.save(ride);
        allRides.flush();

        sendFinalRideUpdate(ride);

        List<Account> passengers = ride.getPassengers();
        if (passengers != null && !passengers.isEmpty()) {
            for (Account passenger : passengers) {
                if (passenger == null || passenger.getEmail() == null) continue;
                try {
                    EmailDetails details = new EmailDetails();
                    details.setRecipient(passenger.getEmail());
                    details.setSubject("Ride ended");
                    details.setMsgBody("Your ride (ID: " + ride.getId() + ") has ended. Final price: " + ride.getTotalPrice());
                    emailService.sendSimpleMail(details);
                } catch (Exception ex) {
                    System.err.println("Failed to send ride-ended email to " + passenger.getEmail() + ": " + ex.getMessage());
                }
            }
        }
    }

    private void sendFinalRideUpdate(Ride ride) {
        try {
            if (ride.getPassengers() != null) {
                for (Account passenger : ride.getPassengers()) {
                    rideTrackingWebSocketService.sendRideUpdateToUser(passenger.getId(), ride.getId());
                }
            }
            if (ride.getCreator() != null) {
                rideTrackingWebSocketService.sendRideUpdateToUser(ride.getCreator().getId(), ride.getId());
            }
            if (ride.getDriver() != null) {
                rideTrackingWebSocketService.sendRideUpdateToUser(ride.getDriver().getId(), ride.getId());
            }
        } catch (Exception ex) {
            System.err.println("Failed to send final ride update via WebSocket for ride " + ride.getId() + ": " + ex.getMessage());
        }
    }

    public RidesOverviewDTO getRidesOverview() {
        List<Ride> rides = allRides.findAll();
        List<ActiveRideDTO> activeRidesList = new ArrayList<>();

        for (Ride ride : rides) {
            if (ride.getStatus() == RideStatus.SCHEDULED || ride.getStatus() == RideStatus.ACTIVE) {
                ActiveRideDTO dto = new ActiveRideDTO();
                dto.setId(ride.getId());

                if (ride.getLocations() != null && !ride.getLocations().isEmpty()) {
                    dto.setOrigin(ride.getLocations().get(0));
                    if (ride.getLocations().size() > 1) {
                        dto.setDestination(ride.getLocations().get(ride.getLocations().size() - 1));
                    }
                }

                if (ride.getStartTime() != null) {
                    dto.setDepartureTime(ride.getStartTime().toString());
                }
                if (ride.getEndTime() != null) {
                    dto.setArrivalTime(ride.getEndTime().toString());
                }

                dto.setPanic(ride.isPanic());
                dto.setStatus(ride.getStatus().toString());
                dto.setPrice(ride.getTotalPrice());

                if (ride.getDriver() != null && ride.getDriver().getEmail() != null) {
                    dto.setDriverEmail(ride.getDriver().getEmail());
                }

                if (ride.getStartTime() != null) {
                    dto.setDate(ride.getStartTime().toLocalDate().toString());
                }

                activeRidesList.add(dto);
            }
        }

        return new RidesOverviewDTO(activeRidesList);
    }

    @Override
    public Ride delete(Long rideId) {
        return null;
    }

    @Override
    public void deleteAll() {

    }


}
