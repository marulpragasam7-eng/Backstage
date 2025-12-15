package pages;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import core.PlaywrightManager;
import core.WaitUtils;
import core.RetryUtils;
import core.Config;
import core.ConfigLoader;

public class ForgotPasswordPage extends BasePage {
  private final Config cfg = ConfigLoader.load();

  public ForgotPasswordPage(Page page) { super(page); }

  public void openLogin(String baseUrl) {
    String url = baseUrl.endsWith("/") ? baseUrl + "Site/Login?status=NotLoggedIn" : baseUrl + "/Site/Login?status=NotLoggedIn";
    page.navigate(url);
    PlaywrightManager.waitForNetworkIdle();
  }

  public void goToForgotPassword() {
    // Try several variants: link, button, text, regex (case-insensitive)
    Locator[] candidates = new Locator[] {
            page.getByRole(AriaRole.LINK,   new Page.GetByRoleOptions().setName("Forgot Password?")),
            page.getByRole(AriaRole.LINK,   new Page.GetByRoleOptions().setName("Forgot password")),
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Forgot Password?")),
            page.getByText("Forgot Password?"),
            page.locator("text=/Forgot\\s*(Password|your password)\\??/i")
    };

    boolean clicked = false;
    for (Locator loc : candidates) {
      try {
        WaitUtils.visible(loc.first(), 20000);
        RetryUtils.retryVoid(cfg.actionRetryCount(), cfg.actionRetryDelayMs(), () -> loc.first().click());
        clicked = true;
        break;
      } catch (RuntimeException ignored) { /* try next */ }
    }

    if (!clicked) {
      throw new AssertionError("Could not find 'Forgot Password' link/button.\nURL: " + page.url() + "\nTitle: " + page.title());
    }
  }

  /** <-- This is the method your step calls */
  public void submitEmail(String email) {
    Locator emailBox = page.getByPlaceholder("Email");
    WaitUtils.visible(emailBox, 20000);
    emailBox.fill(email);

    Locator submit = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Submit"));
    RetryUtils.retryVoid(cfg.actionRetryCount(), cfg.actionRetryDelayMs(), submit::click);

    PlaywrightManager.waitForNetworkIdle();
  }

  public boolean isSubmissionThankYouVisible() {
    Locator msg = page.getByText("Thank you for the submission");
    try {
      WaitUtils.visible(msg, 20000);
      return msg.isVisible();
    } catch (PlaywrightException e) {
      return false;
    }
  }
}