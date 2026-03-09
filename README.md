# SITA9 Analytics Automation Framework

End-to-end automated testing framework for **SITA9 Analytics** at [https://app-sita9.glyph.network/](https://app-sita9.glyph.network/). This project automates the full user journey: account registration, email verification via Mailinator, login, and analytics project setup through to the dashboard.

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Test Flow Summary](#test-flow-summary)
- [Running Tests](#running-tests)
- [CI/CD](#cicd)
- [Project Structure](#project-structure)
- [Documentation](#documentation)
- [Troubleshooting](#troubleshooting)

---

## Overview

SITA9 Analytics is a DeFi analytics platform. This automation framework validates the complete onboarding and setup flow:

1. **Registration** — Create account with email, password, and terms acceptance
2. **Email Verification** — Fully automated via Mailinator (no manual steps)
3. **Login** — Sign in with verified credentials
4. **Analytics Project Setup** — Create project, add contracts, fetch ABIs, complete setup, and view analytics dashboard

---

## Features

- **Fully automated registration** with Mailinator email generation
- **Automated email verification** — Opens Mailinator inbox, waits for verification email, extracts link, and completes verification
- **Cross-platform** — Uses `clear()` for form fields (works on Linux CI and macOS)
- **Headless support** — Runs in headless Chrome for CI/CD
- **CI/CD ready** — GitHub Actions workflow with scheduled runs
- **Sign-up email tracking** — Appends sign-up emails to `signup-emails.txt` for audit

---

## Prerequisites

| Requirement | Version |
|-------------|---------|
| Java | 17+ |
| Maven | 3.6+ |
| Chrome/Chromium | Latest |
| ChromeDriver | Auto-managed by WebDriverManager |

---

## Quick Start

```bash
# Clone the repository
git clone https://github.com/vikassaini4967/Sita9Automation.git
cd Sita9Automation

# Run tests (headed browser)
mvn clean test

# Run tests (headless, for CI simulation)
mvn clean test -Dheadless=true
```

---

## Test Flow Summary

| Phase | Steps | Description |
|-------|-------|-------------|
| **Registration** | 1–8 | Load app, sign up, enter credentials, verify success message |
| **Email Verification** | 9–15 | Open Mailinator, wait for email, extract link, verify, switch back |
| **Login** | 16–17 | Sign in, wait for dashboard |
| **Analytics Setup** | STEP 1–20 | Create project, add contract, fetch ABIs, complete setup, view analytics, verify "Welcome back" |

See [docs/TEST_FLOW.md](docs/TEST_FLOW.md) for the complete step-by-step flow.

---

## Running Tests

### Local (headed browser)

```bash
mvn clean test
```

### Local (headless, simulates CI)

```bash
mvn clean test -Dheadless=true
```

### With Maven profiles

```bash
# Headless profile
mvn clean test -Pheadless

# Or via system property
mvn clean test -Dheadless=true
```

### Run specific test class

```bash
mvn clean test -Dtest=Sita9RegistrationTest
```

---

## CI/CD

Tests run automatically on:

- **Push** to `main` or `master`
- **Pull requests** to `main` or `master`
- **Schedule** — Daily at 6:00 AM UTC

### GitHub Actions workflow

- **Runner:** `ubuntu-latest`
- **JDK:** 17 (Temurin)
- **Chrome:** Installed from Google
- **Command:** `mvn clean test -Dheadless=true`
- **Artifacts:** Surefire reports (7 days), signup-emails (1 day)

View runs: [https://github.com/vikassaini4967/Sita9Automation/actions](https://github.com/vikassaini4967/Sita9Automation/actions)

See [docs/CICD.md](docs/CICD.md) for details.

---

## Project Structure

```
Sita9Automation/
├── .github/
│   └── workflows/
│       └── test.yml          # CI/CD workflow
├── docs/
│   ├── ARCHITECTURE.md      # Tech stack & design
│   ├── TEST_FLOW.md        # Detailed test steps
│   ├── SETUP_AND_RUNNING.md # Setup & run guide
│   └── CICD.md             # CI/CD documentation
├── src/
│   └── test/
│       └── java/
│           └── org/
│               └── Sita9RegistrationTest.java
├── pom.xml
├── signup-emails.txt        # Generated (gitignored)
└── README.md
```

---

## Documentation

| Document | Description |
|----------|-------------|
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | Architecture, tech stack, design decisions |
| [docs/TEST_FLOW.md](docs/TEST_FLOW.md) | Detailed step-by-step test flow |
| [docs/SETUP_AND_RUNNING.md](docs/SETUP_AND_RUNNING.md) | Prerequisites, setup, and run instructions |
| [docs/CICD.md](docs/CICD.md) | CI/CD workflow, triggers, and troubleshooting |

---

## Troubleshooting

### Test fails at "Success message not found" (Step 8)

The app may have redirected to the login page before the success message appeared. The test expects one of: `Account created!`, `User created successfully`, or `Please check your email`. If the app UI changes, update the locators in `Sita9RegistrationTest.java`.

### Test fails at STEP 1 (dashboard not found)

In CI, the dashboard may load slowly. The test retries by loading the app root (`SITA9_APP_URL`) and waiting again. If it still fails, check:

- Login credentials are correct (Mailinator email + password)
- The app is accessible from the CI runner
- Chrome/ChromeDriver versions are compatible

### Mailinator email not received

- Mailinator public inboxes can have delays (typically 10–60 seconds)
- Ensure the email domain is `@mailinator.com`
- Check that `support@glyph.network` is the expected sender

### Headless mode issues

- Use `--headless=new` (Chrome 109+)
- Ensure `--no-sandbox` and `--disable-dev-shm-usage` for Linux CI

---

## License

This project is for testing SITA9 Analytics. Refer to the repository for license terms.
