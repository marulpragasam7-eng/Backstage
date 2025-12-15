package hooks;

import com.microsoft.playwright.Page;
import core.Config;
import core.ConfigLoader;
import core.PlaywrightManager;
import io.cucumber.java.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// import pages.PricebookPage; // Removed, unless the Logger needs it specifically
// The imports below are ONLY needed if you had static imports for the ThreadLocals,
// but since that's bad practice and the core issue, I'm removing the non-standard imports.
// import static core.PlaywrightManager.TL_CONTEXT;
// import static core.PlaywrightManager.TL_BROWSER;
// import static core.PlaywrightManager.TL_PAGE;

public class Hooks {
  private Config cfg;
  private static final Logger log = LoggerFactory.getLogger(Hooks.class);


  @Before(order = 0)
  public void beforeScenario(Scenario scenario) {
    cfg = ConfigLoader.load();
    // ➡️ 1. MODIFICATION: This is the ONLY call needed to handle new session or reuse.
    PlaywrightManager.create(cfg);

    // Set timeouts on the newly created or reused Page object.
    var page = PlaywrightManager.page();
    page.setDefaultTimeout(30000);             // actions/element waits
    page.setDefaultNavigationTimeout(45000);   // navigations

    log.info("Starting scenario: {}", scenario.getName());
  }

  // --- No Change Needed Here ---
  @After(order = 10)
  public void afterScenario(Scenario scenario) {
    if (scenario.isFailed()) {
      try {
        byte[] png = PlaywrightManager.page().screenshot(
                new Page.ScreenshotOptions().setFullPage(true));
        scenario.attach(png, "image/png", "Failure Screenshot");
        log.error("Screenshot attached for failed scenario: {}", scenario.getName());
      } catch (Exception ignored) {
        log.error("Could not take screenshot for failed scenario: {}", scenario.getName(), ignored);
      }
    }
  }

  // --- No Change Needed Here ---
  @After(order = 5)
  public void stopTracing(Scenario scenario) {
    if (cfg != null && cfg.trace()) {
      PlaywrightManager.stopTracingIfAny(scenario.getName());
    }
  }

  // --- No Change Needed Here ---
  /**
   * Final teardown logic.
   * - If the scenario failed, it forces a complete browser shutdown.
   * - If the scenario succeeded, it navigates to the homepage and keeps the browser open for the next scenario.
   */
  @After(order = 0)
  public void finalTearDown(Scenario scenario) {
    Page page = PlaywrightManager.page();

    if (scenario.isFailed()) {
      // Failed scenario: Must close everything to ensure a clean session for the next test.
      PlaywrightManager.close(scenario, cfg, true); // forceClose = true
    } else {
      // Successful scenario: Navigate home and keep the session open.
      if (page != null && cfg != null) {
        try {
          // Navigates the current page back to a known state (homepage)
          page.navigate(cfg.baseUrl());
          log.info("Scenario successful. Navigated to homepage: {} for session reuse.", cfg.baseUrl());
        } catch (Exception e) {
          log.error("Failed to navigate to homepage for session reuse. Forcing browser close.", e);
          PlaywrightManager.close(scenario, cfg, true); // Fallback to force close
          return;
        }
      }
      // Keeps Browser/Context open.
      PlaywrightManager.close(scenario, cfg, false); // forceClose = false
    }
  }

  // ➡️ 2. MODIFICATION: Ensure this method uses the correct class name for the logger.
  @AfterAll
  public static void globalCleanup() {
    log.info("Finished all scenarios. Performing final global browser cleanup.");
    // Calling the close method with forceClose=true on nulls forces the full cleanup.
    PlaywrightManager.close(null, null, true);
  }
}