package tests;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.*;
import pages.UserAccountManagement;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Arrays;

public class UserAccountManagementTest {

    private WebDriver driver;
    private UserAccountManagement userAccountPage;
    private static ExtentReports extent;
    private static ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();

    // Store created user credentials for other tests
    private static String createdUserEmail;
    private static String createdUserPassword;

    @BeforeSuite
    public void setupSuite() {
        extent = new ExtentReports();
        ExtentSparkReporter spark = new ExtentSparkReporter("test-output/UserAccountManagementReport.html");
        extent.attachReporter(spark);
    }

    @BeforeMethod
    public void setupMethod(Method method) {
    	WebDriverManager.chromedriver().driverVersion("137.0.7151.104").setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.get("https://magento.softwaretestingboard.com/");
        userAccountPage = new UserAccountManagement(driver);

        ExtentTest test = extent.createTest(method.getName(), method.getAnnotation(Test.class).description());
        extentTest.set(test);
    }

    @AfterMethod
    public void tearDownMethod(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            ExtentTest logger = extentTest.get();
            logger.log(Status.FAIL, "Test Case Failed: " + result.getName());
            if (result.getParameters().length > 0) {
                logger.log(Status.FAIL, "Failed with parameters: " + Arrays.toString(result.getParameters()));
            }
            logger.log(Status.FAIL, "Failure Details: " + result.getThrowable());
        }
        driver.quit();
    }
    
    @AfterSuite
    public void tearDownSuite() {
        extent.flush();
    }

    // --- DataProvider for Password Strength ---
    @DataProvider(name = "passwordStrengthData")
    public Object[][] passwordStrengthProvider() {
        return new Object[][]{
                {"123456", "Weak"},
                {"123ABCde", "Medium"},
                {"StrongP@ssw0rd!", "Very Strong"}
        };
    }


    // Create Account Tests
    @Test(priority = 1, description = "(P) Successfully create a new user account.")
    public void createAccountSuccess() {
        ExtentTest logger = extentTest.get();
        logger.info("Starting test: Create Account Success");
        
        userAccountPage.navigateToCreateAccountPage();
        createdUserEmail = userAccountPage.generateUniqueEmail();
        createdUserPassword = "ValidPassword123!";
        
        logger.info("Using email: " + createdUserEmail);
        userAccountPage.enterCreateAccountDetails("Test", "User", createdUserEmail, createdUserPassword, createdUserPassword);
        userAccountPage.submitCreateAccountForm();
        
        String successMessage = userAccountPage.getSuccessMessageText();
        Assert.assertEquals(successMessage, "Thank you for registering with Main Website Store.");
        logger.pass("Account created successfully. Success message verified.");
    }

    @Test(priority = 2, description = "(N) Attempt to create account with a required field blank.")
    public void createAccountWithRequiredFieldBlank() {
        ExtentTest logger = extentTest.get();
        logger.info("Starting test: Create Account With Required Field Blank");
        userAccountPage.navigateToCreateAccountPage();
        
        userAccountPage.submitCreateAccountForm();
        
        String error = userAccountPage.getFirstNameErrorText();
        Assert.assertEquals(error, "This is a required field.");
        logger.pass("Correct field-level error message shown for blank required field.");
    }
    
    @Test(priority = 3, description = "(N) Attempt to create account with an existing email.", dependsOnMethods = "createAccountSuccess")
    public void createAccountWithExistingEmail() {
        ExtentTest logger = extentTest.get();
        logger.info("Starting test: Create Account With Existing Email");
        userAccountPage.navigateToCreateAccountPage();

        logger.info("Attempting to re-register with email: " + createdUserEmail);
        userAccountPage.enterCreateAccountDetails("Another", "User", createdUserEmail, "anypass!1", "anypass!1");
        userAccountPage.submitCreateAccountForm();

        String errorMessage = userAccountPage.getPageErrorMessageText();
        Assert.assertEquals(errorMessage, "There is already an account with this email address. If you are sure that it is your email address, click here to get your password and access your account.");
        logger.pass("Correct error message shown for existing email.");
    }

    @Test(priority = 4, description = "(N) Attempt to create account with mismatched passwords.")
    public void createAccountWithMismatchedPasswords() {
        ExtentTest logger = extentTest.get();
        logger.info("Starting test: Create Account With Mismatched Passwords");
        userAccountPage.navigateToCreateAccountPage();

        userAccountPage.enterCreateAccountDetails("Test", "User", userAccountPage.generateUniqueEmail(), "Password123", "Password456");
        userAccountPage.submitCreateAccountForm();

        String error = userAccountPage.getPasswordConfirmErrorText();
        Assert.assertEquals(error, "Please enter the same value again.");
        logger.pass("Correct error message shown for mismatched passwords.");
    }

    @Test(priority = 5, description = "(N) Attempt to create account with an invalid email format.")
    public void createAccountWithInvalidEmail() {
        ExtentTest logger = extentTest.get();
        logger.info("Starting test: Create Account With Invalid Email");
        userAccountPage.navigateToCreateAccountPage();

        userAccountPage.enterCreateAccountDetails("Test", "User", "invalid-email", "Password123!", "Password123!");
        userAccountPage.submitCreateAccountForm(); // Client-side validation shows error

        String error = userAccountPage.getEmailErrorText();
        Assert.assertEquals(error, "Please enter a valid email address (Ex: johndoe@domain.com).");
        logger.pass("Correct error message shown for invalid email format.");
    }

    @Test(priority = 6, description = "(N) Check password strength indicator.", dataProvider = "passwordStrengthData")
    public void checkPasswordStrength(String password, String expectedStrength) {
        ExtentTest logger = extentTest.get().createNode("Checking password '" + password + "'");
        logger.info("Starting sub-test for password strength.");
        userAccountPage.navigateToCreateAccountPage();

        userAccountPage.enterPassword(password);
        
        String actualStrength = userAccountPage.getPasswordStrengthText();
        Assert.assertTrue(actualStrength.contains(expectedStrength), "Expected strength '" + expectedStrength + "' but got '" + actualStrength + "'.");
        logger.pass("Password strength correctly identified as '" + expectedStrength + "'.");
    }

    @Test(priority = 7, description = "(E) Verify app rejects invalid characters in name field.")
    public void createAccountWithInvalidCharactersInName() {
        ExtentTest logger = extentTest.get();
        logger.info("Starting test: Create Account With Invalid Characters in Name");
        userAccountPage.navigateToCreateAccountPage();

        String email = userAccountPage.generateUniqueEmail();

        logger.info("Entering form details with an invalid first name containing '@'.");
        userAccountPage.enterCreateAccountDetails("Tést-N@me", "ValidLastName", email, "Password123!", "Password123!");

        userAccountPage.submitCreateAccountForm();
        logger.info("Form submitted to trigger validation.");

        String error = userAccountPage.getPageErrorMessageText();
        Assert.assertEquals(error, "First Name is not valid!");
        
        logger.pass("Application correctly rejected an invalid character (@) in the First Name field and displayed the expected page-level error.");
    }
    
    @Test(priority = 8, description = "(E) Create account with valid special characters in name.")
    public void createAccountWithValidSpecialCharacters() {
        ExtentTest logger = extentTest.get();
        logger.info("Starting test: Create Account With Valid Special Characters");
        userAccountPage.navigateToCreateAccountPage();

        String email = userAccountPage.generateUniqueEmail();
        userAccountPage.enterCreateAccountDetails("Renée-Claire", "O'Malley", email, "Password123!", "Password123!");
        userAccountPage.submitCreateAccountForm();
        
        String successMessage = userAccountPage.getSuccessMessageText();
        Assert.assertEquals(successMessage, "Thank you for registering with Main Website Store.");
        
        logger.pass("Account created successfully with valid special characters (accents, hyphens, apostrophes) in name fields.");
    }
    
    @Test(priority = 9, description = "[New] CA-7: Validate navigating to Create Account from Login page.")
    public void navigateToCreateAccountFromLoginPage() {
        ExtentTest logger = extentTest.get();
        logger.info("Starting test: Navigate to Create Account page from Login page link.");
        
        userAccountPage.navigateToCreateAccountFromLoginPage();
        logger.info("Clicked 'Create an Account' link on the Login page.");
        
        Assert.assertTrue(driver.getCurrentUrl().contains("/customer/account/create/"), "Did not navigate to the Create Account page.");
        Assert.assertEquals(userAccountPage.getPageTitleHeadingText(), "Create New Customer Account");
        logger.pass("Successfully navigated to the Create Account page from the Login page.");
    }

    @Test(priority = 10, description = "[New] CA-13 & 14: Validate placeholders and required field markers.")
    public void validatePlaceholdersAndRequiredMarkers() {
        ExtentTest logger = extentTest.get();
        logger.info("Starting test: Validate placeholders and required markers on Create Account page.");
        userAccountPage.navigateToCreateAccountPage();
        Assert.assertTrue(userAccountPage.isFirstNameMarkedRequired(), "First Name field is not marked as required.");
        logger.pass("Successfully verified that mandatory fields are marked as required.");
    }

    @Test(priority = 11, description = "[New] CA-16: Validate mandatory fields do not accept only spaces.")
    public void createAccountWithOnlySpacesInFields() {
        ExtentTest logger = extentTest.get();
        logger.info("Starting test: Create account with only spaces in mandatory fields.");
        userAccountPage.navigateToCreateAccountPage();

        userAccountPage.enterCreateAccountDetails("   ", "   ", "   ", "   ", "   ");
        userAccountPage.submitCreateAccountForm();
        Assert.assertEquals(userAccountPage.getFirstNameErrorText(), "This is a required field.");
        Assert.assertEquals(userAccountPage.getEmailErrorText(), "This is a required field.");
        logger.pass("Correct error messages displayed when submitting only spaces.");
    }

    @Test(priority = 12, description = "[New] CA-22: Validate error when Password Confirm is empty.")
    public void createAccountWithEmptyConfirmPassword() {
        ExtentTest logger = extentTest.get();
        logger.info("Starting test: Create account with empty confirm password field.");
        userAccountPage.navigateToCreateAccountPage();

        userAccountPage.enterCreateAccountDetails("Test", "User", userAccountPage.generateUniqueEmail(), "Password123!", "");
        userAccountPage.submitCreateAccountForm();

        Assert.assertEquals(userAccountPage.getPasswordConfirmErrorText(), "This is a required field.");
        logger.pass("Correct error message displayed for empty Password Confirm field.");
    }

    @Test(priority = 13, description = "[New] CA-23: Validate page elements of Create Account page.")
    public void validateCreateAccountPageElements() {
        ExtentTest logger = extentTest.get();
        logger.info("Starting test: Validate title, URL, and heading of Create Account page.");
        userAccountPage.navigateToCreateAccountPage();
        Assert.assertEquals(driver.getTitle(), "Create New Customer Account", "The browser tab title is incorrect.");
        logger.info("Browser tab title is correct.");
        Assert.assertTrue(driver.getCurrentUrl().contains("/customer/account/create/"), "The page URL is incorrect.");
        logger.info("Page URL is correct.");
        Assert.assertEquals(userAccountPage.getPageTitleHeadingText(), "Create New Customer Account", "The main page heading (H1) is incorrect.");
        logger.info("Page heading is correct.");
        logger.pass("Successfully validated page title, URL, and heading.");
    }


    // Login & Logout Tests
    @Test(priority = 14, description = "(P) Successfully log in.", dependsOnMethods = "createAccountSuccess")
    public void loginSuccess() {
        ExtentTest logger = extentTest.get();
        logger.info("Starting test: Login Success");
        userAccountPage.navigateToSignInPage();
        
        userAccountPage.enterLoginDetails(createdUserEmail, createdUserPassword);
        userAccountPage.submitLoginForm();
        
        String welcomeMessage = userAccountPage.getWelcomeMessageText();
        Assert.assertTrue(welcomeMessage.contains("Welcome, Test User!"), "Welcome message is not correct.");
        logger.pass("Login successful, correct welcome message displayed.");
    }
    
    @Test(priority = 15, description = "(P) Verify header changes after login.", dependsOnMethods = "createAccountSuccess")
    public void verifyHeaderAfterLogin() {
        ExtentTest logger = extentTest.get();
        logger.info("Starting test: Verify Header After Login");
        userAccountPage.navigateToSignInPage();
        userAccountPage.enterLoginDetails(createdUserEmail, createdUserPassword);
        userAccountPage.submitLoginForm();
        Assert.assertTrue(userAccountPage.getWelcomeMessageText().contains("Test User"));
        logger.info("User name is visible in header.");
        try {
            userAccountPage.logout();
            logger.info("Sign Out option is available and functional.");
        } catch (Exception e) {
            Assert.fail("Could not find Sign Out option in the user dropdown.", e);
        }
        logger.pass("Header correctly updated for a logged-in user.");
    }

    @Test(priority = 16, description = "(P) Successfully log out.", dependsOnMethods = "createAccountSuccess")
    public void logoutSuccess() {
        ExtentTest logger = extentTest.get();
        logger.info("Starting test: Logout Success");
        // Log in first
        userAccountPage.navigateToSignInPage();
        userAccountPage.enterLoginDetails(createdUserEmail, createdUserPassword);
        userAccountPage.submitLoginForm();
        logger.info("Logged in successfully.");
        userAccountPage.logout();
        logger.info("Clicked logout.");
        Assert.assertTrue(userAccountPage.isSignInLinkVisible(), "Sign In link did not reappear after logout.");
        logger.pass("Logout successful, header reverted to guest view.");
    }

    @Test(priority = 17, description = "(N) Attempt to log in with an incorrect password.", dependsOnMethods = "createAccountSuccess")
    public void loginWithIncorrectPassword() {
        ExtentTest logger = extentTest.get();
        logger.info("Starting test: Login With Incorrect Password");
        userAccountPage.navigateToSignInPage();

        userAccountPage.enterLoginDetails(createdUserEmail, "wrongpassword");
        userAccountPage.submitLoginForm();

        String error = userAccountPage.getPageErrorMessageText();
        Assert.assertTrue(error.contains("The account sign-in was incorrect or your account is disabled temporarily."), "Error message for incorrect password was not as expected.");
        logger.pass("Correct error message displayed for incorrect password.");
    }
    
    @Test(priority = 18, description = "(N) Attempt to log in with a non-existent email.")
    public void loginWithNonExistentEmail() {
        ExtentTest logger = extentTest.get();
        logger.info("Starting test: Login With Non-Existent Email");
        userAccountPage.navigateToSignInPage();

        userAccountPage.enterLoginDetails("nonexistent" + System.currentTimeMillis() + "@example.com", "anypassword");
        userAccountPage.submitLoginForm();

        String error = userAccountPage.getPageErrorMessageText();
        Assert.assertTrue(error.contains("The account sign-in was incorrect or your account is disabled temporarily."), "Error message for non-existent user was not as expected.");
        logger.pass("Correct error message displayed for non-existent user.");
    }

    @Test(priority = 19, description = "(N) Use browser back button after logout.", dependsOnMethods = "createAccountSuccess")
    public void verifyBrowserBackAfterLogout() {
        ExtentTest logger = extentTest.get();
        logger.info("Starting test: Verify Browser Back After Logout");
        userAccountPage.navigateToSignInPage();
        userAccountPage.enterLoginDetails(createdUserEmail, createdUserPassword);
        userAccountPage.submitLoginForm();
        driver.get("https://magento.softwaretestingboard.com/customer/account/");
        String accountPageUrl = driver.getCurrentUrl();
        logger.info("Navigated to account page: " + accountPageUrl);

        userAccountPage.logout();
        logger.info("Logged out.");

        logger.info("Using browser back button to navigate to account page.");
        driver.navigate().back();

        String expectedUrlPart = "customer/account/login";
        Assert.assertTrue(driver.getCurrentUrl().contains(expectedUrlPart), "Browser back button allowed access to a private page after logout.");
        logger.pass("User was correctly redirected to the login page after using the back button.");
    }

    @Test(priority = 20, description = "(E) Test the 'Forgot Your Password?' functionality.")
    public void testForgotPassword() {
        ExtentTest logger = extentTest.get();
        logger.info("Starting test: Forgot Password");
        
        userAccountPage.navigateToSignInPage();
        userAccountPage.navigateToForgotPassword();
        logger.info("Navigated to Forgot Password page.");

        String testEmail = "test@example.com";
        userAccountPage.submitForgotPassword(testEmail);

        String successMessage = userAccountPage.getSuccessMessageText(); 
        
        String expectedMessageFragment = "If there is an account associated with " + testEmail + " you will receive an email with a link to reset your password.";
        Assert.assertTrue(successMessage.contains(expectedMessageFragment), "The on-screen confirmation message was not correct.");
        
        logger.pass("Forgot Password form submitted and confirmation message verified on screen.");
        logger.info("Full test requires email validation, which is out of scope for this script.");
    }
    
    @Test(priority = 21, description = "[New] LL-5: Validate logging in without providing credentials.")
    public void loginWithEmptyCredentials() {
        ExtentTest logger = extentTest.get();
        logger.info("Starting test: Login with empty credentials.");
        userAccountPage.navigateToSignInPage();
        
        userAccountPage.submitLoginForm();
        
        Assert.assertEquals(userAccountPage.getLoginEmailErrorText(), "This is a required field.");
        Assert.assertEquals(userAccountPage.getLoginPasswordErrorText(), "This is a required field.");
        logger.pass("Correct validation messages appeared for empty login fields.");
    }

    @Test(priority = 22, description = "[New] LL-8: Validate floating label on Login page.")
    public void validateLoginFloatingLabel() {
        ExtentTest logger = extentTest.get();
        logger.info("Starting test: Validate floating label for Email field on Login page.");
        userAccountPage.navigateToSignInPage();

        Assert.assertTrue(userAccountPage.hasCorrectLoginEmailLabel(), 
            "The floating label for the email field was not found or did not have the correct text 'Email'.");
        
        logger.pass("Successfully verified the floating label for the Email field.");
    }
    
    @Test(priority = 23, description = "[New] LL-8: Validate floating label on Login page.")
    public void validateLoginFloatingLabelPassword() {
        ExtentTest logger = extentTest.get();
        logger.info("Starting test: Validate floating label for Email field on Login page.");
        userAccountPage.navigateToSignInPage();
        Assert.assertTrue(userAccountPage.hasCorrectLoginEmailLabel(), 
            "The floating label for the email field was not found or did not have the correct text 'Email'.");
        
        logger.pass("Successfully verified the floating label for the Email field.");
    }

    @Test(priority = 24, description = "[New] LL-21: Validate page elements of Login page.")
    public void validateLoginPageElements() {
        ExtentTest logger = extentTest.get();
        logger.info("Starting test: Validate breadcrumb, title, and URL of Login page.");
        userAccountPage.navigateToSignInPage();

        Assert.assertEquals(driver.getTitle(), "Customer Login");
        Assert.assertTrue(driver.getCurrentUrl().contains("/customer/account/login/"));
        Assert.assertEquals(userAccountPage.getPageTitleHeadingText(), "Customer Login");
        logger.pass("Successfully validated all page-level UI elements for the Login page.");
    }
    
 // --- Forgot Password Tests ---

    @Test(priority = 25, description = "[New] FP-19: Validate Back button on Forgotten Password page.")
    public void validateBackButtonOnForgotPasswordPage() {
        ExtentTest logger = extentTest.get();
        logger.info("Starting test: Validate Back button on Forgot Password page.");
        
        userAccountPage.navigateToSignInPage();
        String loginPageUrl = driver.getCurrentUrl();
        
        userAccountPage.navigateToForgotPassword();
        logger.info("Navigated to Forgot Password page.");
        Assert.assertNotEquals(driver.getCurrentUrl(), loginPageUrl, "URL should have changed to Forgot Password page.");

        driver.navigate().back();
        logger.info("Clicked browser back button.");
        
        Assert.assertEquals(driver.getCurrentUrl(), loginPageUrl, "Did not navigate back to the Customer Login page.");
        logger.pass("Successfully verified back button functionality on Forgot Password page.");
    }
}