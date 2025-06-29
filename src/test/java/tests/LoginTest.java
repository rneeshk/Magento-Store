package tests;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.*;
import pages.LoginPage;
import pages.ProductPage;
import pages.RegisterPage;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Set;

public class LoginTest {

    private WebDriver driver;
    private LoginPage loginPage;
    private ProductPage productPage;
    private static ExtentReports extent;
    private ExtentTest test;

    // --- Constants ---
    private final String BASE_URL = "https://magento.softwaretestingboard.com/";
    private final String LOGIN_URL = BASE_URL + "customer/account/login/";
    private final String REGISTER_URL = BASE_URL + "customer/account/create/";
    private final String FORGOT_PASSWORD_URL = BASE_URL + "customer/account/forgotpassword/";
    private final String ACCOUNT_DASHBOARD_URL = BASE_URL + "customer/account/";

    @BeforeSuite
    public void setupSuite() throws UnknownHostException {
        extent = new ExtentReports();
        ExtentSparkReporter spark = new ExtentSparkReporter("test-output/LoginReport.html");
        extent.attachReporter(spark);
        extent.setSystemInfo("Host Name", InetAddress.getLocalHost().getHostName());
        extent.setSystemInfo("Environment", "QA");
        extent.setSystemInfo("User Name", System.getProperty("user.name"));
    }

    @BeforeMethod
    public void setup(Method method) {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new"); // For CI/CD
        driver = new ChromeDriver(options);
        
//        driver = new ChromeDriver();
        
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        
        driver.get(LOGIN_URL);
        loginPage = new LoginPage(driver);
        productPage = new ProductPage(driver);

        
        Test testAnnotation = method.getAnnotation(Test.class);
        test = extent.createTest(method.getName(), testAnnotation.description());
    }
    
    private void createNewUser(String firstname, String lastname, String email, String password) {
        driver.get(REGISTER_URL);
        RegisterPage registerPage = new RegisterPage(driver);
        // Using robust methods from the RegisterPage object
        registerPage.enterFirstName(firstname);
        registerPage.enterLastName(lastname);
        registerPage.enterEmail(email);
        registerPage.enterPassword(password);
        registerPage.enterConfirmPassword(password);
        registerPage.clickCreateAccountButton();
        test.log(Status.INFO, "Precondition: Submitted registration form for user: " + email);
        
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        wait.until(ExpectedConditions.urlContains(ACCOUNT_DASHBOARD_URL));
        
        // This is the key change: We REMOVE the unreliable invisibility wait.
        // The robust signOut method will handle any overlays.
        test.log(Status.INFO, "Precondition: User on dashboard, preparing to sign out.");
        loginPage.signOut();
        
        wait.until(ExpectedConditions.urlContains("logoutSuccess"));
        test.log(Status.INFO, "Precondition: Signed out successfully.");
        
        driver.get(LOGIN_URL); 
        test.log(Status.INFO, "Precondition: Navigated back to the login page, ready for test.");
    }

    // --- Positive Scenarios ---

