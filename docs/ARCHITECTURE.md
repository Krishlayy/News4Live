# News 4 Live Android Application — Architecture Guide

A modern native Android application built with **Kotlin**, **Jetpack Compose**, **Material 3**, and **Clean Architecture** (Presentation, Domain, Data layers).

---

## 1. Architectural Layers

```
                                 ┌─────────────────────────┐
                                 │   UI / Presentation     │
                                 │   - Jetpack Compose     │
                                 │   - Material 3 Theme    │
                                 │   - StateFlow ViewModels│
                                 └────────────┬────────────┘
                                              │
                                              ▼
                                 ┌─────────────────────────┐
                                 │      Domain Layer       │
                                 │   - Use Cases           │
                                 │   - Business Logic      │
                                 │   - Domain Models       │
                                 └────────────┬────────────┘
                                              │
                                              ▼
                                 ┌─────────────────────────┐
                                 │       Data Layer        │
                                 │   - Retrofit Network    │
                                 │   - Room Database (Cache│
                                 │   - Paging 3 Pager      │
                                 └────────────┬────────────┘
                                              │
                    ┌─────────────────────────┴─────────────────────────┐
                    ▼                                                   ▼
       ┌─────────────────────────┐                         ┌─────────────────────────┐
       │   WordPress REST API    │                         │      Room Database      │
       │   (Single Source Truth) │                         │   (Offline & Bookmarks) │
       └─────────────────────────┘                         └─────────────────────────┘
```

---

## 2. Key Components

### 2.1 Presentation Layer (`com.news4live.app.presentation`)
- **Jetpack Compose**: Modern declarative UI framework without legacy XML layouts.
- **Material 3**: Editorial brand color scheme (`EditorialRed`, `EditorialNavy`, `DarkBackground`).
- **MVI / MVVM State Pattern**: Every screen has a dedicated ViewModel exposing immutable `StateFlow<UiState>` ensuring zero UI glitching on configuration changes (screen rotations, dark mode toggles).

### 2.2 Domain Layer (`com.news4live.app.domain`)
- **Pure Kotlin**: No Android framework dependencies.
- **Use Cases**: Single responsibility interactors (`GetHomeFeedUseCase`, `GetPagedArticlesUseCase`, `HtmlBlockParserUseCase`, `BookmarkUseCases`).
- **Clean Models**: Decoupled from WordPress network JSON schema.

### 2.3 Data Layer (`com.news4live.app.data`)
- **WordPress REST API (`wp/v2`)**: Fetches live articles, categories, search results, and embeds.
- **Paging 3**: Efficiently pages through 5,508+ historical articles with automatic memory discarding.
- **Room Database**:
  - `articles`: Offline cache for recently read and home feed articles.
  - `bookmarks`: User-saved articles available offline.
  - `categories`: Locally cached category names and post counts.
  - `recent_searches`: Search keyword history.
- **OkHttp Disk Cache**: 50MB HTTP disk cache with `Cache-Control` header management.

### 2.4 HTML Parsing & Security
- **Jsoup Sanitization**: Strips dangerous scripts and unauthorized iframes before UI rendering.
- **Type-Safe Compose Blocks**: Renders headings, paragraphs, responsive images, blockquotes, bullet lists, and video embeds natively in Compose.

---

## 3. Offline & Connectivity Strategy

1. **Network First, Room Cache Fallback**: When online, articles are fetched from WordPress and transparently cached in Room.
2. **Offline Reading**: If a user is offline (Airplane mode, no signal in rural areas), the app automatically renders cached articles and saved bookmarks without infinite loading spinners.
3. **Optimized Image Loading**: Uses Coil with memory caching, disk caching, and downsampled thumbnail requests (`news-portal-block-thumb` / `medium_large`).
