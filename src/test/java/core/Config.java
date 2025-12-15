package core;

public record Config(
        String baseUrl,
        String browser,
        boolean headless,
        boolean trace,
        boolean video,
        boolean ignoreHttpsErrors,
        int actionRetryCount,
        long actionRetryDelayMs) {}