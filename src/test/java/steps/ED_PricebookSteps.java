package steps;

import core.PlaywrightManager;
import io.cucumber.java.en.*;
import pages.EntryDoorPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ED_PricebookSteps {
    private static final Logger log = LoggerFactory.getLogger(ED_PricebookSteps.class);
    private final EntryDoorPage entryDoorPage = new EntryDoorPage(PlaywrightManager.page());
    private static String dynamicPBName; // Static to persist name across scenario steps

    @Given("I am on the Entry Door Pricebooks page")
    public void navigateToEDPricebooks() {
        entryDoorPage.navigateToPricebooks();
    }

    @When("I create a new pricebook with a unique name for {string}")
    public void createUniquePricebook(String entity) {
        // Generates a name like Automation_20260105_1520
        dynamicPBName = "Automation_" + new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date());
        log.info("Step: Creating pricebook with dynamic name: {}", dynamicPBName);
        entryDoorPage.createPricebook(dynamicPBName, entity);
    }

    @When("I move the pricebook to {string} status for user {string}")
    public void moveToStaged(String status, String user) {
        log.info("Step: Moving '{}' to status '{}' for user '{}'", dynamicPBName, status, user);
        entryDoorPage.changeStatus(dynamicPBName, status, user);
    }

    @Then("I verify the pricebook is visible under the {string} section")
    public void verifySection(String sectionName) throws InterruptedException {
        log.info("Step: Verifying '{}' in section '{}'", dynamicPBName, sectionName);
        entryDoorPage.verifySectionVisibility(dynamicPBName, sectionName, "");
    }

    @When("I move the pricebook to {string} status")
    public void moveToLive(String status) {
        log.info("Step: Moving '{}' to status '{}'", dynamicPBName, status);
        entryDoorPage.changeStatus(dynamicPBName, status, null);
    }
}