package core;

import java.io.InputStream;
import java.util.Properties;

public final class ConfigLoader {
  private ConfigLoader() {}

  public static Config load() {
    String env = System.getProperty("env", "local");
    Properties props = new Properties();

    try (InputStream is = resource("config/config.properties")) {
      if (is != null) props.load(is);
    } catch (Exception e) {
      throw new RuntimeException("Failed to load base config", e);
    }

    try (InputStream is = resource("config/config." + env + ".properties")) {
      if (is != null) props.load(is);
    } catch (Exception ignored) {}

    String baseUrl = System.getProperty("base.url", props.getProperty("base.url", ""));
    String browser = System.getProperty("browser", props.getProperty("browser", "chromium"));
    boolean headless = Boolean.parseBoolean(System.getProperty("headless", props.getProperty("headless", "true")));
    boolean trace = Boolean.parseBoolean(System.getProperty("trace", props.getProperty("trace", "true")));
    boolean video = Boolean.parseBoolean(System.getProperty("video", props.getProperty("video", "false")));
    boolean ignoreHttpsErrors = Boolean.parseBoolean(System.getProperty("ignoreHttpsErrors", props.getProperty("ignoreHttpsErrors", "false")));
    int actionRetryCount = Integer.parseInt(System.getProperty("actionRetryCount", props.getProperty("actionRetryCount", "2")));
    long actionRetryDelayMs = Long.parseLong(System.getProperty("actionRetryDelayMs", props.getProperty("actionRetryDelayMs", "500")));

    // --- FIX: Load email and password from properties ---
    String email = System.getProperty("email", props.getProperty("email", ""));
    String password = System.getProperty("password", props.getProperty("password", ""));
    String loginUrl = System.getProperty("login.url", props.getProperty("login.url", baseUrl + "/login"));
    // --- FIX: Pass the new email and password arguments to the constructor ---
    return new Config(
            baseUrl,
            loginUrl,
            browser,
            headless,
            trace,
            video,
            ignoreHttpsErrors,
            actionRetryCount,
            actionRetryDelayMs,
            email,    // Pass email
            password  // Pass password
    );  }

  private static InputStream resource(String path) {
    return Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
  }
}

