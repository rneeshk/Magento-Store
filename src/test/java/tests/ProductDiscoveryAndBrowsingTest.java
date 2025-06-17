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
import pages.ProductDiscoveryAndBrowsing;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

public class ProductDiscoveryAndBrowsingTest {

    private WebDriver driver;
    private ProductDiscoveryAndBrowsing categoryPage;
    private static ExtentReports extent;
    private ExtentTest logger;
    private static ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();

    @BeforeSuite
    public void setupSuite() {
        extent = new ExtentReports();
        ExtentSparkReporter spark = new ExtentSparkReporter("test-output/ProductDiscoveryReport.html");
        extent = new ExtentReports();  // Instantiate ExtentReports AFTER the reporter
        extent.attachReporter(spark);
        
        extent.setSystemInfo("Host Name", "Magento Test Host");
        extent.setSystemInfo("Environment", "QA");
        extent.setSystemInfo("User Name", "Test User");
    }

    @BeforeMethod
    public void setupMethod(Method method) {
    	WebDriverManager.chromedriver().driverVersion("137.0.7151.104").setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.get("https://magento.softwaretestingboard.com/");
        categoryPage = new ProductDiscoveryAndBrowsing(driver);
        logger = extent.createTest(method.getName());
        ExtentTest test = extent.createTest(method.getName(), method.getAnnotation(Test.class).description());
        extentTest.set(test);
    }

