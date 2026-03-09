# SITA9 Automation — Setup and Running

## Prerequisites

### 1. Java 17 or Higher

```bash
java -version
# Should show: openjdk version "17" or higher
```

### 2. Maven 3.6+

```bash
mvn -version
```

### 3. Chrome or Chromium

- Chrome/Chromium must be installed for Selenium to use.
- ChromeDriver is managed automatically by WebDriverManager.

### 4. Network Access

- Access to `https://app-sita9.glyph.network/`
- Access to `https://www.mailinator.com/`

---

## Installation

### Clone the Repository

```bash
git clone https://github.com/vikassaini4967/Sita9Automation.git
cd Sita9Automation
```

### Build (No Extra Install)

```bash
mvn clean compile
```

Maven will download dependencies automatically.

---

## Running Tests

### Local — Headed Browser (Default)

```bash
mvn clean test
```

- Chrome window opens
- You can watch the test run

### Local — Headless (CI Simulation)

```bash
mvn clean test -Dheadless=true
```

- No visible browser
- Simulates GitHub Actions environment

### Run Specific Test Class

```bash
mvn clean test -Dtest=Sita9RegistrationTest
```

### Generate Reports

```bash
mvn clean test surefire-report:report
```

Reports are in `target/site/surefire-report.html`.

---

## Environment Variables

| Variable | Effect |
|----------|--------|
| `GITHUB_ACTIONS` | When `true`, enables headless mode (set by GitHub Actions) |
| `headless` (system property) | `-Dheadless=true` enables headless |

---

## Output Files

| File | Description |
|------|-------------|
| `signup-emails.txt` | Appended with each new sign-up email (timestamp + email) |
| `target/surefire-reports/` | Test results (XML, HTML, emailable reports) |

---

## IDE Setup

### IntelliJ IDEA

1. Open `pom.xml` as Maven project
2. Run `Sita9RegistrationTest` as TestNG test
3. Right-click test method → Run

### Eclipse

1. Import as Maven project
2. Install TestNG plugin
3. Run as TestNG

### VS Code

1. Install Java Extension Pack
2. Open terminal: `mvn clean test`

---

## Troubleshooting

### ChromeDriver Version Mismatch

WebDriverManager downloads the correct ChromeDriver. If issues persist:

```bash
# Clear WebDriverManager cache
rm -rf ~/.cache/selenium
```

### Port Already in Use

Tests use a single browser instance. Ensure no other Selenium/Chrome processes are running.

### Mailinator Timeout

- Mailinator can take 10–60 seconds to deliver
- Increase wait in `verifyEmailViaMailinator` if needed.

### Headless Fails Locally

- Ensure Chrome supports `--headless=new` (Chrome 109+)
- On Linux: `--no-sandbox` and `--disable-dev-shm-usage` are required
