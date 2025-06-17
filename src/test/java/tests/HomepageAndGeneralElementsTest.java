package tests;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

import io.github.bonigarcia.wdm.WebDriverManager;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.*;
import pages.HomepageAndGeneralElements;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import pages.UserAccountManagement;

public class HomepageAndGeneralElementsTest {

	private WebDriver driver;
	private WebDriverWait wait;
    private HomepageAndGeneralElements homePage;
    private UserAccountManagement userAccountPage;
    private static ExtentReports extent;
    private ExtentTest logger;
    private static ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();
    
    

    @BeforeSuite
    public void setupSuite() {
    	extent = new ExtentReports();
        ExtentSparkReporter spark = new ExtentSparkReporter("test-output/HomepageAndGeneralElementsReport.html");
        extent = new ExtentReports();
        extent.attachReporter(spark);

        extent.setSystemInfo("Host Name", "Magento Test Host");
        extent.setSystemInfo("Environment", "QA");
        extent.setSystemInfo("User Name", "Test User");
    }

    @BeforeMethod
    public void setupMethod(Method method) {
    	WebDriverManager.chromedriver().driverVersion("137.0.7151.104").setup();
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        driver.manage().window().maximize();
        driver.get("https://magento.softwaretestingboard.com/");
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        homePage = new HomepageAndGeneralElements(driver);
        userAccountPage = new UserAccountManagement(driver);
        logger = extent.createTest(method.getName());
        ExtentTest test = extent.createTest(method.getName(), method.getAnnotation(Test.class).description());
        extentTest.set(test);
    }

    @AfterMethod
    public void tearDownMethod(ITestResult result) {
    	ExtentTest logger = extentTest.get();
        if (result.getStatus() == ITestResult.FAILURE) {
            logger.log(Status.FAIL, "Test Case Failed: " + result.getName());
            logger.log(Status.FAIL, "Reason: " + result.getThrowable());
        } else if (result.getStatus() == ITestResult.SKIP) {
            logger.log(Status.SKIP, "Test Case Skipped: " + result.getName());
        } else {
            logger.log(Status.PASS, "Test Case Passed: " + result.getName());
        }

        if (driver != null) {
            driver.quit();
        }
    }

    @AfterSuite
    public void tearDownSuite() {
        if (extent != null) {
            extent.flush();
        }
    }

    // --- Test Cases ---

    // Header & Navigation Tests
    @Test(priority = 1, description = "(P) Verify logo click navigates to homepage.")
    public void verifyLogoNavigatesToHomepage() {
        logger.info("Starting test: verifyLogoNavigatesToHomepage");
        String originalUrl = driver.getCurrentUrl();
        homePage.clickSignIn();
        logger.info("Navigated to Sign In page to leave the homepage.");
        Assert.assertNotEquals(driver.getCurrentUrl(), originalUrl, "Should be on a different page before clicking logo.");
        
        homePage.clickWebsiteLogo();
        logger.info("Clicked the website logo.");
        Assert.assertEquals(driver.getCurrentUrl(), homePage.getHomePageUrl(), "Clicking logo did not navigate back to the homepage.");
        logger.pass("Successfully navigated back to homepage via logo click.");
    }

    @Test(priority = 2, description = "(P) Verify hover displays sub-menu.")
    public void verifyHoverDisplaysSubMenu() {
        logger.info("Starting test: verifyHoverDisplaysSubMenu");
        homePage.hoverOverMainMenu("Women");
        logger.info("Hovered over 'Women' menu.");
        Assert.assertTrue(homePage.getSubMenuFor(homePage.womenMenu).isDisplayed(), "Sub-menu for 'Women' did not appear on hover.");
        logger.pass("'Women' sub-menu displayed correctly.");
    }

    @Test(priority = 3, description = "(P) Verify main navigation link works.")
    public void verifyMainNavigationLink() {
        logger.info("Starting test: verifyMainNavigationLink");
        homePage.clickMainMenuItem("Men");
        logger.info("Clicked on 'Men' main menu item.");
        Assert.assertTrue(driver.getCurrentUrl().contains("/men.html"), "URL does not contain '/men.html'.");
        Assert.assertEquals(homePage.getCurrentPageTitleText(), "Men", "Page title is not 'Men'.");
        logger.pass("Successfully navigated to the 'Men' category page.");
    }
    
