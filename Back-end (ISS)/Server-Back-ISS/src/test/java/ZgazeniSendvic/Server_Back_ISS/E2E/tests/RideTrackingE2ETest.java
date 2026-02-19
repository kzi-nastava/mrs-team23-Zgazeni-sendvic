package ZgazeniSendvic.Server_Back_ISS.E2E.tests;

import ZgazeniSendvic.Server_Back_ISS.E2E.pages.RideTrackingPage;
import ZgazeniSendvic.Server_Back_ISS.model.*;
import ZgazeniSendvic.Server_Back_ISS.repository.AccountRepository;
import ZgazeniSendvic.Server_Back_ISS.repository.RideDriverRatingRepository;
import ZgazeniSendvic.Server_Back_ISS.repository.RideRepository;
import ZgazeniSendvic.Server_Back_ISS.repository.VehicleRepository;
import ZgazeniSendvic.Server_Back_ISS.security.jwt.JwtUtils;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RideTrackingE2ETest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private RideDriverRatingRepository rideDriverRatingRepository;

    @Autowired
    private JwtUtils jwtUtils;

    private static WebDriver driver;
    private static RideTrackingPage rideTrackingPage;

    private static final String BASE_URL = "http://localhost:4200";

    private String userToken;
    private User testUser;
    private Driver testDriver;
    private Ride testRide;

    @BeforeAll
    public static void setUpWebDriver() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
         options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.setExperimentalOption("useAutomationExtension", false);
        driver = new ChromeDriver(options);
        rideTrackingPage = new RideTrackingPage(driver);
    }

    @AfterAll
    public static void tearDownWebDriver() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeEach
    public void setUp() {
        setUpDB();
    }

    @AfterEach
    public void cleanUp() {
        rideDriverRatingRepository.deleteAll();
        rideRepository.deleteAll();
        accountRepository.deleteAll();
        vehicleRepository.deleteAll();
    }

    void setUpDB() {
        rideDriverRatingRepository.deleteAll();
        rideRepository.deleteAll();
        accountRepository.deleteAll();
        vehicleRepository.deleteAll();

        User user = new User();
        user.setEmail("user@test.com");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setName("John");
        user.setLastName("User");
        user.setAddress("123 User St, Test City");
        user.setPhoneNumber("1234567890");
        user.setConfirmed(true);
        testUser = accountRepository.save(user);

        userToken = jwtUtils.generateToken(testUser);

        Vehicle vehicle = new Vehicle(
                "Toyota Camry",
                "ABC-123",
                VehicleType.STANDARD,
                4,
                true,
                true
        );
        vehicle = vehicleRepository.save(vehicle);

        Driver driver = new Driver(vehicle);
        driver.setEmail("driver@test.com");
        driver.setPassword(passwordEncoder.encode("password123"));
        driver.setName("Michael");
        driver.setLastName("Driver");
        driver.setAddress("789 Driver Rd, Test City");
        driver.setPhoneNumber("5551234567");
        driver.setConfirmed(true);
        driver.setAvailable(true);
        driver.setActive(true);
        driver.setBusy(false);
        driver.setWorkedMinutesLast24h(0);
        testDriver = accountRepository.save(driver);

        Location startLocation = new Location(20.4489, 44.8176);
        Location endLocation = new Location(20.3971, 44.9199);

        Ride ride = new Ride();
        ride.setDriver(testDriver);
        ride.setCreator(testUser);
        ride.setPassengers(new ArrayList<>(List.of(testUser)));
        ride.setLocations(new ArrayList<>(List.of(startLocation, endLocation)));
        ride.setStartTime(LocalDateTime.now().minusMinutes(30));
        ride.setEndTime(LocalDateTime.now());
        ride.setStatus(RideStatus.FINISHED);
        ride.setCanceler(null);
        ride.setTotalPrice(500.0);
        ride.setPanic(false);
        ride.setCreationDate(LocalDateTime.now().minusHours(1));
        ride.setStartLatitude(startLocation.getLatitude());
        ride.setStartLongitude(startLocation.getLongitude());
        ride.setEndLatitude(endLocation.getLatitude());
        ride.setEndLongitude(endLocation.getLongitude());
        ride.setCurrentLatitude(endLocation.getLatitude());
        ride.setCurrentLongitude(endLocation.getLongitude());
        testRide = rideRepository.save(ride);
    }

    @Test
    @Order(1)
    @DisplayName("Test opening rate form")
    public void testOpenRateForm() {
        rideTrackingPage.navigateToWithAuth(BASE_URL, userToken, "USER");

        assertTrue(rideTrackingPage.isPageLoaded(), "Ride tracking page should be loaded");
        assertTrue(rideTrackingPage.isRateButtonVisible(), "Rate button should be visible for user");

        rideTrackingPage.clickRateButton();
        rideTrackingPage.waitForRateForm();

        assertTrue(rideTrackingPage.isRateFormDisplayed(), "Rate form should be displayed");

        System.out.println("TEST PASSED: testOpenRateForm");
    }

    @Test
    @Order(2)
    @DisplayName("Test closing rate form with close button")
    public void testCloseRateForm() {
        rideTrackingPage.navigateToWithAuth(BASE_URL, userToken, "USER");
        rideTrackingPage.clickRateButton();
        rideTrackingPage.waitForRateForm();
        rideTrackingPage.clickCloseButton();
        rideTrackingPage.waitForFormToClose();

        assertTrue(rideTrackingPage.isFormClosed(), "Rate form should be closed");

        System.out.println("TEST PASSED: testCloseRateForm");
    }

    @Test
    @Order(3)
    @DisplayName("Test filling rating form fields")
    public void testFillRatingFormFields() {
        rideTrackingPage.navigateToWithAuth(BASE_URL, userToken, "USER");
        rideTrackingPage.clickRateButton();
        rideTrackingPage.waitForRateForm();

        int driverRating = 8;
        int vehicleRating = 9;
        String comment = "Great ride!";

        rideTrackingPage.fillDriverRating(driverRating);
        rideTrackingPage.fillVehicleRating(vehicleRating);
        rideTrackingPage.fillComment(comment);

        assertEquals(String.valueOf(driverRating), rideTrackingPage.getDriverRatingValue(),
                "Driver rating should be set correctly");
        assertEquals(String.valueOf(vehicleRating), rideTrackingPage.getVehicleRatingValue(),
                "Vehicle rating should be set correctly");
        assertEquals(comment, rideTrackingPage.getCommentValue(),
                "Comment should be set correctly");

        System.out.println("TEST PASSED: testFillRatingFormFields");
    }

