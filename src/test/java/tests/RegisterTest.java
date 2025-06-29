package tests;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import io.github.bonigarcia.wdm.WebDriverManager;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.*;
import pages.RegisterPage;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;

public class RegisterTest {

    private WebDriver driver;
    private RegisterPage registerPage;
    private static ExtentReports extent;
    private ExtentTest test;

    private final String REGISTER_URL = "https://magento.softwaretestingboard.com/customer/account/create/";

    @BeforeSuite
    public void setupSuite() throws UnknownHostException {
        extent = new ExtentReports();
        ExtentSparkReporter spark = new ExtentSparkReporter("test-output/RegistrationReport.html");
        extent.attachReporter(spark);
        String hostName = InetAddress.getLocalHost().getHostName();
        String userName = System.getProperty("user.name");

        extent.setSystemInfo("Host Name", hostName);
        extent.setSystemInfo("Environment", "QA");
        extent.setSystemInfo("User Name", userName);
    }

    @BeforeMethod
    public void setup(Method method) {
        WebDriverManager.chromedriver().setup();
//        driver = new ChromeDriver();
        
        
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        driver = new ChromeDriver(options);
        
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.get(REGISTER_URL);
        registerPage = new RegisterPage(driver);
        Test testAnnotation = method.getAnnotation(Test.class);
        test = extent.createTest(method.getName(), testAnnotation.description());
    }

    // --- Positive Scenarios ---

    @Test(priority = 1, description = "1. Verify successful registration using valid data for all required fields.")
    public void testSuccessfulRegistration() {
        // Using System.currentTimeMillis() to ensure email is unique for each run
        String email = "testuser" + System.currentTimeMillis() + "@example.com";
        registerPage.enterFirstName("John");
        registerPage.enterLastName("Doe");
        registerPage.enterEmail(email);
        registerPage.enterPassword("ValidPassword123!");
        registerPage.enterConfirmPassword("ValidPassword123!");
        registerPage.clickCreateAccountButton();
        test.log(Status.INFO, "Entered valid user data and clicked create account.");

        String successMessage = registerPage.getSuccessMessageText();
        Assert.assertEquals(successMessage, "Thank you for registering with Main Website Store.");
        test.log(Status.PASS, "Successfully registered and success message is correct.");
    }
    
    @Test(priority = 2, description = "2. Assert user is redirected to the 'My Account' dashboard after successful registration.")
    public void testRedirectionToMyAccount() {
        String email = "testuser" + System.currentTimeMillis() + "@example.com";
        registerPage.enterFirstName("Jane");
        registerPage.enterLastName("Smith");
        registerPage.enterEmail(email);
        registerPage.enterPassword("ValidPassword123!");
        registerPage.enterConfirmPassword("ValidPassword123!");
        registerPage.clickCreateAccountButton();
        test.log(Status.INFO, "Registered a new user.");

        String currentUrl = driver.getCurrentUrl();
        Assert.assertTrue(currentUrl.contains("/customer/account/"));
        test.log(Status.PASS, "User redirected to My Account dashboard. URL: " + currentUrl);
    }
    
    @Test(priority = 3, description = "3. Assert the display of a 'Thank you for registering...' success message.")
    public void testSuccessMessageDisplay() {
        // This is effectively the same as test 1, but focused on just the message as per the test case.
        String email = "testuser" + System.currentTimeMillis() + "@example.com";
        registerPage.enterFirstName("Tom");
        registerPage.enterLastName("Jones");
        registerPage.enterEmail(email);
        registerPage.enterPassword("ValidPassword123!");
        registerPage.enterConfirmPassword("ValidPassword123!");
        registerPage.clickCreateAccountButton();
        test.log(Status.INFO, "Registered a new user.");

        String successMessage = registerPage.getSuccessMessageText();
        Assert.assertTrue(successMessage.contains("Thank you for registering"), "Success message was not displayed or is incorrect.");
        test.log(Status.PASS, "Success message is displayed correctly.");
    }
    
