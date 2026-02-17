package ZgazeniSendvic.Server_Back_ISS.repository;

import ZgazeniSendvic.Server_Back_ISS.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class RideRepositoryDBTest {

    @Autowired
    private RideRepository rideRepository;
    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void saveAndFindById_shouldPersistAndRetrieveRide() {
        Driver driver = createValidDriver("d@gmail.com");
        entityManager.persist(driver);
        Account creator = createValidAccount("c@gmail.com");
        entityManager.persist(creator);

        Ride ride = new Ride();
        ride.setDriver(driver);
        ride.setCreator(creator);
        ride.setPassengers(new ArrayList<>());
        ride.setPrice(100);
        ride.setStatus(RideStatus.ACTIVE);
        ride.setStartTime(LocalDateTime.now());
        ride.setPanic(false);

        Ride saved = rideRepository.save(ride);
        entityManager.flush();
        entityManager.clear();

        Optional<Ride> found = rideRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getPrice()).isEqualTo(100);
        assertThat(found.get().getDriver().getEmail()).isEqualTo("d@gmail.com");
        assertThat(found.get().getStatus()).isEqualTo(RideStatus.ACTIVE);
    }

    @Test
    void save_shouldUpdateRidePrice() {
        Ride ride = new Ride();
        ride.setPrice(100);
        ride.setStatus(RideStatus.ACTIVE);
        ride.setPassengers(new ArrayList<>());
        ride.setPanic(false);

        Ride saved = rideRepository.save(ride);
        entityManager.flush();
        Long rideId = saved.getId();

        saved.setPrice(150);
        rideRepository.save(saved);
        entityManager.flush();
        entityManager.clear();

        Ride updated = rideRepository.findById(rideId).orElseThrow();
        assertThat(updated.getPrice()).isEqualTo(150);
    }

    @Test
    void findByDriverAndStatus_shouldReturnScheduledRidesOnly() {
        Driver driver = createValidDriver("d1@gmail.com");
        entityManager.persist(driver);

        Ride scheduledRide1 = createRideWithStatus(driver, RideStatus.SCHEDULED, 50);
        Ride scheduledRide2 = createRideWithStatus(driver, RideStatus.SCHEDULED, 60);
        Ride activeRide = createRideWithStatus(driver, RideStatus.ACTIVE, 75);

        entityManager.persist(scheduledRide1);
        entityManager.persist(scheduledRide2);
        entityManager.persist(activeRide);
        entityManager.flush();

        List<Ride> scheduled = rideRepository.findByDriverAndStatus(driver, RideStatus.SCHEDULED);

        assertThat(scheduled).hasSize(2);
        assertThat(scheduled)
            .extracting(Ride::getPrice)
            .containsExactlyInAnyOrder(50.0, 60.0);
    }

    @Test
    void getWorkedMinutesLast24h_shouldReturnCorrectSum() {
        Driver driver = createValidDriver("d2@gmail.com");
        entityManager.persist(driver);

        Ride ride1 = createRideWithDuration(driver, LocalDateTime.now().minusHours(2), 30L);
        entityManager.persist(ride1);
        Ride ride2 = createRideWithDuration(driver, LocalDateTime.now().minusHours(5), 45L);
        entityManager.persist(ride2);
        Ride ride3 = createRideWithDuration(driver, LocalDateTime.now().minusHours(26), 60L);
        entityManager.persist(ride3);

        entityManager.flush();

        LocalDateTime since = LocalDateTime.now().minusHours(24);
        Integer totalMinutes = rideRepository.getWorkedMinutesLast24h(driver.getId(), since);

        assertThat(totalMinutes).isEqualTo(75);
    }

    @Test
    void findById_shouldReturnEmpty_whenRideDoesNotExist() {
        Optional<Ride> found = rideRepository.findById(999L);
        assertThat(found).isEmpty();
    }

    @Test
    void save_shouldPersistRideWithPassengers() {
        Driver driver = createValidDriver("d3@gmail.com");
        entityManager.persist(driver);

        Account passenger1 = createValidAccount("p1@gmail.com");
        entityManager.persist(passenger1);
        Account passenger2 = createValidAccount("p2@gmail.com");
        entityManager.persist(passenger2);

        Ride ride = new Ride();
        ride.setDriver(driver);
        ride.setPassengers(List.of(passenger1, passenger2));
        ride.setPrice(80.0);
        ride.setStatus(RideStatus.SCHEDULED);
        ride.setPanic(false);

        Ride saved = rideRepository.save(ride);
        entityManager.flush();
        entityManager.clear();

        Ride retrieved = rideRepository.findById(saved.getId()).orElseThrow();
        assertThat(retrieved.getPassengers()).hasSize(2);
        assertThat(retrieved.getPassengers())
            .extracting(Account::getEmail)
            .containsExactlyInAnyOrder("p1@gmail.com", "p2@gmail.com");
    }

    private Driver createValidDriver(String email) {
        Driver driver = new Driver();
        driver.setEmail(email);
        driver.setName("John");
        driver.setLastName("Doe");
        driver.setAddress("123 Main St");
        driver.setPhoneNumber("1234567890");
        driver.setPassword("SecurePassword123!");
        return driver;
    }

    private Account createValidAccount(String email) {
        Account account = new Account();
        account.setEmail(email);
        account.setName("Jane");
        account.setLastName("Doe");
        account.setAddress("456 Oak Ave");
        account.setPhoneNumber("9876543210");
        account.setPassword("SecurePassword123!");
        return account;
    }

    private Ride createRideWithStatus(Driver driver, RideStatus status, double price) {
        Ride ride = new Ride();
        ride.setDriver(driver);
        ride.setStatus(status);
        ride.setPrice(price);
        ride.setPassengers(new ArrayList<>());
        ride.setPanic(false);
        return ride;
    }

    private Ride createRideWithDuration(Driver driver, LocalDateTime endTime, Long durationMinutes) {
        LocalDateTime startTime = endTime.minusMinutes(durationMinutes);
        Ride ride = new Ride(null,driver,null,new ArrayList<>(),new ArrayList<>(),
                        100,startTime,endTime,RideStatus.FINISHED,false);
        return ride;
    }
}
