package ZgazeniSendvic.Server_Back_ISS.E2E.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;

public abstract class BasePage {
    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(12));
        PageFactory.initElements(driver, this);
    }

    protected void waitVisible(WebElement el) {
        wait.until(ExpectedConditions.visibilityOf(el));
    }

    protected void waitClickable(WebElement el) {
        wait.until(ExpectedConditions.elementToBeClickable(el));
    }

    protected void click(WebElement el) {
        waitClickable(el);
        el.click();
    }

    protected void type(WebElement el, String text) {
        waitVisible(el);
        el.clear();
        el.sendKeys(text);
    }

    protected void waitUrlContains(String part) {
        wait.until(ExpectedConditions.urlContains(part));
    }

    protected String getAndAcceptAlertTextIfPresent() {
        try {
            WebDriverWait w = new WebDriverWait(driver, Duration.ofMillis(700));
            Alert a = w.until(ExpectedConditions.alertIsPresent());
            String text = a.getText();
            a.accept();
            return text;
        } catch (TimeoutException ignored) {
            return null;
        }
    }
}

