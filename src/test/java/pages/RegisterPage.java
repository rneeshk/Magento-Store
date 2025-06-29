package pages;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.Color;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import org.openqa.selenium.Keys; 

public class RegisterPage {

    private WebDriver driver;
    private WebDriverWait wait;

    // --- Web Elements located using @FindBy ---
    @FindBy(id = "firstname")
    private WebElement firstNameField;

    @FindBy(id = "lastname")
    private WebElement lastNameField;

    @FindBy(id = "email_address")
    private WebElement emailField;

    @FindBy(id = "password")
    private WebElement passwordField;

    @FindBy(id = "password-confirmation")
    private WebElement confirmPasswordField;

    @FindBy(css = "button.action.submit.primary")
    private WebElement createAccountButton;

    // --- Error Message Locators ---
    @FindBy(id = "firstname-error")
    private WebElement firstNameError;

    @FindBy(id = "lastname-error")
    private WebElement lastNameError;

    @FindBy(id = "email_address-error")
    private WebElement emailError;

    @FindBy(id = "password-error")
    private WebElement passwordError;
    
    @FindBy(id = "password-confirmation-error")
    private WebElement confirmPasswordError;
    
    @FindBy(css = "div[data-ui-id='message-error']")
    private WebElement generalError;

    // --- Success & Other UI Elements ---
    @FindBy(css = "div[data-ui-id='message-success']")
    private WebElement successMessage;

    @FindBy(css = ".welcome .logged-in")
    private WebElement headerWelcomeMessage;

    @FindBy(id = "password-strength-meter")
    private WebElement passwordStrengthIndicator;
    
    @FindBy(xpath = "//label[@for='firstname']")
    private WebElement firstNameLabel;

    @FindBy(xpath = "//label[@for='lastname']")
    private WebElement lastNameLabel;

    @FindBy(xpath = "//label[@for='email_address']")
    private WebElement emailLabel;
    
    @FindBy(id = "password-strength-meter-label")
    private WebElement passwordStrengthLabel;

