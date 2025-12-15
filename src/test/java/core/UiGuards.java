package core;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import java.util.List;

public final class UiGuards {
    private UiGuards() {}

    public static void dismissCommonBanners(Page page) {
        List<String> texts = List.of("Accept", "I Agree", "Agree", "OK", "Got it", "Continue");

        for (String t : texts) {
            try {
                Locator candidate = page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName(t));
                if (candidate.first().isVisible()) { candidate.first().click(new Locator.ClickOptions().setTrial(true)); candidate.first().click(); }
            } catch (Throwable ignored) {}

            try {
                Locator candidate = page.getByText(t);
                if (candidate.first().isVisible()) { candidate.first().click(new Locator.ClickOptions().setTrial(true)); candidate.first().click(); }
            } catch (Throwable ignored) {}
        }
    }
}