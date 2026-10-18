# WordPress REST API Integration Report — News 4 Live

**Target Domain**: `https://news4live.com/`  
**Inspection Date**: September 2026  
**Total Published Articles**: 5,508+ articles  
**Total Pages**: 1,102+ pages (at 5 posts/page default)  
**REST API Base**: `https://news4live.com/wp-json/`

---

## 1. Live Endpoint Analysis & Responses

### 1.1 Root Namespaces (`GET /wp-json/`)
The WordPress site exposes standard and plugin REST namespaces:
- `wp/v2` (Core WordPress REST API)
- `post-views-counter` (Post view analytics)
- `google-site-kit/v1` (Google Site Kit)
- `wordfence/v1` (Security firewall)
- `oembed/1.0` (Media embeds)

### 1.2 Posts Endpoint (`GET /wp-json/wp/v2/posts?_embed=1`)
- **Pagination Header**: `X-WP-Total` and `X-WP-TotalPages`
- **Fields**: `id`, `date`, `modified`, `slug`, `link`, `title.rendered`, `content.rendered`, `excerpt.rendered`, `featured_media`, `categories`, `tags`, `_embedded`
- **Embedded Media**: `_embedded['wp:featuredmedia'][0]['source_url']` and `media_details.sizes`
- **Responsive Sizes Available**:
  - `news-portal-block-medium` (300x200) — Used for standard article cards.
  - `news-portal-slider-medium` (750x450) — Used for hero featured stories.
  - `medium_large` (768px width) — Used for article reading page.
  - `thumbnail` (150x150) — Used for compact search results.
  - `full` — Used for full-screen image lightbox.

---

## 2. Category Hierarchy & Mappings

The live website categories were discovered and mapped:

| Category ID | Hindi Name | Slug | Purpose in Android App |
| :--- | :--- | :--- | :--- |
| **31** | **ब्रेकिंग न्यूज़** | `breaking-news` | Breaking News Ticker & Push Notifications |
| **18** | **टॉप न्यूज़** | `top-news` | Top Stories & Hero Editorial Card |
| **8** | **देश** | `desh` | National News Tab |
| **37** | **उत्तर प्रदेश** | `uttar-pradesh` | State News Tab |
| **4** | **मथुरा समाचार** | `mathura-samachar` | Local Mathura News Tab |
| **41** | **हाथरस** | `hathras` | Local Hathras News Tab |
| **9** | **बृज दर्शन** | `brij-darshan` | Religious / Cultural News Tab |
| **13** | **ज्योतिष** | `jyotish` | Daily Horoscope / Astrology Tab |
| **17** | **टेक न्यूज़** | `tech-news` | Technology News Tab |
| **15** | **बिज़निस** | `business` | Business / Economy Tab |
| **16** | **स्पोर्ट्स** | `sports` | Sports News Tab |
| **2** | **यूथ** | `youth` | Youth & Education Tab |

---

## 3. HTML Content & Typography Handling

- **Entities**: WordPress outputs encoded characters (e.g., `&#8211;` for en-dash, `&amp;` for `&`, `&#8216;` and `&#8217;` for single quotes).
  - The Android app parses and unescapes all titles and excerpts via `HtmlCompatHelper.kt`.
- **Sanitization**: Raw `content.rendered` HTML is sanitized with **Jsoup** (`Safelist.relaxed()`) before rendering in Compose, neutralizing any malicious `<script>` or unexpected iframe tags.
- **Hindi Typography**: `HindiNewsTypography` uses Devanagari-optimized font heights (line height 28sp to 34sp) ensuring Hindi *matras* (`ि`, `ी`, `ु`, `ू`, `े`, `ै`, `ं`, `्`) are never clipped.

---

## 4. Companion Plugin Optimization

To minimize mobile battery and data usage over Indian 3G/4G networks, the optional `news4live-companion` WordPress plugin provides:
1. `GET /wp-json/news4live/v1/home` — Returns Breaking, Featured, Latest, and Categories in **1 single network round-trip**.
2. **FCM Webhook** — Fires when a post is published, sending high-priority breaking news push notifications with image URLs to Android clients.
