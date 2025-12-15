
package steps;

import com.microsoft.playwright.Page;
import core.Config;
import core.ConfigLoader;
import core.PlaywrightManager;
import io.cucumber.java.en.*;
import org.junit.jupiter.api.Assertions;
import pages.ForgotPasswordPage;

public class ForgotPasswordSteps {
  private final Config cfg = ConfigLoader.load();
  private Page page;
  private ForgotPasswordPage fpp;

  @Given("I am on the login page for password reset")
  public void on_login_for_reset() {
    page = PlaywrightManager.page();
    fpp = new ForgotPasswordPage(page);
    fpp.openLogin(cfg.baseUrl());
  }

  @When("I request a password reset for {string}")
  public void request_password_reset(String email) {
    fpp.goToForgotPassword();
    fpp.submitEmail(email);
  }

  @Then("I should see the password reset submission confirmation")
  public void see_reset_confirmation() {
    Assertions.assertTrue(fpp.isSubmissionThankYouVisible(),
        "Expected 'Thank you for the submission' message was not visible.");
  }
}
