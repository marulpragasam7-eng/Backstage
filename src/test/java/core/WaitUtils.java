
package core;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.assertions.LocatorAssertions;
import com.microsoft.playwright.options.WaitForSelectorState;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public final class WaitUtils {
  private WaitUtils() {}

  public static void visible(Locator locator, double timeoutMs) throws TimeoutError {
    try {
      assertThat(locator).isVisible(
              new LocatorAssertions.IsVisibleOptions().setTimeout(timeoutMs)
      );
    } catch (TimeoutError e) {
      throw new TimeoutError(
              String.format("Timeout %.0fms exceeded. Locator: %s failed to become visible.",
                      timeoutMs, locator.toString()), e);
    }
  }
  public static void attached(Locator locator, double timeoutMs) {
    locator.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.ATTACHED).setTimeout(timeoutMs));
  }

  public static void hidden(Locator locator, double timeoutMs) {
    locator.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN).setTimeout(timeoutMs));
  }

  public static void networkIdle(Page page) {
    page.waitForLoadState();
    page.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE);
  }
}
