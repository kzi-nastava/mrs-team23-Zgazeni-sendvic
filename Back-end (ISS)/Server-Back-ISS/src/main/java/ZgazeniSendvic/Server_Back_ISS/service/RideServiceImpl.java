package ZgazeniSendvic.Server_Back_ISS.service;

import ZgazeniSendvic.Server_Back_ISS.dto.*;
import ZgazeniSendvic.Server_Back_ISS.model.*;
import ZgazeniSendvic.Server_Back_ISS.repository.AccountRepository;
import ZgazeniSendvic.Server_Back_ISS.repository.RideRepository;
import ZgazeniSendvic.Server_Back_ISS.security.EmailDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ride cant be updated, was not found");
        }

        //here would be check for reason,

        //this is if it canceled
        Ride ride = found.get();
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
                RideStatus.SCHEDULED
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
        allRides.save(ride);
        allRides.flush();

        RideStoppedDTO stopped = new RideStoppedDTO(rideID, ride.getPrice(),  ride.getLocations());
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
