# News 4 Live — Production Hindi News Android Application

A production-ready, native Android news application engineered for modern Indian mobile users (Hindi-first typography, fast loading over 3G/4G/5G/Wi-Fi, offline caching, and responsive UI) powered directly by your existing WordPress website (`https://news4live.com/`) via the WordPress REST API and companion WordPress plugin.

---

## 🌟 Architecture & Highlights

- **WordPress as Primary CMS**: Directly queries your live WordPress site (5,508+ articles across 1,102 pages) via `/wp-json/wp/v2/posts?_embed=1`.
- **Zero Duplicate Article Database**: WordPress Admin remains your news writing and publishing interface. Any newly published article appears automatically in the app without manual uploading.
- **Modern Android Stack**:
  - **Language**: Kotlin 2.0+ with Coroutines & Flow
  - **UI Toolkit**: Jetpack Compose + Material 3 + Dynamic Dark/Light Theme
  - **Architecture**: Clean Architecture + MVVM + MVI StateFlow
  - **Networking**: Retrofit 2 + OkHttp 4 (with 50MB disk cache and robust retry logic)
  - **Pagination**: Paging 3 (Infinite scroll across years of WordPress archives)
  - **Local Persistence**: Room Database (Entity, DAO, TypeConverters for offline reading & bookmarks)
  - **Image Pipeline**: Coil 3 with WebP support and WordPress thumbnail sizing
  - **HTML Renderer**: Safe native Compose HTML parser with Jsoup sanitization
  - **Push Notifications**: Firebase Cloud Messaging (FCM) with BigPictureStyle image previews
  - **Deep Links**: Android App Links for canonical WordPress URLs (`https://news4live.com/*`)

---

## 📱 App Feature Tour

| Feature | Description |
| :--- | :--- |
| **Breaking News Ticker** | Animated horizontal/vertical ticker highlighting breaking alerts from WordPress category 31 ("ब्रेकिंग न्यूज़"). |
| **Top Stories Hero Card** | Large featured story card with high-resolution thumbnail, category badge, and published timestamp. |
| **Dynamic Category Tabs** | Horizontal scrollable category bar populated dynamically from WordPress API ("होम", "उत्तर प्रदेश", "मथुरा समाचार", "हाथरस", "बृज दर्शन", "देश", "ज्योतिष", "टेक न्यूज़", etc.). |
| **Article Reader Experience** | Clean Devanagari typography, selectable text, font size modifier (A-, A+), inline images with full-screen lightbox, blockquotes, and related news. |
| **Live Debounced Search** | Search by keywords with 400ms debounce, query suggestions, and recent searches history. |
| **Offline Reading & Bookmarks** | Save articles to Room database with a single tap for complete offline reading without internet connection. |
| **Native Android Share Sheet** | Share article headline and canonical WordPress link directly to WhatsApp, Telegram, Twitter/X, and Facebook. |
| **More & Settings** | Access Privacy Policy, Terms & Conditions, Contact Us, App Version, and Rate App. |

---

## 🗂️ Project Structure

```
c:/News4Live/
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/news4live/app/
│       │   │   ├── NewsApplication.kt
│       │   │   ├── MainActivity.kt
│       │   │   ├── data/
│       │   │   │   ├── api/
│       │   │   │   ├── database/
│       │   │   │   ├── models/
│       │   │   │   ├── paging/
│       │   │   │   └── repository/
│       │   │   ├── domain/
│       │   │   │   ├── model/
│       │   │   │   ├── repository/
│       │   │   │   └── usecase/
│       │   │   ├── presentation/
│       │   │   │   ├── theme/
│       │   │   │   ├── components/
│       │   │   │   ├── navigation/
│       │   │   │   ├── home/
│       │   │   │   ├── article/
│       │   │   │   ├── category/
│       │   │   │   ├── search/
│       │   │   │   ├── bookmarks/
│       │   │   │   └── more/
│       │   │   ├── notifications/
│       │   │   ├── analytics/
│       │   │   ├── ads/
│       │   │   ├── util/
│       │   │   └── di/
│       │   └── res/
│       └── test/
├── gradle/
│   └── libs.versions.toml
├── wp-plugin/
│   └── news4live-companion/
│       ├── news4live-companion.php
│       └── readme.txt
├── docs/
│   ├── WORDPRESS_INTEGRATION_REPORT.md
│   ├── ARCHITECTURE.md
│   ├── PUSH_NOTIFICATIONS_SETUP.md
│   └── BUILD_AND_RELEASE.md
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── README.md
```

---

## 🔌 WordPress Companion Plugin

The included companion plugin (`wp-plugin/news4live-companion/`) provides:
1. **Aggregated Home Endpoint**: `/wp-json/news4live/v1/home` — Reduces mobile network latency by bundling breaking news, top stories, categories, and latest articles in 1 single database call.
2. **Push Notifications Dispatcher**: Automatically triggers Firebase Cloud Messaging (FCM) when a post is published in WordPress Admin.
3. **Reversible & Safe**: Does not touch WordPress core tables or alter database schema.

---

## 🚀 Getting Started

1. Open the project folder in **Android Studio** (File -> Open -> `c:\News4Live`).
2. Allow Gradle sync to complete.
3. Run on an Android Emulator or connected physical device.
4. To enable FCM push notifications, add your `google-services.json` to the `app/` folder (see [`docs/PUSH_NOTIFICATIONS_SETUP.md`](docs/PUSH_NOTIFICATIONS_SETUP.md)).
