package ZgazeniSendvic.Server_Back_ISS.E2E.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class FavoriteRoutesPage extends BasePage {

    @FindBy(xpath = "(//button[contains(.,'Use route')])[1]")
    private WebElement firstUseRouteBtn;

    public FavoriteRoutesPage(WebDriver driver) {
        super(driver);
    }

    public void useFirstRoute() {
        click(firstUseRouteBtn);
        waitUrlContains("/ride-order");
    }
}
