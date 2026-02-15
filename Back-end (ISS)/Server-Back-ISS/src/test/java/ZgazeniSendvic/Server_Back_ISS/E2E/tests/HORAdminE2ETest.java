package ZgazeniSendvic.Server_Back_ISS.E2E.tests;

import ZgazeniSendvic.Server_Back_ISS.E2E.pages.HORAdminPage;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.Duration;

public class HORAdminE2ETest {

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
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");

        System.out.println("Creating ChromeDriver...");
        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));

        // Initialize page object
        rideHistoryPage = new HORAdminPage(driver);

        System.out.println("Driver created successfully");
    }
    

    @Test
    public void testPageLoads() {
        System.out.println("Testing page load...");
        rideHistoryPage.navigateTo(BASE_URL);
        rideHistoryPage.waitForTableToLoad();

        System.out.println("Page loaded successfully");
        Assertions.assertNotNull(driver.getTitle());
    }

    @AfterEach
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }


}
