
package pages;

import com.microsoft.playwright.*;
import com.microsoft.playwright.assertions.LocatorAssertions;
import com.microsoft.playwright.options.AriaRole;
import core.WaitUtils;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * ManageRSuitePage
 * ----------------
 * Responsibilities:
 *  - Open the "Manage rSuite" area by clicking its button.
 *  - Wait until next actionable navigation (e.g., "Entry Door Management") is visible.
 */
public class ManageRSuitePage {
    private final Page page;

    public ManageRSuitePage(Page page) {
        this.page = page;
    }

    public void openManageRSuite() {
        Locator manageBtn = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Manage rSuite"));
        WaitUtils.visible(manageBtn, 30000);

        assertThat(manageBtn).isVisible(new LocatorAssertions.IsVisibleOptions().setTimeout(10000));        manageBtn.click();

        // Confirm landing into Manage rSuite
        Locator entryDoorLink = page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Entry Door Management"));
        assertThat(entryDoorLink).isVisible();
    }
}
