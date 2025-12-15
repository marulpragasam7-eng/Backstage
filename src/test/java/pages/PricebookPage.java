package pages;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import core.Config;
import core.ConfigLoader;
import core.RetryUtils;
import core.WaitUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class PricebookPage extends BasePage {
    private static final Logger log = LoggerFactory.getLogger(PricebookPage.class);
    private final Config cfg = ConfigLoader.load();

    public PricebookPage(Page page) {
        super(page);
    }

    // ---------- Login & Navigate ----------

    public void openLogin(String baseUrl) {
        String url = baseUrl.endsWith("/") ?
                baseUrl + "Site/Login?status=NotLoggedIn" :
                baseUrl + "/Site/Login?status=NotLoggedIn";
        log.info("Navigating to login page: {}", url);
        page.navigate(url);
        WaitUtils.networkIdle(page);
    }

    public void login(String email, String password) throws Exception {
        log.info("Attempting to log in as user '{}'", email);
        Locator emailBox = page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Email"));
        WaitUtils.visible(emailBox, 20000);
        emailBox.fill(email);

        Locator pwdBox = page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Password"));
        pwdBox.fill(password);
        pwdBox.press("Enter");

        Locator manageBtn = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Manage rSuite"));
        WaitUtils.visible(manageBtn, 30000);
        log.info("Login successful. 'Manage rSuite' button is visible.");
    }

    public void openPricebookLanding() {
        String base = cfg.baseUrl().endsWith("/") ? cfg.baseUrl() : cfg.baseUrl() + "/";
        String url = base + "Pricebook?lp=0&sp=0&ep=0&ap=0";
        log.info("Navigating to Pricebook landing page: {}", url);
        page.navigate(url);
        WaitUtils.networkIdle(page);
    }

    // ---------- Create New Pricebook (Editable) ----------

    public void clickCreateNewPricebook() {
        log.info("Checking for and handling potential 'Let's Begin' popup overlay...");
        Locator letsBeginButton = page.getByText("Let's Begin");

        try {
            // Check if the button is present and visible within a short timeout (e.g., 5 seconds)
            // If it is visible, it means the Whatfix overlay is active and must be clicked to dismiss it.
            if (letsBeginButton.isVisible(new Locator.IsVisibleOptions().setTimeout(5000))) {
                log.info("Whatfix 'Let's Begin' button found, clicking to dismiss/start the guide.");
                letsBeginButton.click();
                // Wait for the overlay to fully disappear and for the network to stabilize after the click
                WaitUtils.networkIdle(page);
                log.info("'Let's Begin' button clicked successfully.");
            }
        } catch (PlaywrightException e) {
            // This is acceptable, the popup might not always be present.
            log.debug("Whatfix 'Let's Begin' button not found or already dismissed. Proceeding with main action.");
        }
        log.info("Clicking the '+ create new pricebook' link...");
        Locator createLink = page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("+ create new pricebook"));
        WaitUtils.visible(createLink, 20000);
        RetryUtils.retryVoid(cfg.actionRetryCount(), cfg.actionRetryDelayMs(), createLink::click);
    }

    public void fillPricebookName(String name) {
        log.info("Filling pricebook name with: '{}'", name);
        Locator nameBox = page.getByPlaceholder("20XX Pricing");
        WaitUtils.visible(nameBox, 20000);
        nameBox.fill(name);
    }

    public void selectOrgAndSupplier(String orgTextFieldName, String regionQuery, String supplierButtonName) {
        log.info("Selecting organization using query '{}' and supplier '{}'", regionQuery, supplierButtonName);
        // 1) Click the org/location field to activate it
        //Locator orgBox = page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName(orgTextFieldName));
        Locator orgBox=page.locator("//span[@id=\'select2-location-select-container\']");
        WaitUtils.visible(orgBox.first(), 20000);
        orgBox.first().click();
        log.debug("Organization dropdown activated.");

        // 2) Find the search box and type
        Locator s2Search = page.locator("span.select2-container--open input.select2-search__field").last();
        WaitUtils.visible(s2Search, 15000);
        s2Search.fill(regionQuery);
        log.debug("Typed '{}' into the organization search field.", regionQuery);

        // 3) Find and click the correct option
        Locator resultsRoot = page.locator("span.select2-container--open .select2-results").last();
        Locator options = resultsRoot.locator(".select2-results__option:visible");
        WaitUtils.visible(options.first(), 10000);
        Locator corporate = options.filter(new Locator.FilterOptions().setHasText("Corporate Corporate"));
        if (corporate.count() > 0 && corporate.first().isVisible()) {
            log.debug("Found and clicked 'Corporate Corporate' option.");
            corporate.first().click();
        } else {
            log.warn("Could not find 'Corporate Corporate' option, falling back to first visible option.");
            options.first().click();
        }

        // 4) Click the Supplier button
        Locator supplierBtn = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(supplierButtonName));
        WaitUtils.visible(supplierBtn, 15000);
        RetryUtils.retryVoid(cfg.actionRetryCount(), cfg.actionRetryDelayMs(), supplierBtn::click);
        log.info("Organization and supplier selection complete.");
    }

    public void clickCreate() {
        log.info("Clicking the 'CREATE' button to finalize pricebook creation...");
        Locator createBtn = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("CREATE"));
        WaitUtils.visible(createBtn, 20000);
        RetryUtils.retryVoid(cfg.actionRetryCount(), cfg.actionRetryDelayMs(), createBtn::click);
        WaitUtils.networkIdle(page);

        // Confirm "Saved!" toast
        try {
            Locator saved = page.getByText("Saved!");
            WaitUtils.visible(saved, 10000);
            saved.click();
            log.info("Confirmed 'Saved!' toast message.");
        } catch (Throwable ignored) {
            log.warn("Did not find 'Saved!' toast message after creation.");
        }

        // Click alert (if any)
        try {
            page.getByRole(AriaRole.ALERT).click(new Locator.ClickOptions().setTrial(true).setTimeout(2000));
            page.getByRole(AriaRole.ALERT).click();
            log.debug("Clicked a post-creation alert.");
        } catch (Throwable ignored) {}
    }

    // ---------- Set Pricebook Parameters ----------

    public void setUniversalMarkup(String value) {
        log.debug("Setting Universal Margin/Markup to: {}", value);
        Locator fld = page.getByLabel("Universal Margin/Markup");
        WaitUtils.visible(fld, 15000);
        fld.fill(value);
    }

    public void setInstallation(String value) {
        log.debug("Setting Installation to: {}", value);
        Locator fld = page.getByLabel("Installation");
        WaitUtils.visible(fld, 15000);
        fld.fill(value);
    }

    public void setLaborExact(String value) {
        log.debug("Setting Labor to: {}", value);
        Locator fld = page.getByLabel("Labor", new Page.GetByLabelOptions().setExact(true));
        WaitUtils.visible(fld, 15000);
        fld.fill(value);
        fld.press("Enter");
    }

    public void setGrossToNetPercent(String value) {
        log.debug("Setting Gross To Net % to: {}", value);
        Locator fld = page.getByLabel("Gross To Net %");
        WaitUtils.visible(fld, 15000);
        fld.fill(value);
        fld.press("Enter");
    }

    public void setGrossToNetDollar(String value) {
        log.debug("Setting Gross To Net $ to: {}", value);
        Locator fld = page.getByLabel("Gross To Net $");
        WaitUtils.visible(fld, 15000);
        fld.fill(value);
        fld.press("Enter");
    }

    public void setSalesTaxRecovery(String value) {
        log.debug("Setting Sales Tax Recovery to: {}", value);
        Locator fld = page.getByLabel("Sales Tax Recovery");
        WaitUtils.visible(fld, 15000);
        fld.fill(value);
        fld.press("Enter");
    }

    // ---------- Change to Staged & Add User ----------

    public void changeStatusToStaged() {
        log.info("Changing pricebook status from 'Editable' to 'Staged'.");
        Locator statusBox = page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Editable"));
        WaitUtils.visible(statusBox, 15000);
        statusBox.click();
        Locator staged = page.getByRole(AriaRole.TREEITEM, new Page.GetByRoleOptions().setName("Staged Not editable."));
        WaitUtils.visible(staged, 15000);
        staged.click();
    }