    @Test(priority = 4, description = "(P) Verify sub-menu navigation link works.")
    public void verifySubMenuNavigationLink() {
        ExtentTest logger = extentTest.get(); // Get the logger for the current test
        logger.info("Starting test: verifySubMenuNavigationLink");

        homePage.clickSubMenuItem("Women", "Tops");
        logger.info("Clicked on sub-menu 'Tops' under 'Women'.");

        // Assertion 1: Verify the URL
        // Adding the .html makes the check more specific and robust.
        String currentUrl = driver.getCurrentUrl();
        Assert.assertTrue(currentUrl.contains("women/tops-women.html"), "URL is not for Women's Tops. Current URL: " + currentUrl);
        logger.pass("URL verification passed.");

        // Assertion 2: Verify the page title (This is the corrected line)
        String pageTitle = homePage.getCurrentPageTitleText();
        Assert.assertEquals(pageTitle, "Tops", "Page title mismatch. Expected 'Tops' but found '" + pageTitle + "'.");
        logger.pass("Page Title verification passed.");

        logger.info("Successfully navigated to the 'Women -> Tops' page and verified content.");
    }

    @Test(priority = 5, description = "(P) Verify guest user sees Sign In and Create Account links.")
    public void verifyGuestUserAuthLinks() {
        logger.info("Starting test: verifyGuestUserAuthLinks");
        Assert.assertTrue(homePage.isSignInLinkVisible(), "'Sign In' link is not visible for guest user.");
        logger.info("'Sign In' link is visible.");
        Assert.assertTrue(homePage.isCreateAccountLinkVisible(), "'Create an Account' link is not visible for guest user.");
        logger.info("'Create an Account' link is visible.");
        
        homePage.clickSignIn();
        Assert.assertTrue(driver.getCurrentUrl().contains("/customer/account/login/"), "Did not navigate to Sign In page.");
        logger.info("Navigated to Sign In page successfully.");
        
        driver.navigate().back();
        homePage.clickCreateAccount();
        Assert.assertTrue(driver.getCurrentUrl().contains("/customer/account/create/"), "Did not navigate to Create Account page.");
        logger.info("Navigated to Create Account page successfully.");
        logger.pass("Guest auth links are visible and navigate correctly.");
    }


    // Search Bar Tests
    @Test(priority = 6, description = "(P) Search for a full product name.")
    public void searchForFullProductName() {
        logger.info("Starting test: searchForFullProductName");
        homePage.searchFor("Radiant Tee");
        homePage.submitSearch();
        logger.info("Searched for 'Radiant Tee'.");
        List<WebElement> results = homePage.getSearchResultItems();
        Assert.assertFalse(results.isEmpty(), "Search returned no results for 'Radiant Tee'.");
        Assert.assertTrue(results.get(0).getText().contains("Radiant Tee"), "First result is not 'Radiant Tee'.");
        logger.pass("Relevant results displayed for full product name search.");
    }

    @Test(priority = 7, description = "(P) Search for a generic category.")
    public void searchForGenericCategory() {
        logger.info("Starting test: searchForGenericCategory");
        homePage.searchFor("bag");
        homePage.submitSearch();
        logger.info("Searched for 'bag'.");
        List<WebElement> results = homePage.getSearchResultItems();
        Assert.assertTrue(results.size() > 1, "Search for 'bag' did not return multiple results.");
        logger.pass("Multiple products shown for generic category search.");
    }

    @Test(priority = 8, description = "(P) Search using a partial product name and verify autocomplete.")
    public void searchWithPartialNameAndVerifyAutocomplete() {
        logger.info("Starting test: searchWithPartialNameAndVerifyAutocomplete");
        homePage.searchFor("Radi");
        logger.info("Typed 'Radi' into search bar.");
        Assert.assertTrue(homePage.areAutoCompleteSuggestionsVisible(), "Autocomplete suggestions did not appear for 'Radi'.");
        logger.pass("Autocomplete suggestions appeared for partial search term.");
    }

    @Test(priority = 9, description = "(P) Search using a product SKU.")
    public void searchWithProductSku() {
        logger.info("Starting test: searchWithProductSku");
        String sku = "WS12"; // SKU for Radiant Tee
        homePage.searchFor(sku);
        homePage.submitSearch();
        logger.info("Searched for SKU: " + sku);
        Assert.assertTrue(homePage.getCurrentPageTitleText().contains("WS12"), "Search result page title does not contain 'WS12' for SKU search.");
        logger.pass("Exact product returned when searching by SKU.");
    }
    
