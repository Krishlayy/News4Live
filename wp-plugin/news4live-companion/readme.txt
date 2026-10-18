=== News 4 Live App Companion ===
Contributors: news4live
Tags: news, rest api, fcm, push notifications, android
Requires at least: 5.8
Tested up to: 6.6
Stable tag: 1.0.0
License: GPLv2 or later

High performance companion plugin for News 4 Live Android application.

== Description ==

This plugin enhances your WordPress website for the native News 4 Live Android app:
- Provides `/wp-json/news4live/v1/home` aggregated endpoint to load breaking news, top stories, categories, and latest articles in a single network round-trip.
- Automatically sends Firebase Cloud Messaging (FCM) push notifications with images when articles or breaking stories are published.
- Adds an editorial "Breaking News" checkbox to the post editor.
- 100% non-destructive: does not modify WordPress core tables or existing posts.

== Installation ==

1. Upload the `news4live-companion` folder to your `/wp-content/plugins/` directory.
2. Activate the plugin through the 'Plugins' menu in WordPress Admin.
3. Go to Settings -> News 4 Live App and enter your Firebase Cloud Messaging Server Key.
