package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
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

public class HomepageAndGeneralElements {

    private WebDriver driver;
    private WebDriverWait wait;
    private Actions actions;

    // --- Locators ---

    // Header & General
    @FindBy(css = "a.logo")
    private WebElement websiteLogo;

    @FindBy(linkText = "Sign In")
    private WebElement signInLink;

    @FindBy(linkText = "Create an Account")
    private WebElement createAccountLink;

    @FindBy(css = "ul.header.links a")
    private List<WebElement> headerLinks;
    
    // Navigation Menu
    @FindBy(id = "ui-id-3") // What's New
    private WebElement whatsNewMenu;
    
    @FindBy(id = "ui-id-4") // Women
	public WebElement womenMenu;

    @FindBy(id = "ui-id-5") // Men
    private WebElement menMenu;
    
    @FindBy(id = "ui-id-6") // Gear
    private WebElement gearMenu;
    
    @FindBy(id = "ui-id-7") // Training
    private WebElement trainingMenu;
    
    @FindBy(id = "ui-id-8") // Sale
    private WebElement saleMenu;
    
    @FindBy(css = ".navigation .level0 > a")
    private List<WebElement> allMainMenuItems;
    
    // Search Bar
    @FindBy(id = "search")
    private WebElement searchInput;

    @FindBy(id = "search_autocomplete")
    private WebElement searchAutoCompleteBox;

    @FindBy(css = "#search_autocomplete ul li")
    private List<WebElement> autoCompleteSuggestions;

    @FindBy(css = ".product-item-link")
    private List<WebElement> searchResultItems;

    @FindBy(css = ".message.notice > div")
    private WebElement noResultsMessage;

    @FindBy(css = "span[data-ui-id='page-title-wrapper']")
    private WebElement pageTitle;

    // Footer
    @FindBy(css = ".footer.content .links a")
    private List<WebElement> footerLinks;
    

    // Search Results Page Elements
    @FindBy(css = ".search.results")
    private WebElement searchResultsContainer;

    @FindBy(css = ".modes-mode.mode-grid")
    private WebElement gridViewButton;

    @FindBy(css = ".modes-mode.mode-list")
    private WebElement listViewButton;

    @FindBy(css = ".products-grid.products-wrapper")
    private WebElement productsGridContainer;

    @FindBy(css = ".product-item-info .tocart")
    private List<WebElement> addToCartButtons; // A stable element to hover over

    @FindBy(css = ".product-item-info .action.tocompare")
    private List<WebElement> addToCompareButtons;

    @FindBy(css = ".message-success > div")
    private WebElement successMessage;

    @FindBy(id = "sorter")
    private WebElement sorterDropdown;

    @FindBy(xpath = "(//select[@id='limiter'])[2]")
    private WebElement limiterDropdown;

    @FindBy(css = ".product-item-info .price-wrapper .price")
    private List<WebElement> productPrices;

    @FindBy(linkText = "Advanced Search")
    private WebElement advancedSearchLink;

    // These are the fields ON THE ADVANCED SEARCH PAGE
    @FindBy(id = "name")
    private WebElement advancedSearchNameInput;

    @FindBy(id = "sku")
    private WebElement advancedSearchSkuInput;

    @FindBy(id = "description")
    private WebElement advancedSearchDescriptionInput;
    
    @FindBy(id = "short_description")
    private WebElement advancedSearchShortDescriptionInput;
    
    @FindBy(id = "price")
    private WebElement advancedSearchPriceFromInput;
    
    @FindBy(id = "price_to")
    private WebElement advancedSearchPriceToInput;

    @FindBy(css = "button.action.search.primary")
    private WebElement advancedSearchFormButton;
    
    @FindBy(css = ".product-item-info")
    private List<WebElement> productInfoItems;


    // --- Constructor ---
    public HomepageAndGeneralElements(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        this.actions = new Actions(driver);
        PageFactory.initElements(driver, this);
    }

    // --- Methods ---

    public void clickWebsiteLogo() {
        websiteLogo.click();
    }
    
    public String getHomePageUrl() {
        return "https://magento.softwaretestingboard.com/";
    }

    public void clickSignIn() {
        signInLink.click();
    }

    public void clickCreateAccount() {
        createAccountLink.click();
    }
    
    public boolean isSignInLinkVisible() {
        return wait.until(ExpectedConditions.visibilityOf(signInLink)).isDisplayed();
    }

    public boolean isCreateAccountLinkVisible() {
        return wait.until(ExpectedConditions.visibilityOf(createAccountLink)).isDisplayed();
    }

    public WebElement getSubMenuFor(WebElement mainMenu) {
        return mainMenu.findElement(By.xpath("following-sibling::ul"));
    }
    
    public void hoverOverMainMenu(String menuName) {
        WebElement menuElement = getMainMenuElement(menuName);
        actions.moveToElement(menuElement).perform();
        // Wait for submenu to be visible
        wait.until(ExpectedConditions.visibilityOf(menuElement.findElement(By.xpath("./..//ul"))));
    }

