package ZgazeniSendvic.Server_Back_ISS.E2E.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class RideTrackingPage {

    private WebDriver driver;
    private WebDriverWait wait;
    private JavascriptExecutor js;

    @FindBy(css = "aside.ride-sidebar")
    private WebElement rideSidebar;

    @FindBy(css = "aside.ride-sidebar .info-item h3")
    private WebElement startingPointLabel;

    @FindBy(css = "aside.ride-sidebar .info-item p")
    private WebElement startingPointValue;

    @FindBy(xpath = "//button[@class='sidebar-buttons' and text()='Note']")
    private WebElement noteButton;

    @FindBy(xpath = "//button[@class='sidebar-buttons' and text()='Rate']")
    private WebElement rateButton;

    @FindBy(xpath = "//button[@class='sidebar-buttons' and text()='End Ride']")
    private WebElement endRideButton;

    @FindBy(css = "div.form-overlay")
    private WebElement formOverlay;

    @FindBy(css = "div.form-container")
    private WebElement formContainer;

    @FindBy(xpath = "//div[@class='form-header']/h2[text()='Rate Ride']")
    private WebElement rateFormHeader;

    @FindBy(css = "button.close-btn")
    private WebElement closeButton;

    @FindBy(css = "input#driverRating")
    private WebElement driverRatingInput;

    @FindBy(css = "input#vehicleRating")
    private WebElement vehicleRatingInput;

    @FindBy(css = "textarea#comment")
    private WebElement commentTextarea;

    @FindBy(xpath = "//div[@class='form-header']/h2[text()='Add Note']")
    private WebElement noteFormHeader;

    @FindBy(css = "textarea.note-textarea")
    private WebElement noteTextarea;

    @FindBy(css = "app-map")
    private WebElement mapComponent;

    public RideTrackingPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        this.js = (JavascriptExecutor) driver;
        PageFactory.initElements(driver, this);
    }

    public void setAuthToken(String jwtToken, String role) {
        js.executeScript(
                "localStorage.setItem('jwt_token', arguments[0]);" +
                        "localStorage.setItem('user_role', arguments[1]);",
                jwtToken, role
        );
    }

    public void navigateToWithAuth(String baseUrl, String jwtToken, String role) {
        driver.get(baseUrl);
        setAuthToken(jwtToken, role);
        driver.get(baseUrl + "/ride-tracking");
        waitForPageLoad();
    }

    public void navigateTo(String baseUrl) {
        driver.get(baseUrl + "/ride-tracking");
    }

    public void waitForPageLoad() {
        wait.until(ExpectedConditions.visibilityOf(rideSidebar));
    }

    public boolean isPageLoaded() {
        try {
            return rideSidebar.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public void clickRateButton() {
        wait.until(ExpectedConditions.elementToBeClickable(rateButton));
        rateButton.click();
    }

    public void clickNoteButton() {
        wait.until(ExpectedConditions.elementToBeClickable(noteButton));
        noteButton.click();
    }

    public void clickEndRideButton() {
        wait.until(ExpectedConditions.elementToBeClickable(endRideButton));
        endRideButton.click();
    }

    public void waitForRateForm() {
        wait.until(ExpectedConditions.visibilityOf(formOverlay));
        wait.until(ExpectedConditions.visibilityOf(rateFormHeader));
    }

    public void waitForNoteForm() {
        wait.until(ExpectedConditions.visibilityOf(formOverlay));
        wait.until(ExpectedConditions.visibilityOf(noteFormHeader));
    }

    public boolean isRateFormDisplayed() {
        try {
            return formOverlay.isDisplayed() && rateFormHeader.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isNoteFormDisplayed() {
        try {
            return formOverlay.isDisplayed() && noteFormHeader.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public void fillDriverRating(int rating) {
        wait.until(ExpectedConditions.visibilityOf(driverRatingInput));
        driverRatingInput.clear();
        driverRatingInput.sendKeys(String.valueOf(rating));
    }

    public void fillVehicleRating(int rating) {
        wait.until(ExpectedConditions.visibilityOf(vehicleRatingInput));
        vehicleRatingInput.clear();
        vehicleRatingInput.sendKeys(String.valueOf(rating));
    }

    public void fillComment(String comment) {
        wait.until(ExpectedConditions.visibilityOf(commentTextarea));
        commentTextarea.clear();
        commentTextarea.sendKeys(comment);
    }

    public String getDriverRatingValue() {
        wait.until(ExpectedConditions.visibilityOf(driverRatingInput));
        return driverRatingInput.getAttribute("value");
    }

    public String getVehicleRatingValue() {
        wait.until(ExpectedConditions.visibilityOf(vehicleRatingInput));
        return vehicleRatingInput.getAttribute("value");
    }

    public String getCommentValue() {
        wait.until(ExpectedConditions.visibilityOf(commentTextarea));
        return commentTextarea.getAttribute("value");
    }

    public void clickSendButton() {
        try {
            WebElement button = wait.until(ExpectedConditions.elementToBeClickable(By.id("send-rating-btn")));

            js.executeScript("arguments[0].scrollIntoView(true);", button);

            final WebElement finalButton = button;
            wait.until(driver -> (Boolean) js.executeScript(
                "return Math.abs(arguments[0].getBoundingClientRect().top - window.innerHeight / 2) < 100;",
                finalButton
            ));

            button.click();
        } catch (Exception e) {
            try {
                WebElement button = driver.findElement(By.id("send-rating-btn"));
                js.executeScript("arguments[0].click();", button);
            } catch (Exception ex) {
                throw new RuntimeException("Failed to click send button", ex);
            }
        }
    }

    public void clickCloseButton() {
        wait.until(ExpectedConditions.elementToBeClickable(closeButton));
        closeButton.click();
    }

    public void submitRating(int driverRating, int vehicleRating, String comment) {
        waitForRateForm();
        fillDriverRating(driverRating);
        fillVehicleRating(vehicleRating);
        if (comment != null && !comment.isEmpty()) {
            fillComment(comment);
        }
        clickSendButton();
    }

    public void waitForFormToClose() {
        wait.until(ExpectedConditions.invisibilityOf(formOverlay));
    }

    public boolean isFormClosed() {
        try {
            return !formOverlay.isDisplayed();
        } catch (Exception e) {
            return true;
        }
    }

    public boolean isRateButtonVisible() {
        try {
            return rateButton.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isNoteButtonVisible() {
        try {
            return noteButton.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isEndRideButtonVisible() {
        try {
            return endRideButton.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    public void waitForRatingToBeSaved(Object repository) {
        WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(3));
        shortWait.pollingEvery(Duration.ofMillis(100));
        shortWait.until(driver -> {
            try {
                java.lang.reflect.Method findAllMethod = repository.getClass().getMethod("findAll");
                List<?> ratings = (List<?>) findAllMethod.invoke(repository);
                return !ratings.isEmpty();
            } catch (Exception e) {
                return false;
            }
        });
    }
}

