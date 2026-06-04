package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.LocatorAssertions;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.ElementState;
import org.opentest4j.AssertionFailedError;
import org.slf4j.Logger;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.slf4j.LoggerFactory;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import java.nio.file.Path;
import java.nio.file.Paths;

public class WholesaleUploadPage {
    private final Page page;
    private static final Logger log = LoggerFactory.getLogger(PricebookPage.class);

    // Locators defined at the top for easy maintenance
    private final String BROWSE_BUTTON = "#uploadFile";
    private final String IMPORT_BUTTON_NAME = "Import Wholesale Pricebook";
    private final String CLOSE_BUTTON_NAME = "Close";
    private final String PROCESSING_TEXT = "This process may take several";
    private final String SUCCESS_UPLOAD_TEXT = "Successfully Uploaded for";

    public WholesaleUploadPage(Page page) {
        this.page = page;
    }
    public void uploadPricebook(String relativePath) {
        Path filePath = Paths.get(relativePath).toAbsolutePath();
        String fileName = filePath.getFileName().toString();

        // 1. Upload & Click Import
        page.waitForFileChooser(() -> page.click(BROWSE_BUTTON)).setFiles(filePath);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(IMPORT_BUTTON_NAME)).click();

        // 2. CRITICAL FIX: Wait for the intercepting overlay to disappear
        // The logs show <div id="LoadingProcess"> intercepts pointer events.
        log.info("Waiting for the loading overlay to be hidden...");
        page.locator("#LoadingProcess").waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.HIDDEN)
                .setTimeout(60000)); // Allowing 60s for backend processing

        // 3. ATTACHED CHECK: Wait for the Success popup text to appear
        Locator successMsg = page.getByText("Successfully Uploaded for Processing.");
        successMsg.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

        // 4. ACTION: Click the Close button
        // Now that the overlay is HIDDEN, this click will succeed instantly.
        log.info("Process finished. Clicking Close button.");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Close")).click();

        // 5. CLEANUP: Ensure the modal is gone before proceeding
        successMsg.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
        log.info("Upload and confirmation complete for: {}", fileName);
    }

public void verifyCompleteProcessingCycle(String successText) {
    // 1. Wait for the loading message to disappear (don't assert visibility)
    // We use waitFor with HIDDEN state because it's okay if it's already gone
    page.locator("#loadingMessage").waitFor(new Locator.WaitForOptions()
            .setState(WaitForSelectorState.HIDDEN)
            .setTimeout(60000));

    // 2. Assert the SUCCESS message is visible
    // This is the stable element you actually want to verify
    assertThat(page.getByText(successText)).isVisible(new LocatorAssertions.IsVisibleOptions().setTimeout(10000));

    log.info("Success message verified: {}", successText);
}

    public void verifyFinalStatusFromList(String filePath) {
        // 1. Get the filename to use as a secondary filter
        String fileName = Paths.get(filePath).getFileName().toString().split("\\.")[0];

        // 2. Refresh view: Clear overlays and scroll to bottom
        page.locator("#LoadingProcess").waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
        page.evaluate("window.scrollTo(0, document.body.scrollHeight)");
        page.waitForTimeout(3000); // Wait for list to render

        // 3. Find the NEW row: It must contain our filename AND be "Queued"
        // This prevents picking up old "Successfully Processed" rows from 2022
        Locator targetRow = page.locator("li.list-group-item")
                .filter(new Locator.FilterOptions().setHasText("Queued for Processing"))
                .filter(new Locator.FilterOptions().setHasText(fileName))
                .first();

        // 4. Print the imported file details found in the Queued state
        if (targetRow.count() > 0) {
            String fullRowText = targetRow.innerText().replace("Queued for Processing", "").trim();
            System.out.println("--------------------------------------------------");
            System.out.println("ASSERTION POINT: Found New Upload -> " + fullRowText);
            System.out.println("CURRENT STATUS: Queued for Processing");
            System.out.println("--------------------------------------------------");
            log.info("Monitoring new upload: {}", fullRowText);
        } else {
            throw new AssertionFailedError("FAILED: Could not find a new row in 'Queued' status for: " + fileName);
        }

        // 5. Polling Logic: Monitor this specific row
        int maxAttempts = 30;
        for (int i = 0; i < maxAttempts; i++) {
            String statusText = targetRow.innerText();

            if (statusText.contains("Successfully Processed")) {
                System.out.println("PASS: [" + fileName + "] moved to SUCCESSFULLY PROCESSED.");
                log.info("File {} processed successfully.", fileName);

                // Handle success modal and click status
                if (page.locator("#SuccessModal").isVisible()) { page.keyboard().press("Escape"); }
                targetRow.locator("span.green-on-gray-form-control").click();
                return;
            }
            else if (statusText.contains("Processing Failed")) {
                System.err.println("FAIL: [" + fileName + "] moved to PROCESSING FAILED.");
                throw new AssertionFailedError("Processing Failed for file: " + fileName);
            }
            else {
                // Still Queued
                log.info("Waiting for status change... Attempt {}/{}", i+1, maxAttempts);
                page.waitForTimeout(5000);
            }
        }

        throw new AssertionFailedError("TIMEOUT: File stayed in 'Queued' status for over 2.5 minutes.");
    }
}