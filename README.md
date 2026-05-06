# MarkMD

Beautiful, fast, native Android Markdown reader and editor built with Jetpack Compose.

## Features

- **Beautiful Rendering** — Headers, lists, code blocks, tables, images, links — all rendered with pixel-perfect precision
- **Light & Dark Mode** — Follows system preference or manual toggle (5 themes: System, Light, Dark, Sepia, AMOLED)
- **Syntax Highlighting** — Code blocks with full language support and copy button
- **Open From Anywhere** — File manager, cloud storage (Drive, Dropbox), email, share sheet, deep link
- **Markdown Editor** — Edit raw markdown with syntax awareness
- **Table of Contents** — Navigate through document headings
- **Font Customization** — Choose font family (Serif, Sans-serif, Monospace) + adjust font size
- **Fast & Lightweight** — Instant open, buttery smooth scroll, no bloat
- **Native Android** — Pure Jetpack Compose, no WebView

## Tech Stack

- **Language**: Kotlin 2.1.x
- **UI**: Jetpack Compose + Material 3
- **Architecture**: MVVM + Clean Architecture
- **DI**: Hilt 2.55
- **Navigation**: Navigation Compose 2.8+ (type-safe routes)
- **Database**: Room 2.7+
- **Preferences**: DataStore Preferences
- **Markdown Renderer**: `com.mikepenz:multiplatform-markdown-renderer` (pure Compose)
- **Syntax Highlighting**: `multiplatform-markdown-renderer-code` (Highlights lib)
- **Image Loading**: Coil 3

## Project Structure

```
app/
├── data/
│   ├── local/
│   │   ├── db/               # Room: DocumentEntity, DocumentDao, AppDatabase
│   │   └── datastore/        # SettingsDataStore (theme, fontSize)
│   ├── model/                # AppTheme, TocEntry, Document, FontFamily
│   └── repository/           # DocumentRepository, SettingsRepository
├── di/                       # Hilt: AppModule, DatabaseModule
├── domain/
│   ├── parser/               # TocParser — parse headings from markdown
│   └── usecase/              # ReadFileUseCase, ParseTocUseCase, etc.
└── ui/
    ├── navigation/           # Routes, AppNavGraph
    ├── screen/
    │   ├── home/             # HomeScreen + HomeViewModel
    │   ├── viewer/           # ViewerScreen + ViewerViewModel
    │   ├── editor/           # EditorScreen + EditorViewModel
    │   └── settings/         # SettingsScreen + SettingsViewModel
    └── theme/                # MarkMDTheme, Color, Type
```

## Getting Started

1. Clone the repository
2. Open in Android Studio Ladybug or later
3. Sync project with Gradle files
4. Run on device or emulator (minSdk 26)

## Building

```bash
./gradlew :app:assembleDebug
```

## License

MIT License
