package com.platform.recs.e2e;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ScreenshotOnFailureListener.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EndToEndTests {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = System.getenv().getOrDefault("FRONTEND_BASE_URL", "http://localhost:5173");
    private static String uniqueEmail(String prefix) {
        return prefix + "+" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
    }

    @BeforeAll
    static void setupClass() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--window-size=1400,900");
        driver = new ChromeDriver(options);
        ScreenshotOnFailureListener.setDriver(driver);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @AfterAll
    static void tearDownClass() {
        if (driver != null) driver.quit();
    }

    @Test
    @Order(1)
    void journey1_authenticationRegistrationAndRoleBasedRedirect() throws Exception {
        // Create a new generator via registration and validate redirect to /assets
        String generatorEmail = uniqueEmail("generator");
        driver.get(BASE_URL + "/register");
        type(By.id("registerName"), "Journey Generator");
        type(By.id("registerEmail"), generatorEmail);
        type(By.id("registerPassword"), "Password123!");
        selectByValue(By.id("registerRole"), "GENERATOR");
        click(By.id("registerButton"));
        wait.until(ExpectedConditions.urlContains("/assets"));
        assertTrue(driver.getCurrentUrl().contains("/assets"), "Generator should redirect to assets");
        assertTrue(pageText().contains("Assets"), "Assets page should load after generator registration");

        // Logout and register a buyer; redirect to /recs
        click(By.id("logoutButton"));
        wait.until(ExpectedConditions.urlContains("/login"));

        String buyerEmail = uniqueEmail("buyer");
        driver.get(BASE_URL + "/register");
        type(By.id("registerName"), "Journey Buyer");
        type(By.id("registerEmail"), buyerEmail);
        type(By.id("registerPassword"), "Password123!");
        selectByValue(By.id("registerRole"), "BUYER");
        click(By.id("registerButton"));
        wait.until(ExpectedConditions.urlContains("/recs"));
        assertTrue(driver.getCurrentUrl().contains("/recs"), "Buyer should redirect to recs");
        assertTrue(pageText().contains("RECs"), "Recs page should load after buyer registration");

        // Admin login redirect to /dashboard
        click(By.id("logoutButton"));
        wait.until(ExpectedConditions.urlContains("/login"));
        driver.get(BASE_URL + "/login");
        type(By.id("loginEmail"), "admin@example.com");
        type(By.id("loginPassword"), "Admin#12345");
        click(By.id("loginButton"));
        wait.until(ExpectedConditions.urlContains("/dashboard"));
        assertTrue(driver.getCurrentUrl().contains("/dashboard"), "Admin should redirect to dashboard");
        assertTrue(pageText().contains("Dashboard") || pageText().contains("ADMIN"), "Dashboard should load after admin login");
    }

    @Test
    @Order(2)
    void journey2_generatorOnboardingAssetAndSubmittingLogs() throws Exception {
        // Login as seed generator
        login("generator@example.com", "Generator#12345");

        // Onboard a solar asset
        driver.get(BASE_URL + "/assets");
        String assetCode = "SOLAR-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        type(By.id("assetCode"), assetCode);
        type(By.id("assetName"), "Journey Solar Farm");
        selectByValue(By.id("assetSource"), "SOLAR");
        type(By.id("assetLocation"), "Pune");
        type(By.id("assetCapacity"), "25.5");
        click(By.id("createAssetButton"));
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("assetTable"), assetCode));

        // Admin verifies the asset
        click(By.id("logoutButton"));
        wait.until(ExpectedConditions.urlContains("/login"));
        login("admin@example.com", "Admin#12345");
        driver.get(BASE_URL + "/assets");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//table[@id='assetTable']//td[text()='" + assetCode + "']")));
        click(By.xpath("//table[@id='assetTable']//tr[td[text()='" + assetCode + "']]//button[contains(@id,'verifyAsset-')]"));
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.xpath("//table[@id='assetTable']//tr[td[text()='" + assetCode + "']]//td[5]"), "ACTIVE"));

        // Generator logs generation
        click(By.id("logoutButton"));
        wait.until(ExpectedConditions.urlContains("/login"));
        login("generator@example.com", "Generator#12345");
        driver.get(BASE_URL + "/generation-logs");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//select[@id='logAssetId']/option[text()='" + assetCode + "']")));
        selectByVisibleText(By.id("logAssetId"), assetCode);
        WebElement dateEl = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("logDate")));
        ((JavascriptExecutor) driver).executeScript(
            "const s = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;" +
            "s.call(arguments[0], arguments[1]); arguments[0].dispatchEvent(new Event('input', {bubbles: true}));",
            dateEl, "2026-10-01");
        selectByValue(By.id("logSource"), "SOLAR");
        type(By.id("logQuantity"), "120.5");
        type(By.id("logVintage"), "2026");
        click(By.id("submitLogButton"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//table[@id='logTable']//td[text()='" + assetCode + "']")));
        assertTrue(pageText().contains(assetCode), "Generation log should appear in list");
    }

    @Test
    @Order(3)
    void journey3_adminReviewVerificationAndMinting() throws Exception {
        login("admin@example.com", "Admin#12345");
        driver.get(BASE_URL + "/generation-logs");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("logTable")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//table[@id='logTable']//button[contains(@id,'verifyLog-')]")));

        // Verify first submitted log if present, then mint it
        click(By.xpath("//table[@id='logTable']//button[contains(@id,'verifyLog-')]"));
        wait.until(ExpectedConditions.textToBe(By.xpath("//table[@id='logTable']//tr[1]//td[7]"), "VERIFIED"));
        click(By.xpath("//table[@id='logTable']//button[contains(@id,'mintLog-')]"));
        wait.until(ExpectedConditions.textToBe(By.xpath("//table[@id='logTable']//tr[1]//td[7]"), "MINTED"));

        // Confirm REC appears
        driver.get(BASE_URL + "/recs");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("recTable")));
        assertTrue(pageText().contains("REC-"), "REC should exist after minting");
    }

    @Test
    @Order(4)
    void journey4_buyerBrowsingPurchasingAndRetirement() throws Exception {
        // Generator lists a minted REC
        login("generator@example.com", "Generator#12345");
        driver.get(BASE_URL + "/recs");
        String recCode = firstRecCodeFromRecsPage();
        click(By.xpath("//table[@id='recTable']//tr[td[text()='" + recCode + "']]//button[contains(@id,'listRec-')]"));
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.xpath("//table[@id='recTable']//tr[td[text()='" + recCode + "']]//td[6]"), "LISTED"));

        // Buyer purchases and retires
        click(By.id("logoutButton"));
        wait.until(ExpectedConditions.urlContains("/login"));
        login("buyer@example.com", "Buyer#12345");
        driver.get(BASE_URL + "/recs");
        click(By.xpath("//table[@id='recTable']//tr[td[text()='" + recCode + "']]//button[contains(@id,'buyRec-')]"));
        Thread.sleep(1500);

        // Filter by TRANSFERRED to confirm purchase transferred ownership
        selectByValue(By.id("recStatusFilter"), "TRANSFERRED");
        click(By.id("searchRecsButton"));
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.xpath("//table[@id='recTable']//tr[td[text()='" + recCode + "']]//td[6]"), "TRANSFERRED"));
        click(By.xpath("//table[@id='recTable']//tr[td[text()='" + recCode + "']]//button[contains(@id,'retireRec-')]"));

        // Filter by RETIRED to confirm retirement
        selectByValue(By.id("recStatusFilter"), "RETIRED");
        click(By.id("searchRecsButton"));
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.xpath("//table[@id='recTable']//tr[td[text()='" + recCode + "']]//td[6]"), "RETIRED"));

        assertTrue(pageText().contains("RETIRED"), "REC should be retired");
    }

    private String firstRecCodeFromRecsPage() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("recTable")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//table[@id='recTable']//tr[1]/td[1]")));
        return driver.findElement(By.xpath("//table[@id='recTable']//tr[1]/td[1]")).getText();
    }

    private void login(String email, String password) {
        driver.get(BASE_URL + "/login");
        type(By.id("loginEmail"), email);
        type(By.id("loginPassword"), password);
        click(By.id("loginButton"));
        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/login")));
    }

    private void type(By by, String text) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(by));
        el.clear();
        el.sendKeys(text);
    }

    private void click(By by) {
        wait.until(ExpectedConditions.elementToBeClickable(by)).click();
    }

    private void selectByValue(By by, String value) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(by));
        el.findElements(By.tagName("option")).stream()
            .filter(opt -> {
                String v = opt.getAttribute("value");
                return value.equals(v) || (v == null && value.equals(opt.getText()));
            })
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException("Option " + value + " not found"))
            .click();
    }

    private void selectByVisibleText(By by, String text) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(by));
        el.findElements(By.tagName("option")).stream()
            .filter(opt -> opt.getText().contains(text))
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException("Option containing " + text + " not found"))
            .click();
    }

    private String pageText() {
        return driver.getPageSource();
    }
}
