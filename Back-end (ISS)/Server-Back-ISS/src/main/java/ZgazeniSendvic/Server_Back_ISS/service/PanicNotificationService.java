package ZgazeniSendvic.Server_Back_ISS.service;

import ZgazeniSendvic.Server_Back_ISS.dto.PanicNotificationDTO;
import ZgazeniSendvic.Server_Back_ISS.model.*;
import ZgazeniSendvic.Server_Back_ISS.repository.PanicNotificationRepository;
import ZgazeniSendvic.Server_Back_ISS.repository.RideRepository;
import ZgazeniSendvic.Server_Back_ISS.repository.VehicleRepository;
import ZgazeniSendvic.Server_Back_ISS.security.CustomUserDetails;
import ZgazeniSendvic.Server_Back_ISS.security.EmailDetails;
import jakarta.transaction.Transactional;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

@Service
public class PanicNotificationService {

    @Autowired
    private PanicNotificationRepository panicNotificationRepository;

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private VehicleRepository vehicleRepository;

    /**
     * Sends panic notification emails to all participants in the ride (driver and passengers)
     * @param panicNotificationDTO Contains rideID and caller information
     */
    public void sendPanicNotificationEmails(PanicNotificationDTO panicNotificationDTO) {
        // Validate DTO
        if (panicNotificationDTO == null || panicNotificationDTO.getRideId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid panic notification data");
        }

        // Find the ride
        Optional<Ride> rideOptional = rideRepository.findById(panicNotificationDTO.getRideId());
        if (rideOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ride not found");
        }

        Ride ride = rideOptional.get();

        // Send email to driver
        if (ride.getDriver() != null && ride.getDriver().getEmail() != null) {
            sendEmailToParticipant(
                    ride.getDriver(),
                    "PANIC ALERT: Emergency on Your Ride",
                    buildPanicEmailBody(panicNotificationDTO, ride, "Driver")
            );
        }

        // Send emails to all passengers
        if (ride.getPassengers() != null && !ride.getPassengers().isEmpty()) {
            for (Account passenger : ride.getPassengers()) {
                if (passenger != null && passenger.getEmail() != null) {
                    sendEmailToParticipant(
                            passenger,
                            "PANIC ALERT: Emergency on Your Ride",
                            buildPanicEmailBody(panicNotificationDTO, ride, "Passenger")
                    );
                }
            }
        }
    }

    /**
     * Sends email to a single participant
     */
    private void sendEmailToParticipant(Account account, String subject, String body) {
        try {
            EmailDetails details = new EmailDetails();
            details.setRecipient(account.getEmail());
            details.setSubject(subject);
            details.setMsgBody(body);
            emailService.sendSimpleMail(details);
        } catch (Exception e) {
            System.err.println("Failed to send panic notification email to " + account.getEmail() + ": " + e.getMessage());
        }
    }

    /**
     * Builds the email body for panic notification
     */
    private String buildPanicEmailBody(PanicNotificationDTO panicNotificationDTO, Ride ride, String recipientType) {
        return "EMERGENCY ALERT!\n\n" +
                "A panic button has been activated on a ride.\n\n" +
                "Ride Information:\n" +
                "- Ride ID: " + ride.getId() + "\n" +
                "- Caller: " + panicNotificationDTO.getCallerName() + " (" + recipientType + ")\n" +
                "- Alert Time: " + panicNotificationDTO.getCreatedAt() + "\n" +
                "- Driver: " + (ride.getDriver() != null ? ride.getDriver().getName() : "Unknown") + "\n" +
                "- Number of Passengers: " + (ride.getPassengers() != null ? ride.getPassengers().size() : 0) + "\n\n" +
                "Status: ACTIVE EMERGENCY\n\n" +
                "System Alert";
    }

    /**
     * Retrieves all unresolved panic notifications with pagination, ordered by newest first
     */
    public Page<PanicNotificationDTO> getUnresolvedPanicNotifications(Pageable pageable) {
        // Get page of unresolved panic notifications
        Page<PanicNotification> panicPage = panicNotificationRepository.findUnresolvedPanics(pageable);

        // Convert to DTO
        return panicPage.map(this::convertToDTO);
    }

    /**
     * Retrieves all panic notifications with pagination, ordered by newest first
     */
    public Page<PanicNotificationDTO> getAllPanicNotifications(Pageable pageable) {
        Page<PanicNotification> panicPage = panicNotificationRepository.findAll(pageable);

        // Convert to DTO and sort by newest first (descending)
        return panicPage.map(this::convertToDTO);
    }

