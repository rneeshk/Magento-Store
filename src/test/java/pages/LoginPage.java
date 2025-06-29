package pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class LoginPage {

    private final WebDriverWait wait;
    private final JavascriptExecutor js;

    @FindBy(id = "email")
    private WebElement emailField;

    @FindBy(id = "pass")
    private WebElement passwordField;

    @FindBy(id = "send2")
    private WebElement signInButton;
    
    // --- Other Links ---
    @FindBy(css = "a.action.remind")
    private WebElement forgotPasswordLink;

    @FindBy(css = ".login-container .create")
    private WebElement createAccountButton;
    
    // --- Error Messages ---
    @FindBy(id = "email-error")
    private WebElement emailRequiredError;

    @FindBy(id = "pass-error")
    private WebElement passwordRequiredError;

    @FindBy(css = "div[data-ui-id='message-error']")
    private WebElement generalErrorMessage;
    
    @FindBy(id = "email-error")
    private WebElement emailError;

    // --- Header & Post-Login Elements ---
    @FindBy(css = ".panel.header .welcome .logged-in")
    private WebElement headerWelcomeMessage;

    @FindBy(xpath = "//div[@class='panel header']//a[contains(.,'Sign In')]")
    private List<WebElement> headerSignInLinks;

    @FindBy(css = "button.action.switch")
    private WebElement customerMenuDropdown;

    @FindBy(xpath = "//div[@class='customer-menu']//a[contains(., 'Sign Out')]")
    private WebElement signOutLink;
    
    @FindBy(id = "customer-email")
    private WebElement checkoutEmailField;
    @FindBy(name = "password")
    private WebElement checkoutPasswordField;
    @FindBy(css = "button.action-login")
    private WebElement checkoutLoginButton;
    @FindBy(css = ".shipping-address-item.selected-item")
    private WebElement loggedInShippingAddressBox;
    
    @FindBy(css = "button[data-role='proceed-to-checkout']")
    private WebElement proceedToCheckoutButton;

    public LoginPage(WebDriver driver) {
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        this.js = (JavascriptExecutor) driver;
        PageFactory.initElements(driver, this);
    }

    // --- Robust Helper Methods ---
    private void safeClick(WebElement element) {
        wait.until(ExpectedConditions.elementToBeClickable(element));
        js.executeScript("arguments[0].scrollIntoView(true);", element);
        js.executeScript("arguments[0].click();", element);
    }
    
    private void safeSendKeys(WebElement element, String text) {
        wait.until(ExpectedConditions.visibilityOf(element));
        js.executeScript("arguments[0].value = arguments[1];", element, text);
    }
    
    // --- Public Page Actions ---
    public void enterEmail(String email) {
        safeSendKeys(emailField, email);
    }

    public void enterPassword(String password) {
        safeSendKeys(passwordField, password);
    }

    public void clickSignInButton() {
        safeClick(signInButton);
    }

    public void login(String email, String password) {
        enterEmail(email);
        enterPassword(password);
        clickSignInButton();
    }
    
    public void signOut() {
        safeClick(customerMenuDropdown);
        safeClick(signOutLink);
    }
    
    public void clickForgotPasswordLink() {
        safeClick(forgotPasswordLink);
    }
    
    public void clickCreateAccountButton() {
        safeClick(createAccountButton);
    }
    
    public void submitFormFromPassword() {
        passwordField.sendKeys(Keys.ENTER);
    }
    
    // --- Assertion Methods & Getters ---
    public String getGeneralErrorMessageText() {
        wait.until(ExpectedConditions.visibilityOf(generalErrorMessage));
        return generalErrorMessage.getText();
    }
    
    public String getEmailError() {
    	wait.until(ExpectedConditions.visibilityOf(emailError));
    	return emailError.getText();
    }

    public String getEmailRequiredErrorText() {
        wait.until(ExpectedConditions.visibilityOf(emailRequiredError));
        return emailRequiredError.getText();
    }
    
    public boolean areBothRequiredErrorMessagesDisplayed() {
        return emailRequiredError.isDisplayed() && passwordRequiredError.isDisplayed();
    }

    public boolean isWelcomeMessageVisibleInHeader() {
        try {
            wait.until(ExpectedConditions.visibilityOf(headerWelcomeMessage));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    public String getHeaderWelcomeMessageText() {
        wait.until(ExpectedConditions.visibilityOf(headerWelcomeMessage));
        return headerWelcomeMessage.getText();
    }

    public boolean isSignInLinkVisibleInHeader() {
        // Use list to check for presence without throwing an exception
        return !headerSignInLinks.isEmpty();
    }

    public boolean isPasswordMasked() {
        return passwordField.getAttribute("type").equals("password");
    }

    public void setMobileViewport(WebDriver driver) {
        driver.manage().window().setSize(new Dimension(390, 844));
    }
    
    public boolean areLoginFieldsVisibleOnMobile() {
        return emailField.isDisplayed() && signInButton.isDisplayed();
    }
    
    public void loginDuringCheckout(String email, String password) {
        // Use safeSendKeys on the checkout email field
        safeSendKeys(checkoutEmailField, email);
        
        checkoutEmailField.sendKeys(Keys.TAB);
        // CRUCIAL: Wait for the password field to appear after email is entered
        wait.until(ExpectedConditions.visibilityOf(checkoutPasswordField));
        
        // Now type the password
        safeSendKeys(checkoutPasswordField, password);
        safeClick(checkoutLoginButton);
    }
    
    @SuppressWarnings("unused")
	private By getByFromElement(WebElement element) {
        // This is a bit of a hack, but it's effective for PageFactory.
        // It extracts the locator strategy from the element's toString() representation.
        String[] parts = element.toString().split(" -> ")[1].split(": ");
        String locatorType = parts[0];
        String locatorValue = parts[1].substring(0, parts[1].length() - 1);

        switch (locatorType) {
            case "id": return By.id(locatorValue);
            case "name": return By.name(locatorValue);
            case "className": return By.className(locatorValue);
            case "css selector": return By.cssSelector(locatorValue);
            case "xpath": return By.xpath(locatorValue);
            case "tagName": return By.tagName(locatorValue);
            default: throw new IllegalStateException("Locator type not supported: " + locatorType);
        }
    }
    
    public String getLoggedInShippingAddressText() { 
    	wait.until(ExpectedConditions.visibilityOf(loggedInShippingAddressBox)); 
    	return loggedInShippingAddressBox.getText(); 
    }
    
    public void clickProceedToCheckout() { 
    	safeClick(proceedToCheckoutButton); 
    }
}