//    public void addStagedUser(String userQuery, String exactUserToPick) {
//        log.info("Adding user '{}' to staged pricebook.", exactUserToPick);
//        Locator users = page.getByPlaceholder("Select users");
//        WaitUtils.visible(users, 15000);
//        users.click();
//        users.fill(userQuery);
//
//        // --- ROBUST FIX: Wait for the search results container to appear ---
//        log.debug("Waiting for user search results to load...");
//        Locator resultsContainer = page.locator("span.select2-container--open .select2-results__options");
//        WaitUtils.visible(resultsContainer, 15000);
//
//        // Now that the results are loaded, find and click the specific user
//        Locator pick = page.getByRole(AriaRole.TREEITEM, new Page.GetByRoleOptions().setName(exactUserToPick));
//        WaitUtils.visible(pick, 15000);
//        pick.click();
//    }
public void openManage() {
    log.info("Opening the 'manage' panel for the pricebook.");
    Locator manage = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("manage").setExact(true));
    WaitUtils.visible(manage, 15000);
    RetryUtils.retryVoid(cfg.actionRetryCount(), cfg.actionRetryDelayMs(), manage::click);
}
public void navigateToEntryDoorPricebooks() {
    log.info("Navigating to 'Entry Door Management'.");
    // Click the main menu link/button for Entry Door Management
    Locator entryDoorManagement = page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Entry Door Management"));
    WaitUtils.visible(entryDoorManagement, 15000);
    RetryUtils.retryVoid(cfg.actionRetryCount(), cfg.actionRetryDelayMs(), entryDoorManagement::click);

    // Click the specific link for Entry Door Pricebooks
    Locator entryDoorPricebooks = page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Entry Door Pricebooks"));
    WaitUtils.visible(entryDoorPricebooks, 15000);
    RetryUtils.retryVoid(cfg.actionRetryCount(), cfg.actionRetryDelayMs(), entryDoorPricebooks::click);
    WaitUtils.networkIdle(page);
    log.info("Arrived at Entry Door Pricebooks landing page.");
}

    public void clickCreateNewEntryDoorPricebook() {
        // Reusing the general "click create" logic but targeting the Entry Door link.
        // The locator text is the same as the standard pricebook.
        log.info("Clicking the '+ create new pricebook' link on Entry Door page...");
        Locator createLink = page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("+ create new pricebook"));
        WaitUtils.visible(createLink, 20000);
        RetryUtils.retryVoid(cfg.actionRetryCount(), cfg.actionRetryDelayMs(), createLink::click);
    }

    public void fillEntryDoorPricebookName(String name) {
        log.info("Filling Entry Door pricebook name with: '{}'", name);
        Locator nameBox = page.getByPlaceholder("20XX Pricing");
        WaitUtils.visible(nameBox, 20000);
        nameBox.fill(name);
    }

    public void selectEntryDoorSupplier(String supplierName) {
        log.info("Selecting Entry Door supplier: '{}'", supplierName);
        // Click the supplier field
        Locator supplierBox = page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName(supplierName));
        WaitUtils.visible(supplierBox, 15000);
        supplierBox.click();

        // The provided recording shows selecting the textbox, then pressing Enter (or clicking the supplier name).
        // We'll rely on the locator name matching the supplier's name to confirm selection.
        // Since the supplier is visible and clickable via the textbox role, we are done after the click.
        log.info("Entry Door supplier selection complete.");
    }