    /**
     * Retrieves panic notifications for a specific ride
     */
    public PanicNotificationDTO getPanicNotificationByRideId(Long rideId) {
        Optional<PanicNotification> panicOptional = panicNotificationRepository.findByRideId(rideId);

        if (panicOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No panic notification found for this ride");
        }

        return convertToDTO(panicOptional.get());
    }

    /**
     * Marks a panic notification as resolved
     */
    public PanicNotificationDTO resolvePanicNotification(Long panicNotificationId) {
        Optional<PanicNotification> panicOptional = panicNotificationRepository.findById(panicNotificationId);

        if (panicOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Panic notification not found");
        }

        PanicNotification panic = panicOptional.get();
        panic.setResolved(true);
        PanicNotification saved = panicNotificationRepository.save(panic);

        //test for flushing

        return convertToDTO(saved);
    }

    /**
     * Converts PanicNotification entity to DTO
     */
    private PanicNotificationDTO convertToDTO(PanicNotification panic) {
        if (panic == null) {
            return null;
        }

        PanicNotificationDTO dto = new PanicNotificationDTO();
        dto.setId(panic.getId());
        dto.setCallerId(panic.getCaller() != null ? panic.getCaller().getId() : null);
        dto.setCallerName(panic.getCaller() != null ? panic.getCaller().getName() : "Unknown");
        dto.setRideId(panic.getRide() != null ? panic.getRide().getId() : null);
        dto.setCreatedAt(panic.getCreatedAt());
        dto.setResolved(panic.isResolved());

        return dto;
    }


    public boolean hasActivePanic(Long rideId) {
        Optional<PanicNotification> panic = panicNotificationRepository.findByRideId(rideId);
        return panic.isPresent() && !panic.get().isResolved();
    }


    //====================== Logic stuf ========================

    //Also marks vehicle
    @Transactional
    public PanicNotificationDTO PanicRide(Long rideID) {
        //pull out account from auth
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof AnonymousAuthenticationToken) {
            throw new AccessDeniedException("Unauthenticated user can't panic the ride");
        }
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        Account account = userDetails.getAccount();


        Optional<Ride> foundRide = rideRepository.findById(rideID);

        if(foundRide.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ride not found");
        }

        if(account == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found");
        }

        Ride ride = getRideForPanic(rideID, account);

        // Create and save panic notification
        PanicNotification panicNotification = new PanicNotification(account, ride, LocalDateTime.now());
        panicNotificationRepository.save(panicNotification);
        panicNotificationRepository.flush();

        ride.setPanic(true);
        //here should mark the vehicle, and resolving should unmark the vehicle?
        rideRepository.save(ride);
        rideRepository.flush();
        Vehicle vehicle = ride.getDriver().getVehicle();
        if(vehicle != null){
            vehicle.setPanicMark(true);
            vehicleRepository.save(vehicle);
        }


        PanicNotificationDTO notificationDTO = new PanicNotificationDTO(
                panicNotification.getId(),
                account.getId(),
                account.getName() + " " + account.getLastName(),
                ride.getId(),
                panicNotification.getCreatedAt(),
                panicNotification.isResolved(),
                null
        );

        return notificationDTO;

    }

    private  @NonNull Ride getRideForPanic(Long rideID, Account account) {

        Optional<Ride> foundRide = rideRepository.findById(rideID);

        if(foundRide.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ride not found");
        }

        if(account == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found");
        }

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

    @Transactional
    public PanicNotificationDTO resolvePanicById(Long panicNotificationId) {
        Optional<PanicNotification> panicOptional = panicNotificationRepository.findById(panicNotificationId);

        if (panicOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Panic notification not found");
        }

        PanicNotification panic = panicOptional.get();
        panic.setResolved(true); // also sets the date of resolving
        panicNotificationRepository.save(panic);

        //unmark the vehicle
        Ride ride = panic.getRide();
        if(ride != null){
            Vehicle vehicle = ride.getDriver().getVehicle();
            if(vehicle != null){
                vehicle.setPanicMark(false);
                vehicleRepository.save(vehicle);
            }
        }

        //make dto
        PanicNotificationDTO notificationDTO = new PanicNotificationDTO(
                panic.getId(),
                panic.getCaller() != null ? panic.getCaller().getId() : null,
                panic.getCaller() != null ? panic.getCaller().getName() + " " + panic.getCaller().getLastName() : "Unknown",
                panic.getRide() != null ? panic.getRide().getId() : null,
                panic.getCreatedAt(),
                panic.isResolved(),
                panic.getResolvedAt()
        );

        return notificationDTO;

    }


}