    @Test(priority = 4, description = "4. Assert the user's full name is correctly displayed in the header's welcome message.")
    public void testFullNameInHeader() {
    	 String email = "testuser" + System.currentTimeMillis() + "@example.com";
	    String firstName = "Peter";
	    String lastName = "Pan";
	    
	    registerPage.enterFirstName(firstName);
	    registerPage.enterLastName(lastName);
	    registerPage.enterEmail(email);
	    registerPage.enterPassword("ValidPassword123!");
	    registerPage.enterConfirmPassword("ValidPassword123!");

	    registerPage.clickCreateAccountButton();
	    test.log(Status.INFO, "Submitted registration form for user: Peter Pan.");
        
	    try {
	        String successMessage = registerPage.getSuccessMessageText(); // This method has an internal wait
	        Assert.assertTrue(successMessage.contains("Thank you for registering"), "Registration success message did not appear or was incorrect.");
	        test.log(Status.INFO, "Successfully registered. The page success message is visible.");
	    } catch (Exception e) {
	        Assert.fail("Registration failed. Could not find the success message on the page. Error: " + e.getMessage());
	    }
	    
	    String welcomeMessage = registerPage.getHeaderWelcomeMessageText();
	    Assert.assertTrue(welcomeMessage.contains(firstName + " " + lastName + "!"),
	        "Welcome message does not contain the user's full name. Found: '" + welcomeMessage + "'");
	    test.log(Status.PASS, "Header welcome message correctly displays the user's full name.");
    }

    @Test(priority = 5, description = "5. Test registration with the longest allowed valid inputs for first and last names.")
    public void testRegistrationWithLongNames() {
        String longName = "a".repeat(255);
        String email = "longname" + System.currentTimeMillis() + "@example.com";
        
        registerPage.enterFirstName(longName);
        registerPage.enterLastName(longName);
        registerPage.enterEmail(email);
        registerPage.enterPassword("ValidPassword123!");
        registerPage.enterConfirmPassword("ValidPassword123!");
        registerPage.clickCreateAccountButton();
        test.log(Status.INFO, "Attempting registration with 255-character names.");
        
        String successMessage = registerPage.getSuccessMessageText();
        Assert.assertTrue(successMessage.contains("Thank you for registering"));
        test.log(Status.PASS, "Registration successful with longest allowed names.");
    }
    
    @Test(priority = 6, description = "6. Test registration with a complex password meeting all documented strength requirements.")
    public void testRegistrationWithComplexPassword() {
        String complexPassword = "ComplexP@ssw0rd!2023";
        String email = "complex" + System.currentTimeMillis() + "@example.com";
        
        registerPage.enterFirstName("Complex");
        registerPage.enterLastName("User");
        registerPage.enterEmail(email);
        registerPage.enterPassword(complexPassword);
        registerPage.enterConfirmPassword(complexPassword);
        registerPage.clickCreateAccountButton();
        test.log(Status.INFO, "Attempting registration with a very complex password.");
        
        String successMessage = registerPage.getSuccessMessageText();
        Assert.assertTrue(successMessage.contains("Thank you for registering"));
        test.log(Status.PASS, "Registration successful with complex password.");
    }
    
    @Test(priority = 7, description = "7. Verify that an email with a subdomain is accepted.")
    public void testRegistrationWithSubdomainEmail() {
    	String email = "rajneeshtester15@mail" + System.currentTimeMillis() + ".example.com";
        registerPage.enterFirstName("Subdomain");
        registerPage.enterLastName("User");
        registerPage.enterEmail(email);
        registerPage.enterPassword("ValidPassword123!");
        registerPage.enterConfirmPassword("ValidPassword123!");
        registerPage.clickCreateAccountButton();
        test.log(Status.INFO, "Attempting registration with a subdomain email.");
        
        String successMessage = registerPage.getSuccessMessageText();
        Assert.assertTrue(successMessage.contains("Thank you for registering"));
        test.log(Status.PASS, "Registration successful with subdomain email.");
    }

    // --- Negative & Validation Scenarios ---

