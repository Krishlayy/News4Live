# News 4 Live — Build & Release Guide

Instructions for building, testing, and generating release APKs and Android App Bundles (AAB) for Google Play Store deployment.

---

## 1. Prerequisites

- **Android Studio**: Android Studio Hedgehog (2023.1.1) or newer (Ladybug / Koala recommended)
- **JDK**: Java Development Kit (JDK 17 or JDK 21)
- **Android SDK**: API Level 34 (Android 14) platform and build-tools 34.0.0

---

## 2. Building Debug APK

Open the terminal in the project root directory and run:

### On Windows (PowerShell / CMD):
```powershell
.\gradlew.bat assembleDebug
```

### On macOS / Linux:
```bash
./gradlew assembleDebug
```

The compiled Debug APK will be located at:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 3. Running Unit Tests

To run local unit tests for HTML sanitization, date formatting, and DTO mappings:

```powershell
.\gradlew.bat test
```

---

## 4. Generating Production Release Bundle (.aab)

Google Play Store requires modern apps to be published in **Android App Bundle (.aab)** format.

### Step 1: Create Keystore
Generate a signing keystore if you don't already have one:
```bash
keytool -genkey -v -keystore news4live-release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias news4live-key
```

### Step 2: Configure Signing in `app/build.gradle.kts`
Create or update `keystore.properties` in project root:
```properties
storeFile=../news4live-release.jks
storePassword=YOUR_STORE_PASSWORD
keyAlias=news4live-key
keyPassword=YOUR_KEY_PASSWORD
```

### Step 3: Build Release App Bundle
```powershell
.\gradlew.bat bundleRelease
```

The generated bundle will be located at:
`app/build/outputs/bundle/release/app-release.aab`

---

## 5. Google Play Store Release Checklist

- [ ] **App Name**: News 4 Live (न्यूज़ 4 लाइव)
- [ ] **Short Description**: ताज़ा, सटीक और निष्पक्ष हिंदी समाचार
- [ ] **Category**: News & Magazines
- [ ] **Content Rating**: Complete questionnaire on Google Play Console
- [ ] **Target Audience**: 13+
- [ ] **Data Safety**:
  - Personal Information: None collected.
  - App Activity: Article views / Search queries (analytics).
  - Device IDs: Firebase installation tokens for push notifications.
  - Security Practices: All data in transit is encrypted over HTTPS.
- [ ] **Privacy Policy URL**: `https://news4live.com/privacy-policy/`
