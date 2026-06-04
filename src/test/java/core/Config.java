package core;

public record Config(
        String baseUrl,
        String loginUrl,
        String browser,
        boolean headless,
        boolean trace,
        boolean video,
        boolean ignoreHttpsErrors,
        int actionRetryCount,
        long actionRetryDelayMs,
        String email,
        String password
) {}