    public void clickMainMenuItem(String menuName) {
        getMainMenuElement(menuName).click();
    }

    public void clickSubMenuItem(String mainMenuName, String subMenuName) {
        hoverOverMainMenu(mainMenuName);
        WebElement subMenuItem = driver.findElement(By.linkText(subMenuName));
        wait.until(ExpectedConditions.visibilityOf(subMenuItem)).click();
    }
    
    public List<WebElement> getAllHeaderLinks() {
        return headerLinks;
    }

    public void performRapidHover(String menuName) {
        WebElement menuElement = getMainMenuElement(menuName);
        for (int i = 0; i < 5; i++) {
            actions.moveToElement(menuElement).perform();
            actions.moveToElement(websiteLogo).perform(); // Move away
        }
    }

    public void searchFor(String term) {
        searchInput.clear();
        searchInput.sendKeys(term);
    }
    
    public void submitSearch() {
        searchInput.sendKeys(Keys.ENTER);
    }

    public List<WebElement> getSearchResultItems() {
        wait.until(ExpectedConditions.visibilityOfAllElements(searchResultItems));
        return searchResultItems;
    }
    
    public boolean areAutoCompleteSuggestionsVisible() {
        try {
            wait.until(ExpectedConditions.visibilityOf(searchAutoCompleteBox));
            return searchAutoCompleteBox.isDisplayed() && !autoCompleteSuggestions.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    public String getNoResultsMessageText() {
        String fullText = wait.until(ExpectedConditions.visibilityOf(noResultsMessage)).getText().trim();
        String firstLine = fullText.split("\n")[0];  // Just the first line
        return firstLine;
    }
    
    public void clickSearchIconWithEmptyField() {
        searchInput.clear();
        searchInput.sendKeys(Keys.ENTER);
    }
    
    public String getCurrentPageTitleText() {
        return wait.until(ExpectedConditions.visibilityOf(pageTitle)).getText();
    }
    
    public List<WebElement> getFooterLinks() {
        return footerLinks;
    }
    
    private WebElement getMainMenuElement(String menuName) {
        switch (menuName.toLowerCase()) {
            case "what's new": return whatsNewMenu;
            case "women": return womenMenu;
            case "men": return menMenu;
            case "gear": return gearMenu;
            case "training": return trainingMenu;
            case "sale": return saleMenu;
            default: throw new IllegalArgumentException("Invalid menu name: " + menuName);
        }
    }

    public boolean isSearchInputVisible() {
        return searchInput.isDisplayed();
    }

    public void switchToListView() {
        wait.until(ExpectedConditions.elementToBeClickable(listViewButton)).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("body .loading-mask")));
    }

    public boolean isGridViewActive() {
        // The grid view button has the 'active' class when grid view is selected
        return gridViewButton.getAttribute("class").contains("active");
    }

    public void addFirstProductToCompare() {

        WebElement firstProduct = productInfoItems.get(0);
        actions.moveToElement(firstProduct).perform();
        
        WebElement compareLink = wait.until(ExpectedConditions.elementToBeClickable(
            firstProduct.findElement(By.cssSelector(".action.tocompare"))
        ));
        compareLink.click();
    }

    public String getSuccessMessageText() {
        return wait.until(ExpectedConditions.visibilityOf(successMessage)).getText();
    }

    public void sortByOnSearchResults(String optionText) {
        Select sorter = new Select(sorterDropdown);
        sorter.selectByVisibleText(optionText);
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("body .loading-mask")));
    }

    public List<Double> getProductPrices() {
        return productPrices.stream()
                .map(el -> Double.parseDouble(el.getText().replace("$", "")))
                .collect(Collectors.toList());
    }

    public void selectShowPerPage(String optionValue) {
        Select limiter = new Select(limiterDropdown);
        limiter.selectByValue(optionValue);
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("body .loading-mask")));
    }

    public int getProductCount() {
        return searchResultItems.size();
    }

    public void navigateToAdvancedSearchPage() {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", advancedSearchLink);
        wait.until(ExpectedConditions.elementToBeClickable(advancedSearchLink)).click();
        wait.until(ExpectedConditions.titleIs("Advanced Search"));
    }
    public void fillAndSubmitAdvancedSearchForm(String productName, String sku, String description, String shortDescription, String price, String priceTo) {
        wait.until(ExpectedConditions.visibilityOf(advancedSearchNameInput)).sendKeys(productName);
        wait.until(ExpectedConditions.visibilityOf(advancedSearchSkuInput)).sendKeys(sku);
        wait.until(ExpectedConditions.visibilityOf(advancedSearchDescriptionInput)).sendKeys(description);
        wait.until(ExpectedConditions.visibilityOf(advancedSearchShortDescriptionInput)).sendKeys(shortDescription);
        wait.until(ExpectedConditions.visibilityOf(advancedSearchPriceFromInput)).sendKeys(price);
        wait.until(ExpectedConditions.visibilityOf(advancedSearchPriceToInput)).sendKeys(priceTo);
        advancedSearchFormButton.click();
    }
}