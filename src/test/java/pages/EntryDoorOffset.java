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
import java.util.regex.Pattern;

public class EntryDoorOffset {
    private final Page page;
    private static final Logger log = LoggerFactory.getLogger(PricebookPage.class);

    public EntryDoorOffset(Page page) {
        this.page = page;
    }

public void exteriorOffset(){
    page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Entry Door Offsets")).click();

    assertThat(page).hasURL(Pattern.compile(".*/ManageOffset/ViewOffse1t.*"));
    log.info("URL verified");


}

}
