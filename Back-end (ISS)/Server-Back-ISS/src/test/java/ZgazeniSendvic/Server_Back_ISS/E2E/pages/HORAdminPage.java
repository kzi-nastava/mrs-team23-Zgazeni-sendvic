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

public class HORAdminPage {

    private WebDriver driver;
    private WebDriverWait wait;
    private JavascriptExecutor js;

    // Filter form elements
    @FindBy(css = "form.filters input[formcontrolname='targetId']")
    private WebElement targetIdInput;

    @FindBy(css = "form.filters input[formcontrolname='fromDate']")
    private WebElement fromDateInput;

    @FindBy(css = "form.filters input[formcontrolname='toDate']")
    private WebElement toDateInput;

    @FindBy(css = "form.filters .filter-actions button[color='primary']")
    private WebElement applyButton;

    @FindBy(css = "form.filters .filter-actions button[mat-stroked-button]")
    private WebElement clearButton;

    // Table elements
    @FindBy(css = "table.rides-table")
    private WebElement ridesTable;

    @FindBy(css = "table.rides-table tr[mat-row]")
    private List<WebElement> tableRows;

    @FindBy(css = "table.rides-table th[mat-sort-header]")
    private List<WebElement> sortableHeaders;

    // State elements
    @FindBy(css = "div.state mat-progress-spinner")
    private WebElement loadingSpinner;

    @FindBy(css = "div.state.error")
    private WebElement errorMessage;

    // Empty state: avoid matching the loading state
    @FindBy(xpath = "//div[contains(@class,'state') and not(contains(@class,'error')) and not(.//mat-progress-spinner)]")
    private WebElement emptyStateMessage;

    // Paginator
    @FindBy(css = "mat-paginator")
    private WebElement paginator;

    // MDC class name is different from older Material
    @FindBy(css = "mat-paginator .mat-mdc-paginator-page-size-select")
    private WebElement pageSizeSelect;

    @FindBy(css = "button.mat-mdc-paginator-navigation-next")
    private WebElement nextPageButton;

    @FindBy(css = "button.mat-mdc-paginator-navigation-previous")
    private WebElement previousPageButton;

    public HORAdminPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        this.js = (JavascriptExecutor) driver;
        PageFactory.initElements(driver, this);
    }

    // Set JWT token in localStorage
    public void setAuthToken(String jwtToken, String role) {
        js.executeScript(
                "localStorage.setItem('jwt_token', arguments[0]);" +
                        "localStorage.setItem('user_role', arguments[1]);",
                jwtToken, role
        );
    }

    // Navigate with authentication
    public void navigateToWithAuth(String baseUrl, String jwtToken) {
        driver.get(baseUrl);
        setAuthToken(jwtToken, "ADMIN");
        driver.get(baseUrl + "/hor-admin");
        waitForTableToLoad();
    }

    // Navigation
    public void navigateTo(String baseUrl) {
        driver.get(baseUrl + "/hor-admin");
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

    // Retrieve sorted
    public void sortByColumn(String columnName) {
        WebElement header = driver.findElement(
                By.xpath("//table[contains(@class,'rides-table')]//th[@mat-sort-header and contains(normalize-space(.), '" + columnName + "')]")
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

    public void sortByRoute() {
        sortByColumn("Route");
    }

    public void sortByStart() {
        sortByColumn("Start");
    }

    public void sortByEnd() {
        sortByColumn("End");
    }

    public void sortByFrom() {
        sortByColumn("From");
    }

    public void sortByTo() {
        sortByColumn("To");
    }

    public void sortByStatus() {
        sortByColumn("Status");
    }

    public void sortByCanceled() {
        sortByColumn("Canceled");
    }

    public void sortByPrice() {
        sortByColumn("Price");
    }

    public void sortByPanic() {
        sortByColumn("Panic");
    }

    // Table data retrieval
    public int getNumberOfRows() {
        waitForLoadingToComplete();
        return driver.findElements(By.cssSelector("table.rides-table tr[mat-row]")).size();
    }

    public List<WebElement> getTableRows() {
        waitForLoadingToComplete();
        return driver.findElements(By.cssSelector("table.rides-table tr[mat-row]"));
    }

    public String getCellValue(int rowIndex, String columnName) {
        String columnIndex = getColumnIndex(columnName);
        WebElement cell = driver.findElement(
                By.xpath("//table[contains(@class,'rides-table')]//tr[@mat-row][" + (rowIndex + 1) + "]/td[" + columnIndex + "]")
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
                By.xpath("//mat-option//span[contains(@class,'mat-mdc-option-text') and contains(., '" + size + "')]")
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
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofMillis(500));
            WebElement emptyState = shortWait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//div[contains(@class,'state') and not(contains(@class,'error')) and not(.//mat-progress-spinner)]")
            ));
            return emptyState.getText().contains("No rides yet");
        } catch (Exception e) {
            return false;
        }
    }

    public boolean hasRides() {
        return !isEmpty() && getNumberOfRows() > 0;
    }

    // Wait helpers
    public void waitForLoadingToComplete() {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(2));
            shortWait.until(ExpectedConditions.invisibilityOf(loadingSpinner));
        } catch (Exception e) {
            System.out.println("Loading spinner not found or already hidden");
        }
    }

    public void waitForTableToLoad() {
        waitForLoadingToComplete();
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(2));
            shortWait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("table.rides-table")));
        } catch (Exception e) {
            System.out.println("Table not found within timeout");
        }
    }

    // Validation helpers
    public boolean isTableSorted(String columnName, boolean ascending) {
        List<WebElement> cells = driver.findElements(
                By.xpath("//table[contains(@class,'rides-table')]//tr[@mat-row]/td[" + getColumnIndex(columnName) + "]")
        );

        if (cells.size() < 2) return true;

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

    public void sortByGivenFieldASC(String columnName, boolean ascending) {
        String realFieldName = switch (columnName.toLowerCase()) {
            case "id" -> "Ride ID";
            case "creationdate" -> "Created";
            case "locations" -> "Route";
            case "starttime" -> "Start";
            case "endtime" -> "End";
            case "startlatitude" -> "From";
            case "endlatitude" -> "To";
            case "status" -> "Status";
            case "canceler" -> "Canceled";
            case "price" -> "Price";
            case "panic" -> "Panic";
            default -> columnName;
        };

        if(ascending){
            sortByColumn(realFieldName);
        } else {
            sortByColumn(realFieldName);
            sortByColumn(realFieldName);
        }


    }

        public void sortByGivenFieldDESC(String columnName) {
            sortByColumn(columnName);
            sortByColumn(columnName);
        }

}
