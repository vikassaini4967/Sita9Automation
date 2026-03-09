# SITA9 Analytics Automation

End-to-end automated testing for [SITA9 Analytics](https://app-sita9.glyph.network/) — registration, email verification, login, and analytics project setup.

## Prerequisites

| Requirement | Version |
|-------------|---------|
| Java | 17+ |
| Maven | 3.6+ |
| Chrome | Latest |

ChromeDriver is managed automatically via WebDriverManager.

## Quick Start

```bash
git clone https://github.com/vikassaini4967/Sita9Automation.git
cd Sita9Automation

# Run tests (headed)
mvn clean test

# Run tests (headless, CI mode)
mvn clean test -Dheadless=true
```

## Test Flow

| Phase | Steps | Description |
|-------|-------|-------------|
| Registration | 1–8 | Sign up, credentials, success verification |
| Email Verification | 9–15 | Mailinator inbox, extract link, verify |
| Login | 16–17 | Sign in, dashboard load |
| Analytics Setup | 1–20 | Create project, add contract, ABIs, complete setup, view analytics |

Full details: [docs/TEST_FLOW.md](docs/TEST_FLOW.md)

## Running Tests

| Command | Description |
|---------|-------------|
| `mvn clean test` | Headed browser |
| `mvn clean test -Dheadless=true` | Headless (CI simulation) |
| `mvn clean test -Dtest=Sita9RegistrationTest` | Single test class |

## CI/CD

- **Triggers:** Push / PR to `main` or `master`, daily at 6:00 AM UTC
- **Runner:** Ubuntu, JDK 17, Chrome
- **Reports:** [Actions](https://github.com/vikassaini4967/Sita9Automation/actions)

Details: [docs/CICD.md](docs/CICD.md)

## Project Structure

```
Sita9Automation/
├── .github/workflows/test.yml
├── docs/
│   ├── ARCHITECTURE.md
│   ├── TEST_FLOW.md
│   ├── SETUP_AND_RUNNING.md
│   └── CICD.md
├── src/test/java/org/Sita9RegistrationTest.java
└── pom.xml
```

## Documentation

| Doc | Description |
|-----|-------------|
| [ARCHITECTURE](docs/ARCHITECTURE.md) | Tech stack, design |
| [TEST_FLOW](docs/TEST_FLOW.md) | Step-by-step flow |
| [SETUP_AND_RUNNING](docs/SETUP_AND_RUNNING.md) | Setup & run guide |
| [CICD](docs/CICD.md) | CI/CD workflow |

## Troubleshooting

| Issue | Cause | Solution |
|-------|-------|----------|
| **Step 8: Success message not found** | App redirects to login before message appears | Add explicit wait for success text. Update locators if UI changed. |
| **STEP 1: Dashboard not found** | Slow load or session not applied in CI | Verify credentials. Test retries via `driver.get(SITA9_APP_URL)`. Check app reachability from runner. |
| **Mailinator email not received** | Delivery delay or wrong inbox | Wait 10–60s. Use `@mailinator.com`. Sender must be `support@glyph.network`. |
| **Headless failures** | Chrome flags or version mismatch | Use `--headless=new`, `--no-sandbox`, `--disable-dev-shm-usage` on Linux. Chrome 109+. |
