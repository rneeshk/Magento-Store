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
    private Actions actions;

    // --- Locators ---

    @FindBy(id = "ui-id-5")
    private WebElement menMenu;

    @FindBy(id = "ui-id-17")
    private WebElement menTopsSubMenu;
    
    @FindBy(id = "sorter")
    private WebElement sorterDropdown;
    
    @FindBy(css = ".product-item-info")
    private List<WebElement> productInfoItems;

    @FindBy(css = ".product-items .product-item")
    private List<WebElement> productItems;
    
    @FindBy(css = "a.action.clear") // Corrected locator for Clear All
    private WebElement clearAllFiltersLink;

    @FindBy(css = ".filter-current ol li")
    private List<WebElement> activeFilterItems;
    
    @FindBy(css = ".message.info.empty")
    private WebElement noProductsMessage;
    
    @FindBy(css = "body .loading-mask")
    private WebElement loadingMask;
    
    @FindBy(id = "toolbar-amount")
    private WebElement paginationToolbar;
    private By paginationToolbarLocator = By.id("toolbar-amount");
    
    @FindBy(css = ".product-info-main .action.tocompare")
    private WebElement pdpAddToCompareButton;

    @FindBy(css = ".comparison .product-item-name")
    private List<WebElement> comparedProductNames;

    @FindBy(css = ".comparison .action.delete")
    private List<WebElement> removeComparedItemButtons;

    @FindBy(id = "product-comparison")
    private WebElement compareProductsTable;

    @FindBy(css = ".message.info.empty")
    private WebElement emptyCompareListMessage;

    @FindBy(linkText = "comparison list")
    private WebElement comparisonListLink;
    private By comparisonListLinkLocator = By.linkText("comparison list");


    @FindBy(css = ".page-title")
    private WebElement pageTitleHeading;
   
    

    // --- Constructor ---
    public ProductDiscoveryAndBrowsing(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        this.actions = new Actions(driver); // Initialize Actions
        PageFactory.initElements(driver, this);
    }
    
    // --- Methods ---

    public void waitForLoadingMaskToDisappear() {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(2));
            shortWait.until(ExpectedConditions.visibilityOf(loadingMask));
        } catch (Exception e) {
        }
        wait.until(ExpectedConditions.invisibilityOf(loadingMask));
    }
    
    public String getPageTitleHeadingText() {
        return wait.until(ExpectedConditions.visibilityOf(pageTitleHeading)).getText();
    }
    
    public String getSuccessMessageText() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".message-success > div"))).getText();
    }

    public void navigateToMenTopsCategory() {
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
        WebElement categoryHeader = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//div[@class='filter-options-item' and .//div[normalize-space()='" + category + "']]")
        ));

        if (!categoryHeader.getAttribute("class").contains("active")) {
            categoryHeader.findElement(By.tagName("div")).click();
        }

        By optionLocator = By.xpath(".//a[contains(normalize-space(), '" + option + "')]");

        WebElement optionLink = wait.until(ExpectedConditions.elementToBeClickable(
            categoryHeader.findElement(optionLocator)
        ));
        
        optionLink.click();
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
        List<WebElement> productItemElements = driver.findElements(By.cssSelector(".product-items .product-item"));
        return productItemElements.size();
    }
    
    public String getPaginationToolbarText() {
        WebElement toolbar = wait.until(ExpectedConditions.visibilityOfElementLocated(paginationToolbarLocator));
        return toolbar.getText();
    }

    
    public void navigateToFirstProductPageFromCategory() {
        wait.until(ExpectedConditions.elementToBeClickable(productInfoItems.get(0))).click();
        wait.until(ExpectedConditions.visibilityOf(pdpAddToCompareButton));
    }

    public void addProductToCompareFromPDP() {
        pdpAddToCompareButton.click();
    }

    public void removeFirstItemFromCompare() {
        wait.until(ExpectedConditions.elementToBeClickable(removeComparedItemButtons.get(0))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".modal-popup.confirm .action-primary"))).click();
    }

    public boolean isCompareProductsTableVisible() {
        try {
            return compareProductsTable.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    public void addFirstProductToCompare() {
        WebElement firstProduct = productInfoItems.get(0);
        actions.moveToElement(firstProduct).perform();
        WebElement compareLink = wait.until(ExpectedConditions.elementToBeClickable(
            firstProduct.findElement(By.cssSelector(".action.tocompare"))
        ));
        compareLink.click();
    }
    
    public void addSecondProductToCompare() {
        WebElement secondProduct = productInfoItems.get(1);
        actions.moveToElement(secondProduct).perform();
        WebElement compareLink = wait.until(ExpectedConditions.elementToBeClickable(
            secondProduct.findElement(By.cssSelector(".action.tocompare"))
        ));
        compareLink.click();
    }

    public List<String> getComparedProductNames() {
        return driver.findElements(By.cssSelector(".comparison .product-item-name")).stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
    }
    
    public String getEmptyCompareListMessage() {
        return wait.until(ExpectedConditions.visibilityOf(emptyCompareListMessage)).getText();
    }

    public void navigateToComparePageViaSuccessLink() {
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(comparisonListLinkLocator));
        link.click();
    }
}