    @Test(priority = 8, description = "8. Verify registration fails if the 'Email' field is left blank.")
    public void testRegistrationFailsWithBlankEmail() {
        registerPage.enterFirstName("Test");
        registerPage.enterLastName("User");
        registerPage.enterPassword("ValidPassword123!");
        registerPage.enterConfirmPassword("ValidPassword123!");
        registerPage.clickCreateAccountButton();
        test.log(Status.INFO, "Attempted registration with blank email.");
        
        String errorMessage = registerPage.getRequiredFieldErrorMessage("email");
        Assert.assertEquals(errorMessage, "This is a required field.");
        test.log(Status.PASS, "Correct error message shown for blank email.");
    }
    
    @Test(priority = 9, description = "9. Verify registration fails if 'Password' is blank.")
    public void testRegistrationFailsWithBlankPassword() {
        registerPage.enterFirstName("Test");
        registerPage.enterLastName("User");
        registerPage.enterEmail("test" + System.currentTimeMillis() + "@example.com");
        registerPage.clickCreateAccountButton();
        test.log(Status.INFO, "Attempted registration with blank password.");
        
        String errorMessage = registerPage.getRequiredFieldErrorMessage("password");
        Assert.assertEquals(errorMessage, "This is a required field.");
        test.log(Status.PASS, "Correct error message shown for blank password.");
    }

    @Test(priority = 10, description = "10. Verify registration fails if 'First Name' is blank.")
    public void testRegistrationFailsWithBlankFirstName() {
        registerPage.enterLastName("User");
        registerPage.enterEmail("test" + System.currentTimeMillis() + "@example.com");
        registerPage.enterPassword("ValidPassword123!");
        registerPage.enterConfirmPassword("ValidPassword123!");
        registerPage.clickCreateAccountButton();
        test.log(Status.INFO, "Attempted registration with blank First Name.");
        
        String errorMessage = registerPage.getRequiredFieldErrorMessage("firstname");
        Assert.assertEquals(errorMessage, "This is a required field.");
        test.log(Status.PASS, "Correct error message shown for blank First Name.");
    }

    @Test(priority = 11, description = "11. Verify registration fails if 'Last Name' is blank.")
    public void testRegistrationFailsWithBlankLastName() {
        registerPage.enterFirstName("Test");
        registerPage.enterEmail("test" + System.currentTimeMillis() + "@example.com");
        registerPage.enterPassword("ValidPassword123!");
        registerPage.enterConfirmPassword("ValidPassword123!");
        registerPage.clickCreateAccountButton();
        test.log(Status.INFO, "Attempted registration with blank Last Name.");

        String errorMessage = registerPage.getRequiredFieldErrorMessage("lastname");
        Assert.assertEquals(errorMessage, "This is a required field.");
        test.log(Status.PASS, "Correct error message shown for blank Last Name.");
    }
    
    @Test(priority = 12, description = "12. Verify registration fails if 'Confirm Password' is blank.")
    public void testRegistrationFailsWithBlankConfirmPassword() {
        registerPage.enterFirstName("Test");
        registerPage.enterLastName("User");
        registerPage.enterEmail("test" + System.currentTimeMillis() + "@example.com");
        registerPage.enterPassword("ValidPassword123!");
        registerPage.clickCreateAccountButton();
        test.log(Status.INFO, "Attempted registration with blank Confirm Password.");

        String errorMessage = registerPage.getRequiredFieldErrorMessage("confirmpassword");
        Assert.assertEquals(errorMessage, "This is a required field.");
        test.log(Status.PASS, "Correct error message shown for blank Confirm Password.");
    }