    @AfterMethod
    public void tearDownMethod(ITestResult result) {
    	logger = extentTest.get();
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


    @Test(priority = 1, description = "(P) Verify sorting by Price.")
    public void verifySortByPrice() {
        ExtentTest logger = extentTest.get();
        logger.info("Starting test: Verify Sort By Price");
        categoryPage.navigateToMenTopsCategory();
        logger.info("Navigated to Men > Tops category page.");
        categoryPage.sortByOption("Price");
        logger.info("Sorted products by Price.");
        List<Double> actualPrices = categoryPage.getProductPrices();
        List<Double> expectedSortedPrices = new java.util.ArrayList<>(actualPrices);
        Collections.sort(expectedSortedPrices);
        Assert.assertEquals(actualPrices, expectedSortedPrices, "Products are not sorted correctly by price (low to high).");
        logger.pass("Successfully verified that products are sorted by price.");
    }

    @Test(priority = 2, description = "(P) Verify sorting by Product Name.")
    public void verifySortByProductName() {
        ExtentTest logger = extentTest.get();
        logger.info("Starting test: Verify Sort By Product Name");
        categoryPage.navigateToMenTopsCategory();
        logger.info("Navigated to Men > Tops category page.");
        categoryPage.sortByOption("Product Name");
        logger.info("Sorted products by Product Name.");
        List<String> actualNames = categoryPage.getProductNames();
        List<String> expectedSortedNames = new java.util.ArrayList<>(actualNames);
        Collections.sort(expectedSortedNames);
        Assert.assertEquals(actualNames, expectedSortedNames, "Products are not sorted correctly by name (A-Z).");
        logger.pass("Successfully verified that products are sorted by name.");
    }

    @Test(priority = 3, description = "(P) Verify applying a single filter.")
    public void verifySingleFilter() {
        ExtentTest logger = extentTest.get();
        logger.info("Starting test: Verify Single Filter");
        categoryPage.navigateToMenTopsCategory();
        logger.info("Navigated to Men > Tops category page.");
        
        // ACTION: Filter by "Tank"
        categoryPage.applyFilter("Style", "Tank");
        logger.info("Applied filter Style: Tank.");
        
        // Get the list of active filter text elements
        List<String> activeFilters = categoryPage.getActiveFilterLabels();
        
        // Assert that exactly ONE filter is active. This is the most important first check.
        Assert.assertEquals(activeFilters.size(), 1, "Expected exactly one active filter, but found " + activeFilters.size());
        logger.info("Verified that exactly one filter is active.");

        // Get the text of the single active filter.
        String filterText = activeFilters.get(0);
        
        // Assert that the text contains the expected category AND value.
        // We use toUpperCase() to make the check case-insensitive and more robust.
        Assert.assertTrue(filterText.toUpperCase().contains("STYLE"), "The active filter text did not contain the category 'STYLE'. Full text: " + filterText);
        Assert.assertTrue(filterText.toUpperCase().contains("TANK"), "The active filter text did not contain the value 'TANK'. Full text: " + filterText);
        logger.info("Verified filter text contains 'STYLE' and 'TANK'.");
        
        // Verify the product list as before
        List<String> productNames = categoryPage.getProductNames();
        Assert.assertTrue(productNames.stream().allMatch(name -> name.toLowerCase().contains("tank")), 
                "Not all displayed products are of the style 'Tank'.");
        logger.pass("Successfully verified single filter application for 'Tank'.");
    }

    @Test(priority = 4, description = "(P) Verify applying multiple filters.")
    public void verifyMultipleFilters() {
        ExtentTest logger = extentTest.get();
        logger.info("Starting test: Verify Multiple Filters");
        
        categoryPage.navigateToMenTopsCategory();
        logger.info("Navigated to Men > Tops page.");

        categoryPage.applyFilter("Style", "Tee");
        logger.info("Applied explicit filter Style: Tee.");
        
        categoryPage.applyFilter("Material", "Cotton");
        logger.info("Applied explicit filter Material: Cotton.");


        List<String> activeFilters = categoryPage.getActiveFilterLabels();
        
        Assert.assertEquals(activeFilters.size(), 2, 
            "Expected 2 explicitly applied filters, but found " + activeFilters.size());
        logger.info("Verified that 2 active filters are shown, as expected.");

        boolean styleFilterFound = activeFilters.stream()
                .anyMatch(f -> f.toUpperCase().contains("STYLE") && f.toUpperCase().contains("TEE"));
                    
        boolean materialFilterFound = activeFilters.stream()
                .anyMatch(f -> f.toUpperCase().contains("MATERIAL") && f.toUpperCase().contains("COTTON"));

        // Assert that each of our clicked filters is present.
        Assert.assertTrue(styleFilterFound, "The 'Style: Tee' filter was not found.");
        Assert.assertTrue(materialFilterFound, "The 'Material: Cotton' filter was not found.");
        logger.pass("Successfully verified that 2 explicit filters are applied and displayed correctly.");
    }
    
 // This is the test case that was failing. Replace it with this version.
    @Test(priority = 5, description = "(P) After applying a filter, use the 'Clear All' link.")
    public void verifyClearAllFilters() {
        ExtentTest logger = extentTest.get();
        logger.info("Starting test: Verify 'Clear All' Filters functionality.");
        
        // SETUP: Navigate and get the original state.
        categoryPage.navigateToMenTopsCategory();
        int initialCount = categoryPage.getProductCount();
        logger.info("Navigated to Men > Tops page. Initial product count: " + initialCount);

        // ACTION 1: Apply a filter to make the "Clear All" link appear.
        categoryPage.applyFilter("Material", "Cotton");
        logger.info("Applied filter 'Material: Cotton'.");
        
        // Sanity check that the filter worked.
        Assert.assertNotEquals(categoryPage.getProductCount(), initialCount, "Sanity check failed: Filter did not change the product count.");
        Assert.assertFalse(categoryPage.getActiveFilterLabels().isEmpty(), "Sanity check failed: 'Now Shopping By' section did not appear.");
        logger.info("'Now Shopping By' section is visible with filters.");

        categoryPage.clickClearAllFilters();
        logger.info("Clicked 'Clear All' filters link.");

        int finalCount = categoryPage.getProductCount();
        Assert.assertEquals(finalCount, initialCount, "Product count did not return to its original value after clearing filters.");
        logger.pass("Verified that the product count reset correctly.");

        // Assertion 2: Verify the "Now Shopping By" block is gone.
        List<String> activeFiltersAfterClear = categoryPage.getActiveFilterLabels();
        Assert.assertTrue(activeFiltersAfterClear.isEmpty(), "'Now Shopping By' section did not disappear after clearing all filters.");
        logger.pass("Verified that the 'Now Shopping By' section is no longer displayed.");
    }

    
    @Test(priority = 6, description = "(E) Verify applying a filter and then sorting.")
    public void verifyFilterThenSort() {
        ExtentTest logger = extentTest.get();
        logger.info("Starting test: Verify Filter Then Sort");
        categoryPage.navigateToMenTopsCategory();
        logger.info("Navigated to Men > Tops page.");
        // FIX: Use title case "Style"
        categoryPage.applyFilter("Style", "Tank");
        logger.info("Applied filter Style: Tank.");
        categoryPage.sortByOption("Price");
        logger.info("Sorted the filtered results by Price.");
        List<String> productNames = categoryPage.getProductNames();
        Assert.assertTrue(productNames.stream().allMatch(name -> name.toLowerCase().contains("tank")), "Filtered results do not all contain 'Tank' after sorting.");
        logger.info("Product names still match the filter after sorting.");
        List<Double> actualPrices = categoryPage.getProductPrices();
        List<Double> expectedSortedPrices = new java.util.ArrayList<>(actualPrices);
        Collections.sort(expectedSortedPrices);
        Assert.assertEquals(actualPrices, expectedSortedPrices, "Filtered products are not sorted correctly by price.");
        logger.pass("Successfully verified that sorting works correctly on a filtered list.");
    }
    
    @Test(priority = 7, description = "(E) On a page with multiple pages of products, verify pagination updates after filtering.")
    public void verifyPaginationUpdatesAfterFilter() {
        ExtentTest logger = extentTest.get();
        logger.info("Starting test: Verify Pagination Updates After Filter");
        categoryPage.navigateToMenCategory();
        logger.info("Navigated to main Men category page via UI click to ensure pagination is present.");
        
        String initialPagination = categoryPage.getPaginationToolbarText();
        logger.info("Initial pagination text: '" + initialPagination + "'");
        
        // ACTION 2: Apply a filter. Filtering by "Tops" is a valid action on the "Men" page.
        categoryPage.applyFilter("Category", "Tops");
        logger.info("Applied filter Category: Tops.");

        // VERIFICATION: Capture the final state of the pagination toolbar.
        String updatedPagination = categoryPage.getPaginationToolbarText();
        logger.info("Updated pagination text: '" + updatedPagination + "'");
        
        // ASSERTION: Verify that the pagination text has changed.
        Assert.assertNotEquals(initialPagination, updatedPagination, 
                "Pagination text did not update after applying a filter. Initial and Final text were the same.");
                
        logger.pass("Successfully verified that the pagination toolbar text changed after filtering.");
    }
}