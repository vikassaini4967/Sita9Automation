# SITA9 Automation — Architecture

## Overview

The SITA9 Automation Framework is a Java-based end-to-end (E2E) test suite built with Selenium WebDriver and TestNG. It automates the full user journey on the SITA9 Analytics web application.

---

## Tech Stack

| Component | Technology | Version |
|-----------|------------|---------|
| Language | Java | 17 |
| Build | Maven | 3.x |
| Test Framework | TestNG | 6.14.3 |
| Browser Automation | Selenium WebDriver | 4.18.1 |
| Driver Management | WebDriverManager (Bonigarcia) | 5.6.2 |
| Browser | Chrome / Chromium | Latest |
| CI/CD | GitHub Actions | - |

---

## Design Principles

### 1. Single Test Class

All steps are in one TestNG test method (`testSita9Registration`) to keep the flow sequential and avoid state loss between steps. Each phase is extracted into private methods for clarity.

### 2. Cross-Platform Compatibility

- **Form fields:** Uses `element.clear()` instead of `Keys.COMMAND + "a"` so tests work on Linux (CI) and macOS.
- **Headless:** Automatically enabled when `-Dheadless=true` or `GITHUB_ACTIONS=true`.

### 3. Resilient Waits

- Explicit waits with `WebDriverWait` and `ExpectedConditions` instead of fixed sleeps.
- Retry logic for dashboard loading in CI (e.g., load app root and retry if still on `/login`).
- Multiple locators for success conditions (e.g., `ExpectedConditions.or` for welcome message).

### 4. Automated Email Verification

Uses Mailinator public inboxes — no manual inbox access. The test:

1. Opens Mailinator in a new tab
2. Waits for verification email from `support@glyph.network`
3. Clicks the email row to open it
4. Extracts the verification link from iframe or page source
5. Navigates to the link in the main window
6. Waits for verification success or redirect
7. Closes Mailinator tab and switches back to SITA9

---

## Application Under Test

| Property | Value |
|----------|-------|
| URL | https://app-sita9.glyph.network/ |
| Type | SPA (Single Page Application) |
| Auth | Email + password, email verification required |

---

## Test Flow Architecture

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        testSita9Registration()                         │
├─────────────────────────────────────────────────────────────────────────┤
│  Registration (Steps 1–8)                                                │
│  ├─ Load app, verify logo                                                │
│  ├─ Sign up, enter email/password, accept terms                          │
│  └─ Assert success message                                               │
├─────────────────────────────────────────────────────────────────────────┤
│  verifyEmailViaMailinator() (Steps 9–15)                                 │
│  ├─ Open Mailinator inbox in new tab                                     │
│  ├─ Wait for verification email, open it, extract link                    │
│  ├─ Navigate to verification URL in main window                          │
│  └─ Wait for success, close Mailinator tab                               │
├─────────────────────────────────────────────────────────────────────────┤
│  loginToSita9() (Steps 16–17)                                            │
│  ├─ Enter credentials, click Sign In                                     │
│  ├─ Wait for dashboard or URL change (60s)                               │
│  └─ Load app root if still on /login (CI fallback)                       │
├─────────────────────────────────────────────────────────────────────────┤
│  runAnalyticsProjectSetupFlow() (STEP 1–20)                              │
│  ├─ STEP 1: Verify welcome/dashboard (with retry)                        │
│  ├─ STEP 2–7: Create project (name, URL, type, network)                  │
│  ├─ STEP 8–13: Add contract, fetch ABIs                                  │
│  ├─ STEP 14–17: Review & Submit, Complete Setup                          │
│  ├─ STEP 18–19: Go to Dashboard, View Analytics                          │
│  └─ STEP 20: Verify "Welcome back", quit driver                         │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## Key Constants

| Constant | Value | Purpose |
|----------|-------|---------|
| `SITA9_APP_URL` | https://app-sita9.glyph.network/ | Base URL |
| `PASSWORD` | Test@123 | Default password for new accounts |
| `MAILINATOR_INBOX_BASE` | https://www.mailinator.com/v4/public/inboxes.jsp?to= | Mailinator inbox URL prefix |
| `SIGNUP_EMAILS_FILE` | signup-emails.txt | File to append sign-up emails |

---

## Dependencies

```xml
<!-- Selenium WebDriver -->
org.seleniumhq.selenium:selenium-java:4.18.1

<!-- ChromeDriver Manager -->
io.github.bonigarcia:webdrivermanager:5.6.2

<!-- TestNG -->
org.testng:testng:6.14.3

<!-- Jackson (optional, for JSON) -->
com.fasterxml.jackson.core:jackson-databind:2.15.2
```

---

## Headless Configuration

When `headless=true` or `GITHUB_ACTIONS=true`:

```java
options.addArguments(
    "--headless=new",
    "--disable-gpu",
    "--no-sandbox",
    "--disable-dev-shm-usage",
    "--window-size=1920,1080",
    "--disable-extensions",
    "--remote-allow-origins=*"
);
```

---

## Artifacts

| Artifact | Location | Purpose |
|----------|----------|---------|
| Sign-up emails | `signup-emails.txt` | Audit trail of created accounts (gitignored) |
| Surefire reports | `target/surefire-reports/` | Test results, HTML, XML |
| TestNG reports | `target/surefire-reports/` | TestNG-specific reports |
