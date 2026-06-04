package steps;

import com.microsoft.playwright.Page;
import core.Config;
import core.ConfigLoader;
import core.PlaywrightManager;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.*;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pages.PricebookPage;

import java.util.Map;

public class PricebookSteps {
    // Initialize the logger for this class
    private static final Logger log = LoggerFactory.getLogger(PricebookSteps.class);
    private final Config cfg = ConfigLoader.load();
    private Page page;
    private PricebookPage pb;
    // CONSTRUCTOR: This runs automatically before every scenario
    public PricebookSteps() {
        // Get the existing page from your Manager
        this.page = PlaywrightManager.page();
        // Initialize the Page Object immediately
        this.pb = new PricebookPage(this.page);
    }
//    @Given("I login to rSuite with email {string} and password {string}")
//    public void i_login_to_rsuite(String email, String password) throws Exception {
//        log.info("Step: I login to rSuite with email '{}'", email);
//        //page = PlaywrightManager.page();
//        //pb = new PricebookPage(page);
//        pb.openLogin(cfg.baseUrl());
//        pb.login(email, password);
//    }
//    @Given("I am logged into rSuite")
//    public void i_am_logged_into_rsuite() throws Exception {
//        // Pull from your existing Config object instead of Gherkin parameters
//        String email = cfg.email();
//        String password = cfg.password();
//         log.info("Step: I login to rSuite with email '{}'", email);
//
//        page = PlaywrightManager.page();
//        pb = new PricebookPage(page);
//
//        // Check if we are already logged in to save time
//        if (!page.url().contains("dashboard")) {
//            pb.openLogin(cfg.baseUrl());
//            pb.login(email, password);
//        }
//    }
//    @Given("I navigate to the rSuite Dashboard")
//    public void i_navigate_to_dashboard() {
//        log.info("Navigating to dashboard");
//
//        // Force the browser back to the starting point for every test
//        page.navigate(cfg.baseUrl());
//    }
    @When("I create a new editable pricebook named {string} for org {string} using region query {string} and supplier {string}")
    public void i_create_new_editable_pricebook(String name, String orgFieldName, String regionQuery, String supplierButtonName) {
        log.info("Step: I create a new editable pricebook named '{}'", name);
        pb.openPricebookLanding();
        pb.clickCreateNewPricebook();
        pb.fillPricebookName(name);
        pb.selectOrgAndSupplier(orgFieldName, regionQuery, supplierButtonName);
        pb.clickCreate();
        //pb.openPricebookCardByName(name);
    }

    @And("I set pricebook parameters")
    public void i_set_pricebook_parameters(DataTable table) {
        Map<String, String> m = table.asMap(String.class, String.class);
        log.info("Step: I set pricebook parameters -> {}", m);

        if (m.containsKey("universalMarkup")) pb.setUniversalMarkup(m.get("universalMarkup"));
        if (m.containsKey("installation")) pb.setInstallation(m.get("installation"));
        if (m.containsKey("labor")) pb.setLaborExact(m.get("labor"));
        if (m.containsKey("grossToNetPercent")) pb.setGrossToNetPercent(m.get("grossToNetPercent"));
        if (m.containsKey("grossToNetDollar")) pb.setGrossToNetDollar(m.get("grossToNetDollar"));
        if (m.containsKey("salesTaxRecovery")) pb.setSalesTaxRecovery(m.get("salesTaxRecovery"));
    }

    @And("I stage the pricebook adding user query {string} selecting {string}")
    public void i_stage_the_pricebook_add_user(String userQuery, String exactUser) {
        log.info("Step: I stage the pricebook adding user '{}'", exactUser);
        pb.openManage();
        pb.changeStatusToStaged();
        pb.addStagedUser(userQuery, exactUser);
    }

    @Then("the pricebook should show staged banner")
    public void the_pricebook_should_show_staged_banner() {
        log.info("Step: Then the pricebook should be saved and show staged banner");
        pb.clickSave();
        // Simply call the method. If the banner isn't visible,
        // the assertion inside the method will fail the test.
        pb.verifyStagedPricebookBanner();

        log.info("Assertion Passed: Staged banner was visible as expected.");
    }

}