
package steps;

import com.microsoft.playwright.Page;
import io.cucumber.java.en.*;
import pages.NegativeLoginPage;
import core.Config;
import core.ConfigLoader;
import core.PlaywrightManager;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class NegativeLoginSteps {

    private Page page;
    private NegativeLoginPage negativeLoginPage;
    private final Config cfg = ConfigLoader.load(); // Same config as Hooks

    @Given("I am on the RSuite Backstage login page")
    public void i_am_on_the_r_suite_backstage_login_page() {
        this.page = PlaywrightManager.page();
        this.negativeLoginPage = new NegativeLoginPage(page);
        negativeLoginPage.open(cfg.baseUrl());
    }


    @When("I attempt to login with email {string} and password {string}")
    public void i_attempt_to_login_with_email_and_password(String email, String password) {
        negativeLoginPage.login(email, password);
    }

   @Then("I should see field error {string}")
    public void i_should_see_field_error(String expected) {
        assertThat(page.getByText(expected)).isVisible();
    }

    @Then("I should see page error {string}")
    public void i_should_see_page_error(String expected) {
        assertThat(page.getByText(expected)).isVisible();
    }

    @Then("I should not see any technical error details on the page")
    public void i_should_not_see_any_technical_error_details_on_the_page() {
        String allText = negativeLoginPage.getAllVisibleErrorsText().toLowerCase();
        if (allText.contains("stack") ||
                allText.contains("trace") ||
                allText.contains("exception") ||
                allText.contains("sql ") ||
                allText.contains("nullpointer")) {
            throw new AssertionError("Technical details leaked on page: " + allText);
        }
    }
}
