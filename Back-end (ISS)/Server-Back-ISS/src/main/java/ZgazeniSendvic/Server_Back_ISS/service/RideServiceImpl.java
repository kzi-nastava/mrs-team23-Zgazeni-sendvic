package ZgazeniSendvic.Server_Back_ISS.service;

import ZgazeniSendvic.Server_Back_ISS.dto.*;
import ZgazeniSendvic.Server_Back_ISS.model.Account;
import ZgazeniSendvic.Server_Back_ISS.model.Ride;
import ZgazeniSendvic.Server_Back_ISS.repository.AccountRepository;
import ZgazeniSendvic.Server_Back_ISS.model.*;
import ZgazeniSendvic.Server_Back_ISS.repository.AccountRepository;
import ZgazeniSendvic.Server_Back_ISS.repository.RideRepository;
import jakarta.transaction.Transactional;
import ZgazeniSendvic.Server_Back_ISS.security.EmailDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ride cant be cancelled, was not found");
        }

        //here would be check for reason,

        //this is if it canceled
        //NEEDS CHECK
        Ride ride = found.get();
         if(!canCancelRide(ride, rideDTO)){
            DriveCancelledDTO cancelled = new DriveCancelledDTO();
            cancelled.setCancelled(false);
            return cancelled;
        }



        ride.setStatus(RideStatus.CANCELED);
        allRides.save(ride);
        allRides.flush();

        //some basic info to be showcased to everyone who needs to see it, if anyone except the one who ordered the rid
        DriveCancelledDTO cancelled = new DriveCancelledDTO();
        cancelled.setCancelled(true);
        cancelled.setRideID(rideID);
        cancelled.setReason(rideDTO.getReason());
        cancelled.setRequesterName(rideDTO.getRequesterID());
        cancelled.setTime((new Date()).toString());

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

    public boolean canCancelRide(Ride ride, DriveCancelDTO rideDTO){
        //assuming both users and non users can cancel, I check, if driver cancelled immediately pass
        //otherwise compare dates, for 10 minute difference
        //now where could Driver role be? in securityContext for sure, as he would def be logged in?

        //hmm lets say through email then, if the token is present, well if auth is present at all
        //email will be there, so one can assume that I can always send the email of the authenticated user (1)
        //and if it is an unauthenticated user? how is an unauthenticated user connected to a ride he ordered?
        //

        /*

        emails are unique and tied to accounts and JWT tokens I use. if the user/driver is currently logged in then
        auth is present and the token for sure contains an email based on which I can pull out the user with the email
        from the database and check if present on ride as driver or passenger. if as passenger, do time check, if as
        rider let it be done. if nothing then disallow same as if time check fails. Now the only concern remaining is
        how this would be done with an unauthenticated user, as I know not how he would be connected to a ride in the
        first place. of course though he would have some form of unique Id he had given us, hmmmm
         */

        // Get principal returns userDetails, I set that up in tokenfilter as principal
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        //Interestingly, when security is on, but the request is unauthenticated, getPrincipal returns anonymousUser
        if(auth instanceof AnonymousAuthenticationToken){
            if(false){ //In the future, un. user gets a token perhaps Objects.equals(ride.getSHAToken(), rideDTO.getRideToken())
                //return compareDates(ride.getStartTime(), rideDTO.getTime(), 10);
                throw new AccessDeniedException("Unauthenticated No token");

            }
            throw new AccessDeniedException("Unauthenticated user didn't have right token");
        }

        //else
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        String email  = userDetails.getUsername();
        if(ride.getDriver().getEmail().equals(email)){
            //assuming proper reason
            //could also use token, though unneccessary
            return true;
        }

        if(ride.isThisPassenger(email)){
            //maybe also token comparison
            return isSameOrBefore(ride.getStartTime(), rideDTO.getTime(), ZoneId.systemDefault());

        }

        System.out.println("This shouldn't happen");
        return false;

    }
    private boolean compareDates(Date date1, Date date2, long minuteDifference, boolean allDate){
        //if diff is 10 minute or less, its cant be cancelled
        long diffMillis = date1.getTime() - date2.getTime();
        long tenMinutesMillis = minuteDifference * 60 * 1000;
        if (diffMillis < tenMinutesMillis) {
            System.out.println("Less than 10 minutes apart");
            return false;
        }
        return true;

    }

    public static boolean isSameOrBefore(
            LocalDateTime localDateTime,
            Date date,
            ZoneId zoneId
    ) {
        LocalDate refDate = localDateTime.toLocalDate();

        LocalDate dateAsLocal = date.toInstant()
                .atZone(zoneId)
                .toLocalDate();

        return !dateAsLocal.isAfter(refDate);
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
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, value);
        }

        ArrayList<Location> passedLocations = new ArrayList<Location>(stopReq.getPassedLocations());

        Ride ride = found.get();
        ride.changeLocations(passedLocations);
        ride.setEndTime(stopReq.getCurrentTime());
        //allRides.save(ride);
        //allRides.flush();

        //Now, I could recalculate based on allPassed, though if only the final dest was returned I couldnt do that
        //In the specification it says only the final Dest is passed, though then the change of midpoints would be
        //impossible, so I would just calc based on Starting-ending, though the way I did it I get access
        //to all destinations passed. For now I'll just assume my way is good, and use passedLocs.
        List<List<Double>> coordinates = new ArrayList<>();

        for (Location loc : passedLocations) {
            coordinates.add(Arrays.asList(
                    loc.getLongitude(),
                    loc.getLatitude()
            ));
        }
        OrsRouteResult result = orsRoutingService.getFastestRouteWithPath(coordinates);
        ride.setTotalPrice(result.getPrice());
        allRides.save(ride);
        allRides.flush();

        RideStoppedDTO stopped = new RideStoppedDTO(rideID, ride.getTotalPrice(),  ride.getLocations());
        return stopped;


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

    @Override
    public Ride delete(Long rideId) {
        return null;
    }

    @Override
    public void deleteAll() {

    }

    @Transactional
    public void PanicRide(Long rideID, String email) {

        Optional<Ride> foundRide = allRides.findById(rideID);
        Optional<Account> foundAccount = allAccounts.findByEmail(email);
        if(foundRide.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ride not found");
        }

        if(foundAccount.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found");
        }

        Ride ride = (Ride) foundRide.get();
        Account account = (Account) foundAccount.get();

        if(ride.getDriver() != account){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This individual is not the rider");
        }

        ride.setPanic(true);
        allRides.save(ride);
        allRides.flush();



    }
}
