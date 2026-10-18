# Firebase Cloud Messaging (FCM) & Push Notifications Guide

This guide explains how to connect your WordPress site (`https://news4live.com/`) to Firebase Cloud Messaging (FCM) so that breaking news and new articles trigger instant push notifications on Android devices.

---

## 1. Firebase Console Setup

1. Go to the [Firebase Console](https://console.firebase.google.com/).
2. Create a new project named **News4Live**.
3. Add an Android app with package name `com.news4live.app`.
4. Download `google-services.json` and place it inside the `app/` directory:
   ```
   News4Live/
   └── app/
       └── google-services.json
   ```
5. Navigate to **Project Settings** -> **Cloud Messaging** tab.
6. Copy your **Server Key** (or create a Service Account Key for HTTP v1).

---

## 2. WordPress Companion Plugin Setup

1. Copy the `wp-plugin/news4live-companion` folder to your WordPress server at:
   `/wp-content/plugins/news4live-companion/`
2. Log in to **WordPress Admin** -> **Plugins** -> **Installed Plugins**.
3. Click **Activate** on **News 4 Live App Companion**.
4. Go to **Settings** -> **News 4 Live App**.
5. Paste your **Firebase Cloud Messaging Server Key** in the input field and click **Save Settings**.

---

## 3. How Push Notifications Work

```
Editor writes News in WordPress Admin
               │
               ▼
Clicks 'Publish' (or checks 'Mark as Breaking News')
               │
               ▼
WordPress Hook (`transition_post_status`) executes
               │
               ▼
Extracts Title, Excerpt, Image URL & Post ID
               │
               ▼
Dispatches high-priority payload to FCM API
               │
               ▼
Firebase delivers notification to Android Devices
               │
               ▼
User taps notification -> Directly opens Article Screen
```

### Conceptual Push Payload:
```json
{
  "to": "/topics/breaking_news",
  "priority": "high",
  "data": {
    "post_id": "23425",
    "title": "आईएएस सलोनी गौतम ने छात्राओं का मार्गदर्शन किया",
    "excerpt": "काउंसलिंग सत्र में वर्ष 2025 बैच की आईएएस सलोनी गौतम ने सफलता के सुझाव दिए...",
    "image": "https://news4live.com/wp-content/uploads/2026/09/IMG-20260908-WA0031.jpg",
    "url": "https://news4live.com/saloni-gautam-news/"
  },
  "notification": {
    "title": "🔴 ब्रेकिंग: आईएएस सलोनी गौतम ने छात्राओं का मार्गदर्शन किया",
    "body": "काउंसलिंग सत्र में वर्ष 2025 बैच की आईएएस सलोनी गौतम ने सफलता के सुझाव दिए...",
    "image": "https://news4live.com/wp-content/uploads/2026/09/IMG-20260908-WA0031.jpg"
  }
}
```

---

## 4. Android App Notification Handling

- Handled by [`NewsFirebaseMessagingService.kt`](file:///c:/News4Live/app/src/main/java/com/news4live/app/notifications/NewsFirebaseMessagingService.kt).
- Displays rich **BigPictureStyle** notifications with headline, summary, and article image.
- Tapping the notification launches the app directly into the article detail view.