    @Test(priority = 1, description = "1. Verify successful login with a valid registered email and password.")
    public void testSuccessfulLogin() {
        String email = "logintest" + System.currentTimeMillis() + "@example.com";
        String password = "Password123!";
        createNewUser("Test", "User", email, password);
        loginPage.login(email, password);
        test.log(Status.INFO, "Attempting login with valid credentials.");
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.urlContains(ACCOUNT_DASHBOARD_URL));
        Assert.assertTrue(driver.getCurrentUrl().contains(ACCOUNT_DASHBOARD_URL));
        test.log(Status.PASS, "Login was successful and user was redirected to the account dashboard.");
    }

    @Test(priority = 2, description = "2. Assert redirection to the 'My Account' dashboard post-login.")
    public void testRedirectionToMyAccount() {
        testSuccessfulLogin();
    }

    @Test(priority = 3, description = "3. Assert the header updates to show a welcome message and account links.")
    public void testHeaderUpdatesOnLogin() {
        String email = "header" + System.currentTimeMillis() + "@example.com";
        String password = "Password123!";
        createNewUser("Welcome", "Tester", email, password);
        loginPage.login(email, password);
        test.log(Status.INFO, "Logged in to check header changes.");
        Assert.assertTrue(loginPage.isWelcomeMessageVisibleInHeader(), "Welcome message is not visible in the header.");
        Assert.assertTrue(loginPage.getHeaderWelcomeMessageText().contains("Welcome Tester"), "Welcome message does not contain the correct user name.");
        test.log(Status.PASS, "Header correctly updated with a welcome message for the logged-in user.");
    }
    
    @Test(priority = 4, description = "4. Verify login is successful using a case-insensitive email address.")
    public void testLoginWithCaseInsensitiveEmail() {
        String email = "case" + System.currentTimeMillis() + "@example.com";
        String password = "Password123!";
        createNewUser("Case", "Sensitive", email, password);
        loginPage.login(email.toUpperCase(), password);
        test.log(Status.INFO, "Attempting login with an uppercase version of the registered email.");
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.urlContains(ACCOUNT_DASHBOARD_URL));
        Assert.assertTrue(loginPage.isWelcomeMessageVisibleInHeader(), "Login failed with case-insensitive email.");
        test.log(Status.PASS, "Login was successful using a case-insensitive email address.");
    }

    @Test(priority = 5, description = "5. Verify if a guest adds items to the cart, the cart contents persist after logging in.")
    public void testCartPersistenceAfterLogin() {
        // ARRANGE
        String email = "cartuser" + System.currentTimeMillis() + "@example.com";
        String password = "Password123!";
        createNewUser("Cart", "User", email, password);

        driver.get(BASE_URL + "radiant-tee.html");
        test.log(Status.INFO, "Navigated to product page as a GUEST.");

        productPage.addRadiantTeeToCart();
        test.log(Status.INFO, "Added product to cart as a GUEST.");

        Assert.assertEquals(productPage.getCartCounterNumber(), "1", "Cart count was not '1' for the guest user.");
        test.log(Status.INFO, "Confirmed guest cart has 1 item.");

        driver.get(LOGIN_URL);
        loginPage.login(email, password);
        new WebDriverWait(driver, Duration.ofSeconds(15)).until(ExpectedConditions.urlContains(ACCOUNT_DASHBOARD_URL));
        test.log(Status.INFO, "Logged in to merge the guest cart.");

        Assert.assertEquals(productPage.getCartCounterNumber(), "1", "Cart contents did not persist after login.");
        test.log(Status.PASS, "Cart contents correctly persisted after the user logged in.");
    }


    @Test(priority = 6, description = "6. Verify successful login from the dedicated login page.")
    public void testLoginFromDedicatedPage() {
//        testSuccessfulLogin();
    	loginPage.login("rajneeshtest15@example.com", "rajneeshtester@15");
    	new WebDriverWait(driver, Duration.ofSeconds(15)).until(ExpectedConditions.urlContains(ACCOUNT_DASHBOARD_URL));
        test.log(Status.INFO, "Logged in successful from the dedicated page.");
        
    }
    
   
    
    @Test(priority = 7, description = "7. Verify login fails with a valid email but an incorrect password.")
    public void testLoginFailsWithIncorrectPassword() {
        loginPage.login("test@example.com", "ThisIsAWrongPassword");
        test.log(Status.INFO, "Attempted login with a wrong password.");
        Assert.assertTrue(loginPage.getGeneralErrorMessageText().contains("The account sign-in was incorrect"));
        test.log(Status.PASS, "Correct error message displayed.");
    }

    @Test(priority = 8, description = "8. Verify login fails with an unregistered email address.")
    public void testLoginFailsWithUnregisteredEmail() {
        loginPage.login("nosuchuser" + System.currentTimeMillis() + "@example.com", "anypassword");
        test.log(Status.INFO, "Attempted login with an unregistered email.");
        Assert.assertTrue(loginPage.getGeneralErrorMessageText().contains("The account sign-in was incorrect"));
        test.log(Status.PASS, "Correct error message displayed.");
    }
    
    @Test(priority = 9, description = "9. Verify a generic error message is shown for any incorrect credential combination.")
    public void testGenericErrorMessage() {
        loginPage.login("test@example.com", "WrongPassword!");
        String errorMsgWrongPass = loginPage.getGeneralErrorMessageText();
        driver.get(LOGIN_URL);
        loginPage.login("wrong" + System.currentTimeMillis() + "@example.com", "anypassword");
        String errorMsgWrongEmail = loginPage.getGeneralErrorMessageText();
        Assert.assertEquals(errorMsgWrongPass, errorMsgWrongEmail);
        test.log(Status.PASS, "A generic, identical error message was shown for both failure types.");
    }

    @Test(priority = 10, description = "10. Verify login fails if the 'Email' field is left blank.")
    public void testLoginFailsWithBlankEmail() {
        loginPage.enterPassword("anypassword");
        loginPage.clickSignInButton();
        Assert.assertEquals(loginPage.getEmailRequiredErrorText(), "This is a required field.");
        test.log(Status.PASS, "Correct 'required field' validation message shown.");
    }
    
    @Test(priority = 11, description = "11. Verify login fails if the 'Password' field is left blank.")
    public void testLoginFailsWithBlankPassword() {
        loginPage.enterEmail("test@example.com");
        loginPage.clickSignInButton();
        // Since password field is required, safeSendKeys won't work, so a direct find is ok
        Assert.assertEquals(driver.findElement(By.id("pass-error")).getText(), "This is a required field.");
        test.log(Status.PASS, "Correct 'required field' validation message shown.");
    }

    @Test(priority = 12, description = "12. Verify submitting the form with both fields blank shows required field validation messages.")
    public void testLoginFailsWithAllFieldsBlank() {
        loginPage.clickSignInButton();
        Assert.assertTrue(loginPage.areBothRequiredErrorMessagesDisplayed());
        test.log(Status.PASS, "Validation messages for both required fields are displayed.");
    }

    @Test(priority = 13, description = "13. Test for account lockout after a configured number of failed attempts.")
    public void testAccountLockout() {
        String email = "lockout" + System.currentTimeMillis() + "@example.com";
        createNewUser("Lockout", "User", email, "Password123!");
        String lastErrorMessage = "";
        for (int i = 1; i <= 6; i++) {
            loginPage.login(email, "wrongpassword" + i);
            lastErrorMessage = loginPage.getGeneralErrorMessageText();
            if (lastErrorMessage.contains("your account is locked")) break;
            driver.get(LOGIN_URL);
        }
        Assert.assertTrue(lastErrorMessage.contains("your account is locked") || lastErrorMessage.contains("Please wait"), "Account did not lock as expected.");
        test.log(Status.PASS, "After multiple failed attempts, the error message indicates a lockout.");
    }

    @Test(priority = 14, description = "14. Verify that leading/trailing spaces in the email input are trimmed and login succeeds.")
    public void testLoginWithSpacesInEmail() {
        String email = "spaces" + System.currentTimeMillis() + "@example.com";
        createNewUser("Spaces", "Email", email, "Password123!");
        loginPage.login("  " + email + "  ", "Password123!");
        Assert.assertTrue(loginPage.isWelcomeMessageVisibleInHeader(), "Login failed when email had extra spaces.");
        test.log(Status.PASS, "Login succeeded, indicating spaces in email input were trimmed.");
    }
    
    @Test(priority = 15, description = "15. Verify that leading/trailing spaces in the password input are not trimmed and login fails.")
    public void testLoginFailsWithSpacesInPassword() {
        String email = "spacespass" + System.currentTimeMillis() + "@example.com";
        createNewUser("Spaces", "Password", email, "Password123!");
        loginPage.login(email, "  Password123!  ");
        Assert.assertTrue(loginPage.getGeneralErrorMessageText().contains("The account sign-in was incorrect"));
        test.log(Status.PASS, "Login failed correctly, indicating spaces in password were not trimmed.");
    }

    @Test(priority = 16, description = "16. Attempt a basic SQL injection and verify login failure.")
    public void testSqlInjectionAttempt() {
        loginPage.login("' OR '1'='1", "' OR '1'='1");
        Assert.assertTrue(loginPage.getEmailError().contains("Please enter a valid email address"));
        test.log(Status.PASS, "Application correctly blocked the SQL injection attempt.");
    }
    
    @Test(priority = 17, description = "17. Verify the 'Forgot Your Password?' link is present and navigates correctly.")
    public void testForgotPasswordLink() {
        loginPage.clickForgotPasswordLink();
        Assert.assertEquals(driver.getCurrentUrl(), FORGOT_PASSWORD_URL);
        test.log(Status.PASS, "Successfully navigated to the Forgot Password page.");
    }
    
    @Test(priority = 18, description = "18. Verify the 'Create an Account' button is present and navigates correctly.")
    public void testCreateAccountButtonNavigation() {
        loginPage.clickCreateAccountButton();
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.urlToBe(REGISTER_URL));
        Assert.assertEquals(driver.getCurrentUrl(), REGISTER_URL);
        test.log(Status.PASS, "Successfully navigated to the registration page.");
    }
    
    @Test(priority = 19, description = "19. Verify the password field masks user input.")
    public void testPasswordIsMasked() {
        Assert.assertTrue(loginPage.isPasswordMasked(), "Password field is not of type 'password'.");
        test.log(Status.PASS, "Password field correctly masks user input.");
    }
    
    @Test(priority = 20, description = "20. Verify a session cookie is created upon successful login.")
    public void testSessionCookieCreation() {
        String email = "cookie" + System.currentTimeMillis() + "@example.com";
        createNewUser("Cookie", "Monster", email, "Password123!");
        loginPage.login(email, "Password123!");
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.urlContains(ACCOUNT_DASHBOARD_URL));
        Set<Cookie> cookies = driver.manage().getCookies();
        Assert.assertTrue(cookies.stream().anyMatch(c -> c.getName().equals("PHPSESSID")));
        test.log(Status.PASS, "A session cookie (PHPSESSID) was successfully created.");
    }

    @Test(priority = 21, description = "21. Verify navigating back to the login page redirects to the account dashboard.")
    public void testRedirectFromLoginWhenLoggedIn() {
        String email = "redirect" + System.currentTimeMillis() + "@example.com";
        createNewUser("Redirect", "User", email, "Password123!");
        loginPage.login(email, "Password123!");
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.urlContains(ACCOUNT_DASHBOARD_URL));
        driver.get(LOGIN_URL);
        Assert.assertEquals(driver.getCurrentUrl(), ACCOUNT_DASHBOARD_URL, "User was not redirected.");
        test.log(Status.PASS, "Logged-in user was correctly redirected to their dashboard.");
    }
    
    @Test(priority = 22, description = "22. Verify the 'Sign In' link is replaced by account-specific links.")
    public void testHeaderLinksChangeAfterLogin() {
        Assert.assertTrue(loginPage.isSignInLinkVisibleInHeader());
        String email = "linkchange" + System.currentTimeMillis() + "@example.com";
        createNewUser("Link", "Change", email, "Password123!");
        loginPage.login(email, "Password123!");
        Assert.assertTrue(loginPage.isWelcomeMessageVisibleInHeader());
        Assert.assertFalse(loginPage.isSignInLinkVisibleInHeader());
        test.log(Status.PASS, "Header links correctly updated after login.");
    }
    
    @Test(priority = 23, description = "23. Verify the page is responsive on mobile viewports.")
    public void testResponsiveLayoutOnMobile() {
        loginPage.setMobileViewport(driver);
        Assert.assertTrue(loginPage.areLoginFieldsVisibleOnMobile());
        test.log(Status.PASS, "Login form elements are visible on a mobile viewport.");
    }
    
    @Test(priority = 24, description = "24. Verify hitting the 'Enter' key in the password field submits the form.")
    public void testEnterKeySubmitsForm() {
        String email = "enterkey" + System.currentTimeMillis() + "@example.com";
        createNewUser("Enter", "Key", email, "Password123!");
        loginPage.enterEmail(email);
        loginPage.enterPassword("Password123!");
        loginPage.submitFormFromPassword();
        Assert.assertTrue(loginPage.isWelcomeMessageVisibleInHeader());
        test.log(Status.PASS, "Pressing the Enter key successfully submitted the form.");
    }

    @AfterMethod
    public void tearDown(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            test.log(Status.FAIL, "Test Case Failed: " + result.getName());
            test.log(Status.FAIL, result.getThrowable());
        } else if (result.getStatus() == ITestResult.SKIP) {
            test.log(Status.SKIP, "Test Case Skipped: " + result.getName());
        } else {
            test.log(Status.PASS, "Test Case Passed: " + result.getName());
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