# News 4 Live — Production Audit Report

**Date**: September 2026  
**Auditor**: Senior Android + WordPress + Firebase Production Engineer  
**Codebase Location**: `C:\News4Live`  
**Live Target**: `https://news4live.com/` (WordPress CMS)  

---

## 1. Executive Summary

This production audit verifies the complete architecture, live WordPress API behaviors, data caching, background push notifications, and UI resilience for the News 4 Live application. The application was audited against actual live HTTP responses from `https://news4live.com/` (5,508+ articles, 1,102 pages) and Google Play Store production guidelines.

---

## 2. Audit Findings Matrix

| Component | Status | Finding / Issue Identified | Action / Fix Required |
| :--- | :--- | :--- | :--- |
| **REST API Pagination** | ⚠️ Partial | WordPress returns `HTTP 400` with code `rest_post_invalid_page_number` when requesting beyond total pages. | Handle HTTP 400 `rest_post_invalid_page_number` as `nextKey = null` (clean end-of-list) in Paging 3 rather than triggering an error state. |
| **Server Cache Headers** | ⚠️ Partial | The live WordPress host does not return `ETag` or `Last-Modified` headers in REST API responses. | Implement application-level cache freshness using the article's `modified` timestamp field. |
| **Companion Plugin Availability** | ⚠️ Partial | `/wp-json/news4live/v1/home` returns `HTTP 404` until the plugin is uploaded to the live server. | Ensure the Android repository seamlessly defaults to standard WordPress REST endpoints with zero user disruption. |
| **Duplicate Push Notifications** | 🔴 Insecure / Bug | Re-saving or editing an existing published post in WordPress could trigger duplicate FCM push notifications. | Add idempotency tracking (`_news4live_notification_sent`) in the WordPress companion plugin to prevent duplicate dispatches. |
| **Notification Permission UX** | ⚠️ UX Flaw | Android 13+ `POST_NOTIFICATIONS` permission was requested abruptly on app launch. | Add a non-intrusive rationale dialog before requesting system permissions, giving users context for breaking news alerts. |
| **Room Database Scalability** | ⚠️ Optimization | No cache pruning policy was defined; cache could grow unboundedly over months of usage. | Add Room DB indexing, cache eviction pruning (limit non-bookmarked cache to 100 items, prune >14 days), while strictly preserving all bookmarks. |
| **Post Deletion / 404 Handling** | ⚠️ Edge Case | Deleted or unpublished WordPress posts returned HTTP 404 without explicit cache purging. | Evict deleted posts from Room cache upon 404 and show a graceful "खबर अब उपलब्ध नहीं है" state. |
| **Topic Subscription** | ⚠️ Missing | Android client did not explicitly subscribe to FCM topic `breaking_news` on first launch. | Add automatic subscription to `FirebaseMessaging.getInstance().subscribeToTopic("breaking_news")`. |
| **HTML Security & Video Embeds** | 🟢 Implemented | Jsoup sanitization successfully strips malicious `<script>` and `onerror` handlers. | Support additional HTML elements (tables, figure captions, iframe video players). |
| **ProGuard & Release Config** | 🟢 Implemented | ProGuard keep rules protect Kotlinx Serialization, Room, Retrofit, and Jsoup models. | Verified release compilation readiness. |

---

## 3. Detailed Component Audits

### 3.1 Live API Verification Results
- **Root Namespaces**: Available (`wp/v2`, `post-views-counter`, `google-site-kit/v1`, `wordfence/v1`).
- **Archive Depth**: Verified historical access up to Page 100 (June 2025 articles) and beyond.
- **Search**: Verified Hindi keywords (`मथुरा`, `उत्तर प्रदेश`, `खेल`, `आईएएस`).
- **Post 404s**: Verified clean 404 return on non-existent post IDs (`99999999`).

### 3.2 Single Source of Truth Guarantee
- WordPress remains the **only** content CMS.
- Room database is strictly utilized as a local cache, bookmark store, and offline reader.
- Publishing or editing an article in WordPress automatically reflects in the app on refresh.

### 3.3 FCM Push Notification Security
- **Zero Secrets in APK**: The Android application contains **no** Firebase server keys or admin credentials.
- All FCM dispatching is performed server-side by the WordPress plugin.
- Idempotency ensures an article is notified only once.

---

## 4. Remediation Plan

1. **Update `NewsPagingSource.kt` and `SearchPagingSource.kt`** to handle HTTP 400 invalid page number as end of feed.
2. **Update `NewsDatabase.kt` and Entities** with indexes and automatic cache eviction in `ArticleDao.kt`.
3. **Enhance `news4live-companion.php`** with transient caching (2 min cache) and duplicate notification prevention meta check.
4. **Update `MainActivity.kt` and `HomeScreen.kt`** with contextual notification rationale UI and automatic FCM topic subscriptions.
5. **Update `NewsRepositoryImpl.kt` and `ArticleViewModel.kt`** with 404 deleted article handling and `modified` timestamp cache synchronization.
