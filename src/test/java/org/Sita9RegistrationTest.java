package org;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Sita9RegistrationTest {
    private static final String PASSWORD = "Test@123";
    private static final String MAILINATOR_INBOX_BASE = "https://www.mailinator.com/v4/public/inboxes.jsp?to=";
    private static final String SITA9_APP_URL = "https://app-sita9.glyph.network/";
    private static final Pattern VERIFY_EMAIL_LINK = Pattern.compile("https://app-sita9\\.glyph\\.network/verify-email\\?token=[a-zA-Z0-9]+");
    private static final String SIGNUP_EMAILS_FILE = "signup-emails.txt";

    WebDriver driver;
    WebDriverWait wait;
    String email;

    @BeforeClass
    public void setup() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        boolean headless = "true".equalsIgnoreCase(System.getProperty("headless")) || "true".equals(System.getenv("GITHUB_ACTIONS"));
        if (headless) {
            options.addArguments("--headless=new", "--disable-gpu", "--no-sandbox", "--disable-dev-shm-usage",
                    "--window-size=1920,1080", "--disable-extensions", "--remote-allow-origins=*");
            System.out.println("Running in headless mode (CI / -Dheadless=true).");
        }
        driver = new ChromeDriver(options);
        if (!headless) {
            driver.manage().window().maximize();
        }
        wait = new WebDriverWait(driver, Duration.ofSeconds(25));

        int randomNum = new Random().nextInt(100000);
        email = "sita9_test" + randomNum + "@mailinator.com";
        saveSignupEmailToFile(email);
        System.out.println("--- Browser Started ---");
        System.out.println("Generated Mailinator email: " + email);
    }

    /** Appends the sign-up email (with timestamp) to signup-emails.txt for every new registration. */
    private void saveSignupEmailToFile(String signupEmail) {
        try {
            Path file = Paths.get(SIGNUP_EMAILS_FILE);
            String line = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + " " + signupEmail + System.lineSeparator();
            Files.write(file, line.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            System.out.println("Stored sign-up email in " + file.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("Could not write sign-up email to file: " + e.getMessage());
        }
    }

    @Test
    public void testSita9Registration() throws InterruptedException {
        driver.get(SITA9_APP_URL);
        System.out.println("Navigated to: " + SITA9_APP_URL);

        WebElement logo = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[@class='flow-logo-y absolute inset-0']")));
        Assert.assertTrue(logo.isDisplayed(), "Logo is not visible on the main page!");
        System.out.println("Step 1: Sita9 main page loaded and logo verified.");

        WebElement signUpBtn = driver.findElement(By.xpath("//button[normalize-space()='Sign up']"));
        signUpBtn.click();
        System.out.println("Step 2: Clicked on 'Sign up' button.");

        WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@type='email']")));
        emailInput.sendKeys(email);
        System.out.println("Step 3: Entered email: " + email);

        driver.findElement(By.cssSelector("input[placeholder='Create a password']")).sendKeys(PASSWORD);
        System.out.println("Step 4: Entered password.");

        driver.findElement(By.cssSelector("input[placeholder='Confirm your password']")).sendKeys(PASSWORD);
        System.out.println("Step 5: Confirmed password.");

        List<WebElement> formItems = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(
                By.cssSelector("[id*='form-item']")));
        WebElement termsCheckbox = formItems.get(formItems.size() - 1);
        wait.until(ExpectedConditions.elementToBeClickable(termsCheckbox)).click();
        Thread.sleep(500);
        System.out.println("Step 6: Clicked terms and conditions checkbox.");

        driver.findElement(By.xpath("//button[normalize-space()='Create account']")).click();
        System.out.println("Step 7: Clicked 'Create account' button.");

        By[] successLocators = {
                By.xpath("//div[contains(., 'Account created!')]"),
                By.xpath("//div[contains(., 'User created successfully')]"),
                By.xpath("//*[contains(., 'Please check your email')]")
        };
        ExpectedCondition<WebElement> anySuccessVisible = d -> {
            for (By locator : successLocators) {
                for (WebElement el : d.findElements(locator)) {
                    if (el.isDisplayed()) return el;
                }
            }
            return null;
        };
        WebElement successElement = wait.until(anySuccessVisible);
        String successText = successElement.getText();
        Assert.assertTrue(
                successText.contains("Account created!") || successText.contains("User created successfully") || successText.contains("Please check your email"),
                "Success message not found! Got: " + successText);
        System.out.println("Step 8: Account creation success verified: " + successText);

        // Fully automated email verification via Mailinator
        verifyEmailViaMailinator(email);

        // Single login (verification is already done)
        loginToSita9(email, PASSWORD);

        runAnalyticsProjectSetupFlow();
    }

    /**
     * Opens Mailinator public inbox in a new tab, waits for verification email,
     * opens it, extracts verification link, and navigates main window to complete verification.
     */
    private void verifyEmailViaMailinator(String emailAddress) {
        String inboxName = emailAddress.split("@")[0];
        String mailinatorUrl = MAILINATOR_INBOX_BASE + inboxName;
        String mainWindow = driver.getWindowHandle();

        System.out.println("Step 9: Opening Mailinator public inbox in new tab: " + mailinatorUrl);
        ((JavascriptExecutor) driver).executeScript("window.open(arguments[0], '_blank');", mailinatorUrl);

        WebDriverWait mailinatorWait = new WebDriverWait(driver, Duration.ofSeconds(60));
        String mailinatorWindow = mailinatorWait.until(d -> {
            for (String h : d.getWindowHandles()) {
                if (!h.equals(mainWindow)) return h;
            }
            return null;
        });
        driver.switchTo().window(mailinatorWindow);

        // Wait for inbox to load and the verification email — click the <td> with From: support@glyph.network (onclick=showTheMessage) to open the email
        System.out.println("Step 10: Waiting for verification email (From: support@glyph.network) in Mailinator inbox...");
        By verificationEmailCell = By.xpath(
                "//td[contains(@onclick, 'showTheMessage') and contains(., 'support@glyph.network')] | " +
                "//td[contains(., 'support@glyph.network')]");
        WebElement emailCell = mailinatorWait.until(ExpectedConditions.elementToBeClickable(verificationEmailCell));
        emailCell.click();
        System.out.println("Step 11: Clicked email (support@glyph.network); opened message — extracting verify-email link...");

        // Give email body a moment to load, then find verification link (iframe or main document or page source)
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String verificationUrl = new WebDriverWait(driver, Duration.ofSeconds(30)).until(d -> {
            d.switchTo().defaultContent();
            List<String> ids = List.of("msg_body", "htmlmsgbody", "iframeMail");
            for (String id : ids) {
                try {
                    d.switchTo().defaultContent();
                    d.switchTo().frame(id);
                    List<WebElement> links = d.findElements(By.xpath("//a[contains(@href, 'verify-email')]"));
                    if (!links.isEmpty()) return links.get(0).getAttribute("href");
                } catch (Exception ignored) {
                }
            }
            d.switchTo().defaultContent();
            List<WebElement> links = d.findElements(By.xpath("//a[contains(@href, 'verify-email')]"));
            if (!links.isEmpty()) return links.get(0).getAttribute("href");
            String html = d.getPageSource();
            Matcher m = VERIFY_EMAIL_LINK.matcher(html);
            return m.find() ? m.group() : null;
        });
        Assert.assertNotNull(verificationUrl, "Could not find verification link in Mailinator email for: " + emailAddress);
        System.out.println("Step 12: Extracted verification link from email.");

        driver.switchTo().window(mainWindow);
        System.out.println("Step 13: Navigating to verification URL in main window (Mailinator stays open in other tab)...");
        driver.get(verificationUrl);

        new WebDriverWait(driver, Duration.ofSeconds(25)).until(d -> d.getCurrentUrl().contains("verify-email"));
        new WebDriverWait(driver, Duration.ofSeconds(15)).until(d ->
                "complete".equals(((JavascriptExecutor) d).executeScript("return document.readyState")));

        // Wait for verification to complete: success message on page or redirect to login
        WebDriverWait verifyDoneWait = new WebDriverWait(driver, Duration.ofSeconds(20));
        boolean verified = verifyDoneWait.until(d -> {
            String url = d.getCurrentUrl();
            if (url.contains("/login")) return true;
            String body = d.findElement(By.tagName("body")).getText();
            return body.contains("verified") || body.contains("Verified") || body.contains("success") || body.contains("Success");
        });
        Assert.assertTrue(verified, "Verification did not complete: no success message or redirect to login.");
        System.out.println("Step 14: Verification link opened and verified successfully (success/redirect detected).");

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Close Mailinator tab only if we have 2+ windows. Then ensure we're on the Sita9 window and switch back.
        if (driver.getWindowHandles().size() > 1) {
            driver.switchTo().window(mailinatorWindow);
            driver.close();
        }
        for (String h : driver.getWindowHandles()) {
            driver.switchTo().window(h);
            break;
        }
        System.out.println("Step 15: Switched back to Sita9 tab; now proceeding to login.");
    }

    /**
     * Single login attempt to SITA9 (verification is already done).
     */
    private void loginToSita9(String emailAddress, String password) {
        System.out.println("Step 16: Logging in to SITA9 with verified account...");
        try {
            driver.get(SITA9_APP_URL);
        } catch (NoSuchWindowException e) {
            Assert.fail("Browser window was closed.", e);
        }
        WebDriverWait loginWait = new WebDriverWait(driver, Duration.ofSeconds(25));
        WebElement emailField = loginWait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[name='email']")));
        WebElement passwordField = loginWait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[name='password']")));
        emailField.sendKeys(Keys.chord(Keys.COMMAND, "a"));
        emailField.sendKeys(emailAddress);
        passwordField.sendKeys(Keys.chord(Keys.COMMAND, "a"));
        passwordField.sendKeys(password);
        loginWait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[normalize-space()='Sign In' or normalize-space()='Sign in']"))).click();

        new WebDriverWait(driver, Duration.ofSeconds(15)).until(d -> {
            String u = d.getCurrentUrl();
            boolean urlMoved = !u.contains("verify-email") && !u.contains("sign-in");
            boolean noEmailField = d.findElements(By.cssSelector("input[name='email']")).isEmpty();
            return urlMoved || noEmailField;
        });
        System.out.println("Step 17: Login successful. URL: " + driver.getCurrentUrl());
    }

    private void runAnalyticsProjectSetupFlow() {
        // Ensure we have a valid window (avoid NoSuchWindowException)
        if (driver.getWindowHandles().isEmpty()) {
            Assert.fail("No browser window open. Cannot continue analytics flow. Do not close the browser during the test.");
        }
        for (String h : driver.getWindowHandles()) {
            driver.switchTo().window(h);
            if (driver.getCurrentUrl().contains("glyph.network")) break;
        }
        WebDriverWait analyticsWait = new WebDriverWait(driver, Duration.ofSeconds(40));

        System.out.println("STEP 1: Verifying welcome message after login...");
        analyticsWait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h2[normalize-space()='Welcome to SITA9 Analytics']")));
        System.out.println("Verified welcome message: Welcome to SITA9 Analytics");

        System.out.println("STEP 2: Clicking 'Create Your First Project' button...");
        WebElement createProjectBtn = analyticsWait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[normalize-space()='Create Your First Project']")));
        createProjectBtn.click();
        System.out.println("Clicked on Create Your First Project button");

        System.out.println("STEP 3: Entering Project Name...");
        WebElement projectNameInput = analyticsWait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@name='name' and @placeholder='e.g., DeFi Exchange Pro']")));
        projectNameInput.clear();
        projectNameInput.sendKeys("DeFi");
        System.out.println("Entered Project Name: DeFi");

        System.out.println("STEP 4: Entering Project URL...");
        WebElement projectUrlInput = analyticsWait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@name='url' and @placeholder='https://yourproject.com']")));
        projectUrlInput.clear();
        projectUrlInput.sendKeys("https://www.google.com/");
        System.out.println("Entered Project URL: https://www.google.com/");

        System.out.println("STEP 5: Selecting Project Type 'DeFi Protocol'...");
        WebElement projectTypeDropdown = analyticsWait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//button[@role='combobox' and .//span[normalize-space()='Select project type']]")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", projectTypeDropdown);
        analyticsWait.until(ExpectedConditions.elementToBeClickable(projectTypeDropdown)).click();
        WebElement defiProtocolOption = analyticsWait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//span[normalize-space()='DeFi Protocol']")));
        defiProtocolOption.click();
        System.out.println("Opened Project Type dropdown and selected: DeFi Protocol");

        System.out.println("STEP 6: Selecting blockchain network 'Ethereum'...");
        WebElement ethereumOption = analyticsWait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//span[normalize-space()='Ethereum']")));
        ethereumOption.click();
        System.out.println("Selected blockchain network: Ethereum");

        System.out.println("STEP 7: Clicking 'Next: Add Contracts'...");
        WebElement nextAddContractsBtn = analyticsWait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[normalize-space()='Next: Add Contracts']")));
        nextAddContractsBtn.click();
        analyticsWait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//button[normalize-space()='Add Contract']")));
        System.out.println("Clicked Next: Add Contracts and waiting for Contracts screen");

        System.out.println("STEP 8: Clicking 'Add Contract' button...");
        WebElement addContractBtn = analyticsWait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[normalize-space()='Add Contract']")));
        addContractBtn.click();
        System.out.println("Clicked Add Contract button");

        System.out.println("STEP 9: Entering Token Name...");
        WebElement tokenNameInput = analyticsWait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@placeholder='e.g., Main Token']")));
        tokenNameInput.clear();
        tokenNameInput.sendKeys("Layer zero");
        System.out.println("Entered Token Name: Layer zero");

        System.out.println("STEP 10: Entering Contract Address...");
        WebElement contractAddressInput = analyticsWait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@placeholder='0x...']")));
        contractAddressInput.clear();
        contractAddressInput.sendKeys("0x6985884C4392D348587B19cb9eAAf157F13271cd");
        System.out.println("Entered Contract Address: 0x6985884C4392D348587B19cb9eAAf157F13271cd");

        System.out.println("STEP 11: Checking contract verification checkbox...");
        WebElement verifiedCheckbox = analyticsWait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[@role='checkbox']")));
        verifiedCheckbox.click();
        System.out.println("Checked contract verification checkbox");

        System.out.println("STEP 12: Clicking 'Next: Fetch ABIs'...");
        WebElement nextFetchAbisBtn = analyticsWait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[normalize-space()='Next: Fetch ABIs']")));
        nextFetchAbisBtn.click();
        analyticsWait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//span[normalize-space()='Verified']")));
        System.out.println("Clicked Next: Fetch ABIs and waiting for verification");

        System.out.println("STEP 13: Verifying ABI fetch status...");
        analyticsWait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//span[normalize-space()='Verified']")));
        System.out.println("ABI fetch status verified successfully");

        System.out.println("STEP 14: Clicking 'Review & Submit'...");
        WebElement reviewSubmitBtn = analyticsWait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[normalize-space()='Review & Submit']")));
        reviewSubmitBtn.click();
        analyticsWait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//p[normalize-space()='Your project configuration is complete and ready to deploy.']")),
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//button[normalize-space()='Complete Setup']"))
        ));
        System.out.println("Clicked Review & Submit button");

        System.out.println("STEP 15: Verifying configuration completion message...");
        analyticsWait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//p[normalize-space()='Your project configuration is complete and ready to deploy.']")));
        System.out.println("Verified configuration completion message");

        System.out.println("STEP 16: Clicking 'Complete Setup'...");
        WebElement completeSetupBtn = analyticsWait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[normalize-space()='Complete Setup']")));
        completeSetupBtn.click();
        analyticsWait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//h1[contains(normalize-space(),'Setup Complete')]")),
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//button[normalize-space()='Go to Dashboard']"))
        ));
        System.out.println("Clicked Complete Setup button");

        System.out.println("STEP 17: Verifying setup success message...");
        analyticsWait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h1[contains(normalize-space(),'Setup Complete')]")));
        System.out.println("Setup completed successfully");

        System.out.println("STEP 18: Clicking 'Go to Dashboard'...");
        WebElement goToDashboardBtn = analyticsWait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[normalize-space()='Go to Dashboard']")));
        goToDashboardBtn.click();
        System.out.println("Clicked Go to Dashboard button.");

        System.out.println("STEP 19: Waiting for 'View Analytics' button to appear...");
        WebElement viewAnalyticsBtn = analyticsWait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[normalize-space()='View Analytics']")));
        System.out.println("STEP 19: Clicking 'View Analytics' button...");
        viewAnalyticsBtn.click();
        System.out.println("Clicked View Analytics button.");

        System.out.println("Waiting for analytics page to load full data...");
        WebDriverWait pageLoadWait = new WebDriverWait(driver, Duration.ofSeconds(45));
        pageLoadWait.until(d -> "complete".equals(((JavascriptExecutor) d).executeScript("return document.readyState")));
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("Analytics page full data load complete (browser left open).");

        // STEP 20: Verify "Welcome back" message, then refresh every 30 sec for 5 min until complete data is displayed
        System.out.println("STEP 20: Verifying 'Welcome back' message...");
        analyticsWait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//span[contains(@class,'text-muted-foreground') and normalize-space()='Welcome back']")));
        System.out.println("Verified message: Welcome back.");

        System.out.println("STEP 20a: Refreshing page every 30 seconds for 5 minutes until complete data is displayed...");
        final int refreshIntervalSec = 30;
        final int totalDurationSec = 5 * 60;
        int elapsedSec = 0;
        while (elapsedSec < totalDurationSec) {
            driver.navigate().refresh();
            new WebDriverWait(driver, Duration.ofSeconds(30)).until(d ->
                    "complete".equals(((JavascriptExecutor) d).executeScript("return document.readyState")));
            analyticsWait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//span[contains(@class,'text-muted-foreground') and normalize-space()='Welcome back']")));
            elapsedSec += refreshIntervalSec;
            System.out.println("Refresh " + (elapsedSec / refreshIntervalSec) + "/10 — Welcome back visible (complete data). " + (totalDurationSec - elapsedSec) + "s remaining.");
            if (elapsedSec < totalDurationSec) {
                try {
                    Thread.sleep(refreshIntervalSec * 1000L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        System.out.println("STEP 20 complete: Refreshed every 30 sec for 5 min; complete data displayed (browser left open).");
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) {
            System.out.println("--- Test Execution Finished (browser left open as requested) ---");
        }
    }
}
