# MarkMD

Beautiful, fast, native Android Markdown reader and editor built with Jetpack Compose.

## Features

### Reader

- **GitHub-style Rendering** — Headings, paragraphs, lists, blockquotes, tables, code blocks, images, and links rendered with pixel-perfect precision using pure Compose (no WebView)
- **Syntax Highlighting** — Fenced code blocks with language-aware highlighting and copy button
- **Table of Contents** — Bottom sheet listing all headings; tap any entry to scroll instantly to that section
- **Full-text Search** — Search bar with previous/next navigation; matches are highlighted inline inside the rendered text — including headings, paragraphs, tables, and code blocks
- **Share Document** — Share the raw `.md` file via any app on the device

### Editor

- **Raw Markdown Editor** — Edit document content directly; changes are saved back to the original file URI
- **Auto-reload on Save** — Viewer automatically reloads the document when the editor saves, without any manual refresh

### Home

- **Recent Documents** — Persisted list of recently opened files; swipe left to delete from history
- **Open from Anywhere** — Pick files from the system file picker, cloud storage (Drive, Dropbox), email attachments, share sheet, or deep links
- **Import from URL** — Enter a direct URL to any raw `.md` file; content is downloaded and opened immediately
- **Paste from Clipboard** — Paste markdown text directly from clipboard, give it a name, and open it instantly
- **Save to Local Storage** — Imported/clipboard documents show a save icon; tap to export a permanent copy anywhere on device via the system file picker

### Appearance & Settings

- **5 Themes** — System (follows OS), Light, Dark, Sepia, AMOLED Black
- **Font Size** — Adjustable font size (10–32 sp) per-reader, persisted in settings
- **Font Family** — Choose from Serif, Sans-serif, or Monospace
- **Keep Screen On** — Prevent screen timeout while reading
- **Show Line Numbers** — Toggle line numbers in the editor

## Tech Stack

| Category | Library / Version |
| -------- | ----------------- |
| Language | Kotlin 2.1.0 |
| UI | Jetpack Compose + Material 3 (BOM 2025.02.00) |
| Architecture | MVVM + Clean Architecture |
| DI | Hilt 2.55 |
| Navigation | Navigation Compose 2.8.7 (type-safe routes) |
| Database | Room 2.6.1 |
| Preferences | DataStore Preferences 1.1.2 |
| Markdown Renderer | `com.mikepenz:multiplatform-markdown-renderer-m3` 0.31.0 |
| Syntax Highlighting | `multiplatform-markdown-renderer-code` 0.31.0 |
| Image Loading | Coil 3.1.0 |

## Project Structure

```
app/
├── data/
│   ├── local/
│   │   ├── db/               # Room: DocumentEntity, DocumentDao, AppDatabase
│   │   └── datastore/        # SettingsDataStore (theme, fontSize, fontFamily, etc.)
│   ├── model/                # AppTheme, TocEntry, Document, FontFamily
│   └── repository/           # DocumentRepository (+ fileSaved SharedFlow), SettingsRepository
├── di/                       # Hilt: AppModule, DatabaseModule
├── domain/
│   ├── parser/               # splitMarkdownByHeadings, TocParser
│   └── usecase/              # ReadFileUseCase, WriteFileUseCase, ParseTocUseCase,
│                             #   GetRecentDocumentsUseCase, SaveProgressUseCase,
│                             #   ImportUrlUseCase, SaveClipboardUseCase
└── ui/
    ├── navigation/           # Routes, AppNavGraph
    ├── screen/
    │   ├── home/             # HomeScreen + HomeViewModel (recent docs, open file, delete)
    │   ├── viewer/           # ViewerScreen + ViewerViewModel (render, search, ToC, share)
    │   │   └── components/   # MarkdownViewer, TocSheet, ReaderSettingsSheet, SearchBar
    │   ├── editor/           # EditorScreen + EditorViewModel (raw edit + save)
    │   └── settings/         # SettingsScreen + SettingsViewModel
    └── theme/                # MarkMDTheme, Color, Type
```

## Getting Started

1. Clone the repository
2. Open in Android Studio Ladybug or later
3. Sync project with Gradle files
4. Run on device or emulator (minSdk 26)

```bash
./gradlew :app:assembleDebug
```

## License

MIT License
