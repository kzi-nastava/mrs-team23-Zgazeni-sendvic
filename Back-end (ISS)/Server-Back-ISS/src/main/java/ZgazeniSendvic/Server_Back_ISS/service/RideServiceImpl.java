package ZgazeniSendvic.Server_Back_ISS.service;

import ZgazeniSendvic.Server_Back_ISS.dto.*;
import ZgazeniSendvic.Server_Back_ISS.model.Ride;
import ZgazeniSendvic.Server_Back_ISS.model.Driver;
import ZgazeniSendvic.Server_Back_ISS.repository.RideRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
public class RideServiceImpl implements IRideService {

    @Autowired
    RideRepository allRides;

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
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ride cant be updated, was not found");
        }

        //here would be check for reason,

        //this is if it canceled
        Ride ride = found.get();
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

    public void DummyRideInit(){

        Ride dummyRide = new Ride(
                                      // id
                "New York",                 // origin
                "Los Angeles",              // destination
                new Date(),                 // departureTime
                new Date(),               // timeLeft
                40.7128,                    // latitude
                -74.0060,                   // longitude
                false,                      // panic
                false,                      // canceled
                false,                      // started
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

    public void startRide(Long rideId) {
        Ride ride = allRides.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));

        if (ride.isCanceled()) {
            throw new RuntimeException("Cannot start a canceled ride");
        }

        if (ride.isStarted()) {
            throw new RuntimeException("Ride already started");
        }

        ride.setStarted(true);
        ride.setDepartureTime(new Date()); // optional but realistic
        allRides.save(ride);
    }

    public RideStoppedDTO stopRide(Long rideID, RideStopDTO stopReq){

        Optional<Ride> found = allRides.findById(rideID);
        if (found.isEmpty()) {
            String value = "Ride was not found";
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, value);
        }

        ArrayList<String> passedLocations = new ArrayList<String> (List.of(stopReq.getPassedLocations().split(",")));

        Ride ride = found.get();
        ride.changeLocations(passedLocations);
        ride.setFinalDestTime(stopReq.getCurrentTime());
        allRides.save(ride);
        allRides.flush();

        RideStoppedDTO stopped = new RideStoppedDTO(rideID, ride.getPrice(),  ride.getAllDestinations());
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
            ride.setPrice(rideEndDTO.getPrice());
        }
        ride.setPaid(rideEndDTO.isPaid());
        ride.setEnded(rideEndDTO.isEnded());

        allRides.save(ride);
        allRides.flush();
    }

    @Override
    public Ride delete(Long rideId) {
        return null;
    }

    @Override
    public void deleteAll() {

    }
}
