package pages;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import core.Config;
import core.ConfigLoader;
import core.PlaywrightManager;
import core.WaitUtils;
import core.RetryUtils;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class RegistrationPage extends BasePage {
  private final Config cfg = ConfigLoader.load();

  public RegistrationPage(Page page) {
    super(page);
  }

  public void openLogin(String baseUrl) {
    String url = baseUrl.endsWith("/") ?
            baseUrl + "Site/Login?status=NotLoggedIn" :
            baseUrl + "/Site/Login?status=NotLoggedIn";
    page.navigate(url);
    // Let the page load its resources
    WaitUtils.networkIdle(page);
  }

  public void clickCreateOne() {
    // Try several variants to be resilient on Stage
    Locator[] candidates = new Locator[] {
            page.getByRole(AriaRole.LINK,   new Page.GetByRoleOptions().setName("Create One")),
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Create One")),
            page.getByText("Create One"),
            page.locator("text=/Create\\s*(One|Account)/i")
    };

    boolean clicked = false;
    for (Locator loc : candidates) {
      try {
        WaitUtils.visible(loc.first(), 20000); // 20s to survive Stage latency
        RetryUtils.retryVoid(cfg.actionRetryCount(), cfg.actionRetryDelayMs(), () -> loc.first().click());
        clicked = true;
        break;
      } catch (RuntimeException ignored) {
        // try next fallback
      }
    }
    if (!clicked) {
      throw new AssertionError("Could not find 'Create One' link/button. URL: " + page.url() + " Title: " + page.title());
    }
  }

  /** <-- This is the method your step is calling */
  public void enterEmailAndNext(String email) {
    Locator emailBox = page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Email"));
    WaitUtils.visible(emailBox, 20000);
    emailBox.fill(email);

    Locator next = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Next"));
    RetryUtils.retryVoid(cfg.actionRetryCount(), cfg.actionRetryDelayMs(), () -> next.click());

    PlaywrightManager.waitForNetworkIdle();
  }

  public void setPasswordAndNext(String password) {
    Locator pwd = page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Password"));
    WaitUtils.visible(pwd, 20000);
    pwd.fill(password);

    Locator next = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Next"));
    RetryUtils.retryVoid(cfg.actionRetryCount(), cfg.actionRetryDelayMs(), () -> next.click());

    PlaywrightManager.waitForNetworkIdle();
  }

  public void fillProfileAndNext(String first, String last, String phone) {
    Locator firstName = page.getByPlaceholder("First Name");
    WaitUtils.visible(firstName, 20000);
    firstName.fill(first);

    page.getByPlaceholder("Last Name").fill(last);
    page.getByPlaceholder("(XXX)XXX-XXXX").fill(phone);

    Locator next = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Next"));
    RetryUtils.retryVoid(cfg.actionRetryCount(), cfg.actionRetryDelayMs(), () -> next.click());

    PlaywrightManager.waitForNetworkIdle();
  }

  public void selectStore(String storeName) {
    Locator storeDropdown = page.locator("#select2-store-select-container");
    WaitUtils.visible(storeDropdown, 20000);
    RetryUtils.retryVoid(cfg.actionRetryCount(), cfg.actionRetryDelayMs(), storeDropdown::click);

    Locator option = page.getByRole(AriaRole.TREEITEM, new Page.GetByRoleOptions().setName(storeName));
    WaitUtils.visible(option, 20000);
    RetryUtils.retryVoid(cfg.actionRetryCount(), cfg.actionRetryDelayMs(), option::click);
  }

  public void selectFunctionalRole(String roleName) {
    Locator roleDropdown = page.locator("#select2-FunctionalRole-container");
    WaitUtils.visible(roleDropdown, 20000);
    RetryUtils.retryVoid(cfg.actionRetryCount(), cfg.actionRetryDelayMs(), roleDropdown::click);

    Locator option = page.getByRole(AriaRole.TREEITEM, new Page.GetByRoleOptions().setName(roleName));
    WaitUtils.visible(option, 20000);
    RetryUtils.retryVoid(cfg.actionRetryCount(), cfg.actionRetryDelayMs(), option::click);
  }

  public void submitCreateAccount() {
    Locator createBtn = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("CREATE ACCOUNT"));
    WaitUtils.visible(createBtn, 20000);
    RetryUtils.retryVoid(cfg.actionRetryCount(), cfg.actionRetryDelayMs(), createBtn::click);
    PlaywrightManager.waitForNetworkIdle();
  }

  public boolean isThankYouVisibleStrict() {
    // Strictly assert the exact heading and the exact message container you showed
    Locator heading = page.locator("xpath=//h1[normalize-space()='Thank you!']");
    Locator message = page.locator("xpath=//div[@id='ThankYouScreen']//label[@class='regcommondesc']");
    try {
      // Stage-safe explicit waits
      core.WaitUtils.visible(heading.first(), 30000);
      core.WaitUtils.visible(message.first(), 30000);

      String headingText = heading.first().innerText().trim();
      String msgText     = message.first().innerText().trim();

      // Strict checks
      boolean headingOk = "Thank you!".equals(headingText);
      boolean msgOk     = msgText.startsWith("An account verification email has been sent to");

      boolean ok = headingOk && msgOk;

      if (ok) {
        // Extract the email from the message WITHOUT altering case
        Pattern p = Pattern.compile("^An account verification email has been sent to\\s*([\\w.+\\-]+@[\\w\\-]+(?:\\.[\\w\\-]+)+).*");
        Matcher m = p.matcher(msgText);
        String email = m.find() ? m.group(1) : "<email not found>";

        // Print success line with heading and email
        System.out.printf("Registration success → Heading: '%s' | Email: '%s'%n", headingText, email);
      } else {
        System.out.printf("Strict verification FAILED (URL='%s' Title='%s')%n", page.url(), page.title());
        System.out.printf("Heading='%s' | Message='%s'%n", headingText, msgText);
      }

      return ok;
    } catch (com.microsoft.playwright.PlaywrightException e) {
      System.out.printf("Error verifying thank-you screen (URL='%s' Title='%s'): %s%n",
              page.url(), page.title(), e.getMessage());
      return false;
    }
  }
}