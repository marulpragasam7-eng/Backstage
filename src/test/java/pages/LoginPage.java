package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;

public class LoginPage {
    private final Page page;
    private static final Logger log = LoggerFactory.getLogger(LoginPage.class);

    // Precise selectors based on your HTML
    private final String emailInput = "#Email";
    private final String passwordInput = "#Password";
    private final String loginBtn = "button[type='submit']";

    public LoginPage(Page page) {
        this.page = page;
    }

    public void navigateToLogin(String url) {
        log.info("Navigating to: {}", url);
        page.navigate(url);
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }
    public void login(String email, String password) {
        try {
            log.info("Searching for login fields (including iframes)...");

            // This selector finds the email field even if it is inside an iframe
            Locator emailField = page.locator("#Email");

            // Wait for it to exist anywhere on the page/frames
            emailField.waitFor(new Locator.WaitForOptions().setTimeout(30000));

            // Fill the fields
            emailField.fill(email);
            page.locator("#Password").fill(password);

            log.info("Fields filled. Clicking login...");
            page.locator("button[type='submit']").click();

            // Short wait to allow the click to register
            page.waitForTimeout(3000);

        } catch (Exception e) {
            // This is critical: Take the screenshot!
            page.screenshot(new Page.ScreenshotOptions().setPath(java.nio.file.Paths.get("target/login_debug.png")));

            // Print the page title to the console to see where we actually are
            System.out.println("CRITICAL DEBUG - Page Title: " + page.title());
            System.out.println("CRITICAL DEBUG - Current URL: " + page.url());

            throw new RuntimeException("Could not find login fields. View target/login_debug.png to see what the browser sees.");
        }
    }
//    public void login(String email, String password) {
//        try {
//            // 1. Ensure fields are visible before typing
//            page.locator(emailInput).waitFor();
//
//            // 2. Perform Login Actions
//            page.fill(emailInput, email);
//            page.fill(passwordInput, password);
//
//            log.info("Clicking login button...");
//            page.click(loginBtn);
//
//            // 3. Short wait for the post-login redirect to begin
//            // We use 5 seconds instead of 20 to keep the test moving
//            page.waitForTimeout(5000);
//
//            log.info("Login actions completed. Proceeding to next step.");
//
//        } catch (Exception e) {
//            page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("target/login_action_failed.png")));
//            log.error("Failed to interact with login fields: {}", e.getMessage());
//            throw new RuntimeException("Login interaction failed. Check target/login_action_failed.png");
//        }
//    }
}