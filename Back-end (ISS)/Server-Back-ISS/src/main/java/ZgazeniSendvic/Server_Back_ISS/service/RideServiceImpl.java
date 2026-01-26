package ZgazeniSendvic.Server_Back_ISS.service;

import ZgazeniSendvic.Server_Back_ISS.dto.*;
import ZgazeniSendvic.Server_Back_ISS.model.Account;
import ZgazeniSendvic.Server_Back_ISS.model.Ride;
import ZgazeniSendvic.Server_Back_ISS.repository.RideRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.ZonedDateTime;
import java.util.*;

@Service
public class RideServiceImpl implements IRideService {

    @Autowired
    RideRepository allRides;
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
        Ride ride = found.get();

        if(!canCancelRide(ride, rideDTO)){
            DriveCancelledDTO cancelled = new DriveCancelledDTO();
            cancelled.setCancelled(false);
            return cancelled;
        }

        ride.setCanceled(true);
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
            if(Objects.equals(ride.getSHAToken(), rideDTO.getRideToken())){
                return compareDates(ride.getDepartureTime(), rideDTO.getTime(), 10);

            }
            throw new AccessDeniedException("Unauthenticated user didn't have right token");
        }

        //else
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        String email  = userDetails.getUsername();
        if(ride.isDriver(email)){
            //assuming proper reason
            //could also use token, though unneccessary
            return true;
        }

        if(ride.isPassenger(email)){
            //maybe also token comparison
            return compareDates(ride.getDepartureTime(), rideDTO.getTime(), 10);

        }

        System.out.println("This shouldn't happen");
        return false;

    }
    private boolean compareDates(Date date1, Date date2, long minuteDifference){
        //if diff is 10 minute or less, its cant be cancelled
        long diffMillis = date1.getTime() - date2.getTime();
        long tenMinutesMillis = minuteDifference * 60 * 1000;
        if (diffMillis < tenMinutesMillis) {
            System.out.println("Less than 10 minutes apart");
            return false;
        }
        return true;

    }

    public void DummyRideInit(){

        Ride dummyRide = new Ride(
                                      // id
                "New York",                 // origin
                "Los Angeles",              // destination
                new Date(),                // departureTime
                new Date(),       // timeLeft
                40.7128,                    // latitude
                -74.0060,                   // longitude
                false,                      // panic
                false,                      // canceled
                99.99,                      // price
                Arrays.asList("Chicago", "Denver") // locationsPassed
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


    public RideStoppedDTO stopRide(Long rideID, RideStopDTO stopReq){

        Optional<Ride> found = allRides.findById(rideID);
        if (found.isEmpty()) {
            String value = "Ride was not found";
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, value);
        }

        ArrayList<String> passedLocations = new ArrayList<String> (List.of(stopReq.getPassedLocations().split(",")));

        Ride ride = found.get();
        ride.changeLocations(passedLocations); //this one sets all up, the first one, last one, and midpoints
        ride.setFinalDestTime(stopReq.getCurrentTime());

        //Now, I could recalculate based on allPassed, though if only the final dest was returned I couldnt do that
        //In the specification it says only the final Dest is passed, though then the change of midpoints would be
        //impossible, so I would just calc based on Starting-ending, though the way I did it I get access
        //to all destinations passed. For now I'll just assume my way is good, and use passedLocs.
        OrsRouteResult result = orsRoutingService.getFastestRouteAddresses(passedLocations);
        ride.setPrice(result.getPrice());
        allRides.save(ride);
        allRides.flush();

        RideStoppedDTO stopped = new RideStoppedDTO(rideID, ride.getPrice(),  ride.getAllDestinations());
        return stopped;





    }

    @Override
    public Ride delete(Long rideId) {
        return null;
    }

    @Override
    public void deleteAll() {

    }
}
