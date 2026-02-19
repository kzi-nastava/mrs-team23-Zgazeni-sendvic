package ZgazeniSendvic.Server_Back_ISS.E2E.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class RideOrderPage extends BasePage {

    @FindBy(xpath = "//button[@type='submit' and normalize-space()='Request Ride']")
    private WebElement requestRideBtn;

    @FindBy(css = "div[formarrayname='locations'] input[formcontrolname='address']")
    private java.util.List<WebElement> addressInputs;

    public RideOrderPage(WebDriver driver) { super(driver); }

    public void fillAllAddresses() {
        wait.until(d -> addressInputs.size() >= 2); // at least start & dest

        for (int i = 0; i < addressInputs.size(); i++) {
            WebElement inp = addressInputs.get(i);
            inp.clear();
            inp.sendKeys(i == 0 ? "Favorite pickup" :
                    (i == addressInputs.size() - 1 ? "Favorite destination" : "Favorite stop " + i));
        }
    }

    public void requestRideExpectSuccess() {
        click(requestRideBtn);

        String alertText = getAndAcceptAlertTextIfPresent();
        if (alertText == null) {
            throw new AssertionError("Expected success/failure alert after requesting ride, but none appeared.");
        }
        if (!alertText.toLowerCase().contains("success")) {
            throw new AssertionError("Ride request did not succeed. Alert: " + alertText);
        }
    }
}