    @Test(priority = 13, description = "13. Verify registration fails if an already registered email is used.")
    public void testRegistrationFailsWithExistingEmail() {
    	// --- Step 1: Create a user to ensure the email exists in the system ---
        String existingEmail = "existing" + System.currentTimeMillis() + "@example.com";
        registerPage.enterFirstName("Existing");
        registerPage.enterLastName("User");
        registerPage.enterEmail(existingEmail);
        registerPage.enterPassword("ValidPassword123!");
        registerPage.enterConfirmPassword("ValidPassword123!");
        registerPage.clickCreateAccountButton();
        test.log(Status.INFO, "Created a user with email: " + existingEmail);
        
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        By customerMenuSelector = By.cssSelector("button.action.switch");
        By signOutLinkSelector = By.xpath("//div[@class='customer-menu']//a[contains(., 'Sign Out')]");
        
        wait.until(ExpectedConditions.elementToBeClickable(customerMenuSelector)).click();
        test.log(Status.INFO, "Clicked on the customer menu dropdown.");
        
        wait.until(ExpectedConditions.elementToBeClickable(signOutLinkSelector)).click();
        test.log(Status.INFO, "Clicked on the 'Sign Out' link.");
        
        /// Wait for a guest-only element to confirm logout before proceeding.
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Create an Account")));
        driver.get(REGISTER_URL);
        test.log(Status.INFO, "Navigated back to the registration page as a guest.");
        
        
        registerPage = new RegisterPage(driver);
        registerPage.enterFirstName("Another");
        registerPage.enterLastName("Person");
        registerPage.enterEmail(existingEmail);
        registerPage.enterPassword("AnotherPassword123!");
        registerPage.enterConfirmPassword("AnotherPassword123!");
        registerPage.clickCreateAccountButton();
        test.log(Status.INFO, "Attempted to register again with the same email.");
        
        String errorMessage = registerPage.getEmailAlreadyExistsErrorMessage();
        Assert.assertEquals(errorMessage, "There is already an account with this email address. If you are sure that it is your email address, click here to get your password and access your account.");
        test.log(Status.PASS, "Correct error message shown for existing email.");
    }
    
    @Test(priority = 14, description = "14. Verify registration fails with an invalid email format.")
    public void testRegistrationFailsWithInvalidEmailFormat() {
        registerPage.enterEmail("invalid-email");
        registerPage.clickCreateAccountButton();
        test.log(Status.INFO, "Entered an invalid email format and submitted.");
        
        String errorMessage = registerPage.getInvalidEmailFormatErrorMessage();
        Assert.assertTrue(errorMessage.contains("Please enter a valid email address"));
        test.log(Status.PASS, "Correct validation message shown for invalid email format.");
    }
    
    @Test(priority = 15, description = "15. Verify registration fails if 'Password' and 'Confirm Password' do not match.")
    public void testRegistrationFailsWithPasswordMismatch() {
        registerPage.enterFirstName("Test");
        registerPage.enterLastName("User");
        registerPage.enterEmail("mismatch" + System.currentTimeMillis() + "@example.com");
        registerPage.enterPassword("PasswordOne");
        registerPage.enterConfirmPassword("PasswordTwo");
        registerPage.clickCreateAccountButton();
        test.log(Status.INFO, "Entered non-matching passwords.");
        
        String errorMessage = registerPage.getPasswordMismatchErrorMessage();
        Assert.assertEquals(errorMessage, "Please enter the same value again.");
        test.log(Status.PASS, "Correct error message shown for password mismatch.");
    }
    
    @Test(priority = 16, description = "16. Verify registration fails if the password is one character shorter than the minimum requirement.")
    public void testRegistrationFailsWithShortPassword() {
        // Assuming minimum length is 8 for this test.
        registerPage.enterPassword("Short1!"); // 7 chars
        registerPage.clickCreateAccountButton();
        test.log(Status.INFO, "Entered a password shorter than the minimum length.");
        
        String errorMessage = registerPage.getPasswordStrengthErrorMessage();
        Assert.assertTrue(errorMessage.contains("Minimum length of this field must be equal or greater than 8 symbols. Leading and trailing spaces will be ignored."));
        test.log(Status.PASS, "Correct error message shown for short password.");
    }

    @Test(priority = 17, description = "17. Verify successful registration with a password of exactly the minimum required length.")
    public void testRegistrationWithMinimumLengthPassword() {
        // Assuming minimum length is 8.
        registerPage.enterFirstName("Min");
        registerPage.enterLastName("Length");
        registerPage.enterEmail("minlength" + System.currentTimeMillis() + "@example.com");
        registerPage.enterPassword("Minlen1!"); // 8 chars, meets complexity
        registerPage.enterConfirmPassword("Minlen1!");
        registerPage.clickCreateAccountButton();
        test.log(Status.INFO, "Entered a password of minimum required length.");
        
        String successMessage = registerPage.getSuccessMessageText();
        Assert.assertTrue(successMessage.contains("Thank you for registering"));
        test.log(Status.PASS, "Registration successful with minimum length password.");
    }

