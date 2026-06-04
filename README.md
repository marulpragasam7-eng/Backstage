
# Playwright + Java + Cucumber + Extent (QA/Stage + Registration & Forgot Password)

Parallel-ready UI automation with **Playwright for Java**, **Cucumber JVM**, **ExtentReports**.

**QA URL:** `https://test.rsuitebackstage.com`  
**Stage URL:** `https://stage.rsuitebackstage.com`

## Run
```bash
# QA sanity
mvn clean test -Denv=qa -Dcucumber.filter.tags="@smoke"

# Registration flow on Stage
mvn clean test -Denv=stage -Dcucumber.filter.tags="@registration"

# Forgot password flow on Stage
mvn clean test -Denv=stage -Dcucumber.filter.tags="@forgot"

# Headed Firefox
mvn clean test -Denv=stage -Dbrowser=firefox -Dheadless=false

# Rerun failed scenarios (Surefire produces target/rerun.txt)
mvn -q -Dtest=runner.RunCukesTest test -Dcucumber.features=@target/rerun.txt || true
```

## Reports
- Extent Spark HTML: `target/extent-reports/Spark.html`
- Cucumber JSON: `target/cucumber.json`

## Notes
- Each scenario runs in isolated **BrowserContext** (ThreadLocal) for parallel stability.
- Traces saved to `target/traces/`.
- **Retries** for known flaky actions implemented in `core/RetryUtils` and used in POMs.
- **Explicit waits** centralized in `core/WaitUtils` (visibility, network idle, dropdown readiness).
