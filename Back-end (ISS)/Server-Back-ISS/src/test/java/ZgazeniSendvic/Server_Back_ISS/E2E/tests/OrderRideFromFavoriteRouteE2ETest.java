package ZgazeniSendvic.Server_Back_ISS.E2E.tests;

import ZgazeniSendvic.Server_Back_ISS.E2E.pages.FavoriteRoutesPage;
import ZgazeniSendvic.Server_Back_ISS.E2E.pages.LoginPage;
import ZgazeniSendvic.Server_Back_ISS.E2E.pages.ProfilePage;
import ZgazeniSendvic.Server_Back_ISS.E2E.pages.RideOrderPage;
import ZgazeniSendvic.Server_Back_ISS.model.*;
import ZgazeniSendvic.Server_Back_ISS.repository.*;

import org.junit.jupiter.api.*;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OrderRideFromFavoriteRouteE2ETest {

    private WebDriver driver;

    private static final String FRONTEND_BASE = "http://localhost:4200";
    private static final String USER_EMAIL = "user1@test.com";
    private static final String USER_PASS = "Pass123!";

    private static final List<double[]> EXPECTED_LOCATIONS = List.of(
            new double[]{44.7866, 20.4489},
            new double[]{44.8125, 20.4612},
            new double[]{44.8176, 20.4569}
    );

    @Autowired PasswordEncoder passwordEncoder;

    @Autowired AccountRepository accountRepository;
    @Autowired RouteRepository routeRepository;
    @Autowired RideRequestRepository rideRequestRepository;

    @BeforeEach
    void setup() {
        seedDatabase();

        ChromeOptions opts = new ChromeOptions();
        // opts.addArguments("--headless=new");
        opts.addArguments("--window-size=1280,900");

        driver = new ChromeDriver(opts);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
    }

    @AfterEach
    void teardown() {
        if (driver != null) driver.quit();
    }

    @Test
    void orderRideUsingFavoriteRoute_createsRideRequestInDb() {
        // --- UI steps ---
        var login = new LoginPage(driver);
        login.open(FRONTEND_BASE);
        login.login(USER_EMAIL, USER_PASS);

        new WebDriverWait(driver, Duration.ofSeconds(50)).until(d ->
                ((JavascriptExecutor)d).executeScript("return window.localStorage.getItem('jwt_token')") != null
        );

        var profile = new ProfilePage(driver);
        profile.open(FRONTEND_BASE);
        profile.goToFavoriteRoutes();

        var logs = driver.manage().logs().get(org.openqa.selenium.logging.LogType.BROWSER);
        logs.forEach(l -> System.out.println(l.getLevel() + " " + l.getMessage()));

        var fav = new FavoriteRoutesPage(driver);
        fav.useFirstRoute();

        var rideOrder = new RideOrderPage(driver);
        rideOrder.fillAllAddresses();
        rideOrder.requestRideExpectSuccess();

        // --- DB assert (repo-based) ---
        assertTrue(
                existsRideRequestWithLocations(USER_EMAIL, EXPECTED_LOCATIONS),
                "Expected RideRequest with favorite-route locations to exist in DB"
        );
    }

    private void seedDatabase() {
        // Clear in FK-safe order (child -> parent)
        rideRequestRepository.deleteAll();
        routeRepository.deleteAll();
        accountRepository.deleteAll();

        // If Account is abstract in your model, replace with your concrete type (User/Gost/etc).
        // Your earlier snippet uses User extends Account, so prefer that.
        User user = new User();
        user.setEmail(USER_EMAIL);
        user.setName("User");
        user.setLastName("One");
        user.setPhoneNumber("38161111222");
        user.setAddress("Test Address");
        user.setConfirmed(true);
        user.setPassword(passwordEncoder.encode(USER_PASS));
        user = accountRepository.save(user);

        Location start = new Location(EXPECTED_LOCATIONS.get(0)[0], EXPECTED_LOCATIONS.get(0)[1]);
        Location mid   = new Location(EXPECTED_LOCATIONS.get(1)[0], EXPECTED_LOCATIONS.get(1)[1]);
        Location dest  = new Location(EXPECTED_LOCATIONS.get(2)[0], EXPECTED_LOCATIONS.get(2)[1]);

        Route route = new Route();
        route.setOwner(user);
        route.setStart(start);
        route.setDestination(dest);
        route.setMidPoints(List.of(mid));

        routeRepository.save(route);
    }

    private boolean existsRideRequestWithLocations(String creatorEmail, List<double[]> expected) {
        // Prefer a derived query that sorts by newest first
        List<RideRequest> candidates =
                rideRequestRepository.findTop10ByCreator_EmailOrderByIdDesc(creatorEmail);

        for (RideRequest rr : candidates) {
            var locs = rr.getLocations();
            if (locs == null || locs.size() != expected.size()) continue;

            boolean match = true;
            for (int i = 0; i < expected.size(); i++) {
                double expLat = expected.get(i)[0];
                double expLng = expected.get(i)[1];

                double actLat = locs.get(i).getLatitude();
                double actLng = locs.get(i).getLongitude();

                if (Math.abs(expLat - actLat) > 1e-6 || Math.abs(expLng - actLng) > 1e-6) {
                    match = false;
                    break;
                }
            }
            if (match) return true;
        }
        return false;
    }
}

