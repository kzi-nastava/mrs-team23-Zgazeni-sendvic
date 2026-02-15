package ZgazeniSendvic.Server_Back_ISS.E2E.pages;


import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class HORAdminPage {

    private WebDriver driver;
    private WebDriverWait wait;

    // Filter form elements
    @FindBy(css = "input[formcontrolname='targetId']")
    private WebElement targetIdInput;

    @FindBy(css = "input[formcontrolname='fromDate']")
    private WebElement fromDateInput;

    @FindBy(css = "input[formcontrolname='toDate']")
    private WebElement toDateInput;

    @FindBy(css = "button[color='primary']")
    private WebElement applyButton;

    @FindBy(css = "button[type='button']:not([color='primary'])")
    private WebElement clearButton;

    // Table elements
    @FindBy(css = "table.rides-table")
    private WebElement ridesTable;

    @FindBy(css = "tr.mat-row")
    private List<WebElement> tableRows;

    @FindBy(css = "th[mat-sort-header]")
    private List<WebElement> sortableHeaders;

    // State elements
    @FindBy(css = ".state mat-progress-spinner")
    private WebElement loadingSpinner;

    @FindBy(css = ".state.error")
    private WebElement errorMessage;

    @FindBy(css = ".state:not(.error)")
    private WebElement emptyStateMessage;

    // Paginator
    @FindBy(css = "mat-paginator")
    private WebElement paginator;

    @FindBy(css = ".mat-paginator-page-size-select")
    private WebElement pageSizeSelect;

    @FindBy(css = "button.mat-paginator-navigation-next")
    private WebElement nextPageButton;

    @FindBy(css = "button.mat-paginator-navigation-previous")
    private WebElement previousPageButton;

    public HORAdminPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }

    // Navigation
    public void navigateTo(String baseUrl) {
        driver.get(baseUrl + "/hor-admin"); // Adjust path as needed
    }

    // Filter actions
    public void enterTargetId(String targetId) {
        wait.until(ExpectedConditions.elementToBeClickable(targetIdInput));
        targetIdInput.clear();
        targetIdInput.sendKeys(targetId);
    }

    public void enterFromDate(String date) {
        wait.until(ExpectedConditions.elementToBeClickable(fromDateInput));
        fromDateInput.clear();
        fromDateInput.sendKeys(date);
    }

    public void enterToDate(String date) {
        wait.until(ExpectedConditions.elementToBeClickable(toDateInput));
        toDateInput.clear();
        toDateInput.sendKeys(date);
    }

    public void clickApplyFilters() {
        wait.until(ExpectedConditions.elementToBeClickable(applyButton));
        applyButton.click();
        waitForLoadingToComplete();
    }

    public void clickClearFilters() {
        wait.until(ExpectedConditions.elementToBeClickable(clearButton));
        clearButton.click();
    }

    public void applyFilters(String targetId, String fromDate, String toDate) {
        if (targetId != null && !targetId.isEmpty()) {
            enterTargetId(targetId);
        }
        if (fromDate != null && !fromDate.isEmpty()) {
            enterFromDate(fromDate);
        }
        if (toDate != null && !toDate.isEmpty()) {
            enterToDate(toDate);
        }
        clickApplyFilters();
    }

    // Sorting actions
    public void sortByColumn(String columnName) {
        WebElement header = driver.findElement(
                By.xpath("//th[contains(text(), '" + columnName + "')]")
        );
        wait.until(ExpectedConditions.elementToBeClickable(header));
        header.click();
        waitForLoadingToComplete();
    }

    public void sortByRideId() {
        sortByColumn("Ride ID");
    }

    public void sortByCreated() {
        sortByColumn("Created");
    }

    public void sortByStart() {
        sortByColumn("Start");
    }

    public void sortByEnd() {
        sortByColumn("End");
    }

    public void sortByStatus() {
        sortByColumn("Status");
    }

    public void sortByPrice() {
        sortByColumn("Price");
    }

    // Table data retrieval
    public int getNumberOfRows() {
        waitForLoadingToComplete();
        return tableRows.size();
    }

    public List<WebElement> getTableRows() {
        waitForLoadingToComplete();
        return tableRows;
    }

    public String getCellValue(int rowIndex, String columnName) {
        String columnIndex = getColumnIndex(columnName);
        WebElement cell = driver.findElement(
                By.xpath("//tr[@class='mat-row'][" + (rowIndex + 1) + "]/td[" + columnIndex + "]")
        );
        return cell.getText();
    }

    public String getRideIdFromRow(int rowIndex) {
        return getCellValue(rowIndex, "Ride ID");
    }

    public String getStatusFromRow(int rowIndex) {
        return getCellValue(rowIndex, "Status");
    }

    public String getPriceFromRow(int rowIndex) {
        return getCellValue(rowIndex, "Price");
    }

    // Pagination actions
    public void goToNextPage() {
        wait.until(ExpectedConditions.elementToBeClickable(nextPageButton));
        nextPageButton.click();
        waitForLoadingToComplete();
    }

    public void goToPreviousPage() {
        wait.until(ExpectedConditions.elementToBeClickable(previousPageButton));
        previousPageButton.click();
        waitForLoadingToComplete();
    }

    public void selectPageSize(int size) {
        wait.until(ExpectedConditions.elementToBeClickable(pageSizeSelect));
        pageSizeSelect.click();
        WebElement option = driver.findElement(
                By.xpath("//mat-option/span[contains(text(), '" + size + "')]")
        );
        option.click();
        waitForLoadingToComplete();
    }

    // State checks
    public boolean isLoading() {
        try {
            return loadingSpinner.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean hasError() {
        try {
            return errorMessage.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public String getErrorMessage() {
        wait.until(ExpectedConditions.visibilityOf(errorMessage));
        return errorMessage.getText();
    }

    public boolean isEmpty() {
        try {
            return emptyStateMessage.isDisplayed() &&
                    emptyStateMessage.getText().contains("No rides yet");
        } catch (Exception e) {
            return false;
        }
    }

    public boolean hasRides() {
        return !isEmpty() && getNumberOfRows() > 0;
    }

    // Wait helpers
    public void waitForLoadingToComplete() {
        wait.until(ExpectedConditions.invisibilityOf(loadingSpinner));
    }

    public void waitForTableToLoad() {
        waitForLoadingToComplete();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("table.rides-table")));
    }

    // Validation helpers
    public boolean isTableSorted(String columnName, boolean ascending) {
        // Get all values from the specified column
        List<WebElement> cells = driver.findElements(
                By.xpath("//td[position()=" + getColumnIndex(columnName) + "]")
        );

        if (cells.size() < 2) return true; // Can't verify sort with less than 2 items

        // Check if values are in order (simplified - expand based on data type)
        for (int i = 0; i < cells.size() - 1; i++) {
            String current = cells.get(i).getText();
            String next = cells.get(i + 1).getText();

            int comparison = current.compareTo(next);
            if (ascending && comparison > 0) return false;
            if (!ascending && comparison < 0) return false;
        }
        return true;
    }

    // Helper method to get column index by name
    private String getColumnIndex(String columnName) {
        return switch (columnName.toLowerCase()) {
            case "ride id" -> "1";
            case "created" -> "2";
            case "route" -> "3";
            case "start" -> "4";
            case "end" -> "5";
            case "from" -> "6";
            case "to" -> "7";
            case "status" -> "8";
            case "canceled" -> "9";
            case "price" -> "10";
            case "panic" -> "11";
            default -> "1";
        };
    }

}
