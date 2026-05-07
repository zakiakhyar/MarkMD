# CodeMD: Markdown Reader & Editor

Beautiful, fast, native Android Markdown reader and editor built with Jetpack Compose.

**Application ID:** `com.codemd.reader`  
**Website:** [codemd.zaki.my.id](https://codemd.zaki.my.id)  
**Privacy Policy:** [codemd.zaki.my.id/privacy-policy.html](https://codemd.zaki.my.id/privacy-policy.html)  
**Terms of Service:** [codemd.zaki.my.id/terms-of-service.html](https://codemd.zaki.my.id/terms-of-service.html)

---

## Features

### Reader

- **GitHub-style Rendering** — Headings, paragraphs, lists, blockquotes, tables, code blocks, images, and links rendered with pixel-perfect precision using pure Compose (no WebView)
- **Inline Bold & Italic** — Full inline markdown span support (`**bold**`, `*italic*`, `~~strikethrough~~`) rendered correctly in all contexts
- **Syntax Highlighting** — Fenced code blocks with language-aware highlighting and copy button
- **Table of Contents** — Bottom sheet listing all headings; tap any entry to scroll instantly to that section
- **Full-text Search** — Search FAB with previous/next navigation; matches highlighted inline in rendered text
- **Focus Mode** — Tap the fullscreen FAB to hide all UI chrome for distraction-free reading; tap the exit FAB to return
- **8 Reading Themes** — System, Light, Dark, Sepia, AMOLED, Dark Blue, Dark Green, Solarized
- **Share Document** — Share the raw `.md` file via any app on the device
- **Export to PDF** — Export document as PDF to Downloads folder

### Editor

- **Raw Markdown Editor** — Edit document content directly; changes saved back to original file URI
- **Auto-reload on Save** — Viewer automatically reloads when the editor saves

### Home

- **Recent Documents** — Persisted list of recently opened files; swipe left to delete from history
- **Pin Documents** — Pin frequently used files to the top of the list
- **Open from Anywhere** — File picker, cloud storage (Drive, Dropbox), email attachments, share sheet, or deep links
- **Import from URL** — Download and open any raw `.md` file from a direct URL
- **Paste from Clipboard** — Paste markdown text from clipboard, name it, and open instantly
- **Save to Local Storage** — Export imported/clipboard documents permanently via the system file picker

### Appearance & Settings

- **App Themes** — System, Light, Dark, Sepia, AMOLED, Dark Blue (follows OS or user choice)
- **8 Reading Themes** — Independent theme for the viewer (System, Light, Dark, Sepia, AMOLED, Dark Blue, Dark Green, Solarized)
- **Font Size** — Adjustable 10–32 sp with pinch-to-zoom gesture in viewer; default 12 sp
- **6 Bundled Font Families** — System Default, Lora, Merriweather, Literata, Atkinson Hyperlegible, Source Code Pro (TTF files bundled in-app, no network required)
- **Keep Screen On** — Prevent screen timeout while reading
- **Show Line Numbers** — Toggle line numbers in the editor

---

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
| Fonts | Bundled TTF in `res/font/` (Lora, Merriweather, Literata, Atkinson Hyperlegible, Source Code Pro) |

---

## Project Structure

```
app/
├── data/
│   ├── local/
│   │   ├── db/               # Room: DocumentEntity, DocumentDao, AppDatabase
│   │   └── datastore/        # SettingsDataStore (theme, readingTheme, fontSize, fontFamily…)
│   ├── model/                # AppTheme, ReadingTheme, TocEntry, Document, FontFamily
│   └── repository/           # DocumentRepository, SettingsRepository
├── di/                       # Hilt: AppModule, DatabaseModule
├── domain/
│   ├── parser/               # splitMarkdownByHeadings, TocParser
│   └── usecase/              # ReadFileUseCase, WriteFileUseCase, ParseTocUseCase,
│                             #   GetRecentDocumentsUseCase, ImportUrlUseCase,
│                             #   SaveClipboardUseCase, SaveProgressUseCase
└── ui/
    ├── navigation/           # Routes, AppNavGraph
    ├── screen/
    │   ├── home/             # HomeScreen + HomeViewModel
    │   ├── viewer/           # ViewerScreen + ViewerViewModel
    │   │   └── components/   # MarkdownViewer, GitHubMarkdownStyle, TocSheet,
    │   │                     #   ReaderSettingsSheet, SearchBar
    │   ├── editor/           # EditorScreen + EditorViewModel
    │   └── settings/         # SettingsScreen + SettingsViewModel
    └── theme/                # AppTheme, Color, Type, AppFonts
```

---

## Build

```bash
# Debug
./gradlew :app:assembleDebug

# Release (R8 minify + resource shrink enabled)
./gradlew :app:assembleRelease
```

Minimum SDK: **26** (Android 8.0)  
Target SDK: **36**

---

## License

MIT License
