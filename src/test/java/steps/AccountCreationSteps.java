
package steps;

import com.microsoft.playwright.Page;
import core.Config;
import core.ConfigLoader;
import core.PlaywrightManager;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.*;
import org.junit.jupiter.api.Assertions;
import pages.RegistrationPage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class AccountCreationSteps {
  private final Config cfg = ConfigLoader.load();
  private Page page;
  private RegistrationPage reg;

  @Given("I am on the login page")
  public void i_am_on_the_login_page() {
    page = PlaywrightManager.page();
    reg = new RegistrationPage(page);
    reg.openLogin(cfg.baseUrl());
  }

  @When("I create a new account with email {string} and password {string}")
  public void i_create_new_account_with_email_and_password(String email, String password, DataTable table) {
    Map<String, String> data = table.asMap(String.class, String.class);
    String first = data.getOrDefault("first", "Test");
    String last = data.getOrDefault("last", "User");
    String phone = data.getOrDefault("phone", "1234567891");
    String store = data.getOrDefault("store", "Corporate");
    String role = data.getOrDefault("role", "Back Office");

    String finalEmail = email;
    if ("RANDOM".equalsIgnoreCase(email)) {
      String stamp = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now());
      finalEmail = "automation+" + stamp + "@example.com"; // TODO: replace with a test inbox domain
    }

    reg.clickCreateOne();
    reg.enterEmailAndNext(finalEmail);
    reg.setPasswordAndNext(password);
    reg.fillProfileAndNext(first, last, phone);
    reg.selectStore(store);
    reg.selectFunctionalRole(role);
    reg.submitCreateAccount();
  }

  @Then("I should see the account verification message")
  public void i_should_see_the_account_verification_message() {
    Assertions.assertTrue(reg.isThankYouVisibleStrict(),
        "Expected account verification thank-you message was not visible or not strict-match.");
  }
}