    // Constructor
    public RegisterPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }

    // --- Page Actions ---

    public void enterFirstName(String firstName) {
        firstNameField.sendKeys(firstName);
    }

    public void enterLastName(String lastName) {
        lastNameField.sendKeys(lastName);
    }

    public void enterEmail(String email) {
        emailField.sendKeys(email);
    }

    public void enterPassword(String password) {
    	passwordField.click();
    	passwordField.clear();
        passwordField.sendKeys(password);
        
    }

    public void enterConfirmPassword(String confirmPassword) {
        confirmPasswordField.sendKeys(confirmPassword);
    }

    public void clickCreateAccountButton() {
//        createAccountButton.click();
    	try {
            wait.until(ExpectedConditions.elementToBeClickable(createAccountButton));
            createAccountButton.click();
        } catch (ElementClickInterceptedException e) {
            System.out.println("Standard click was intercepted. Using JavaScript click as a fallback.");
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].click();", createAccountButton);
        }
    }
    
    
    public void clearAllFields() {
        firstNameField.clear();
        lastNameField.clear();
        emailField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
    }
    
    // --- Assertion Methods ---

    public String getSuccessMessageText() {
        wait.until(ExpectedConditions.visibilityOf(successMessage));
        return successMessage.getText();
    }

    public String getHeaderWelcomeMessageText() {
        wait.until(ExpectedConditions.visibilityOf(headerWelcomeMessage));
        return headerWelcomeMessage.getText();
    }
    
    public String getRequiredFieldErrorMessage(String fieldName) {
        switch(fieldName.toLowerCase()) {
            case "firstname":
                return firstNameError.getText();
            case "lastname":
                return lastNameError.getText();
            case "email":
                return emailError.getText();
            case "password":
                return passwordError.getText();
            case "confirmpassword":
                return confirmPasswordError.getText();
            default:
                return "Invalid field name provided";
        }
    }

    public String getEmailAlreadyExistsErrorMessage() {
        wait.until(ExpectedConditions.visibilityOf(generalError));
        return generalError.getText();
    }
    
    public String getInvalidEmailFormatErrorMessage() {
        wait.until(ExpectedConditions.visibilityOf(emailError));
        return emailError.getText();
    }

    public String getPasswordMismatchErrorMessage() {
        wait.until(ExpectedConditions.visibilityOf(confirmPasswordError));
        return confirmPasswordError.getText();
    }
    
    public String getPasswordStrengthErrorMessage() {
        wait.until(ExpectedConditions.visibilityOf(passwordError));
        return passwordError.getText();
    }
    
    public boolean areAllErrorMessagesDisplayed() {
        return firstNameError.isDisplayed() &&
               lastNameError.isDisplayed() &&
               emailError.isDisplayed() &&
               passwordError.isDisplayed() &&
               confirmPasswordError.isDisplayed();
    }

    public boolean isPasswordMasked() {
        return passwordField.getAttribute("type").equals("password");
    }

    public String getPasswordStrengthIndicatorText() {
        return passwordStrengthLabel.getText();
    }
    
    public String getFieldLabelText(String fieldName) {
    	switch (fieldName.toLowerCase()) {
	        case "firstname":
	            return firstNameLabel.getText();
	        case "lastname":
	            return lastNameLabel.getText();
	        case "email":
	            return emailLabel.getText();
	        default:
	            return "Invalid field name provided";
	    }
    }
    
    public void setMobileViewport() {
        driver.manage().window().setSize(new Dimension(390, 844)); // iPhone 12 Pro dimensions
    }

    public boolean isCreateAccountButtonEnabled() {
        return createAccountButton.isEnabled();
    }
    
    public WebElement getSuccessMessageElement() {
        return successMessage;
    }

    public WebElement getGeneralErrorElement() {
        return generalError;
    }
    public void triggerValidationByTabbingFromEmailField() {
        emailField.sendKeys(Keys.TAB);
    }
    
    public WebElement getFirstNameErrorElement() {
        return firstNameError;
    }

    public WebElement getLastNameErrorElement() {
        return lastNameError;
    }

    public WebElement getEmailErrorElement() {
        return emailError;
    }
    
    public void enterWhitespaceAndTabOutOfAllFields() {
        firstNameField.sendKeys(" ");
        firstNameField.sendKeys(Keys.TAB);

        lastNameField.sendKeys(" ");
        lastNameField.sendKeys(Keys.TAB);

        emailField.sendKeys(" ");
        emailField.sendKeys(Keys.TAB);
        
        passwordField.sendKeys(" ");
        passwordField.sendKeys(Keys.TAB);
        
        confirmPasswordField.sendKeys(" ");
        confirmPasswordField.sendKeys(Keys.TAB); 
    }
    
    public WebElement getPasswordStrengthIndicatorElement() {
        return passwordStrengthLabel;
    }
    
    public WebElement getFirstNameField() {
        return firstNameField;
    }

    public WebElement getLastNameField() {
        return lastNameField;
    }

    public WebElement getEmailField() {
        return emailField;
    }

    public WebElement getPasswordField() {
        return passwordField;
    }

    public WebElement getConfirmPasswordField() {
        return confirmPasswordField;
    }

    public WebElement getCreateAccountButton() {
        return createAccountButton;
    }

    public void tabFromElement(WebElement element) {
        element.sendKeys(Keys.TAB);
    }
    
    public WebElement getActiveElement() {
        return driver.switchTo().activeElement();
    }
    
    public String getErrorMessageColor(String fieldName) {
        WebElement errorElement;
        switch (fieldName.toLowerCase()) {
            case "firstname":
                errorElement = firstNameError;
                break;
            case "lastname":
                errorElement = lastNameError;
                break;
            case "email":
                errorElement = emailError;
                break;
            default:
                throw new IllegalArgumentException("Invalid field name provided for error color check: " + fieldName);
        }
        String cssColor = errorElement.getCssValue("color");
        
        return Color.fromString(cssColor).asHex();
    }
}