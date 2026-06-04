
package pages;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;

public class EntryDoorManagementPage {
    private final Page page;

    public EntryDoorManagementPage(Page page) {
        this.page = page;
    }

    public void goToEntryDoorManagement() {
        //page.click("text=Entry Door Management");
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Entry Door Management")).click();
    }
    public Page openWholesaleUpload() {
        // Use waitForPopup to catch the tab that appears when clicking the link
        return page.waitForPopup(() -> {
            page.locator("a:has-text('Wholesale Upload')").click();
        });
    }
}
