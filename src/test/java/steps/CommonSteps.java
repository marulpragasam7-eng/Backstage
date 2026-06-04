package steps;

import core.ConfigLoader;
import core.PlaywrightManager;
import io.cucumber.java.en.Given;
import pages.LoginPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class CommonSteps {
    private final LoginPage loginPage = new LoginPage(PlaywrightManager.page());
    private static final Logger log = LoggerFactory.getLogger(LoginPage.class);

    @Given("I am logged into rSuite")
    public void i_am_logged_into_rsuite() {
        loginPage.navigateToLogin(ConfigLoader.load().baseUrl());
        loginPage.login(ConfigLoader.load().email(), ConfigLoader.load().password());
    }

    @Given("I navigate to the rSuite Dashboard")
    public void navigateToDashboard() {
        String url = core.ConfigLoader.load().baseUrl();
        PlaywrightManager.page().navigate(url);

        // Verification: Ensure we see the Manage dropdown
        boolean isDashboardVisible = PlaywrightManager.page().isVisible("#navbarDropdown:has-text('Manage rSuite')");

        if (!isDashboardVisible) {
            // If not visible, we might have been logged out or redirected
            log.warn("Dashboard not detected, attempting to re-login...");
            i_am_logged_into_rsuite();
        }
    }
}