package core;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import io.cucumber.java.Scenario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.Arrays;

public final class PlaywrightManager {
  private static final ThreadLocal<Playwright> TL_PLAYWRIGHT = new ThreadLocal<>();
  private static final ThreadLocal<Browser> TL_BROWSER = new ThreadLocal<>();
  private static final ThreadLocal<BrowserContext> TL_CONTEXT = new ThreadLocal<>();
  private static final ThreadLocal<Page> TL_PAGE = new ThreadLocal<>();
  private static final Logger log = LoggerFactory.getLogger(PlaywrightManager.class);
  private PlaywrightManager() {}

  public static void create(Config cfg) {
    // Check if a session is already active (browser/context are set).
    // If we're reusing the session, just create a new page in the existing context.
    if (TL_BROWSER.get() != null && TL_CONTEXT.get() != null) {
      log.info("Reusing existing browser session for new scenario.");
      Page newPage = TL_CONTEXT.get().newPage();
      TL_PAGE.set(newPage);
      newPage.setDefaultTimeout(30000); // Apply timeouts again on new page
      newPage.setDefaultNavigationTimeout(45000);
      return;
    }

    // --- Standard creation logic for a brand new session ---
    Playwright pw = Playwright.create();
    TL_PLAYWRIGHT.set(pw);

    BrowserType.LaunchOptions launch = new BrowserType.LaunchOptions().setHeadless(cfg.headless()).setArgs(Arrays.asList("--start-maximized"));;
    Browser browser;
    switch (cfg.browser().toLowerCase()) {
      case "firefox" -> browser = pw.firefox().launch(launch);
      case "webkit"  -> browser = pw.webkit().launch(launch);
      default        -> browser = pw.chromium().launch(launch);
    }
    TL_BROWSER.set(browser);

    Browser.NewContextOptions ctxOpts = new Browser.NewContextOptions().setViewportSize(null)
            .setIgnoreHTTPSErrors(cfg.ignoreHttpsErrors());
    if (cfg.video()) {
      // NOTE: Video is recorded per BrowserContext. If reusing, the video recording
      // will contain multiple scenarios unless a new context is created each time.
      // For this simplified reuse, we continue recording on the same context.
      ctxOpts.setRecordVideoDir(Paths.get("target", "videos"));
    }

    BrowserContext ctx = browser.newContext(ctxOpts);
    TL_CONTEXT.set(ctx);

    if (cfg.trace()) {
      ctx.tracing().start(new Tracing.StartOptions()
              .setScreenshots(true)
              .setSnapshots(true)
              .setSources(true));
    }

    Page page = ctx.newPage();
    TL_PAGE.set(page);
  }

  public static Page page() {
    return TL_PAGE.get();
  }
  public static BrowserContext context() {
    return TL_CONTEXT.get();
  }

  public static void waitForNetworkIdle() {
    Page p = TL_PAGE.get();
    if (p != null) {
      p.waitForLoadState(LoadState.NETWORKIDLE);
    }
  }

  public static void stopTracingIfAny(String scenarioName) {
    // Tracing usually records on the context, so we let it run until the final tear down,
    // or stop/start it per scenario if desired. Keeping the original logic here for now.
    BrowserContext ctx = TL_CONTEXT.get();
    if (ctx != null) {
      try {
        String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        ctx.tracing().stop(new Tracing.StopOptions()
                .setPath(Paths.get("target", "traces", sanitize(scenarioName + "_" + stamp) + ".zip")));
      } catch (Exception ignored) {}
    }
  }

  /**
   * Closes Playwright resources conditionally.
   * On success (forceClose=false), only the current Page is closed, and video is renamed.
   * On failure (forceClose=true), all resources (Browser/Context/Playwright) are closed.
   *
   * @param scenario The current Cucumber scenario.
   * @param cfg The configuration object.
   * @param forceClose If true, closes everything. If false, closes only the current Page.
   */
  public static void close(Scenario scenario, Config cfg, boolean forceClose) {
    Page page = TL_PAGE.get();
    BrowserContext context = TL_CONTEXT.get();

    // 1. Get the planned video path and close the PAGE (closing the page finalizes the video file).
    Path originalVideoPath = null;
    if (cfg != null && cfg.video() && page != null && page.video() != null) {
      originalVideoPath = page.video().path();
    }
    try { if (page != null) page.close(); } catch (Exception ignored) {}
    TL_PAGE.remove(); // The page is always closed and removed from ThreadLocal.

    // 2. Rename the video now that the file is finalized (after page.close()).
    if (originalVideoPath != null) {
      try {
        String newFileName = sanitize(scenario.getName()) + ".webm";
        Path newPath = Paths.get("target", "videos", newFileName);
        Files.move(originalVideoPath, newPath);
        log.info("Video for scenario '{}' saved as: {}", scenario.getName(), newFileName);
      } catch (IOException e) {
        log.error("Failed to rename video for scenario: {}. Reason: {}", scenario.getName(), e.getMessage());
      }
    }

    // 3. Conditional cleanup of context, browser, and Playwright
    if (forceClose) {
      log.warn("Scenario failed or final tear down requested. Closing all Playwright resources.");
      try { if (context != null) context.close(); } catch (Exception ignored) {}
      try { if (TL_BROWSER.get() != null) TL_BROWSER.get().close(); } catch (Exception ignored) {}
      try { if (TL_PLAYWRIGHT.get() != null) TL_PLAYWRIGHT.get().close(); } catch (Exception ignored) {}

      // Clear all ThreadLocals only on full close
      TL_CONTEXT.remove();
      TL_BROWSER.remove();
      TL_PLAYWRIGHT.remove();
    } else {
      log.info("Browser session retained for next scenario.");
    }
  }

  private static String sanitize(String name) {
    return name.replaceAll("[^a-zA-Z0-9._-]", "_");
  }
}