package ZgazeniSendvic.Server_Back_ISS.E2E.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class LoginPage extends BasePage {

    // Adjust selectors if needed
    @FindBy(css = "input[formcontrolname='email'], input[type='email']")
    private WebElement email;

    @FindBy(css = "input[formcontrolname='password'], input[type='password']")
    private WebElement password;

    @FindBy(css = "button.primary-btn[type='submit']")
    private WebElement loginSubmitBtn;

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public void open(String baseUrl) {
        driver.get(baseUrl + "/login");
        waitUrlContains("/login");
        waitVisible(email);
    }

    public void login(String userEmail, String userPass) {
        type(email, userEmail);
        type(password, userPass);
        click(loginSubmitBtn);
    }
}


