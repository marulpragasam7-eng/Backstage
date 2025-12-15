
package pages;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class NegativeLoginPage {
    private final Page page;

    // Locators based on your snippet (ARIA roles with names)
    private final Locator emailInput;
    private final Locator passwordInput;
    private final Locator loginButton;

    // Error messages (adjust selectors to your DOM if needed)
    private final Locator emailFieldError;     // validation next to email
    private final Locator passwordFieldError;  // validation next to password
    private final Locator pageError;           // banner/summary level error

    public NegativeLoginPage(Page page) {
        this.page = page;
        this.emailInput = page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Email"));
        this.passwordInput = page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Password"));
        this.loginButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Login"));

        // Prefer role/aria-based locators; fall back to data-testid if available
        // Replace with actual selectors if your app uses specific markup:
        this.emailFieldError = page.locator("text='The Email field is required.'");
        this.passwordFieldError = page.locator("text='The Password field is required.'");
        this.pageError = page.locator("text='Invalid account email or password.'");
    }

    public void open(String baseUrl) {
        //page.navigate(baseUrl + "/Site/Login?status=NotLoggedIn");
        page.navigate(baseUrl);
        // Ensure we’re on the right page
        assertThat(loginButton).isVisible();
    }

    public void login(String email, String password) {
        emailInput.fill(""); // ensure clean
        passwordInput.fill("");
        if (email != null) emailInput.fill(email);
        if (password != null) passwordInput.fill(password);
        loginButton.click();
    }

    public Locator emailInput() {
        return emailInput;
    }
    public Locator passwordInput() {
        return passwordInput;
    }

    public Locator emailFieldError() {
        return emailFieldError;
    }
    public Locator passwordFieldError() {
        return passwordFieldError;
    }
    public Locator pageError() {
        return pageError;
    }

    public String getAllVisibleErrorsText() {
        return page.locator(".validation-summary-errors, .field-validation-error, [role='alert']")
                .allInnerTexts()
                .toString();
    }
}
