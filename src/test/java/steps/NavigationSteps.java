package steps;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pages.EntryDoorManagementPage;
import pages.ManageRSuitePage;
import pages.WholesaleUploadPage;
import core.PlaywrightManager;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;

public class NavigationSteps {
    private final ManageRSuitePage manageRSuitePage;
    private final EntryDoorManagementPage entryDoorManagementPage;
    private WholesaleUploadPage wholesaleUploadPage;
    private Page uploadTab; // Holds the reference to the new tab

    private static final Logger log = LoggerFactory.getLogger(NavigationSteps.class);

    public NavigationSteps() {
        this.manageRSuitePage = new ManageRSuitePage(PlaywrightManager.page());
        this.entryDoorManagementPage = new EntryDoorManagementPage(PlaywrightManager.page());
    }

    @And("I open Manage rSuite")
    public void i_open_manage_rsuite() {
        manageRSuitePage.openManageRSuite();
    }

    @And("I go to Entry Door Management")
    public void i_go_to_entry_door_management() {
        entryDoorManagementPage.goToEntryDoorManagement();
    }

    @And("I open Wholesale Upload")
    public void i_open_wholesale_upload() {
        log.info("Clicking link and waiting for popup tab...");
        try {
            // This captures the specific tab that opens when the link is clicked
            this.uploadTab = PlaywrightManager.page().waitForPopup(() -> {
                PlaywrightManager.page().locator("a:has-text('Wholesale Upload')").click();
            });
            log.info("Successfully switched context to the Upload Tab.");
        } catch (PlaywrightException e) {
            log.warn("Popup didn't open within timeout, falling back to main page context.");
            this.uploadTab = PlaywrightManager.page();
        }
    }

    @And("I import the wholesale pricebook from {string}")
    public void i_import_the_wholesale_pricebook(String path) {
        log.info("Starting upload process on the target tab...");
        // Initialize the page object using the captured tab, not the main page
        this.wholesaleUploadPage = new WholesaleUploadPage(uploadTab);
        wholesaleUploadPage.uploadPricebook(path);

    }

//    @Then("I verify the wholesale pricebook is processed for {string}")
//    public void i_verify_the_wholesale_pricebook_is_processed(String fullStatusMessage) {
//        // "fullStatusMessage" would be: "Backstage PCS Entry Door Pricing (1.12.2022) Successfully Processed"
//        log.info("Starting final verification for: " + fullStatusMessage);
//        wholesaleUploadPage.verifyCompleteProcessingCycle(fullStatusMessage);
//        log.info("Pricebook upload and processing verified successfully.");
//    }
@And("I verify Successfully Uploaded for Processing {string}")
public void i_verify_successfully_uploaded_for_processing(String message) {
    // Calling the logic you already defined in WholesaleUploadPage
    wholesaleUploadPage.verifyCompleteProcessingCycle(message);
}
    @Then("I verify the wholesale pricebook is processed for {string}")
    public void i_verify_the_wholesale_pricebook_is_processed(String filePath) {
        // We pass the filePath so the Page Object can extract the prefix dynamically
        wholesaleUploadPage.verifyFinalStatusFromList(filePath);
    }
}