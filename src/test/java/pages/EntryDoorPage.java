package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

public class EntryDoorPage {
    private static final Logger log = LoggerFactory.getLogger(EntryDoorPage.class);
    private final Page page;

    // --- 1. SEPARATED LOCATORS (String-based or private methods) ---
    private final String LIST_CONTAINER = ".staging-list-container";
    private final String STATUS_INPUT = "input[readonly].form-control";
    private final String BADGE_SPAN = "span.badge";
    private final String PROCESSING_BANNER = "span.float-right";
    private final int UI_TIMEOUT = 10000;

    // --- 2. DYNAMIC LOCATOR FACTORIES ---
    // These methods return locators that depend on dynamic text
    private Locator getSectionHeader(String sectionName) {
        Pattern pattern = Pattern.compile(Pattern.quote(sectionName), Pattern.CASE_INSENSITIVE);
        return page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName(pattern));
    }

    private Locator getPricebookInContainer(String pbName, String containerName) {
        return page.locator(LIST_CONTAINER)
                .filter(new Locator.FilterOptions().setHasText(containerName))
                .getByText(pbName, new Locator.GetByTextOptions().setExact(true));
    }

    private Locator getManageButton(String pbName) {

        return page.locator(".staging-manage-container")
                .filter(new Locator.FilterOptions().setHasText(pbName))
                .locator("button.js-staging-manage")
                .getByText("manage", new Locator.GetByTextOptions().setExact(true))
                .first();
    }

    // --- 3. CONSTRUCTOR ---
    public EntryDoorPage(Page page) {
        this.page = page;
    }

    // --- 4. ACTION METHODS (Now much cleaner) ---

    public void navigateToPricebooks() {
        log.info("Navigating to Entry Door Pricebooks page...");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Manage rSuite")).click();
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Entry Door Management")).click();
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Entry Door Pricebooks")).click();
    }

    public void createPricebook(String name, String entitySearch) {
        log.info("Creating new pricebook: '{}'", name);
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("+ create new pricebook")).click();
        page.getByPlaceholder("20XX Pricing").fill(name);

        page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("RbA of Anchorage, AK")).click();
        page.getByRole(AriaRole.SEARCHBOX).fill(entitySearch);
        page.getByRole(AriaRole.SEARCHBOX).press("Enter");

        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("CREATE")).click();
        handleToastNotification("Created!");
    }

    public void changeStatus(String pbName, String targetStatus, String user) {
        log.info("Initiating status change for '{}' to '{}'", pbName, targetStatus);

        getManageButton(pbName).click();

        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("SAVE")).waitFor();

        page.locator("#select2-status-select-container").click();
        page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName(targetStatus)).click();

        if (user != null && !user.isEmpty()) {
            Locator userPicker = page.getByPlaceholder("Select users");
            userPicker.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
            userPicker.fill(user);
            page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName(user)).click();
        }

        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("SAVE")).click();
        handleToastNotification("Saved!");
        verifyBackgroundProcessing(targetStatus);
    }
    public void performSearch(String pbName, Locator scope) {
        Locator searchInput = (scope != null)
                ? scope.getByPlaceholder("Search by Pricebook Name")
                : page.getByPlaceholder("Search by Pricebook Name");

        searchInput.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

        // Ensure input is cleared properly before filling
        searchInput.click();
        searchInput.fill(""); // Faster than Ctrl+A + Backspace
        searchInput.fill(pbName);
        page.keyboard().press("Enter");
    }
    public void verifySectionVisibility(String pbName, String sectionName, String fallbackSection) {
        log.info("Starting verification for Pricebook: {} in Section: {}", pbName, sectionName);

        boolean found = false;
        int maxRetries = 10;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            log.info("Attempt {} to find Pricebook '{}'...", attempt, pbName);

            // 1. Refresh and Navigate
            navigateToPricebooks();

            // 2. Identify Section and Header
            Locator sectionHeader = page.locator("#staged-section .collapsible-section-header");
            sectionHeader.waitFor(new Locator.WaitForOptions().setTimeout(20000));

            // 3. Smart Expand: Click only if currently collapsed
            String isExpanded = sectionHeader.getAttribute("aria-expanded");
            if (isExpanded == null || isExpanded.equalsIgnoreCase("false")) {
                log.info("Section is collapsed. Clicking to expand...");
                sectionHeader.click();
                // Wait for a short moment for the animation to start
                page.waitForTimeout(1000);
            }

            // 4. Search for the Pricebook Name anywhere within the staged section
            // We use a locator that looks for any 'p' tag containing the text
            Locator pricebookEntry = page.locator("#staged-section p")
                    .filter(new Locator.FilterOptions().setHasText(pbName));

            // 5. Check if it exists and is visible
            if (pricebookEntry.isVisible()) {
                log.info("SUCCESS: Pricebook '{}' found and visible on attempt {}.", pbName, attempt);
                pricebookEntry.scrollIntoViewIfNeeded();
                found = true;

                break;
            }

            log.warn("Pricebook '{}' not visible yet. Waiting 3s before next attempt...", pbName);
            page.waitForTimeout(3000);
        }

        if (!found) {
            log.error("FAIL: Pricebook '{}' not found in Staged section after {} attempts.", pbName, maxRetries);
            throw new RuntimeException("Pricebook verification failed.");
        }
    }
    public void changetoLive(){


    }
    private void verifyBackgroundProcessing(String targetStatus) {
        String procText = "PROCESSING PRICEBOOK MOVE TO " + targetStatus.toUpperCase();
        Locator banner = page.locator(PROCESSING_BANNER).filter(new Locator.FilterOptions().setHasText(procText)).first();
        try {
            banner.waitFor(new Locator.WaitForOptions().setTimeout(5000));
        } catch (Exception ignored) {}
    }

    private void handleToastNotification(String text) {
        Locator toast = page.getByText(text);
        toast.waitFor();
        if (page.getByRole(AriaRole.ALERT).isVisible()) {
            page.getByRole(AriaRole.ALERT).click();
        }
    }
}