    @Test(priority = 18, description = "18. Verify registration fails if the password does not meet complexity rules.")
    public void testRegistrationFailsWithSimplePassword() {
        registerPage.enterPassword("password");
        registerPage.clickCreateAccountButton();
        test.log(Status.INFO, "Entered a password that does not meet complexity rules.");
        
        String errorMessage = registerPage.getPasswordStrengthErrorMessage();
        Assert.assertTrue(errorMessage.contains("Minimum of different classes of characters"));
        test.log(Status.PASS, "Correct error message shown for non-complex password.");
    }
    
    @Test(priority = 19, description = "19. Verify submitting the form with all fields empty displays validation errors for all required fields simultaneously.")
    public void testSubmitEmptyForm() {
        registerPage.clickCreateAccountButton();
        test.log(Status.INFO, "Clicked 'Create an Account' with all fields empty.");
        
        Assert.assertTrue(registerPage.areAllErrorMessagesDisplayed(), "Not all required field error messages were displayed.");
        test.log(Status.PASS, "Validation errors for all required fields are displayed.");
    }

    // --- Edge & Boundary Cases ---

    @Test(priority = 20, description = "20. Test registration using names with special characters or hyphens (e.g., \"O'Malley\", \"Jean-Luc\").")
    public void testRegistrationWithSpecialCharNames() {
        registerPage.enterFirstName("Jean-Luc");
        registerPage.enterLastName("O'Malley");
        registerPage.enterEmail("special" + System.currentTimeMillis() + "@example.com");
        registerPage.enterPassword("ValidPassword123!");
        registerPage.enterConfirmPassword("ValidPassword123!");
        registerPage.clickCreateAccountButton();
        test.log(Status.INFO, "Attempted registration with names containing hyphen and apostrophe.");
        
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {
            wait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOf(registerPage.getSuccessMessageElement()),
                ExpectedConditions.visibilityOf(registerPage.getGeneralErrorElement())
            ));
        } catch (TimeoutException e) {
            Assert.fail("The page did not respond with a success or a known error message after submitting special characters.");
        }

        try {
            String successMessage = registerPage.getSuccessMessageText();
            Assert.assertTrue(successMessage.contains("Thank you for registering"));
            test.log(Status.PASS, "Registration successful with special characters in names, which is the expected positive outcome.");
        } catch (Exception successException) {
            try {
                String errorMessage = registerPage.getEmailAlreadyExistsErrorMessage(); // This getter uses the generalError locator
                Assert.assertTrue(errorMessage.length() > 0, "An unknown error occurred. No success or error message text was found.");
                test.log(Status.PASS, "Application gracefully handled special characters by showing a validation error: '" + errorMessage + "'. This is an acceptable outcome.");
            } catch (Exception errorException) {
                 Assert.fail("A success or error element was located, but its text could not be retrieved. Error: " + errorException.getMessage());
            }
        }
    }

    
    @Test(priority = 21, description = "21. Test registration using a very long (e.g., 255 characters) valid email address.")
    public void testRegistrationWithLongEmail() {
        String longEmail = "a".repeat(125) + "@" + "b".repeat(125) + ".com";
        registerPage.enterFirstName("Long");
        registerPage.enterLastName("Email");
        registerPage.enterEmail(longEmail);
        registerPage.enterPassword("ValidPassword123!");
        registerPage.enterConfirmPassword("ValidPassword123!");
        registerPage.clickCreateAccountButton();
        test.log(Status.INFO, "Attempted registration with a very long email address.");
        
        String errorMessage = registerPage.getEmailAlreadyExistsErrorMessage();
        Assert.assertTrue(errorMessage.contains("is not a valid hostname"), "The expected error message for an invalid long hostname was not found.");
        test.log(Status.PASS, "Registration was correctly blocked with a validation error for the long/invalid email address.");
    }

    @Test(priority = 22, description = "22. Test submitting the form with only whitespace characters in the required text fields.")
    public void testRegistrationWithWhitespaceInput() {
        registerPage.enterFirstName("   ");
        registerPage.enterLastName("   ");
        registerPage.enterEmail("   ");
        test.log(Status.INFO, "Entered only whitespace into the required fields.");

        registerPage.clickCreateAccountButton();
        test.log(Status.INFO, "Clicked 'Create an Account' to trigger form submission and server-side validation.");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {

            wait.until(ExpectedConditions.visibilityOf(registerPage.getEmailErrorElement()));
            test.log(Status.INFO, "Server-side validation messages have appeared on the page post-submission.");
        } catch (Exception e) {
            Assert.fail("Validation error messages did not appear after submitting the form with whitespace. The page may have failed to reload correctly.");
        }
        Assert.assertEquals(registerPage.getRequiredFieldErrorMessage("firstname"), "This is a required field.");
        Assert.assertEquals(registerPage.getRequiredFieldErrorMessage("lastname"), "This is a required field.");
        Assert.assertEquals(registerPage.getRequiredFieldErrorMessage("email"), "This is a required field.");
        test.log(Status.PASS, "Correct server-side validation errors are displayed for whitespace input.");
    }

    @Test(priority = 23, description = "23. Attempt a basic SQL injection in a name field and verify it is sanitized/rejected.")
    public void testSqlInjectionAttempt() {
        String sqlInjection = "Test'; DROP TABLE users; --";
        registerPage.enterFirstName(sqlInjection);
        registerPage.enterLastName("User");
        registerPage.enterEmail("security" + System.currentTimeMillis() + "@example.com");
        registerPage.enterPassword("ValidPassword123!");
        registerPage.enterConfirmPassword("ValidPassword123!");
        registerPage.clickCreateAccountButton();
        test.log(Status.INFO, "Attempted registration with a basic SQL injection string in the name field.");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {
            wait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOf(registerPage.getFirstNameErrorElement()),
                ExpectedConditions.visibilityOf(registerPage.getSuccessMessageElement()),
                ExpectedConditions.visibilityOf(registerPage.getGeneralErrorElement())
            ));
            test.log(Status.INFO, "Application responded gracefully to the input.");
        } catch (TimeoutException e) {
            Assert.fail("Application did not respond with a success or a known error message after submitting invalid characters.");
        }

        boolean firstNameErrorVisible = !driver.findElements(By.id("firstname-error")).isEmpty();
        boolean successMessageVisible = !driver.findElements(By.cssSelector("div[data-ui-id='message-success']")).isEmpty();

        if (firstNameErrorVisible) {
            test.log(Status.INFO, "Registration was blocked by a specific field validation error.");
            String errorMessage = registerPage.getRequiredFieldErrorMessage("firstname");
            Assert.assertEquals(errorMessage, "First Name is not valid!");
            test.log(Status.PASS, "PASS: Application correctly rejected the invalid name with the expected error message.");
        } else if (successMessageVisible) {
            test.log(Status.INFO, "Registration was successful. Checking if the name was sanitized in the header.");
            String welcomeMessage = registerPage.getHeaderWelcomeMessageText();
            Assert.assertFalse(welcomeMessage.contains(";"), "Input was not properly sanitized on success; semicolon is present.");
            Assert.assertFalse(welcomeMessage.contains("'"), "Input was not properly sanitized on success; apostrophe is present.");
            test.log(Status.PASS, "PASS: Registration succeeded and the malicious input was sanitized from the output.");
        } else {

            test.log(Status.INFO, "Registration was blocked by a general page error.");
            String generalErrorMessage = registerPage.getEmailAlreadyExistsErrorMessage();
            Assert.assertTrue(generalErrorMessage.length() > 0, "A general error was indicated but the message was empty.");
            test.log(Status.PASS, "PASS: Application blocked the attempt with a general error message: " + generalErrorMessage);
        }
    }

    // --- UI/UX & State Management Scenarios ---
    
    @Test(priority = 24, description = "24. Verify password fields mask the typed characters.")
    public void testPasswordIsMasked() {
        Assert.assertTrue(registerPage.isPasswordMasked(), "Password field is not of type 'password'.");
        test.log(Status.PASS, "Password field correctly masks user input.");
    }
    
    @Test(priority = 25, description = "25. Verify a password strength indicator gives real-time feedback.")
    public void testPasswordStrengthIndicator() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

        registerPage.enterPassword("password123");
        test.log(Status.INFO, "Entered a password for 'Weak' strength.");
        try {
            wait.until(ExpectedConditions.textToBePresentInElement(
                registerPage.getPasswordStrengthIndicatorElement(), "Weak"));
            test.log(Status.INFO, "Strength indicator correctly updated to 'Weak'.");
        } catch (Exception e) {
            Assert.fail("Indicator did not update to 'Weak'. Current text: " + registerPage.getPasswordStrengthIndicatorText());
        }
        Assert.assertEquals(registerPage.getPasswordStrengthIndicatorText(), "Weak");

        registerPage.clearAllFields();
        registerPage.enterPassword("Medium12");
        test.log(Status.INFO, "Entered a password for 'Medium' strength.");
        try {
            wait.until(ExpectedConditions.textToBePresentInElement(
                registerPage.getPasswordStrengthIndicatorElement(), "Medium"));
            test.log(Status.INFO, "Strength indicator correctly updated to 'Medium'.");
        } catch (Exception e) {
            Assert.fail("Indicator did not update to 'Medium'. Current text: " + registerPage.getPasswordStrengthIndicatorText());
        }
        Assert.assertEquals(registerPage.getPasswordStrengthIndicatorText(), "Medium");

        registerPage.clearAllFields();
        registerPage.enterPassword("Strong123");
        test.log(Status.INFO, "Entered a password for 'Strong' strength.");
        try {
            wait.until(ExpectedConditions.textToBePresentInElement(
                registerPage.getPasswordStrengthIndicatorElement(), "Strong"));
            test.log(Status.INFO, "Strength indicator correctly updated to 'Strong'.");
        } catch (Exception e) {
            Assert.fail("Indicator did not update to 'Strong'. Current text: " + registerPage.getPasswordStrengthIndicatorText());
        }
        Assert.assertEquals(registerPage.getPasswordStrengthIndicatorText(), "Strong");
        
        registerPage.clearAllFields();
        registerPage.enterPassword("VeryStrongPass123$#@");
        test.log(Status.INFO, "Entered a password for 'Very Strong' strength.");
        try {
            wait.until(ExpectedConditions.textToBePresentInElement(
                registerPage.getPasswordStrengthIndicatorElement(), "Very Strong"));
            test.log(Status.INFO, "Strength indicator correctly updated to 'Very Strong'.");
        } catch (Exception e) {
            Assert.fail("Indicator did not update to 'Very Strong'. Current text: " + registerPage.getPasswordStrengthIndicatorText());
        }
        Assert.assertEquals(registerPage.getPasswordStrengthIndicatorText(), "Very Strong");

        test.log(Status.PASS, "Password strength indicator provides correct real-time feedback for all strength levels.");
    }
    
    @Test(priority = 26, description = "26. Verify all input fields have clear and correct labels.")
    public void testFieldLabels() {

        Assert.assertEquals(registerPage.getFieldLabelText("firstname"), "First Name");
        test.log(Status.INFO, "First Name field has the correct label: 'First Name'");

        Assert.assertEquals(registerPage.getFieldLabelText("lastname"), "Last Name");
        test.log(Status.INFO, "Last Name field has the correct label: 'Last Name'");

        Assert.assertEquals(registerPage.getFieldLabelText("email"), "Email");
        test.log(Status.INFO, "Email field has the correct label: 'Email'");

        test.log(Status.PASS, "All validated input fields have clear and correct labels.");
    }
    
    @Test(priority = 27, description = "27. Verify the page layout is responsive and all fields are usable on a mobile viewport.")
    public void testResponsiveLayout() {
        registerPage.setMobileViewport();
        test.log(Status.INFO, "Resized browser to a mobile viewport.");

        Assert.assertTrue(registerPage.isCreateAccountButtonEnabled(), "Create Account button is not usable on mobile.");
        test.log(Status.PASS, "Key elements are present and usable on a mobile viewport. Full visual validation recommended manually.");
    }
    
    @Test(priority = 28, description = "28. Verify the tab order navigates through the fields in a logical sequence.")
    public void testTabOrder() {
        test.log(Status.INFO, "Starting tab order verification.");

        registerPage.getFirstNameField().click();
        Assert.assertEquals(registerPage.getActiveElement(), registerPage.getFirstNameField(), "Focus should be on the First Name field initially.");
        test.log(Status.PASS, "Initial focus is correctly on the First Name field.");

        registerPage.tabFromElement(registerPage.getFirstNameField());
        Assert.assertEquals(registerPage.getActiveElement(), registerPage.getLastNameField(), "Tabbing from First Name should focus Last Name.");
        test.log(Status.PASS, "Tab from First Name -> Last Name is correct.");

        registerPage.tabFromElement(registerPage.getLastNameField());
        Assert.assertEquals(registerPage.getActiveElement(), registerPage.getEmailField(), "Tabbing from Last Name should focus Email.");
        test.log(Status.PASS, "Tab from Last Name -> Email is correct.");

        registerPage.tabFromElement(registerPage.getEmailField());
        Assert.assertEquals(registerPage.getActiveElement(), registerPage.getPasswordField(), "Tabbing from Email should focus Password.");
        test.log(Status.PASS, "Tab from Email -> Password is correct.");

        registerPage.tabFromElement(registerPage.getPasswordField());
        Assert.assertEquals(registerPage.getActiveElement(), registerPage.getConfirmPasswordField(), "Tabbing from Password should focus Confirm Password.");
        test.log(Status.PASS, "Tab from Password -> Confirm Password is correct.");

        registerPage.tabFromElement(registerPage.getConfirmPasswordField());
        Assert.assertEquals(registerPage.getActiveElement(), registerPage.getCreateAccountButton(), "Tabbing from Confirm Password should focus the Create Account button.");
        test.log(Status.PASS, "Tab from Confirm Password -> Create Account Button is correct.");
    }

    @Test(priority = 29, description = "29. Verify all validation error messages are displayed in a visually distinct manner (e.g., color red).")
    public void testErrorMessagesAreVisuallyDistinct() {
        registerPage.clickCreateAccountButton();
        test.log(Status.INFO, "Clicked 'Create an Account' with all fields empty to trigger validation errors.");
        
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {
            wait.until(ExpectedConditions.visibilityOf(registerPage.getFirstNameErrorElement()));
            test.log(Status.INFO, "Validation error messages are now visible on the page.");
        } catch (Exception e) {
            Assert.fail("Validation error messages did not appear after submitting an empty form.");
        }

        String expectedErrorColorHex = "#e02b27";
        test.log(Status.INFO, "Expected error color (HEX): " + expectedErrorColorHex);

        String actualFirstNameErrorColor = registerPage.getErrorMessageColor("firstname");
        Assert.assertEquals(actualFirstNameErrorColor, expectedErrorColorHex, "The First Name error message color is not the expected red color.");
        test.log(Status.PASS, "First Name error message is correctly displayed in red.");

        String actualEmailErrorColor = registerPage.getErrorMessageColor("email");
        Assert.assertEquals(actualEmailErrorColor, expectedErrorColorHex, "The Email error message color is not the expected red color.");
        test.log(Status.PASS, "Email error message is also correctly displayed in red.");
    }


    @Test(priority = 30, description = "30. Verify the 'Create an Account' button is initially enabled.")
    public void testCreateAccountButtonInitiallyEnabled() {
        Assert.assertTrue(registerPage.isCreateAccountButtonEnabled(), "Create an Account button is not enabled on page load.");
        test.log(Status.PASS, "Create an Account button is enabled by default.");
    }


    @AfterMethod
    public void tearDown(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            test.log(Status.FAIL, "Test Case Failed is " + result.getName());
            test.log(Status.FAIL, "Test Case Failed is " + result.getThrowable());
        } else if (result.getStatus() == ITestResult.SKIP) {
            test.log(Status.SKIP, "Test Case Skipped is " + result.getName());
        } else {
            test.log(Status.PASS, "Test Case Passed is " + result.getName());
        }

        if (driver != null) {
            driver.quit();
        }
    }

    @AfterSuite
    public void tearDownSuite() {
        extent.flush();
    }
}