    @Test(priority = 10, description = "(N) Search for a product that does not exist.")
    public void searchForNonExistentProduct() {
        logger.info("Starting test: searchForNonExistentProduct");
        homePage.searchFor("abcdefg");
        homePage.submitSearch();
        logger.info("Searched for a non-existent product 'abcdefg'.");
        String expectedMessage = "Your search returned no results.";
        Assert.assertEquals(homePage.getNoResultsMessageText(), expectedMessage, "No results message is incorrect or not displayed.");
        logger.pass("Correct 'no results' message was displayed.");
    }

    @Test(priority = 11, description = "(N) Click search with an empty field.")
    public void searchWithEmptyField() {
        logger.info("Starting test: searchWithEmptyField");
        String initialUrl = driver.getCurrentUrl();
        homePage.clickSearchIconWithEmptyField();
        logger.info("Submitted search with an empty field.");
        // The site reloads the homepage on empty search
        Assert.assertEquals(driver.getCurrentUrl(), initialUrl, "Page did not stay on the homepage after empty search.");
        logger.pass("Empty search was handled gracefully by reloading the page.");
    }

    @Test(priority = 12, description = "(N) Search using only special characters.")
    public void searchWithSpecialCharacters() {
        logger.info("Starting test: searchWithSpecialCharacters");
        homePage.searchFor("!@#$%");
        homePage.submitSearch();
        logger.info("Searched for '!@#$%'.");
        String expectedMessage = "Your search returned no results.";
        Assert.assertEquals(homePage.getNoResultsMessageText(), expectedMessage, "No results message is incorrect for special character search.");
        logger.pass("Search with special characters was handled gracefully.");
    }

    @Test(priority = 13, description = "(E) Search with leading/trailing spaces.")
    public void searchWithLeadingTrailingSpaces() {
        logger.info("Starting test: searchWithLeadingTrailingSpaces");
        homePage.searchFor(" shirt ");
        homePage.submitSearch();
        logger.info("Searched for ' shirt '.");
        Assert.assertTrue(homePage.getCurrentPageTitleText().contains("shirt"), "Search did not trim spaces and return correct results.");
        Assert.assertFalse(homePage.getSearchResultItems().isEmpty(), "Search for ' shirt ' returned no items.");
        logger.pass("Search correctly trimmed spaces and returned results.");
    }

    @Test(priority = 14, description = "(E) Search using mixed case.")
    public void searchWithMixedCase() {
        logger.info("Starting test: searchWithMixedCase");
        homePage.searchFor("rAdIaNt TeE");
        homePage.submitSearch();
        logger.info("Searched for 'rAdIaNt TeE'.");
        List<WebElement> results = homePage.getSearchResultItems();
        Assert.assertFalse(results.isEmpty(), "Search returned no results.");
        Assert.assertTrue(results.get(0).getText().contains("Radiant Tee"), "Mixed case search did not return the correct product.");
        logger.pass("Search is case-insensitive and returned the correct product.");
    }
    
    @Test(priority = 15, description = "[New] Validate searching for a product after login.")
    public void searchAfterLogin() {
        ExtentTest logger = extentTest.get();
        logger.info("Starting test: Search after login");
        
        userAccountPage.navigateToCreateAccountPage();
        String testEmail = userAccountPage.generateUniqueEmail();
        String testPassword = "PasswordForSearch123!";
        logger.info("Creating a temporary user for this test with email: " + testEmail);
        userAccountPage.enterCreateAccountDetails("Search", "User", testEmail, testPassword, testPassword);
        userAccountPage.submitCreateAccountForm();
        
        Assert.assertTrue(driver.getCurrentUrl().contains("/customer/account/"), "Account creation did not redirect to My Account page.");
        logger.info("Account created successfully. Logging out to prepare for login test.");
        userAccountPage.logout();

        wait.until(ExpectedConditions.urlContains("logoutSuccess"));
        logger.info("Logout confirmation page reached.");

        logger.info("Attempting to log in with the new credentials.");
        userAccountPage.navigateToSignInPage();
        userAccountPage.enterLoginDetails(testEmail, testPassword);
        userAccountPage.submitLoginForm();
        
        String welcomeMessage = userAccountPage.getWelcomeMessageText();
        Assert.assertTrue(welcomeMessage.contains("Welcome"), "Login failed after creating the account.");
        logger.info("Logged in successfully.");

        homePage.searchFor("shirt");
        homePage.submitSearch();
        logger.info("Performed search for 'shirt' while logged in.");

        Assert.assertFalse(homePage.getSearchResultItems().isEmpty(), "Search returned no results for a logged-in user.");
        logger.pass("Successfully verified search functionality for an authenticated user.");
    }

