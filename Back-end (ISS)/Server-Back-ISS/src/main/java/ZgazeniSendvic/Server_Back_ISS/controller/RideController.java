package ZgazeniSendvic.Server_Back_ISS.controller;


import ZgazeniSendvic.Server_Back_ISS.dto.*;

import ZgazeniSendvic.Server_Back_ISS.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
// removed import of all dto's, might break

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import java.awt.print.Pageable;
import java.time.LocalDate;
// removed import of all utils, might break

@RestController
@RequestMapping("/api/")
class RideController {

    @Autowired
    RideServiceImpl rideService;
    @Autowired
    OrsRoutingService orsRoutingService;
    
    @PutMapping(path = "ride-cancel/{rideID}",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DriveCancelledDTO> cancelDrive(@RequestBody DriveCancelDTO cancelRequest,
                                                         @PathVariable Long rideID) throws Exception{

       //here I would pull out the email out of token if token is present
        // then would commence attaining the id based on that, by which I would decide wether or not to allow cancelling
        // for example if its a driver check the reason, otherwise check if too late, by comparing dates
        //if the check passes, ride is cancelled as showcased below

        //rideService.DummyRideInit();
        //rideService.DummyRideInit();
        DriveCancelledDTO cancelled = rideService.updateCancel(rideID,cancelRequest);


        return new ResponseEntity<DriveCancelledDTO>(cancelled, HttpStatus.OK);


    }


    @GetMapping(path = "ride-estimation/{arrival}/{destinationsStr}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OrsRouteResult>
    estimateRide(@PathVariable String arrival, @PathVariable String destinationsStr)throws Exception{


        //RouteEstimationDTO estimation = rideService.routeEstimate(arrival + "," + destinationsStr);
        OrsRouteResult result = orsRoutingService.getFastestRouteAddresses(arrival, destinationsStr);


        orsRoutingService.addressToCordinates("Novi Sad");
        return new ResponseEntity<OrsRouteResult>(result, HttpStatus.OK);


    }

    @PutMapping(path = "ride-tracking/stop/{rideID}",consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RideStoppedDTO> stopRide(@RequestBody RideStopDTO stopReq, @PathVariable Long rideID)
            throws Exception{

        RideStoppedDTO stopped  =  rideService.stopRide(rideID,stopReq);


        return new ResponseEntity<RideStoppedDTO>(stopped, HttpStatus.OK);

    }

    @GetMapping(path = "admin-HOR/{targetID}",consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ARideRequestedDTO>> adminRetrieveRides
            (@PathVariable Long targetID,
            Pageable pageable,
             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate)
    throws Exception{
        // here a service would go over the pageable and request params etc...

        ARideRequestedDTO ride = new ARideRequestedDTO(
                7L,
                Arrays.asList("Stop A", "Stop B", "Stop C"),
                "Start Stop",
                new Date(),
                new Date(System.currentTimeMillis() + 3600000),
                false,
                null,
                29.99,
                false
        );

        List<ARideRequestedDTO> allRides = new ArrayList<>();
        allRides.add(ride);

        return new ResponseEntity<List<ARideRequestedDTO>>(allRides, HttpStatus.OK);

    }

    @GetMapping(path = "admin-HOR-Detailed/{targetID}",consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ARideDetailsRequestedDTO> adminRetrieveDetailed(@PathVariable Long targetID)
            throws Exception{

        //would find based on id in service

        List<AHORAccountDetailsDTO> passengers = Arrays.asList(new AHORAccountDetailsDTO(), new AHORAccountDetailsDTO());
        AHORAccountDetailsDTO driver = new AHORAccountDetailsDTO();
        List<String> reports = Arrays.asList("Passenger was late","DRIVER was friendly");
        List<Integer> ratings = Arrays.asList(5, 4, 5);
        ARideDetailsRequestedDTO detailed = new ARideDetailsRequestedDTO(targetID,passengers,driver,reports,ratings);

        return new ResponseEntity<ARideDetailsRequestedDTO>(detailed, HttpStatus.OK);

    }

    @GetMapping(value = "future-rides",consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FutureRidesDTO> getFutureRides() {
        FutureRidesService futureRidesService = new FutureRidesService();
        FutureRidesDTO futureRidesDTO = futureRidesService.getFutureRides();
        return new ResponseEntity<FutureRidesDTO>(futureRidesDTO, HttpStatus.OK);
    }

    @GetMapping(value = "history-of-rides/{userId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<HistoryOfRidesDTO> HistoryOfRidesController( @PathVariable Long userId) {
        HistoryOfRidesService historyOfRidesService = new HistoryOfRidesService();
        HistoryOfRidesDTO historyOfRidesDTO = historyOfRidesService.getHistoryOfRides(userId);
        return ResponseEntity.ok(historyOfRidesDTO);
    }

    @GetMapping(value = "next-ride/{filter}",consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<NextRideDTO> getNextRide(@PathVariable("filter") String filter) {
        NextRideService nextRideService = new NextRideService();
        NextRideDTO nextRideDTO = new NextRideDTO();
        if (filter.equals("1")) {
            nextRideDTO = nextRideService.getNextRideClosest();
        } else if (filter.equals("2")) {
            nextRideDTO = nextRideService.getNextRideCostliest();
        }
        return new ResponseEntity<NextRideDTO>(nextRideDTO, HttpStatus.OK);
    }

    @PostMapping(value="ride-driver-rating/{userId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> rideDriverRating(@PathVariable("userId") Long userId, @RequestBody RideDriverRatingDTO rideDriverRatingDTO) {
        boolean success = RideDriverRatingService.saveRating(rideDriverRatingDTO);
        if (success) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(500).build();
        }
    }

    @PutMapping(
            value = "ride-start",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Void> rideStart(@RequestBody RideStartDTO rsDTO) {

        rideService.startRide(rsDTO.getRideId());
        return ResponseEntity.ok().build();
    }

    @PutMapping(value="ride-end/{userId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> rideEnd(@PathVariable("userId") Long userId, @RequestBody RideEndDTO rideEndDTO) {
        RideEndService rideEndService = new RideEndService();
        boolean success = rideEndService.RideEndService(rideEndDTO);
        if (success) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping(value = "ride-noting-user/{userId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> noteRide(@PathVariable("userId") Long userId, @RequestBody RideNoteDTO rideNoteDTO) {
        NoteAddingService noteAddingService = new NoteAddingService();
        boolean success = noteAddingService.addNoteToRide(rideNoteDTO.getRideId(), userId, rideNoteDTO.getNote());
        if (success) {
            System.out.println("Ride "+ rideNoteDTO.getRideId() +", note: " + rideNoteDTO.getNote());
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping(value = "ride-tracking-user/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<VehicleTrackerDTO> rideTracking( @PathVariable("id") Long id) {
        VehicleTrackerService vehicleTrackerService = new VehicleTrackerService();
        VehicleTrackerDTO vehicleTrackerDTO = vehicleTrackerService.getVehicleTrackingData(id);
        if (vehicleTrackerDTO == null) {
            return new ResponseEntity<VehicleTrackerDTO>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<VehicleTrackerDTO>(vehicleTrackerDTO, HttpStatus.OK);
    }

    @GetMapping(value = "vehicle-positions", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<VehiclePositionsDTO> getVehiclePositions() {
        VehiclePositionsService vehiclePositionsService = new VehiclePositionsService();
        List<VehiclePositionDTO> vehiclePositions = vehiclePositionsService.getAllVehiclePositions();
        VehiclePositionsDTO responseDTO = new VehiclePositionsDTO(vehiclePositions);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

}
