package hooks;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import core.Config;
import core.ConfigLoader;
import core.PlaywrightManager;
import io.cucumber.java.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

public class Hooks {
  private static final Logger log = LoggerFactory.getLogger(Hooks.class);
  private static final Path AUTH_FILE = Paths.get("target/auth.json");
  private static boolean isAuthChecked = false;


  @Before(order = 0)
  public void beforeScenario(Scenario scenario) throws IOException {
    Config cfg = ConfigLoader.load();

    synchronized (Hooks.class) {
      if (!isAuthChecked && !scenario.getSourceTagNames().contains("@login")) {
        if (!Files.exists(AUTH_FILE)) {
          log.info("No session found. Performing one-time login...");
          performOneTimeLogin(cfg);
        }
        isAuthChecked = true;
      }
    }

    if (scenario.getSourceTagNames().contains("@login")) {
      PlaywrightManager.create(cfg);
    } else {
      PlaywrightManager.create(cfg, AUTH_FILE);
    }
  }

  private void performOneTimeLogin(Config cfg) throws IOException {
    log.info(">>>> STARTING ONE-TIME LOGIN ATTEMPT <<<<");
    Files.createDirectories(AUTH_FILE.getParent());
    PlaywrightManager.create(cfg);
    Page page = PlaywrightManager.page();

    try {
      page.navigate(cfg.baseUrl());

      if (!page.url().contains("Login") && !page.url().contains("login")) {
        page.navigate(cfg.baseUrl() + "/Site/Login?status=NotLoggedIn");
      }

      page.getByLabel("Email").or(page.getByPlaceholder("Email")).or(page.locator("input[type='email']")).first()
              .fill(cfg.email());
      page.getByLabel("Password").or(page.getByPlaceholder("Password")).or(page.locator("input[type='password']")).first()
              .fill(cfg.password());

      page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(Pattern.compile("Login|Sign In|Log In", Pattern.CASE_INSENSITIVE)))
              .click();

      page.waitForCondition(() -> !page.url().contains("login"), new Page.WaitForConditionOptions().setTimeout(20000));

      PlaywrightManager.saveStorageState(AUTH_FILE);
      log.info(">>>> AUTH STATE SAVED SUCCESSFULLY <<<<");

    } catch (Exception e) {
      log.error("Login failed! Capturing screenshot for debugging...");
      page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("target/auth_failure.png")));
      throw e;
    } finally {
      PlaywrightManager.close(null, cfg, false);
    }
  }

  @After(order = 0)
  public void tearDown(Scenario scenario) {
    PlaywrightManager.close(scenario, ConfigLoader.load(), scenario.isFailed());
  }

  @AfterAll
  public static void globalCleanup() {
    PlaywrightManager.close(null, null, true);
  }
}