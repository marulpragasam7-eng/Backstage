  package core;

  import com.microsoft.playwright.*;
  import io.cucumber.java.Scenario;
  import org.slf4j.Logger;
  import org.slf4j.LoggerFactory;

  import java.nio.file.Files;
  import java.nio.file.Path;
  import java.nio.file.Paths;
  import java.nio.file.StandardCopyOption;

  public class PlaywrightManager {
    private static final Logger log = LoggerFactory.getLogger(PlaywrightManager.class);

    public static Playwright playwright;
    public static Browser browser;
    public static BrowserContext context;
    public static Page page;

    private static void init(Config cfg) {
      if (playwright == null) {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(cfg.headless()).setArgs(java.util.List.of("--start-maximized")));
            }
    }

    public static void create(Config cfg) {
      create(cfg, null);
    }

    public static void create(Config cfg, Path storageStatePath) {
      init(cfg);
      if (context != null) context.close();

      // ENABLE VIDEO RECORDING
      Browser.NewContextOptions options = new Browser.NewContextOptions()
              .setRecordVideoDir(Paths.get("target/videos/")).setViewportSize(null);;

      if (storageStatePath != null && storageStatePath.toFile().exists()) {
        options.setStorageStatePath(storageStatePath);
      }

      context = browser.newContext(options);

      // START TRACING
      context.tracing().start(new Tracing.StartOptions()
              .setScreenshots(true)
              .setSnapshots(true)
              .setSources(true));

      page = context.newPage();
    }

    public static Page page() { return page; }

    public static void saveStorageState(Path path) {
      if (context != null) {
        context.storageState(new BrowserContext.StorageStateOptions().setPath(path));
      }
    }

    public static void close(Scenario scenario, Config cfg, boolean forceClose) {
      if (context != null) {
        // Logic for FAILED scenarios: Capture everything
        if (scenario != null && scenario.isFailed()) {
          String sanitizedName = scenario.getName().replaceAll("[^a-zA-Z0-9]", "_");

          // 1. Capture Screenshot & Attach to Cucumber Report
          try {
            byte[] screenshot = page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
            scenario.attach(screenshot, "image/png", "Failed_Screenshot_" + sanitizedName);
          } catch (Exception e) {
            log.error("Failed to attach screenshot: {}", e.getMessage());
          }

          // 2. Stop and Save Trace
          Path tracePath = Paths.get("target/traces/" + sanitizedName + "_trace.zip");
          context.tracing().stop(new Tracing.StopOptions().setPath(tracePath));
          scenario.log("Trace saved to: " + tracePath.toString());

          // 3. Get Video Path before closing context
          Path videoTempPath = page.video() != null ? page.video().path() : null;

          // 4. Close Context
          context.close();

          // 5. Rename Video to Scenario Name
          if (videoTempPath != null) {
            try {
              Path finalVideoPath = Paths.get("target/videos/" + sanitizedName + ".webm");
              Files.createDirectories(finalVideoPath.getParent());
              Files.move(videoTempPath, finalVideoPath, StandardCopyOption.REPLACE_EXISTING);
              scenario.log("Video saved to: " + finalVideoPath.toString());
            } catch (Exception e) {
              log.error("Failed to rename video file: {}", e.getMessage());
            }
          }
        } else {
          // Logic for passed tests: Stop tracing without saving and close
          context.tracing().stop(new Tracing.StopOptions());
          context.close();
        }
        context = null;
      }

      if (forceClose) {
        if (browser != null) { browser.close(); browser = null; }
        if (playwright != null) { playwright.close(); playwright = null; }
      }
    }
  }