package pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import pages.ProductPage;
import java.time.Duration;

public class ProductPage {

    private final WebDriverWait wait;
    private final JavascriptExecutor js;

    // --- Locators ---
    @FindBy(id = "option-label-size-143-item-167")
    private WebElement sizeOption;

    @FindBy(id = "option-label-color-93-item-50")
    private WebElement colorOption;

    @FindBy(id = "product-addtocart-button")
    private WebElement addToCartButton;

    @FindBy(css = "div.loading-mask")
    private WebElement loadingMask;

    @FindBy(css = "div[data-ui-id='message-success']")
    private WebElement successMessage;

    @FindBy(css = "span.counter-number")
    private WebElement cartCounterNumber;

    // --- Constructor ---
    public ProductPage(WebDriver driver) {
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        this.js = (JavascriptExecutor) driver; // <-- INITIALIZE IT
        PageFactory.initElements(driver, this);
    }

    private void safeClick(WebElement element) {
        wait.until(ExpectedConditions.elementToBeClickable(element));
        js.executeScript("arguments[0].scrollIntoView(true);", element);
        js.executeScript("arguments[0].click();", element);
    }

    public void addRadiantTeeToCart() {
        safeClick(sizeOption);
        safeClick(colorOption);
        waitForLoaderToDisappear();
        safeClick(addToCartButton);
        wait.until(ExpectedConditions.visibilityOf(successMessage));
    }

    public String getCartCounterNumber() {
        wait.until(ExpectedConditions.visibilityOf(cartCounterNumber));
        return cartCounterNumber.getText();
    }
    
    private void waitForLoaderToDisappear() {
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.loading-mask")));
            wait.until(ExpectedConditions.invisibilityOf(loadingMask));
        } catch (TimeoutException e) {
        }
    }
}