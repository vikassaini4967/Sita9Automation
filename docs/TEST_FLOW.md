# SITA9 Automation — Detailed Test Flow

This document describes the complete step-by-step flow of the `testSita9Registration` test.

---

## Phase 1: Registration (Steps 1–8)

| Step | Action | Locator / Verification |
|------|--------|------------------------|
| 1 | Load SITA9 app | `driver.get(SITA9_APP_URL)` |
| 1 | Verify logo | `//div[@class='flow-logo-y absolute inset-0']` |
| 2 | Click Sign up | `//button[normalize-space()='Sign up']` |
| 3 | Enter email | `//input[@type='email']` — `sita9_test{random}@mailinator.com` |
| 4 | Enter password | `input[placeholder='Create a password']` — `Test@123` |
| 5 | Confirm password | `input[placeholder='Confirm your password']` |
| 6 | Accept terms | Last `[id*='form-item']` checkbox |
| 7 | Click Create account | `//button[normalize-space()='Create account']` |
| 8 | Verify success | Any of: `Account created!`, `User created successfully`, `Please check your email` |

---

## Phase 2: Email Verification (Steps 9–15)

| Step | Action | Details |
|------|--------|---------|
| 9 | Open Mailinator | New tab: `https://www.mailinator.com/v4/public/inboxes.jsp?to={inbox}` |
| 10 | Wait for verification email | From: `support@glyph.network` |
| 11 | Click email row | `//td[contains(@onclick,'showTheMessage') and contains(.,'support@glyph.network')]` |
| 12 | Extract verification link | From iframes `msg_body`, `htmlmsgbody`, `iframeMail` or page source |
| 13 | Navigate to verification URL | Main window: `driver.get(verificationUrl)` |
| 14 | Wait for verification success | URL contains `/login` OR body contains `verified`/`success` |
| 15 | Close Mailinator tab | Switch back to SITA9 window |

---

## Phase 3: Login (Steps 16–17)

| Step | Action | Details |
|------|--------|---------|
| 16 | Navigate to app | `driver.get(SITA9_APP_URL)` |
| 16 | Enter credentials | `input[name='email']`, `input[name='password']` (clear + sendKeys) |
| 16 | Click Sign In | `//button[normalize-space()='Sign In' or normalize-space()='Sign in']` |
| 17 | Wait for login outcome | Up to 60s: URL not `/login` OR dashboard visible |
| 17 | Post-login load | If URL contains `login`: `driver.get(SITA9_APP_URL)`; else refresh |
| 17 | Wait for readyState | `document.readyState === 'complete'` + sleep |

---

## Phase 4: Analytics Project Setup (STEP 1–20)

### STEP 1: Verify Dashboard

| Action | Details |
|--------|---------|
| Switch to SITA9 window | `driver.getCurrentUrl().contains("glyph.network")` |
| If on `/login` | `driver.get(SITA9_APP_URL)`, wait readyState, sleep 3s |
| Wait for dashboard | Up to 90s for any of: `Welcome to SITA9 Analytics` (h2), `Create Your First Project` (button) |
| Retry | On timeout: load app root again, wait, retry 90s |

### STEP 2: Create Project

| Action | Locator |
|--------|---------|
| Click Create Your First Project | `//button[normalize-space()='Create Your First Project']` |

### STEP 3–4: Project Details

| Step | Field | Value |
|------|-------|-------|
| 3 | Project Name | `DeFi` | `//input[@name='name' and @placeholder='e.g., DeFi Exchange Pro']` |
| 4 | Project URL | `https://www.google.com/` | `//input[@name='url' and @placeholder='https://yourproject.com']` |

### STEP 5–6: Project Type & Network

| Step | Action | Locator |
|------|--------|---------|
| 5 | Select Project Type | `DeFi Protocol` | `//button[@role='combobox' and .//span[normalize-space()='Select project type']]` → `//span[normalize-space()='DeFi Protocol']` |
| 6 | Select Network | `Ethereum` | `//span[normalize-space()='Ethereum']` |

### STEP 7: Add Contracts

| Action | Locator |
|--------|---------|
| Click Next: Add Contracts | `//button[normalize-space()='Next: Add Contracts']` |
| Wait for Add Contract button | `//button[normalize-space()='Add Contract']` |

### STEP 8–11: Contract Details

| Step | Field | Value |
|------|-------|-------|
| 8 | Click Add Contract | `//button[normalize-space()='Add Contract']` |
| 9 | Token Name | `Layer zero` | `//input[@placeholder='e.g., Main Token']` |
| 10 | Contract Address | `0x6985884C4392D348587B19cb9eAAf157F13271cd` | `//input[@placeholder='0x...']` |
| 11 | Verified checkbox | Click | `//button[@role='checkbox']` |

### STEP 12–13: Fetch ABIs

| Step | Action | Locator |
|------|--------|---------|
| 12 | Click Next: Fetch ABIs | `//button[normalize-space()='Next: Fetch ABIs']` |
| 13 | Verify Verified status | `//span[normalize-space()='Verified']` |

### STEP 14–16: Review & Submit

| Step | Action | Locator |
|------|--------|---------|
| 14 | Click Review & Submit | `//button[normalize-space()='Review & Submit']` |
| 15 | Verify config message | `//p[normalize-space()='Your project configuration is complete and ready to deploy.']` |
| 16 | Click Complete Setup | `//button[normalize-space()='Complete Setup']` |

### STEP 17–18: Setup Complete

| Step | Action | Locator |
|------|--------|---------|
| 17 | Verify Setup Complete | `//h1[contains(normalize-space(),'Setup Complete')]` |
| 18 | Click Go to Dashboard | `//button[normalize-space()='Go to Dashboard']` |

### STEP 19–20: Analytics & Completion

| Step | Action | Locator |
|------|--------|---------|
| 19 | Click View Analytics | `//button[normalize-space()='View Analytics']` |
| 19 | Wait for page load | `readyState === 'complete'` + 5s sleep |
| 20 | Verify Welcome back | `//span[contains(@class,'text-muted-foreground') and normalize-space()='Welcome back']` |
| 20 | Close browser | `driver.quit()` |

---

## Timeouts Summary

| Wait | Duration |
|------|----------|
| Default WebDriverWait | 25s |
| Login outcome | 60s |
| Dashboard (STEP 1) | 90s (2 attempts) |
| Mailinator email | 60s |
| Verification link extraction | 30s |
| Analytics page load | 45s |

---

## Success Criteria

The test passes when:

1. Account creation succeeds (Step 8)
2. Verification email is received and link is verified (Steps 9–15)
3. Login succeeds and dashboard is visible (Steps 16–17, STEP 1)
4. Project is created and setup is completed (STEPs 2–18)
5. "Welcome back" message is visible on analytics page (STEP 20)
6. Browser is closed cleanly
