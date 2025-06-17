package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class UserAccountManagement {

    private WebDriver driver;
    private WebDriverWait wait;

    // --- Locators ---

    // Header Links
    @FindBy(linkText = "Create an Account")
    private WebElement createAccountLink;

    @FindBy(linkText = "Sign In")
    private WebElement signInLink;

    // Create Account Page
    @FindBy(id = "firstname")
    private WebElement firstNameInput;

    @FindBy(id = "lastname")
    private WebElement lastNameInput;

    @FindBy(id = "email_address")
    private WebElement emailInput;

    @FindBy(id = "password")
    private WebElement passwordInput;

    @FindBy(id = "password-confirmation")
    private WebElement passwordConfirmInput;
    
    @FindBy(id = "password-strength-meter")
    private WebElement passwordStrengthIndicator;

    @FindBy(css = "button[title='Create an Account']")
    private WebElement createAccountButton;

    @FindBy(css = ".message-success > div")
    private WebElement successMessage;
    
    @FindBy(css = ".message-error > div")
    private WebElement pageErrorMessage;
    
    // Field-level Errors
    @FindBy(id = "firstname-error")
    private WebElement firstNameError;

    @FindBy(id = "lastname-error")
    private WebElement lastNameError;

    @FindBy(id = "email_address-error")
    private WebElement emailError;

    @FindBy(id = "password-error")
    private WebElement passwordError;
    
    @FindBy(id = "pass-error")
    private WebElement loginPasswordError;

    @FindBy(id = "password-confirmation-error")
    private WebElement passwordConfirmError;
    
    @FindBy(id = "email-error")
    private WebElement loginEmailError;


    // Login Page
    @FindBy(id = "email") 
    private WebElement loginEmailInput;

    @FindBy(id = "pass")
    private WebElement loginPasswordInput;

    @FindBy(id = "send2")
    private WebElement signInButton;

    // Logged-In Header
    @FindBy(css = ".header .welcome .logged-in")
    private WebElement welcomeMessage;
    private By welcomeMessageLocator = By.cssSelector(".header .welcome .logged-in");

    @FindBy(css = ".header .customer-welcome button")
    private WebElement userDropdownArrow;
    
    @FindBy(xpath = "//div[@aria-hidden='false']//a[normalize-space()='Sign Out']")
    private WebElement signOutLink;

    // Forgot Password
    @FindBy(css = ".login-container .action.remind")
    private WebElement forgotPasswordLink;

    @FindBy(id = "email_address") 
    private WebElement forgotPasswordEmailInput;

    @FindBy(css = "button.action.submit.primary")
    private WebElement resetPasswordButton;
    
    @FindBy(css = ".login-container .action.create")
    private WebElement createAccountFromLoginLink;

    @FindBy(css = "label[for='firstname']")
    private WebElement firstNameLabel;

    @FindBy(css = ".breadcrumbs")
    private WebElement breadcrumbs;

    @FindBy(css = ".page-title")
    private WebElement pageTitleHeading;

    @FindBy(css = "input#password[type='password']")
    private WebElement passwordInputWithTypePassword;

    @FindBy(css = "input#email[placeholder='Email']")
    private WebElement loginEmailPlaceholder;
    
    @FindBy(css = "label[for='email']")
    private WebElement loginEmailLabel;

    @FindBy(css = "input#pass[type='password']")
    private WebElement loginPasswordInputWithTypePassword;
    
    // --- Constructor ---
    public UserAccountManagement(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        PageFactory.initElements(driver, this);
    }
    
    // --- Methods ---

    public void navigateToCreateAccountPage() {
        createAccountLink.click();
    }

    public void navigateToSignInPage() {
        signInLink.click();
    }

    public String generateUniqueEmail() {
        return "testuser" + System.currentTimeMillis() + "@example.com";
    }

    public void enterCreateAccountDetails(String fname, String lname, String email, String pwd, String confPwd) {
        firstNameInput.sendKeys(fname);
        lastNameInput.sendKeys(lname);
        emailInput.sendKeys(email);
        passwordInput.sendKeys(pwd);
        passwordConfirmInput.sendKeys(confPwd);
    }

    public void enterPassword(String pwd) {
        passwordInput.clear();
        passwordInput.sendKeys(pwd);
    }

    public String getPasswordStrengthText() {
        return wait.until(ExpectedConditions.visibilityOf(passwordStrengthIndicator)).getText();
    }

    public void submitCreateAccountForm() {
        createAccountButton.click();
    }
    
    public String getSuccessMessageText() {
        return wait.until(ExpectedConditions.visibilityOf(successMessage)).getText();
    }

    public String getPageErrorMessageText() {
        return wait.until(ExpectedConditions.visibilityOf(pageErrorMessage)).getText();
    }

    public String getFirstNameErrorText() {
        return wait.until(ExpectedConditions.visibilityOf(firstNameError)).getText();
    }

    public String getEmailErrorText() {
        return wait.until(ExpectedConditions.visibilityOf(emailError)).getText();
    }
    
    public String getLoginEmailErrorText() {
        return wait.until(ExpectedConditions.visibilityOf(loginEmailError)).getText();
    }

    public String getPasswordConfirmErrorText() {
        return wait.until(ExpectedConditions.visibilityOf(passwordConfirmError)).getText();
    }
    
    public String getLoginPasswordErrorText() {
        return wait.until(ExpectedConditions.visibilityOf(loginPasswordError)).getText();
    }
    
    

    public void enterLoginDetails(String email, String pwd) {
        loginEmailInput.sendKeys(email);
        loginPasswordInput.sendKeys(pwd);
    }

    public void submitLoginForm() {
        signInButton.click();
    }

    public String getWelcomeMessageText() {
        WebElement welcomeElement = wait.until(ExpectedConditions.visibilityOfElementLocated(welcomeMessageLocator));
        return welcomeElement.getText();
    }

    public boolean isSignInLinkVisible() {
        return wait.until(ExpectedConditions.visibilityOf(signInLink)).isDisplayed();
    }

    public void logout() {
        wait.until(ExpectedConditions.elementToBeClickable(userDropdownArrow)).click();
        wait.until(ExpectedConditions.elementToBeClickable(signOutLink)).click();
    }

    public void navigateToForgotPassword() {
        forgotPasswordLink.click();
    }

    public void submitForgotPassword(String email) {
        forgotPasswordEmailInput.sendKeys(email);
        resetPasswordButton.click();
    }
    
    public void navigateToCreateAccountFromLoginPage() {
        signInLink.click();
        wait.until(ExpectedConditions.visibilityOf(createAccountFromLoginLink)).click();
    }

    public String getFirstNamePlaceholder() {
        return firstNameInput.getAttribute("placeholder");
    }

    public String getLoginEmailPlaceholder() {
        return loginEmailInput.getAttribute("placeholder");
    }

    public boolean isFirstNameMarkedRequired() {
        try {
            WebElement parentDiv = firstNameLabel.findElement(By.xpath("./ancestor::div[contains(@class, 'field')]"));
            String parentClasses = parentDiv.getAttribute("class");
            return parentClasses.contains("required");
        } catch (Exception e) {
            return false;
        }
    }

    public boolean hasCorrectLoginEmailLabel() {
        try {
            String labelText = wait.until(ExpectedConditions.visibilityOf(loginEmailLabel)).getText();
            return labelText.equals("Email");
        } catch (Exception e) {
            return false;
        }
    }
    
    public void submitFormWithKeyboard() {
        passwordConfirmInput.sendKeys(Keys.ENTER);
    }

    public void loginWithKeyboard() {
        loginPasswordInput.sendKeys(Keys.ENTER);
    }

    public String getBreadcrumbText() {
        return wait.until(ExpectedConditions.visibilityOf(breadcrumbs)).getText();
    }

    public String getPageTitleHeadingText() {
        return wait.until(ExpectedConditions.visibilityOf(pageTitleHeading)).getText();
    }

    public boolean isPasswordInputTypePassword() {
        try {
            return loginPasswordInputWithTypePassword.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
}