    @Test(priority = 16, description = "[New] Validate searching using Advanced Search.")
    public void validateAdvancedSearch() {
        ExtentTest logger = extentTest.get();
        logger.info("Starting test: Advanced Search");
        
        // STEP 1: Navigate to the Advanced Search page.
        homePage.navigateToAdvancedSearchPage();
        logger.info("Navigated to the Advanced Search form page.");
        
        // STEP 2: Fill the form and submit the search.
        homePage.fillAndSubmitAdvancedSearchForm("Overnight Duffle", "24-WB07", "","","0","50");
        logger.info("Filled and submitted the advanced search for 'shirt'.");

        // VERIFICATION: Check the results page.
        Assert.assertTrue(driver.getCurrentUrl().contains("catalogsearch/advanced/result/"), "URL is not for advanced search results.");
        Assert.assertFalse(homePage.getSearchResultItems().isEmpty(), "Advanced search returned no results.");
        
        logger.pass("Advanced search successfully navigated to results page and found items.");
    }

    @Test(priority = 17, description = "[New] Validate List and Grid views on search results.")
    public void validateListViewAndGridView() {
        ExtentTest logger = extentTest.get();
        logger.info("Starting test: List and Grid view switcher");
        homePage.searchFor("shirt");
        homePage.submitSearch();

        Assert.assertTrue(homePage.isGridViewActive(), "Grid view is not active by default.");
        logger.info("Verified Grid view is active by default.");

        homePage.switchToListView();
        logger.info("Switched to List view.");

        Assert.assertFalse(homePage.isGridViewActive(), "Grid view is still active after switching to List view.");
        logger.pass("Successfully verified switching from Grid to List view.");
    }

    @Test(priority = 18, description = "[New] Validate Add to Compare from search results.")
    public void validateAddToCompare() {
        ExtentTest logger = extentTest.get();
        logger.info("Starting test: Add to Compare");
        homePage.searchFor("shirt");
        homePage.submitSearch();
        logger.info("Searched for 'shirt'.");

        String firstProductName = homePage.getSearchResultItems().get(0).getText();
        
        homePage.addFirstProductToCompare();
        logger.info("Clicked 'Add to Compare' on the first product.");

        String expectedMessage = "You added product " + firstProductName + " to the comparison list.";
        String actualMessage = homePage.getSuccessMessageText();

        Assert.assertEquals(actualMessage, expectedMessage, "The 'Add to Compare' success message is incorrect.");
        logger.pass("Successfully verified the 'Add to Compare' functionality and success message.");
    }

    @Test(priority = 19, description = "[New] Validate sorting products on search results page.")
    public void validateSortSearchResults() {
        ExtentTest logger = extentTest.get();
        logger.info("Starting test: Sort Search Results by Price");
        homePage.searchFor("shirt");
        homePage.submitSearch();
        
        homePage.sortByOnSearchResults("Price");
        logger.info("Sorted search results by Price.");

        List<Double> actualPrices = homePage.getProductPrices();

        List<Double> expectedSortedPrices = new java.util.ArrayList<>(actualPrices);
        
        expectedSortedPrices.sort(java.util.Collections.reverseOrder());
        Assert.assertEquals(actualPrices, expectedSortedPrices, "Products on search results page are not sorted correctly by price (high to low).");
        
        logger.pass("Successfully verified products can be sorted by price on the search results page.");
    }

    @Test(priority = 20, description = "[New] Validate 'Show per page' limiter on search results.")
    public void validateShowPerPageLimiter() {
        ExtentTest logger = extentTest.get();
        logger.info("Starting test: Show per page limiter");
        
        homePage.searchFor("tee");
        homePage.submitSearch();

        int initialCount = homePage.getProductCount();
        logger.info("Initial number of products displayed: " + initialCount);
        Assert.assertEquals(initialCount, 12, "Test data issue: Search for 'tee' did not return the default 12 items.");

        homePage.selectShowPerPage("24");
        logger.info("Selected to show '24' products per page.");
        int newCount = homePage.getProductCount();
        logger.info("New number of products displayed: " + newCount);
        
        Assert.assertTrue(newCount > initialCount, 
                "Product count did not increase after changing 'Show per page' limiter. Initial: " + initialCount + ", New: " + newCount);
        
        logger.pass("Successfully verified 'Show per page' limiter functionality.");
    }

