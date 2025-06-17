package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

public class ProductDiscoveryAndBrowsing {

    private WebDriver driver;
    private WebDriverWait wait;

    // --- Locators ---

    // Navigation
    @FindBy(id = "ui-id-5") // Men Menu
    private WebElement menMenu;

    @FindBy(id = "ui-id-17") // Men > Tops Submenu
    private WebElement menTopsSubMenu;
    
    // Sorting
    @FindBy(id = "sorter")
    private WebElement sorterDropdown;
    
    // Product List
    @FindBy(css = ".product-item-info .price-wrapper .price")
    private List<WebElement> productPrices;

    @FindBy(css = ".product-item-name a")
    private List<WebElement> productNames;

    @FindBy(css = ".product-items .product-item")
    private List<WebElement> productItems;
    
    // Filters (Layered Navigation)
    @FindBy(css = "a[class='action clear filter-clear'] span")
    private WebElement clearAllFiltersLink;

    @FindBy(css = ".filter-current ol li")
    private List<WebElement> activeFilterItems;
    
    @FindBy(css = ".message.info.empty")
    private WebElement noProductsMessage;
    
    // Common
    @FindBy(css = "body .loading-mask")
    private WebElement loadingMask;
    
    @FindBy(id = "toolbar-amount")
    private WebElement paginationToolbar;

    // --- Constructor ---
    public ProductDiscoveryAndBrowsing(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        PageFactory.initElements(driver, this);
    }
    
    // --- Methods ---

    public void navigateToMenTopsCategory() {
        Actions actions = new Actions(driver);
        actions.moveToElement(menMenu).perform();
        wait.until(ExpectedConditions.elementToBeClickable(menTopsSubMenu)).click();
        waitForLoadingMaskToDisappear();
    }
    
    public void navigateToMenCategory() {
        wait.until(ExpectedConditions.elementToBeClickable(menMenu)).click();
        waitForLoadingMaskToDisappear();
    }
    
    public void sortByOption(String optionText) {
        Select sorter = new Select(sorterDropdown);
        sorter.selectByVisibleText(optionText);
        waitForLoadingMaskToDisappear();
    }
    
    public void applyFilter(String category, String option) {
        // 1. Find the filter category header (e.g., the div containing "Style")
        WebElement categoryHeader = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//div[@class='filter-options-item' and .//div[normalize-space()='" + category + "']]")
        ));

        // 2. Check if the filter is collapsed. If so, click the header to expand it.
        // The "active" class indicates it's already expanded.
        if (!categoryHeader.getAttribute("class").contains("active")) {
            categoryHeader.findElement(By.tagName("div")).click(); // Click the title to expand
        }

        // 3. Construct a stable, relative locator for the specific filter option link
        By optionLocator = By.xpath(".//a[contains(normalize-space(), '" + option + "')]");

        // 4. Explicitly wait for the option link to be visible AND clickable within the expanded content area
        WebElement optionLink = wait.until(ExpectedConditions.elementToBeClickable(
            categoryHeader.findElement(optionLocator)
        ));
        
        // 5. Click the option link
        optionLink.click();

        // 6. Wait for the page to finish reloading the products
        waitForLoadingMaskToDisappear();
    }
    
    public List<Double> getProductPrices() {
        List<WebElement> priceElements = driver.findElements(By.cssSelector(".product-item-info .price-wrapper .price"));
        return priceElements.stream()
                .map(el -> Double.parseDouble(el.getText().replace("$", "")))
                .collect(Collectors.toList());
    }
    
    public List<String> getProductNames() {
        List<WebElement> nameElements = driver.findElements(By.cssSelector(".product-item-name a"));
        return nameElements.stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
    }

    public List<String> getActiveFilterLabels() {
        // This find will return an empty list if the element is not found, which is perfect for our assertion.
        List<WebElement> activeFilterElements = driver.findElements(By.cssSelector(".filter-current ol li"));
        return activeFilterElements.stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
    }

    public void clickClearAllFilters() {
        clearAllFiltersLink.click();
        waitForLoadingMaskToDisappear();
    }

    public boolean isNoProductsMessageDisplayed() {
        try {
            return wait.until(ExpectedConditions.visibilityOf(noProductsMessage)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    public int getProductCount() {
        // This find will correctly count the products on the current page state.
        List<WebElement> productItemElements = driver.findElements(By.cssSelector(".product-items .product-item"));
        return productItemElements.size();
    }
    
    public String getPaginationToolbarText() {
        return wait.until(ExpectedConditions.visibilityOf(paginationToolbar)).getText();
    }

    public void waitForLoadingMaskToDisappear() {
        // This is a robust way to handle waits on Magento
        // Wait for the mask to be visible first (it appears briefly)
        try {
            wait.until(ExpectedConditions.visibilityOf(loadingMask));
        } catch (Exception e) {
            // Mask may not appear if the action is too fast, which is fine.
        }
        // Then wait for it to be invisible
        wait.until(ExpectedConditions.invisibilityOf(loadingMask));
    }
    
}