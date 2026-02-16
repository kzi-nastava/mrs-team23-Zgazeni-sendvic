package ZgazeniSendvic.Server_Back_ISS.E2E.tests;

import ZgazeniSendvic.Server_Back_ISS.E2E.pages.HORAdminPage;
import ZgazeniSendvic.Server_Back_ISS.dto.OrsRouteResult;
import ZgazeniSendvic.Server_Back_ISS.model.*;
import ZgazeniSendvic.Server_Back_ISS.repository.AccountRepository;
import ZgazeniSendvic.Server_Back_ISS.repository.RideRepository;
import ZgazeniSendvic.Server_Back_ISS.repository.VehicleRepository;
import ZgazeniSendvic.Server_Back_ISS.security.jwt.JwtUtils;
import ZgazeniSendvic.Server_Back_ISS.service.AccountServiceImpl;
import ZgazeniSendvic.Server_Back_ISS.service.OrsRoutingService;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;


@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
public class HORAdminE2ETest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private RideRepository rideRepository;

    @MockitoBean
    private OrsRoutingService orsRoutingService;


    @Autowired
    private JwtUtils jwtUtils;

    private String adminToken;

    private OrsRouteResult mockRouteResult;

    private Account ridePassanger;

    private static final Set<String> ADMIN_ALLOWED_SORT_FIELDS = Set.of(
            "id",
            "locations",
            "startTime",
            "endTime",
            "status",
            "canceler",
            "price",
            "panic",
            "creationDate",
            "From",
            "To"
    );



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


    void setUpDB() {
        // Clean up database
        rideRepository.deleteAll();
        accountRepository.deleteAll();
        vehicleRepository.deleteAll();

        // Initialize locations
        Location location1 = new Location(20.4489, 44.8176); // Belgrade coordinates
        Location location2 = new Location(20.3971, 44.9199); // Novi Sad coordinates
        Location location3 = new Location(20.4500, 44.8500); // Another location

        // Create and save test passenger
        User testPassenger = new User();
        testPassenger.setEmail("passenger@test.com");
        testPassenger.setPassword(passwordEncoder.encode("password123"));
        testPassenger.setName("John");
        testPassenger.setLastName("Passenger");
        testPassenger.setAddress("123 Passenger St, Test City");
        testPassenger.setPhoneNumber("1234567890");
        testPassenger.setConfirmed(true);
        ridePassanger = accountRepository.save(testPassenger);

        Account Admin = new Admin();
        Admin.setEmail("Admina@test.com");
        Admin.setPassword(passwordEncoder.encode("password123"));
        Admin.setName("John");
        Admin.setLastName("Admin");
        Admin.setAddress("123 Passenger St, Test City");
        Admin.setPhoneNumber("1234567890");
        Admin.setConfirmed(true);
        Admin = accountRepository.save(Admin);

        this.adminToken = jwtUtils.generateToken(Admin);

        // Create and save vehicle for testDriver
        Vehicle vehicle1 = new Vehicle(
                "Toyota Camry",
                "ABC-123",
                VehicleType.STANDARD,
                4,
                true,
                true
        );
        vehicle1 = vehicleRepository.save(vehicle1);

        // Create and save testDriver
        Driver testDriver = new Driver(vehicle1);
        testDriver.setEmail("driver@test.com");
        testDriver.setPassword(passwordEncoder.encode("password123"));
        testDriver.setName("Michael");
        testDriver.setLastName("Driver");
        testDriver.setAddress("789 Driver Rd, Test City");
        testDriver.setPhoneNumber("5551234567");
        testDriver.setConfirmed(true);
        testDriver.setAvailable(true);
        testDriver.setActive(true);
        testDriver.setBusy(false);
        testDriver.setWorkedMinutesLast24h(0);
        testDriver = accountRepository.save(testDriver);

        // Create and save vehicle for notDriverOfRide
        Vehicle vehicle2 = new Vehicle(
                "Mercedes S-Class",
                "XYZ-789",
                VehicleType.LUXURY,
                4,
                false,
                false
        );
        vehicle2 = vehicleRepository.save(vehicle2);

        // Create and save notDriverOfRide
        Driver notDriverOfRide = new Driver(vehicle2);
        notDriverOfRide.setEmail("notdriver@test.com");
        notDriverOfRide.setPassword(passwordEncoder.encode("password123"));
        notDriverOfRide.setName("Jane");
        notDriverOfRide.setLastName("NotDriver");
        notDriverOfRide.setAddress("456 NotDriver Ave, Test City");
        notDriverOfRide.setPhoneNumber("5559876543");
        notDriverOfRide.setConfirmed(true);
        notDriverOfRide.setAvailable(true);
        notDriverOfRide.setActive(true);
        notDriverOfRide.setBusy(false);
        notDriverOfRide.setWorkedMinutesLast24h(0);
        notDriverOfRide = accountRepository.save(notDriverOfRide);

        // Create active ride
        Ride activeRide = new Ride();
        activeRide.setId(null);
        activeRide.setDriver(testDriver);
        activeRide.setCreator(testPassenger);
        activeRide.setPassengers(new ArrayList<>(List.of(testPassenger)));
        activeRide.setLocations(new ArrayList<>(List.of(
                returnNewLocation(location1),
                returnNewLocation(location2)
        )));
        activeRide.setPrice(25.50);
        activeRide.setStartTime(LocalDateTime.now().minusMinutes(30));
        activeRide.setStatus(RideStatus.ACTIVE);
        activeRide.setPanic(false);
        activeRide = rideRepository.save(activeRide);


        // Ride Not belonging to user I check

        Ride OtherRide = new Ride();
        OtherRide.setId(null);
        OtherRide.setDriver(testDriver);
        OtherRide.setCreator(testDriver);
        OtherRide.setPassengers(new ArrayList<>(List.of()));
        OtherRide.setLocations(new ArrayList<>(List.of(
                returnNewLocation(location1),
                returnNewLocation(location2)
        )));
        OtherRide.setPrice(9999999);
        OtherRide.setStartTime(LocalDateTime.now().minusMinutes(30));
        OtherRide.setStatus(RideStatus.ACTIVE);
        OtherRide.setPanic(false);
        OtherRide = rideRepository.save(OtherRide);





        // Create scheduled ride
        Ride scheduledRide = new Ride();
        scheduledRide.setId(null);
        scheduledRide.setDriver(testDriver);
        scheduledRide.setCreator(testPassenger);
        scheduledRide.setPassengers(new ArrayList<>(List.of(testPassenger)));
        scheduledRide.setLocations(new ArrayList<>(List.of(
                returnNewLocation(location1),
                returnNewLocation(location3)
        )));
        scheduledRide.setPrice(30.00);
        scheduledRide.setStartTime(LocalDateTime.now().plusHours(1));
        scheduledRide.setStatus(RideStatus.SCHEDULED);
        scheduledRide.setPanic(false);
        scheduledRide = rideRepository.save(scheduledRide);


        // Create finished ride
        Ride finishedRide = new Ride();
        finishedRide.setId(null);
        finishedRide.setDriver(testDriver);
        finishedRide.setCreator(testPassenger);
        finishedRide.setPassengers(new ArrayList<>(List.of(testPassenger)));
        finishedRide.setLocations(new ArrayList<>(List.of(
                returnNewLocation(location2),
                returnNewLocation(location3)
        )));
        finishedRide.setPrice(40.00);
        finishedRide.setStartTime(LocalDateTime.now().minusHours(2));
        finishedRide.setEndTime(LocalDateTime.now().minusHours(1));
        finishedRide.setStatus(RideStatus.FINISHED);
        finishedRide.setPanic(false);
        finishedRide = rideRepository.save(finishedRide);

        // Create cancelled ride
        Ride cancelledRide = new Ride();
        cancelledRide.setId(null);
        cancelledRide.setDriver(testDriver);
        cancelledRide.setCreator(testPassenger);
        cancelledRide.setPassengers(new ArrayList<>(List.of(testPassenger)));
        cancelledRide.setLocations(new ArrayList<>(List.of(
                returnNewLocation(location1),
                returnNewLocation(location3)
        )));
        cancelledRide.setPrice(20.00);
        cancelledRide.setStartTime(LocalDateTime.now().plusHours(2));
        cancelledRide.setStatus(RideStatus.CANCELED);
        cancelledRide.setPanic(false);
        cancelledRide = rideRepository.save(cancelledRide);

        // Mock OrsRoutingService to avoid external API calls
        // Price calculation: distance (15000m = 15km) * 150 = 2250
        OrsRouteResult mockRouteResult = new OrsRouteResult(15000.0, 1200.0,
                List.of(List.of(20.4489, 44.8176), List.of(20.3971, 44.9199)), "driving-car");
        when(orsRoutingService.getFastestRouteWithPath(anyList())).thenReturn(mockRouteResult);
        this.mockRouteResult = mockRouteResult;

    }


    private static final String BASE_URL = "http://localhost:4200";
    private WebDriver driver;
    private HORAdminPage rideHistoryPage;

    @BeforeAll
    public static void setupClass() {
        WebDriverManager.chromedriver().setup();

    }

    @BeforeEach
    public void setupTest() {
        System.out.println("Setting up Chrome options...");
        ChromeOptions options = new ChromeOptions();

        // Essential for Ubuntu/Linux headless environments
        //options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");

        System.out.println("Creating ChromeDriver...");
        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(10));

        // Initialize page object
        rideHistoryPage = new HORAdminPage(driver);

        System.out.println("Driver created successfully");
        setUpDB();
    }


    @Test
    @DisplayName("E2E Test: Admin retrieves all rides for user ID 1 without filtering or sorting")
    public void testGetAllRidesForUserIdOne() {
        System.out.println("Testing retrieval of all rides for user ID 1...");

        // Navigate to HOR admin page with authentication
        rideHistoryPage.navigateToWithAuth(BASE_URL, adminToken);

        rideHistoryPage.enterTargetId(ridePassanger.getId().toString());
        rideHistoryPage.clickApplyFilters();

        // Verify page has loaded and contains rides
        boolean hasRides = rideHistoryPage.hasRides();
        System.out.println("Page has rides: " + hasRides);

        // Get number of rows displayed
        int numberOfRows = rideHistoryPage.getNumberOfRows();
        System.out.println("Number of rides displayed: " + numberOfRows);

        // Assert that rides are loaded
        Assertions.assertTrue(numberOfRows > 0, "Expected rides to be loaded for user ID 1");

        System.out.println("Successfully retrieved all rides for user ID 1");
    }



    @Test
    @DisplayName("E2E Test: Sort by price displays rides in correct order")
    public void testSortByPrice_MatchesBackendOrder() {
        System.out.println("Testing sort by price...");

        // Navigate to HOR admin page with authentication
        rideHistoryPage.navigateToWithAuth(BASE_URL, adminToken);

        rideHistoryPage.enterTargetId(ridePassanger.getId().toString());
        rideHistoryPage.sortByPrice();

        // Wait for initial load
        int initialRows = rideHistoryPage.getNumberOfRows();
        System.out.println("Initial number of rides: " + initialRows);
        Assertions.assertTrue(initialRows > 0, "Expected rides to be loaded");

        //get them sorted by price in descending order from the backend
        Page<Ride> ridesPage = rideRepository.findByAccountAndDateRange(
                accountRepository.findById(1L).orElse(null),
                null,
                null,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "price"))
        );

        //check ID's are in same positions
        for (int i = 0; i < ridesPage.getContent().size(); i++) {
            Ride ride = ridesPage.getContent().get(i);
            String displayedPrice = rideHistoryPage.getPriceFromRow(i);
            System.out.println("Displayed price at row " + i + ": " + displayedPrice);
            Assertions.assertTrue(displayedPrice.contains(String.format("%.2f", ride.getPrice())),
                    "Expected price " + ride.getPrice() + " at row " + i);
        }
    }



    static Stream<String> provideFieldsForSorting() {
        return ADMIN_ALLOWED_SORT_FIELDS.stream();
    }


    @ParameterizedTest
    @MethodSource(value = "provideFieldsForSorting")
    @DisplayName("E2E Test: Sort by given field ascending")
    public void testSortByGivenFieldAscending(String field){

        System.out.println("Testing sort by field: " + field);

        // Navigate to HOR admin page with authentication
        rideHistoryPage.navigateToWithAuth(BASE_URL, adminToken);
        System.out.println("Successfully filtered rides by given field: " + adminToken);

        rideHistoryPage.enterTargetId(ridePassanger.getId().toString());
        rideHistoryPage.sortByGivenFieldASC(field, true);

        // Wait for initial load
        int initialRows = rideHistoryPage.getNumberOfRows();
        System.out.println("Initial number of rides: " + initialRows);
        Assertions.assertTrue(initialRows > 0, "Expected rides to be loaded");

        //get them sorted by price in descending order from the backend
        Page<Ride> ridesPage = rideRepository.findByAccountAndDateRange(
                accountRepository.findById(ridePassanger.getId()).orElse(null),
                null,
                null,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, field))
        );

        //check ID's are in same positions
        for (int i = 0; i < ridesPage.getContent().size(); i++) {
            Ride ride = ridesPage.getContent().get(i);
            //compare ID's
            String displayedId = rideHistoryPage.getRideIdFromRow(i);
            Long rideId = ride.getId();
            Long displayedIdLong = Long.parseLong(displayedId);
            Assertions.assertEquals(displayedIdLong, rideId, "Expected ride id " + rideId);

        }


    }

    @ParameterizedTest
    @MethodSource(value = "provideFieldsForSorting")
    @DisplayName("E2E Test: Sort by given field ascending")
    public void testSortByGivenFieldDescending(String field){

        System.out.println("Testing sort by field: " + field);

        // Navigate to HOR admin page with authentication
        rideHistoryPage.navigateToWithAuth(BASE_URL, adminToken);
        System.out.println("Successfully filtered rides by given field: " + adminToken);

        rideHistoryPage.enterTargetId(ridePassanger.getId().toString());
        rideHistoryPage.sortByGivenFieldASC(field, false);

        // Wait for initial load
        int initialRows = rideHistoryPage.getNumberOfRows();
        System.out.println("Initial number of rides: " + initialRows);
        Assertions.assertTrue(initialRows > 0, "Expected rides to be loaded");

        //get them sorted by price in descending order from the backend
        Page<Ride> ridesPage = rideRepository.findByAccountAndDateRange(
                accountRepository.findById(ridePassanger.getId()).orElse(null),
                null,
                null,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, field))
        );

        //check ID's are in same positions
        for (int i = 0; i < ridesPage.getContent().size(); i++) {
            Ride ride = ridesPage.getContent().get(i);
            //compare ID's
            String displayedId = rideHistoryPage.getRideIdFromRow(i);
            Long rideId = ride.getId();
            Long displayedIdLong = Long.parseLong(displayedId);
            Assertions.assertEquals(displayedIdLong, rideId, "Expected ride id " + rideId);

        }


    }

    static Stream<Arguments> provideDatesForFiltering() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of(null, LocalDateTime.now().plusDays(99)),
                Arguments.of(LocalDateTime.now().minusDays(99), null),
                Arguments.of(LocalDateTime.now().plusDays(99), null),
                Arguments.of(null, LocalDateTime.now().minusDays(99))
        );
    }


    @ParameterizedTest
    @MethodSource(value = "provideDatesForFiltering")
    @DisplayName("E2E Test: Filter by date range")
    public void testFilterByDateRange(LocalDateTime from, LocalDateTime to){
        System.out.println("Testing filter by date range: from " + from + " to " + to);

        // Navigate to HOR admin page with authentication
        rideHistoryPage.navigateToWithAuth(BASE_URL, adminToken);
        System.out.println("Successfully filtered rides by date range: " + adminToken);

        rideHistoryPage.enterTargetId(ridePassanger.getId().toString());
        if(from != null) {
            rideHistoryPage.enterFromDate(from.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        if(to != null) {
            rideHistoryPage.enterToDate(to.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        rideHistoryPage.clickApplyFilters();



        //get them directly from backend
        Page<Ride> ridesPage = rideRepository.findByAccountAndDateRange(
                accountRepository.findById(ridePassanger.getId()).orElse(null),
                from,
                to,
                PageRequest.of(0, 10)
        );



        // Wait for initial load
        int initialRows = rideHistoryPage.getNumberOfRows();
        System.out.println("Initial number of rides: " + initialRows);
        Assertions.assertEquals(ridesPage.getContent().size(), initialRows, "Expected number of rides to match filtered results");

        // No check of same position, as they are random
    }

    @AfterEach
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    public Location returnNewLocation(Location location) {
        return new Location(location.getLongitude(), location.getLatitude());
    }

}
