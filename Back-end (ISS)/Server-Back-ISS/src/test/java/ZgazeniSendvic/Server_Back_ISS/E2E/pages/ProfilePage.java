package ZgazeniSendvic.Server_Back_ISS.E2E.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class ProfilePage extends BasePage {

    // This must match the button text you added to profile-card.html
    @FindBy(xpath = "//button[contains(.,'Favorite routes')]")
    private WebElement favoriteRoutesBtn;

    public ProfilePage(WebDriver driver) {
        super(driver);
    }

    public void open(String baseUrl) {
        driver.get(baseUrl + "/profile");
        waitUrlContains("/profile");
    }

    public void goToFavoriteRoutes() {
        click(favoriteRoutesBtn);
        waitUrlContains("/favorite-routes");
    }
}


