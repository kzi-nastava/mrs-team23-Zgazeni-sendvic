package ZgazeniSendvic.Server_Back_ISS.service;

import ZgazeniSendvic.Server_Back_ISS.dto.DriveCancelDTO;
import ZgazeniSendvic.Server_Back_ISS.dto.DriveCancelledDTO;
import ZgazeniSendvic.Server_Back_ISS.model.Ride;
import ZgazeniSendvic.Server_Back_ISS.repository.RideRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.beans.Transient;
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
    @Transient
    public DriveCancelledDTO updateCancel(Long rideID, DriveCancelDTO rideDTO) {
        Optional<Ride> found = allRides.findById(rideID);
        if(found.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ride cant be updated, was not found");
        }

        //here would be check for reason,

        //this is if it canceled
        Ride ride = found.get();
        ride.setCanceled(true);

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
                1L,                         // id
                "New York",                 // origin
                "Los Angeles",              // destination
                new Date(),                 // departureTime
                "30 minutes",               // timeLeft
                40.7128,                    // latitude
                -74.0060,                   // longitude
                false,                      // panic
                false,                      // canceled
                99.99,                      // price
                Arrays.asList("Chicago", "Denver") // locationsPassed
        );

        insert(dummyRide);

    }

    @Override
    public Ride delete(Long rideId) {
        return null;
    }

    @Override
    public void deleteAll() {

    }
}