    @Test(priority = 21, description = "[New] Validate search bar is displayed on different pages.")
    public void validateSearchBarIsAlwaysPresent() {
        ExtentTest logger = extentTest.get();
        logger.info("Starting test: Search bar presence on multiple pages");

        Assert.assertTrue(homePage.isSearchInputVisible(), "Search bar is not visible on the Homepage.");
        logger.info("Verified search bar is present on the Homepage.");

        homePage.clickSignIn();
        Assert.assertTrue(homePage.isSearchInputVisible(), "Search bar is not visible on the Customer Login page.");
        logger.info("Verified search bar is present on the Customer Login page.");
        
        logger.pass("Search bar is consistently displayed across different pages.");
    }

    @Test(priority = 22, description = "[New] Validate page elements of Search Results page.")
    public void validateSearchResultsPageElements() {
        ExtentTest logger = extentTest.get();
        logger.info("Starting test: Validate page elements of Search Results page");
        homePage.searchFor("shirt");
        homePage.submitSearch();

        Assert.assertEquals(driver.getTitle(), "Search results for: 'shirt'");
        Assert.assertTrue(driver.getCurrentUrl().contains("catalogsearch/result/?q=shirt"));
        Assert.assertEquals(homePage.getCurrentPageTitleText(), "Search results for: 'shirt'");
        logger.pass("Successfully validated the Title, URL, and Page Heading of the search results page.");
    }
    
    // Footer Tests
    @DataProvider(name = "footerLinksData")
    public Object[][] footerLinksProvider() {
        return new Object[][] {
                {"Notes", "/magento-store-notes", "Magento 2 Store(Sandbox site) - Notes - Software Testing Board"},
                {"Practice API Testing using Magento 2", "/practice-api-testing-using-magento-2", "Practice API Testing using Magento 2 - Software Testing Board"},
                {"Write for us", "softwaretestingboard.com/write-for-us", "Write For Us - Software Testing Board"},
                {"Subscribe", "softwaretestingboard.com/subscribe", "Subscribe - Software Testing Board"},
                {"Search Terms", "/search/term/popular/", "Popular Search Terms"},
                {"Privacy and Cookie Policy", "/privacy-policy-cookie-restriction-mode", "Privacy Policy"},
                {"Advanced Search", "/catalogsearch/advanced/", "Advanced Search"},
                {"Orders and Returns", "/sales/guest/form/", "Orders and Returns"}
        };
    }
    
    @Test(priority = 23, description = "(P) Verify all footer links navigate correctly.", dataProvider = "footerLinksData")
    public void verifyAllFooterLinks(String linkText, String expectedUrlPart, String expectedPageTitle) {
        ExtentTest node = extentTest.get().createNode("Verification for link: '" + linkText + "'");

        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight)");
        
        WebElement linkToClick = homePage.getFooterLinks().stream()
                .filter(link -> link.getText().trim().equalsIgnoreCase(linkText))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Link with text '" + linkText + "' not found in the footer."));

        String originalHandle = driver.getWindowHandle();
        
        node.info("Clicking link: '" + linkText + "'");
        linkToClick.click();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Set<String> allHandles = driver.getWindowHandles();
        
        if (allHandles.size() > 1) {
            node.info("New tab detected. Switching focus...");
            for (String handle : allHandles) {
                if (!handle.equalsIgnoreCase(originalHandle)) {
                    driver.switchTo().window(handle);
                    break;
                }
            }
        } else {
            node.info("Link opened in the same tab.");
        }
        
        String currentUrl = driver.getCurrentUrl();
        String currentPageTitle = driver.getTitle();

        Assert.assertTrue(currentUrl.contains(expectedUrlPart), 
            "URL Mismatch for '" + linkText + "'. Expected to contain '" + expectedUrlPart + "' but was '" + currentUrl + "'.");
        node.pass("URL check passed. URL contains '" + expectedUrlPart + "'.");

        Assert.assertEquals(currentPageTitle, expectedPageTitle,
            "Page Title Mismatch for '" + linkText + "'. Expected '" + expectedPageTitle + "' but was '" + currentPageTitle + "'.");
        node.pass("Page Title check passed. Title is '" + expectedPageTitle + "'.");
        
        if (allHandles.size() > 1) {
            node.info("Closing new tab and switching back to original.");
            driver.close();
            driver.switchTo().window(originalHandle);
        }

        extentTest.get().pass("Successfully verified footer link: '" + linkText + "'");
    }
}