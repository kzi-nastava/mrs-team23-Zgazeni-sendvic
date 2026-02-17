package ZgazeniSendvic.Server_Back_ISS.repository;

import ZgazeniSendvic.Server_Back_ISS.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@DisplayName("RideRepositoryTest")
public class RideRepositoryDockerTest {

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("test_db")
            .withUsername("test_user")
            .withPassword("test_pass");

    @DynamicPropertySource
    static void registerDataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    void setUp() {
        rideRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    @DisplayName("Should return Ride when id exists")
    void testFindById_RideExists() {
        // Create Account
        Account creator = new User();
        creator.setEmail("creator@test.com");
        creator.setPassword(passwordEncoder.encode("password123"));
        creator.setName("John");
        creator.setLastName("Doe");
        creator.setAddress("123 Main St");
        creator.setPhoneNumber("1234567890");
        creator.setConfirmed(true);
        accountRepository.save(creator);

        // Create Driver
        Driver driver = new Driver();
        driver.setEmail("driver@test.com");
        driver.setPassword(passwordEncoder.encode("password123"));
        driver.setName("Jane");
        driver.setLastName("Smith");
        driver.setAddress("456 Oak Ave");
        driver.setPhoneNumber("0987654321");
        driver.setConfirmed(true);
        driver.setActive(true);
        accountRepository.save(driver);

        // Create Ride
        Ride ride = new Ride();
        ride.setDriver(driver);
        ride.setCreator(creator);
        ride.setPassengers(new ArrayList<>());
        ride.setLocations(new ArrayList<>());
        ride.setPrice(15.0);
        ride.setStartTime(LocalDateTime.now());
        ride.setEndTime(LocalDateTime.now().plusHours(1));
        ride.setStatus(RideStatus.ACTIVE);
        ride.setPanic(false);
        Ride savedRide = rideRepository.save(ride);

        // Test findById
        Optional<Ride> result = rideRepository.findById(savedRide.getId());

        assertTrue(result.isPresent());
        assertEquals(savedRide.getId(), result.get().getId());
        assertEquals(15.0, result.get().getPrice());
        assertEquals(RideStatus.ACTIVE, result.get().getStatus());
    }

    @Test
    @DisplayName("Should return empty Optional when id does not exist")
    void testFindById_RideNotExists() {
        Optional<Ride> result = rideRepository.findById(9999L);

        assertFalse(result.isPresent());
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return rides for account within date range")
    void testFindByAccountAndDateRange_ReturnsRide() {
        Account creator = new User();
        creator.setEmail("creator-range@test.com");
        creator.setPassword(passwordEncoder.encode("password123"));
        creator.setName("Range");
        creator.setLastName("Creator");
        creator.setAddress("1 Range St");
        creator.setPhoneNumber("1111111111");
        creator.setConfirmed(true);
        accountRepository.save(creator);

        Driver driver = new Driver();
        driver.setEmail("driver-range@test.com");
        driver.setPassword(passwordEncoder.encode("password123"));
        driver.setName("Range");
        driver.setLastName("Driver");
        driver.setAddress("2 Range Ave");
        driver.setPhoneNumber("2222222222");
        driver.setConfirmed(true);
        driver.setActive(true);
        accountRepository.save(driver);

        Ride ride = new Ride();
        ride.setDriver(driver);
        ride.setCreator(creator);
        ride.setPassengers(new ArrayList<>());
        ride.setLocations(new ArrayList<>());
        ride.setPrice(10.0);
        ride.setStartTime(LocalDateTime.now().minusMinutes(30));
        ride.setEndTime(LocalDateTime.now().minusMinutes(10));
        ride.setStatus(RideStatus.ACTIVE);
        ride.setPanic(false);
        rideRepository.save(ride);

        LocalDateTime fromDate = LocalDateTime.now().minusHours(1);
        LocalDateTime toDate = LocalDateTime.now().plusHours(1);

        Page<Ride> result = rideRepository.findByAccountAndDateRange(
                creator,
                fromDate,
                toDate,
                PageRequest.of(0, 10)
        );

        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
        assertEquals(creator.getId(), result.getContent().get(0).getCreator().getId());
    }

    @Test
    @DisplayName("Should return empty page when date range excludes rides")
    void testFindByAccountAndDateRange_EmptyWhenOutOfRange() {
        Account creator = new User();
        creator.setEmail("creator-out@test.com");
        creator.setPassword(passwordEncoder.encode("password123"));
        creator.setName("Out");
        creator.setLastName("Range");
        creator.setAddress("3 Range St");
        creator.setPhoneNumber("3333333333");
        creator.setConfirmed(true);
        accountRepository.save(creator);

        Driver driver = new Driver();
        driver.setEmail("driver-out@test.com");
        driver.setPassword(passwordEncoder.encode("password123"));
        driver.setName("Out");
        driver.setLastName("Driver");
        driver.setAddress("4 Range Ave");
        driver.setPhoneNumber("4444444444");
        driver.setConfirmed(true);
        driver.setActive(true);
        accountRepository.save(driver);

        Ride ride = new Ride();
        ride.setDriver(driver);
        ride.setCreator(creator);
        ride.setPassengers(new ArrayList<>());
        ride.setLocations(new ArrayList<>());
        ride.setPrice(20.0);
        ride.setStartTime(LocalDateTime.now().minusMinutes(30));
        ride.setEndTime(LocalDateTime.now().minusMinutes(10));
        ride.setStatus(RideStatus.ACTIVE);
        ride.setPanic(false);
        rideRepository.save(ride);

        LocalDateTime fromDate = LocalDateTime.now().minusDays(10);
        LocalDateTime toDate = LocalDateTime.now().minusDays(5);

        Page<Ride> result = rideRepository.findByAccountAndDateRange(
                creator,
                fromDate,
                toDate,
                PageRequest.of(0, 10)
        );

        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());
    }
}
