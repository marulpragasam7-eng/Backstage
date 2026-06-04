package steps;

import core.ConfigLoader;
import core.PlaywrightManager;
import core.Config;
import pages.LoginPage;
import pages.PricebookPage;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;

public class LoginSteps {
    private final LoginPage loginPage;
    private final PricebookPage pb; // Marked final to ensure initialization

    // Load config once
    private static Config currentConfig = ConfigLoader.load();

    public LoginSteps() {
        // Fetch the active page from the thread-local manager
        var activePage = PlaywrightManager.page();

        // FIX: Initialize BOTH page objects
        this.loginPage = new LoginPage(activePage);
        this.pb = new PricebookPage(activePage);
    }

    @Given("I am on the Backstage Stage login page")
    public void i_am_on_login_page() {
        // Pass the baseUrl from your currentConfig record
        loginPage.navigateToLogin(currentConfig.baseUrl());
    }

    @When("I login as {string} with password {string}")
    public void i_login_with_credentials(String email, String password) throws Exception {
        // Fallback to config if feature file strings are empty
        String actualEmail = (email == null || email.isBlank()) ? currentConfig.email() : email;
        String actualPassword = (password == null || password.isBlank()) ? currentConfig.password() : password;

        // Now 'pb' is initialized and won't throw NullPointerException
        pb.openLogin(currentConfig.baseUrl());
        pb.login(actualEmail, actualPassword);
    }

    public static void setConfig(Config cfg) {
        currentConfig = cfg;
    }
}