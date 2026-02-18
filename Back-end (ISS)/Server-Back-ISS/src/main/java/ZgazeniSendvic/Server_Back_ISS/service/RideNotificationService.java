package ZgazeniSendvic.Server_Back_ISS.service;

import ZgazeniSendvic.Server_Back_ISS.model.Account;
import ZgazeniSendvic.Server_Back_ISS.model.Driver;
import ZgazeniSendvic.Server_Back_ISS.model.Ride;
import ZgazeniSendvic.Server_Back_ISS.model.RideStatus;
import ZgazeniSendvic.Server_Back_ISS.repository.RideRepository;
import ZgazeniSendvic.Server_Back_ISS.security.EmailDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RideNotificationService {

    @Autowired
    EmailService emailService;
    @Autowired
    RideRepository rideRepository;

    public void sendNoDriversEmail(Account user) {
        EmailDetails details = new EmailDetails();
        details.setRecipient(user.getEmail());
        details.setSubject("Ride request failed");
        details.setMsgBody(
                "Unfortunately, there are currently no available drivers. " +
                        "Please try again later."
        );

        emailService.sendSimpleMail(details);
    }

    public void sendRideAcceptedEmail(Account user, Ride ride) {
        EmailDetails details = new EmailDetails();
        details.setRecipient(user.getEmail());
        details.setSubject("Your ride has been accepted");
        details.setMsgBody(
                "Your ride has been successfully scheduled.\n\n" +
                        "Driver: " + ride.getDriver().getName() + "\n" +
                        "Estimated price: " + ride.getTotalPrice()
        );

        emailService.sendSimpleMail(details);
    }

    public void sendNewRideForDriverEmail(Driver driver, Ride ride) {
        EmailDetails details = new EmailDetails();
        details.setRecipient(driver.getEmail());
        details.setSubject("New ride assigned");
        details.setMsgBody(
                "You have been assigned a new ride.\n\n" +
                        "Pickup location: " + ride.getLocations().get(0) + "\n" +
                        "Scheduled time: " + ride.getScheduledTime()
        );

        emailService.sendSimpleMail(details);
    }

    public void sendReminderEmail(Account user, Ride ride) {
        EmailDetails details = new EmailDetails();
        details.setRecipient(user.getEmail());
        details.setSubject("Ride reminder");
        details.setMsgBody(
                "Reminder: You have a scheduled ride at "
                        + ride.getScheduledTime()
        );

        emailService.sendSimpleMail(details);
    }

    @Scheduled(fixedRate = 60000) // every 1 minute
    public void sendRideReminders() {

        List<Ride> scheduledRides =
                rideRepository.findByStatus(RideStatus.SCHEDULED);

        for (Ride ride : scheduledRides) {

            if (ride.getScheduledTime() == null) continue;

            long minutesUntil =
                    Duration.between(
                            LocalDateTime.now(),
                            ride.getScheduledTime()
                    ).toMinutes();

            if (minutesUntil <= 15 && minutesUntil >= 0) {
                if (shouldSendReminder(ride, minutesUntil)) {
                    sendReminderEmail( ride.getCreator(), ride);
                }
            }
        }
    }

    private boolean shouldSendReminder(Ride ride, long minutesUntil) {

        // 15 min reminder
        if (minutesUntil == 15) return true;

        // Every 5 minutes after that
        return minutesUntil < 15 && minutesUntil % 5 == 0;
    }
}