//    @Test
//    @Order(4)
//    @DisplayName("Test submitting rating with all fields")
//    public void testSubmitCompleteRating() {
//        rideTrackingPage.navigateToWithAuth(BASE_URL, userToken, "USER");
//        rideTrackingPage.clickRateButton();
//        rideTrackingPage.waitForRateForm();
//
//        int driverRating = 10;
//        int vehicleRating = 9;
//        String comment = "Excellent service!";
//
//        rideTrackingPage.submitRating(driverRating, vehicleRating, comment);
//        rideTrackingPage.waitForRatingToBeSaved(rideDriverRatingRepository);
//
//        List<RideDriverRating> ratings = rideDriverRatingRepository.findAll();
//        assertFalse(ratings.isEmpty(), "Rating should be saved in database");
//
//        RideDriverRating savedRating = ratings.get(0);
//        assertEquals(driverRating, savedRating.getDriverRating(),
//                "Driver rating should match");
//        assertEquals(vehicleRating, savedRating.getVehicleRating(),
//                "Vehicle rating should match");
//        assertEquals(comment, savedRating.getComment(),
//                "Comment should match");
//        assertEquals(testUser.getId(), savedRating.getUserId(),
//                "User ID should match");
//        assertEquals(testRide.getId(), savedRating.getRideId(),
//                "Ride ID should match");
//
//        System.out.println("TEST PASSED: testSubmitCompleteRating");
//    }
//
//    @Test
//    @Order(5)
//    @DisplayName("Test submitting rating without comment")
//    public void testSubmitRatingWithoutComment() {
//        rideTrackingPage.navigateToWithAuth(BASE_URL, userToken, "USER");
//        rideTrackingPage.clickRateButton();
//        rideTrackingPage.waitForRateForm();
//
//        int driverRating = 7;
//        int vehicleRating = 8;
//
//        rideTrackingPage.submitRating(driverRating, vehicleRating, "");
//        rideTrackingPage.waitForRatingToBeSaved(rideDriverRatingRepository);
//
//        List<RideDriverRating> ratings = rideDriverRatingRepository.findAll();
//        assertFalse(ratings.isEmpty(), "Rating should be saved in database");
//
//        RideDriverRating savedRating = ratings.get(0);
//        assertEquals(driverRating, savedRating.getDriverRating(),
//                "Driver rating should match");
//        assertEquals(vehicleRating, savedRating.getVehicleRating(),
//                "Vehicle rating should match");
//
//        System.out.println("TEST PASSED: testSubmitRatingWithoutComment");
//    }
//
//    @Test
//    @Order(6)
//    @DisplayName("Test rating with boundary values")
//    public void testRatingWithBoundaryValues() {
//        rideTrackingPage.navigateToWithAuth(BASE_URL, userToken, "USER");
//        rideTrackingPage.clickRateButton();
//        rideTrackingPage.waitForRateForm();
//        rideTrackingPage.submitRating(1, 1, "Minimum rating test");
//        rideTrackingPage.waitForRatingToBeSaved(rideDriverRatingRepository);
//
//        List<RideDriverRating> ratings = rideDriverRatingRepository.findAll();
//        assertEquals(1, ratings.size(), "Should have one rating saved (API may have failed)");
//
//        RideDriverRating savedRating = ratings.get(0);
//        assertEquals(1, savedRating.getDriverRating(), "Driver rating should be 1");
//        assertEquals(1, savedRating.getVehicleRating(), "Vehicle rating should be 1");
//
//        System.out.println("TEST PASSED: testRatingWithBoundaryValues");
//    }
//
//    @Test
//    @Order(7)
//    @DisplayName("Test rating with maximum values")
//    public void testRatingWithMaximumValues() {
//        rideTrackingPage.navigateToWithAuth(BASE_URL, userToken, "USER");
//        rideTrackingPage.clickRateButton();
//        rideTrackingPage.waitForRateForm();
//        rideTrackingPage.submitRating(10, 10, "Maximum rating test");
//        rideTrackingPage.waitForRatingToBeSaved(rideDriverRatingRepository);
//
//        List<RideDriverRating> ratings = rideDriverRatingRepository.findAll();
//        assertEquals(1, ratings.size(), "Should have one rating saved (API may have failed)");
//
//        RideDriverRating savedRating = ratings.get(0);
//        assertEquals(10, savedRating.getDriverRating(), "Driver rating should be 10");
//        assertEquals(10, savedRating.getVehicleRating(), "Vehicle rating should be 10");
//
//        System.out.println("TEST PASSED: testRatingWithMaximumValues");
//    }

    @Test
    @Order(8)
    @DisplayName("Test that driver does not see Rate button")
    public void testDriverDoesNotSeeRateButton() {
        String driverToken = jwtUtils.generateToken(testDriver);
        rideTrackingPage.navigateToWithAuth(BASE_URL, driverToken, "DRIVER");

        assertTrue(rideTrackingPage.isPageLoaded(), "Ride tracking page should be loaded");
        assertFalse(rideTrackingPage.isRateButtonVisible(),
                "Rate button should NOT be visible for driver");

        assertTrue(rideTrackingPage.isEndRideButtonVisible(),
                "End Ride button should be visible for driver");

        System.out.println("TEST PASSED: testDriverDoesNotSeeRateButton");
    }

    @Test
    @Order(9)
    @DisplayName("Test that user sees Rate and Note buttons but not End Ride")
    public void testUserSeesCorrectButtons() {
        rideTrackingPage.navigateToWithAuth(BASE_URL, userToken, "USER");

        assertTrue(rideTrackingPage.isPageLoaded(), "Ride tracking page should be loaded");
        assertTrue(rideTrackingPage.isRateButtonVisible(),
                "Rate button should be visible for user");
        assertTrue(rideTrackingPage.isNoteButtonVisible(),
                "Note button should be visible for user");
        assertFalse(rideTrackingPage.isEndRideButtonVisible(),
                "End Ride button should NOT be visible for user");

        System.out.println("TEST PASSED: testUserSeesCorrectButtons");
    }

    @Test
    @Order(10)
    @DisplayName("Test switching between Note and Rate forms")
    public void testSwitchingBetweenForms() {
        rideTrackingPage.navigateToWithAuth(BASE_URL, userToken, "USER");
        rideTrackingPage.clickNoteButton();
        rideTrackingPage.waitForNoteForm();

        assertTrue(rideTrackingPage.isNoteFormDisplayed(), "Note form should be displayed");

        rideTrackingPage.clickCloseButton();
        rideTrackingPage.waitForFormToClose();
        rideTrackingPage.clickRateButton();
        rideTrackingPage.waitForRateForm();

        assertTrue(rideTrackingPage.isRateFormDisplayed(), "Rate form should be displayed");

        System.out.println("TEST PASSED: testSwitchingBetweenForms");
    }
}