// NOTE: The 'selectOrgAndSupplier' method you already have for standard pricebooks
// is generic enough to be reused for the organization selection, as it uses text box names.
// public void selectOrgAndSupplier(String orgTextFieldName, String regionQuery, String supplierButtonName) { ... }


// --- Entry Door Staging Methods ---

    public void stageEntryDoorPricebook(String user1, String user2) {
        log.info("Staging Entry Door pricebook and adding users: {}, {}", user1, user2);
    }
    public void addStagedUser(String userQuery, String exactUserToPick) {
        // NOTE: The 'expectedUserName' variable is redundant if you use 'exactUserToPick'
        // String expectedUserName = "Arul Pragasam";

        log.info("Adding user '{}' to staged pricebook. Querying with: '{}'", exactUserToPick, userQuery);

        // 1. Define the locator for the search box
        Locator usersSearchBox = page.getByPlaceholder("Select users");

        // 2. Define the locator for the user list item that appears after typing
        // FIX: This line now correctly passes the exactUserToPick variable to String.format()
        Locator userListItemLocator = page.locator(String.format("//li[@title='%s']", exactUserToPick));

        // Assert the search box is visible
        assertThat(usersSearchBox).isVisible();
        WaitUtils.visible(usersSearchBox, 15000); // Wait for the element to be ready

        // 3. Click the box and type the query
        usersSearchBox.click();
        usersSearchBox.fill(userQuery);

        // 4. Assert and click the resulting user list item
        log.debug("Waiting for the user '{}' to appear in search results...", exactUserToPick);

        // Assert the specific user list item is visible and click it.
        // Playwright's assertion handles the waiting (up to default 5s or a custom timeout if set).
        //assertThat(userListItemLocator).isVisible(new LocatorAssertions.IsVisibleOptions().setTimeout(20000));
        page.keyboard().press("Enter");
        // Clicked: Click the list item to select the user
        userListItemLocator.click();

        // The previous incorrect locator 'pick' has been removed/replaced
        // Locator pick=  page.locator("//form[@id='js-form']"); // <-- Not the correct element to click
    }

    public void clickSave() {
        log.info("Clicking the 'SAVE' button to commit staged changes.");
        Locator saveBtn = page.locator("//button[contains(@class, 'staging-submit')]");
        WaitUtils.visible(saveBtn, 15000);
        saveBtn.click();
        WaitUtils.networkIdle(page);
    }

    public void verifyStagedPricebookBanner() {
        // Define the exact text you expect to find
        String expectedText = "THIS PRICEBOOK IS STAGED AND CANNOT BE EDITED";

        // Locate the element by its text content
        Locator stagedBanner = page.locator("//div[@class ='locked-config-info-bar']");

        // Assert that the element has the exact text.
        // This will wait automatically for the element to appear.
        assertThat(stagedBanner).hasText(expectedText);
        log.info("Successfully verified the staged pricebook banner.");
    }



    /**
     * Clicks the main 'Manage rSuite' button/link visible on the home page/dashboard.
     * This is the entry point for accessing different management areas like Pricebook or Entry Door.
     */
    public void navigateToManageRSuite() {
        log.info("Clicking the 'Manage rSuite' button/link.");
        Locator manageBtn = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Manage rSuite"));
        WaitUtils.visible(manageBtn, 20000);
        // Use RetryUtils for robustness
        RetryUtils.retryVoid(cfg.actionRetryCount(), cfg.actionRetryDelayMs(), manageBtn::click);
        WaitUtils.networkIdle(page);
        log.info("Successfully navigated to Manage rSuite dashboard.");
    }
}

