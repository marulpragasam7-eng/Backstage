
package core;

import java.util.function.Supplier;

public final class RetryUtils {
  private RetryUtils() {}

  public static <T> T retry(int attempts, long delayMs, Supplier<T> action) {
    RuntimeException last = null;
    for (int i = 1; i <= attempts; i++) {
      try {
        return action.get();
      } catch (RuntimeException e) {
        last = e;
        sleep(delayMs);
      }
    }
    throw last != null ? last : new RuntimeException("Retry failed without exception.");
  }

  public static void retryVoid(int attempts, long delayMs, Runnable action) {
    RuntimeException last = null;
    for (int i = 1; i <= attempts; i++) {
      try {
        action.run();
        return;
      } catch (RuntimeException e) {
        last = e;
        sleep(delayMs);
      }
    }
    if (last != null) throw last;
  }

  private static void sleep(long ms) {
    try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
  }
}
