package tests;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
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
    private WebDriverWait wait; // THE FIX: Declare the wait object
    private static ExtentReports extent;
    private static ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();

    @BeforeSuite
    public void setupSuite() {
        extent = new ExtentReports();
        ExtentSparkReporter spark = new ExtentSparkReporter("test-output/ProductDiscoveryReport.html");
        extent.attachReporter(spark);
        extent.setSystemInfo("Host Name", "Magento Test Host");
        extent.setSystemInfo("Environment", "QA");
        extent.setSystemInfo("User Name", "Test User");
    }

    @BeforeMethod
    public void setupMethod(Method method) {
    	WebDriverManager.chromedriver().driverVersion("137.0.7151.104").setup();
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.get("https://magento.softwaretestingboard.com/");
        categoryPage = new ProductDiscoveryAndBrowsing(driver);
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
        
        categoryPage.applyFilter("Style", "Tank");
        logger.info("Applied filter Style: Tank.");
        
        List<String> activeFilters = categoryPage.getActiveFilterLabels();
        
        Assert.assertEquals(activeFilters.size(), 1, "Expected exactly one active filter, but found " + activeFilters.size());
        logger.info("Verified that exactly one filter is active.");

        String filterText = activeFilters.get(0);
        
        Assert.assertTrue(filterText.toUpperCase().contains("STYLE"), "The active filter text did not contain the category 'STYLE'. Full text: " + filterText);
        Assert.assertTrue(filterText.toUpperCase().contains("TANK"), "The active filter text did not contain the value 'TANK'. Full text: " + filterText);
        logger.info("Verified filter text contains 'STYLE' and 'TANK'.");
        
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
        logger.info("Applied explicit filter Style: T-Shirt.");
        categoryPage.applyFilter("Material", "Cotton");
        logger.info("Applied explicit filter Material: Cotton.");
        List<String> activeFilters = categoryPage.getActiveFilterLabels();
        Assert.assertEquals(activeFilters.size(), 2, "Expected 2 explicitly applied filters, but found " + activeFilters.size());
        
        boolean styleFilterFound = activeFilters.stream().anyMatch(f -> f.toUpperCase().contains("STYLE") && f.toUpperCase().contains("TEE"));
        boolean materialFilterFound = activeFilters.stream().anyMatch(f -> f.toUpperCase().contains("MATERIAL") && f.toUpperCase().contains("COTTON"));
        Assert.assertTrue(styleFilterFound, "The 'Style: Tee' filter was not found.");
        Assert.assertTrue(materialFilterFound, "The 'Material: Cotton' filter was not found.");
        logger.pass("Successfully verified that 2 explicit filters are applied and displayed correctly.");
    }
    
    @Test(priority = 5, description = "(P) After applying a filter, use the 'Clear All' link.")
    public void verifyClearAllFilters() {
        ExtentTest logger = extentTest.get();
        logger.info("Starting test: Verify 'Clear All' Filters functionality.");
        
        categoryPage.navigateToMenTopsCategory();
        int initialCount = categoryPage.getProductCount();
        logger.info("Navigated to Men > Tops page. Initial product count: " + initialCount);

        categoryPage.applyFilter("Material", "Cotton");
        logger.info("Applied filter 'Material: Cotton'.");
        
        Assert.assertNotEquals(categoryPage.getProductCount(), initialCount, "Sanity check failed: Filter did not change the product count.");
        Assert.assertFalse(categoryPage.getActiveFilterLabels().isEmpty(), "Sanity check failed: 'Now Shopping By' section did not appear.");
        logger.info("'Now Shopping By' section is visible with filters.");

        categoryPage.clickClearAllFilters();
        logger.info("Clicked 'Clear All' filters link.");

        int finalCount = categoryPage.getProductCount();
        Assert.assertEquals(finalCount, initialCount, "Product count did not return to its original value after clearing filters.");
        logger.pass("Verified that the product count reset correctly.");

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
    
    @Test(priority = 7, description = "(E) Verify pagination updates after filtering on a sub-category page.")
    public void verifyPaginationUpdatesAfterFilteringOnSubCategory() {
        ExtentTest logger = extentTest.get();
        logger.info("Starting test: Verify Pagination Updates After Filtering on a Sub-Category");
        
        categoryPage.navigateToMenTopsCategory();
        logger.info("Navigated to 'Men > Tops' category page.");
        
        String initialPagination = categoryPage.getPaginationToolbarText();
        logger.info("Initial pagination text: '" + initialPagination + "'");
        
        categoryPage.applyFilter("Style", "Tee");
        logger.info("Applied filter Style: Tee.");

        String updatedPagination = categoryPage.getPaginationToolbarText();
        logger.info("Updated pagination text: '" + updatedPagination + "'");
        
        Assert.assertNotEquals(initialPagination, updatedPagination, 
                "Pagination text did not update after applying the 'Style' filter. Initial and Final text were the same.");
                
        logger.pass("Successfully verified that the pagination toolbar text changed after filtering.");
    }
    
    @Test(priority = 8, description = "[New] Validate adding a product from its Product Display Page (PDP).")
    public void addProductToCompareFromPDP() {
        ExtentTest logger = extentTest.get();
        logger.info("Starting test: Add to Compare from PDP");

        categoryPage.navigateToMenTopsCategory();
        logger.info("Navigated to category page.");

        categoryPage.navigateToFirstProductPageFromCategory();
        logger.info("Navigated to the first product's display page.");

        categoryPage.addProductToCompareFromPDP();
        String successMessage = categoryPage.getSuccessMessageText();

        Assert.assertTrue(successMessage.contains("You added product"), "Success message for adding to compare from PDP is incorrect.");
        logger.pass("Successfully verified adding a product to compare from the PDP.");
    }

    @Test(priority = 9, description = "[New] Validate Compare Page with two distinct products.")
    public void validateComparePageWithTwoItems() {
        ExtentTest logger = extentTest.get();
        logger.info("Starting test: Compare Page with two items");
        categoryPage.navigateToMenTopsCategory();
        logger.info("Navigated to category page.");

        categoryPage.addFirstProductToCompare();
        logger.info("Added the first product to compare.");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".message-success")));
        
        categoryPage.addSecondProductToCompare();
        logger.info("Added the second product to compare.");

        categoryPage.navigateToComparePageViaSuccessLink();
        logger.info("Navigated to the Compare Products page.");

        List<String> comparedItems = categoryPage.getComparedProductNames();
        Assert.assertEquals(comparedItems.size(), 2, "Expected 2 items in the comparison list, but found " + comparedItems.size());
        logger.pass("Successfully verified that two distinct products appear on the Compare Products page.");
    }

    @Test(priority = 10, description = "[New] Validate adding the same product to compare twice.")
    public void addSameProductToCompareTwice() {
        ExtentTest logger = extentTest.get();
        logger.info("Starting test: Add same product to compare twice");
        categoryPage.navigateToMenTopsCategory();
        logger.info("Navigated to category page.");

        categoryPage.addFirstProductToCompare();
        logger.info("Added the first product to compare.");
        String firstSuccessMessage = categoryPage.getSuccessMessageText();
        Assert.assertTrue(firstSuccessMessage.contains("You added product"), "Initial add to compare failed.");

        categoryPage.addFirstProductToCompare();
        logger.info("Attempted to add the same product again.");
        categoryPage.getSuccessMessageText();

        categoryPage.navigateToComparePageViaSuccessLink();
        List<String> comparedItems = categoryPage.getComparedProductNames();
        Assert.assertEquals(comparedItems.size(), 1, "Adding the same product twice should result in only one item in the compare list.");
        logger.pass("Successfully verified that adding a duplicate product does not add a new item to the list.");
    }

    @Test(priority = 11, description = "[New] Validate removing an item from the Compare Page.")
    public void removeItemFromComparePage() {
        ExtentTest logger = extentTest.get();
        logger.info("Starting test: Remove item from Compare Page");

        categoryPage.navigateToMenTopsCategory();
        categoryPage.addFirstProductToCompare();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".message-success")));
        categoryPage.addSecondProductToCompare();
        
        categoryPage.navigateToComparePageViaSuccessLink();
        logger.info("Setup complete: Navigated to compare page with 2 items.");

        Assert.assertEquals(categoryPage.getComparedProductNames().size(), 2, "Setup failed: Did not start with 2 items.");

        categoryPage.removeFirstItemFromCompare();
        logger.info("Removed the first item from the list.");
        
        wait.until(ExpectedConditions.numberOfElementsToBe(By.cssSelector(".comparison .product-item-name"), 1));
        List<String> comparedItems = categoryPage.getComparedProductNames();
        Assert.assertEquals(comparedItems.size(), 1, "Item was not successfully removed from the comparison list.");
        logger.pass("Successfully verified removing an item from the compare page.");
    }

    @Test(priority = 12, description = "[New] Validate the page elements of the Compare Page.")
    public void validateComparePageElements() {
        ExtentTest logger = extentTest.get();
        logger.info("Starting test: Validate Compare Page elements");
        categoryPage.navigateToMenTopsCategory();
        
        categoryPage.addFirstProductToCompare();
        categoryPage.navigateToComparePageViaSuccessLink();
        logger.info("Navigated to the Compare Products page.");
        
        Assert.assertEquals(driver.getTitle(), "Products Comparison List - Magento Commerce", "Browser tab title is incorrect.");
        Assert.assertTrue(driver.getCurrentUrl().contains("catalog/product_compare/"), "Page URL is incorrect.");
        Assert.assertEquals(categoryPage.getPageTitleHeadingText(), "Compare Products", "Main page heading is incorrect.");
        logger.pass("Successfully validated the Title, URL, and Page Heading of the Compare Products page.");
    }

    @Test(priority = 13, description = "[New] Validate the state of an empty Compare Page.")
    public void validateEmptyComparePage() {
        ExtentTest logger = extentTest.get();
        logger.info("Starting test: Validate Empty Compare Page");
        driver.get("https://magento.softwaretestingboard.com/catalog/product_compare/index/");
        
        Assert.assertEquals(categoryPage.getPageTitleHeadingText(), "Compare Products", "Page heading is incorrect for empty compare page.");
        String expectedMessage = "You have no items to compare.";
        String actualMessage = categoryPage.getEmptyCompareListMessage();
        Assert.assertEquals(actualMessage, expectedMessage, "The 'no items to compare' message is incorrect or not found.");
        logger.pass("Successfully verified the content of an empty compare page.");
    }
}