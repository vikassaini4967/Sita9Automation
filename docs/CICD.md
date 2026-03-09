# SITA9 Automation — CI/CD Documentation

## Overview

The SITA9 Automation project uses **GitHub Actions** for continuous integration and deployment. Tests run automatically on push, pull request, and on a daily schedule.

---

## Workflow File

**Location:** `.github/workflows/test.yml`

---

## Triggers

| Trigger | Branches | When |
|---------|----------|------|
| Push | `main`, `master` | On every push |
| Pull Request | `main`, `master` | On every PR |
| Schedule | - | Daily at 6:00 AM UTC (`0 6 * * *`) |

---

## Job Steps

### 1. Checkout

```yaml
- uses: actions/checkout@v4
```

### 2. Set Up JDK 17

```yaml
- uses: actions/setup-java@v4
  with:
    java-version: '17'
    distribution: 'temurin'
    cache: maven
```

### 3. Cache Maven Packages

```yaml
- uses: actions/cache@v4
  with:
    path: ~/.m2/repository
    key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
    restore-keys: ${{ runner.os }}-m2-
```

### 4. Install Chrome

```yaml
- run: |
    sudo apt-get update
    sudo apt-get install -y wget
    wget -q https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb
    sudo apt-get install -y ./google-chrome-stable_current_amd64.deb || ...
    google-chrome-stable --version
```

### 5. Run Tests (Headless)

```yaml
- run: mvn clean test -Dheadless=true
  env:
    GITHUB_ACTIONS: true
```

### 6. Upload Artifacts

| Artifact | Condition | Retention |
|----------|-----------|-----------|
| `surefire-reports` | Always | 7 days |
| `signup-emails` | Success + file exists | 1 day |

---

## Environment

| Environment | Value |
|-------------|-------|
| Runner | `ubuntu-latest` |
| OS | Linux |
| Java | 17 (Temurin) |
| Browser | Chrome (stable) |
| Headless | Yes (`-Dheadless=true`, `GITHUB_ACTIONS=true`) |

---

## Viewing Results

1. Go to [https://github.com/vikassaini4967/Sita9Automation/actions](https://github.com/vikassaini4967/Sita9Automation/actions)
2. Click a workflow run
3. Click the `test` job
4. Expand steps to view logs

---

## Artifacts

| Artifact | Contents |
|----------|----------|
| surefire-reports | `target/surefire-reports/` — TestNG XML, HTML, emailable report |
| signup-emails | `signup-emails.txt` — Emails used for sign-up (if test passed) |

---

## Troubleshooting CI Failures

### Test fails at Step 8 (Success message not found)

- App may show login page instead of success message
- Possible timing: success message appears briefly then redirects
- **Fix:** Add explicit wait for success message before checking, or relax assertion

### Test fails at STEP 1 (Dashboard not found)

- In CI, URL may stay on `/login` after login
- Dashboard may load slowly in headless
- **Fix:** Test already retries by loading app root; ensure `driver.get(SITA9_APP_URL)` runs when on `/login`

### Chrome/ChromeDriver mismatch

- GitHub Actions installs latest Chrome
- WebDriverManager downloads matching ChromeDriver
- If mismatch: check Chrome version in logs, update `webdrivermanager` if needed

### Network issues

- Ensure `app-sita9.glyph.network` and `mailinator.com` are reachable from GitHub runners
- No proxy or firewall blocking GitHub Actions

### Flaky tests

- Add more explicit waits
- Increase timeouts for CI (e.g., 90s for dashboard)
- Use retries for critical steps

---

## Manual Trigger (Optional)

To add a `workflow_dispatch` trigger for manual runs:

```yaml
on:
  workflow_dispatch:
  push:
    branches: [ main, master ]
  # ...
```

Then use "Run workflow" in the Actions tab.
