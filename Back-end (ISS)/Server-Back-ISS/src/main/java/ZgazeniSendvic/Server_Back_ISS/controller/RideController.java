package ZgazeniSendvic.Server_Back_ISS.controller;


import ZgazeniSendvic.Server_Back_ISS.dto.*;

import ZgazeniSendvic.Server_Back_ISS.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import jakarta.validation.Valid;
// removed import of all dto's, might break

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import java.awt.print.Pageable;
import java.time.LocalDate;
// removed import of all utils, might break

@RestController
@CrossOrigin (origins="*")
@RequestMapping("/api/")
class RideController {

    @Autowired
    RideServiceImpl rideService;
    @Autowired
    OrsRoutingService orsRoutingService;


    @Autowired
    VehiclePositionsService vehiclePositionsService;

    @Autowired
    NoteAddingService noteAddingService;

    @Autowired
    RideDriverRatingService rideDriverRatingService;

    @Autowired
    HistoryOfRidesService historyOfRidesService;
    @Autowired
    PanicNotificationService panicNotificationService;

    @PutMapping(path = "ride-cancel/{rideID}",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DriveCancelledDTO> cancelDrive(@RequestBody DriveCancelDTO cancelRequest,
                                                         @PathVariable Long rideID) throws Exception{


        DriveCancelledDTO cancelled = rideService.updateCancel(rideID,cancelRequest);


        return new ResponseEntity<DriveCancelledDTO>(cancelled, HttpStatus.OK);


    }

    @PreAuthorize("hasAnyRole('DRIVER','ACCOUNT','USER')")
    @PostMapping(path = "ride-PANIC/{rideID}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> panicRide(@PathVariable Long rideID) throws Exception{

        PanicNotificationDTO notification = rideService.PanicRide(rideID);
        panicNotificationService.sendPanicNotificationEmails(notification);


        return new ResponseEntity<PanicNotificationDTO>(notification, HttpStatus.OK);


    }


    @PostMapping(path = "ride-estimation",consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OrsRouteResult>
    estimateRide(@RequestBody RouteEstimationRequestDTO estimationRequest)throws Exception{


        //RouteEstimationDTO estimation = rideService.routeEstimate(arrival + "," + destinationsStr);
        OrsRouteResult result = orsRoutingService.getFastestRouteAddresses(estimationRequest.getBeginningDestination(),
                estimationRequest.getEndingDestination());


        orsRoutingService.addressToCordinates("Novi Sad");
        return new ResponseEntity<OrsRouteResult>(result, HttpStatus.OK);


    }

    @PostMapping(path = "ride-estimation-coordinates",consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OrsRouteResult>
    estimateRideCords(@RequestBody RouteEstimationRequestLocationsDTO estimationRequest)throws Exception{


        //RouteEstimationDTO estimation = rideService.routeEstimate(arrival + "," + destinationsStr);
        OrsRouteResult result = orsRoutingService.getFastestRouteWithLocations (estimationRequest.getLocations());


        System.out.println(orsRoutingService.addressToCordinates("Beograd"));
        return new ResponseEntity<OrsRouteResult>(result, HttpStatus.OK);


    }

    @PreAuthorize("hasRole('DRIVER')")
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

    @GetMapping(value = "future-rides", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FutureRidesDTO> getFutureRides() {
        FutureRidesService futureRidesService = new FutureRidesService();
        FutureRidesDTO futureRidesDTO = futureRidesService.getFutureRides();
        return new ResponseEntity<FutureRidesDTO>(futureRidesDTO, HttpStatus.OK);
    }

    @GetMapping(value = "history-of-rides/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<HistoryOfRidesDTO> HistoryOfRidesController( @PathVariable Long userId) {
        HistoryOfRidesDTO historyOfRidesDTO = historyOfRidesService.getHistoryOfRides(userId);
        return ResponseEntity.ok(historyOfRidesDTO);
    }

    @GetMapping(value = "next-ride", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<NextRideDTO> getNextRide() {
        NextRideService nextRideService = new NextRideService();
        NextRideDTO nextRideDTO = nextRideService.getNextRideClosest();
        return new ResponseEntity<NextRideDTO>(nextRideDTO, HttpStatus.OK);
    }

    @PostMapping(value="ride-driver-rating/{userId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> rideDriverRating(@PathVariable("userId") Long userId, @RequestBody RideDriverRatingDTO rideDriverRatingDTO) {
        try {
            if (rideDriverRatingDTO.getUserId() == null) {
                rideDriverRatingDTO.setUserId(userId);
            }
            boolean success = rideDriverRatingService.saveRating(rideDriverRatingDTO);
            if (success) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(500).build();
            }
        } catch (org.springframework.web.server.ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).build();
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

    @PutMapping(value="ride-end", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> rideEnd(@RequestBody RideEndDTO rideEndDTO) {
        try {
            rideService.endRide(rideEndDTO);
            return ResponseEntity.ok().build();
        } catch (org.springframework.web.server.ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping(value = "ride-noting-user/{userId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> noteRide(@PathVariable("userId") Long userId, @Valid @RequestBody RideNoteDTO rideNoteDTO) {
        try {
            boolean success = noteAddingService.addNoteToRide(rideNoteDTO, userId);
            if (!success) {
                return ResponseEntity.status(500).build();
            }
            System.out.println("Ride " + rideNoteDTO.getRideId() + ", note: " + rideNoteDTO.getNote());
            return ResponseEntity.ok().build();
        } catch (org.springframework.web.server.ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).build();
        } catch (Exception e) {
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
        VehiclePositionsDTO responseDTO = vehiclePositionsService.getAllVehiclePositions();
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    @GetMapping(value = "vehicle-positions/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<VehiclePositionDTO> getVehiclePositionById(@PathVariable("id") Long id) {
        System.out.println("Fetching vehicle position with ID: " + id);
        VehiclePositionDTO dto = vehiclePositionsService.findById(id);
        return ResponseEntity.ok(dto);
    }

}
