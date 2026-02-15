package ZgazeniSendvic.Server_Back_ISS.E2E.tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class HORAdminE2ETest {

    private WebDriver driver;

    @BeforeAll
    public static void setupClass() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    public void setupTest() {
        ChromeOptions options = new ChromeOptions();

        // Essential for Ubuntu/Linux headless environments
        options.addArguments("--headless=new");  // New headless mode
        options.addArguments("--no-sandbox");     // Required for Ubuntu/CI
        options.addArguments("--disable-dev-shm-usage");  // Overcomes limited resource problems
        options.addArguments("--disable-gpu");    // Applicable to Ubuntu
        options.addArguments("--window-size=1920,1080");

        driver = new ChromeDriver(options);
    }

    @Test
    public void testYourAngularApp() {
        driver.get("http://localhost:4200");
        // Your test code here
        Assertions.assertEquals("DriveBy", driver.getTitle());
    }

    @AfterEach
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }


}
