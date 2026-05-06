# MarkMD — Android Markdown Reader (Kotlin Native + Latest Stack)

## Overview

Build a **beautiful, fast, native Android Markdown reader and editor** called **MarkMD** using the latest Kotlin and Jetpack Compose stack. The app opens `.md` files from any source (file manager, cloud storage, share intent, email attachments), renders them with pixel-perfect precision, supports live editing with preview, exports to PDF, and remembers reading progress per file.

### Must-Have Features
- **Beautiful Rendering** — headers, lists, code blocks, tables, images, links — all rendered precisely
- **Light & Dark Mode** — follows system preference or manual toggle (5 themes: System, Light, Dark, Sepia, AMOLED)
- **Syntax Highlighting** — code blocks with full language support, horizontal scroll, copy button
- **Open From Anywhere** — file manager, cloud storage (Drive, Dropbox), email, share sheet, deep link
- **Markdown Editor** — edit raw markdown with live preview (split or toggle mode)
- **PDF Export** — export the rendered document to PDF, saved to Downloads or shared
- **Font Customization** — choose font family (Serif, Sans-serif, Monospace, custom) + adjust font size
- **Fast & Lightweight** — instant open, buttery smooth scroll, no bloat
- **Native Android** — pure Jetpack Compose, no WebView, no hybrid

---

## Tech Stack (Best 2025)

| Layer | Choice | Reason |
|---|---|---|
| Language | **Kotlin 2.1.x** | Latest stable, required by best MD renderer |
| UI | **Jetpack Compose + Material 3** | Native, declarative, no XML |
| Architecture | **MVVM + Clean Architecture** | Separation of concerns, testable |
| DI | **Hilt 2.5x** | First-party, minimal boilerplate |
| Navigation | **Navigation Compose 2.8+** (type-safe routes) | Type-safe, no string routes |
| Database | **Room 2.7+** | Recent docs, scroll progress |
| Preferences | **DataStore Preferences** | Async, coroutine-native |
| **Markdown Renderer** | **`com.mikepenz:multiplatform-markdown-renderer`** latest | Pure Compose, no AndroidView, best quality |
| Syntax Highlighting | **`multiplatform-markdown-renderer-code`** (Highlights lib) | Auto dark/light, 50+ languages, copy button |
| Image Loading | **Coil 3** + `multiplatform-markdown-renderer-coil3` | Kotlin-first, Compose-native, smallest footprint |
| Build | **AGP 8.7+**, compileSdk 36, minSdk 26 | Required for renderer + latest APIs |

> **Why `multiplatform-markdown-renderer` over Markwon?**
> Markwon uses `AndroidView` bridge — not native Compose. `multiplatform-markdown-renderer` is pure Compose, supports `LazyColumn` for large files, has built-in table rendering with horizontal scroll, syntax highlighting via `Highlights`, and is actively maintained (80+ releases).

---

## Project Structure

```
app/
├── data/
│   ├── local/
│   │   ├── db/               # Room: DocumentEntity, DocumentDao, AppDatabase
│   │   └── datastore/        # PreferencesDataStore (theme, fontSize)
│   ├── model/                # AppTheme, TocEntry, Document
│   └── repository/           # DocumentRepository, SettingsRepository
├── di/                       # Hilt: AppModule, DatabaseModule, RepositoryModule
├── domain/
│   ├── parser/               # TocParser — parse headings from markdown string
│   └── usecase/              # ReadFileUseCase, ParseTocUseCase, SaveProgressUseCase,
│                             # GetRecentDocumentsUseCase
└── ui/
    ├── navigation/           # AppNavGraph — type-safe routes
    ├── screen/
    │   ├── home/             # HomeScreen + HomeViewModel
    │   ├── viewer/           # ViewerScreen + ViewerViewModel
    │   │   └── components/
    │   │       ├── MarkdownViewer.kt     # Core composable — pure Compose
    │   │       └── TopAppBarViewer.kt
    │   └── settings/         # SettingsScreen + SettingsViewModel
    └── theme/                # MdReaderTheme, ThemeTokens, Type
```

---

## Core Features

### 1. File Opening

- **Internal picker** — `ActivityResultContracts.OpenDocument`, filter `text/markdown`, `text/plain`, `*/*`
- **External intent** — `ACTION_VIEW` and `ACTION_SEND` from file managers, cloud storage, email, share sheet
- **Persistable URI permission** — `contentResolver.takePersistableUriPermission` for re-open without permission dialog
- **`ReadFileUseCase` fallback chain**:
  1. `openInputStream` — standard SAF
  2. `openFileDescriptor`
  3. MediaStore `_data` path (legacy)
  4. DocumentsContract path fallback
- **Cache strategy** — read and cache content to internal storage immediately on first open, navigate with cache URI to avoid permission expiry

### 2. Markdown Rendering

Use `com.mikepenz.markdown.m3.Markdown` composable — **pure Compose, no AndroidView, no XML**.

```kotlin
@Composable
fun MarkdownViewer(
    content: String,
    lazyListState: LazyListState,
    theme: AppTheme,
    fontSize: Int,
    scrollToAnchor: String?,
    onAnchorScrolled: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = when (theme) {
        AppTheme.DARK, AppTheme.AMOLED -> true
        AppTheme.LIGHT, AppTheme.SEPIA -> false
        AppTheme.SYSTEM -> isSystemInDarkTheme()
    }
    val highlightsBuilder = remember(isDark) {
        Highlights.Builder().theme(SyntaxThemes.atom(darkMode = isDark))
    }
    val markdownState = rememberMarkdownState(content)

    // TOC scroll: observe scrollToAnchor, find item index by heading text, scrollToItem()
    LaunchedEffect(scrollToAnchor) {
        scrollToAnchor ?: return@LaunchedEffect
        // find heading position in markdownState and scroll via lazyListState
        onAnchorScrolled()
    }

    Markdown(
        markdownState = markdownState,
        colors = markdownColor(
            text = MaterialTheme.colorScheme.onBackground,
            codeText = MaterialTheme.colorScheme.onSurfaceVariant,
            codeBackground = MaterialTheme.colorScheme.surfaceVariant,
            linkText = MaterialTheme.colorScheme.primary,
        ),
        typography = markdownTypography(
            paragraph = MaterialTheme.typography.bodyLarge.copy(fontSize = fontSize.sp),
            code = MaterialTheme.typography.bodyMedium.copy(fontSize = (fontSize - 1).sp),
            // h1–h6 scaled relative to fontSize
        ),
        components = markdownComponents(
            codeBlock = {
                MarkdownHighlightedCodeBlock(
                    content = it.content,
                    node = it.node,
                    highlightsBuilder = highlightsBuilder,
                    showHeader = true,          // shows language label + copy button
                )
            },
            codeFence = {
                MarkdownHighlightedCodeFence(
                    content = it.content,
                    node = it.node,
                    highlightsBuilder = highlightsBuilder,
                    showHeader = true,
                )
            },
        ),
        imageTransformer = Coil3ImageTransformerImpl,
        success = { state, components, mod ->
            LazyMarkdownSuccess(           // LazyColumn for large files
                state = state,
                components = components,
                lazyListState = lazyListState,
                modifier = mod,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            )
        },
        modifier = modifier,
    )
}
```

- **Code blocks** — horizontal scroll built-in, 1-touch, with language header + copy button
- **Tables** — native grid rendering, horizontal scroll if wider than screen
- **Images** — async loading via Coil 3, animated size change
- **Large files** — `LazyMarkdownSuccess` uses `LazyColumn` for efficient rendering

### 3. Table of Contents (TOC)

```kotlin
data class TocEntry(val level: Int, val title: String, val anchor: String)

// ParseTocUseCase
val regex = Regex("""^(#{1,6})\s+(.+)""", RegexOption.MULTILINE)
regex.findAll(content).map { match ->
    val level = match.groupValues[1].length
    val title = match.groupValues[2].trim()
    val anchor = title.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')
    TocEntry(level, title, anchor)
}
```

- **`ExtendedFloatingActionButton`** bottom-right — "Contents", only shown when TOC non-empty
- **`ModalBottomSheet`** — TOC list with heading-level indentation, `ListItem` per entry
- **Scroll to heading** — pass `LazyListState` down, find item index by heading text match, call `lazyListState.animateScrollToItem(index)`

### 4. Markdown Editor

- **Two modes**: `READ` and `EDIT` — toggle via FAB or `TopAppBar` icon
- **Edit mode** — `BasicTextField` or `OutlinedTextField` with:
  - Raw markdown input, monospace font, line numbers optional
  - Toolbar above keyboard: **B**, *I*, `code`, link, heading, list shortcuts
  - Auto-indent on Enter inside list items
- **Preview mode** — same `MarkdownViewer` composable, reused
- **Split mode** (landscape / tablet) — `Row` with editor left, preview right, synced scroll
- **Auto-save** — save to file on every edit, debounced 1s; dirty state indicator in title
- **New file** — create blank `.md` in app's internal storage, user can rename and export

```kotlin
enum class ViewerMode { READ, EDIT, SPLIT }

data class EditorUiState(
    val content: String,
    val cursorPosition: TextRange,
    val isDirty: Boolean,
    val mode: ViewerMode
)
```

### 5. PDF Export

- **Trigger** — share icon in `TopAppBar` → bottom sheet with "Export as PDF" option
- **Implementation** — use Android's built-in `PdfDocument` API + `PrintedPdfDocument` or `android.print` framework:
  ```kotlin
  // Render Compose content to PDF via PrintManager or PdfDocument
  // For rich layout fidelity, use WebView in headless mode to render HTML→PDF
  // then delete WebView after PDF is generated (WebView only used for export, not rendering)
  ```
- **Output** — save to `MediaStore.Downloads` (API 29+) or `Environment.DIRECTORY_DOWNLOADS` (API 28)
- **Share** — offer `ShareCompat.IntentBuilder` immediately after export with `application/pdf` MIME type
- **Page size** — A4 default, user can select A4 / Letter in settings
- **Font in PDF** — matches the selected app font family

### 6. Themes

```kotlin
enum class AppTheme { SYSTEM, LIGHT, DARK, SEPIA, AMOLED }
```

| Theme | Background | Text | Code BG |
|---|---|---|---|
| Light | `#FFFFFF` | `#1A1A1A` | `#EEF0F2` |
| Dark | `#1E1E1E` | `#E8E8E8` | `#2B2B2B` |
| Sepia | `#F5ECD7` | `#3B2B1A` | `#E8DCC8` |
| AMOLED | `#000000` | `#EEEEEE` | `#1A1A1A` |
| System | follows OS | follows OS | follows OS |

- Toggle button in `TopAppBar` cycles through all 5 themes
- Persisted to DataStore, restored on next launch
- `markdownColor()` and `markdownTypography()` receive theme-aware values

### 7. Font Customization

- **Font family** — 4 built-in options:
  | Option | Typeface | Best for |
  |---|---|---|
  | System Default | `FontFamily.Default` | General reading |
  | Serif | `FontFamily.Serif` | Long-form articles |
  | Monospace | `FontFamily.Monospace` | Technical docs |
  | OpenDyslexic | bundled `.ttf` asset | Accessibility |
- **Font size** — slider 10sp–28sp, default 16sp; step 1sp
- Both persisted to DataStore (`fontFamily: String`, `fontSize: Int`)
- Applied to `markdownTypography()` — all heading scales relative to base font size
- Also applied to editor `BasicTextField` font

```kotlin
enum class AppFont { SYSTEM, SERIF, MONOSPACE, OPEN_DYSLEXIC }

fun AppFont.toFontFamily(context: Context): FontFamily = when (this) {
    AppFont.SYSTEM -> FontFamily.Default
    AppFont.SERIF -> FontFamily.Serif
    AppFont.MONOSPACE -> FontFamily.Monospace
    AppFont.OPEN_DYSLEXIC -> FontFamily(Font(R.font.open_dyslexic))
}
```

### 8. Reading Progress

- `DocumentEntity` in Room: `uri`, `fileName`, `lastOpenedAt`, `scrollIndex`, `scrollOffset`
- Save on scroll — debounced 500ms — save `lazyListState.firstVisibleItemIndex` + `firstVisibleItemScrollOffset`
- Restore — `rememberLazyListState(initialFirstVisibleItemIndex, initialFirstVisibleItemScrollOffset)`

### 9. Recent Documents

- `HomeScreen` — `LazyColumn` list of recent files, sorted by `lastOpenedAt` DESC
- Each item shows file name + last opened date + `READ` / `EDIT` quick action chips
- Swipe to delete from recents
- `+` FAB opens file picker
- `✎` icon on each item opens directly in edit mode

---

## Architecture

### ViewerViewModel

```kotlin
@HiltViewModel
class ViewerViewModel @Inject constructor(
    private val readFileUseCase: ReadFileUseCase,
    private val writeFileUseCase: WriteFileUseCase,       // NEW: save edits
    private val exportPdfUseCase: ExportPdfUseCase,      // NEW: PDF export
    private val parseTocUseCase: ParseTocUseCase,
    private val saveProgressUseCase: SaveProgressUseCase,
    private val documentRepository: DocumentRepository,
    private val settingsRepository: SettingsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val rawUri: String = checkNotNull(savedStateHandle["uri"])

    val uiState: StateFlow<ViewerUiState>        // Loading | Success | Error
    val appTheme: StateFlow<AppTheme>            // from DataStore
    val fontSize: StateFlow<Int>                 // from DataStore, default 16
    val appFont: StateFlow<AppFont>              // NEW: from DataStore
    val viewerMode: StateFlow<ViewerMode>        // NEW: READ | EDIT | SPLIT
    val isTocOpen: StateFlow<Boolean>
    val exportState: StateFlow<ExportState>      // NEW: Idle | Exporting | Done | Error

    fun toggleToc()
    fun closeToc()
    fun setTheme(theme: AppTheme)
    fun setFont(font: AppFont)                   // NEW
    fun setFontSize(size: Int)                   // NEW
    fun setMode(mode: ViewerMode)                // NEW
    fun onContentEdited(newContent: String)      // NEW: debounced auto-save
    fun onScrollChanged(index: Int, offset: Int)
    fun exportToPdf()                            // NEW
}

sealed class ViewerUiState {
    data object Loading : ViewerUiState()
    data class Success(
        val markdownContent: String,
        val editableContent: String,             // mutable copy for editor
        val fileName: String,
        val toc: List<TocEntry>,
        val initialScrollIndex: Int,
        val initialScrollOffset: Int,
        val isDirty: Boolean,                    // unsaved changes indicator
        val uri: Uri
    ) : ViewerUiState()
    data class Error(val message: String) : ViewerUiState()
}

sealed class ExportState {
    data object Idle : ExportState()
    data object Exporting : ExportState()
    data class Done(val uri: Uri) : ExportState()
    data class Error(val message: String) : ExportState()
}
```

### Navigation (Type-Safe, Navigation 2.8+)

```kotlin
@Serializable object Home
@Serializable data class Viewer(val uri: String)

// NavGraph
composable<Home> { HomeScreen(...) }
composable<Viewer> { backStack ->
    val args = backStack.toRoute<Viewer>()
    ViewerScreen(uri = args.uri, ...)
}

// Navigate
navController.navigate(Viewer(uri = Uri.encode(uri.toString())))
```

---

## AndroidManifest

```xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" android:maxSdkVersion="32" />
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />

<activity android:name=".ui.MainActivity"
    android:exported="true"
    android:windowSoftInputMode="adjustResize">

    <!-- Launcher -->
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>

    <!-- Open .md from file manager / cloud / email -->
    <intent-filter android:label="Open with MarkMD">
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:mimeType="text/markdown" />
        <data android:mimeType="text/plain" />
        <data android:mimeType="application/octet-stream" />
    </intent-filter>

    <!-- Receive .md from share sheet -->
    <intent-filter>
        <action android:name="android.intent.action.SEND" />
        <category android:name="android.intent.category.DEFAULT" />
        <data android:mimeType="text/markdown" />
        <data android:mimeType="text/plain" />
    </intent-filter>
</activity>
```

---

## Gradle Config

### `settings.gradle.kts`
```kotlin
pluginManagement {
    repositories { google(); mavenCentral(); gradlePluginPortal() }
}
dependencyResolutionManagement {
    repositories { google(); mavenCentral() }
}
```

### root `build.gradle.kts`
```kotlin
plugins {
    id("com.android.application")        version "8.7.0"  apply false
    id("org.jetbrains.kotlin.android")   version "2.1.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.21" apply false
    id("com.google.dagger.hilt.android") version "2.56"   apply false
    id("com.google.devtools.ksp")        version "2.1.21-1.0.32" apply false
}
```

### `app/build.gradle.kts`
```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")  // replaces composeOptions in Kotlin 2.x
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

android {
    compileSdk = 36
    defaultConfig { minSdk = 26; targetSdk = 36 }
    buildFeatures { compose = true }
    // NO composeOptions block needed with Kotlin 2.x + compose plugin
}

dependencies {
    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2025.05.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")

    // Markdown renderer — pure Compose
    val mdVer = "0.39.2"
    implementation("com.mikepenz:multiplatform-markdown-renderer-android:$mdVer")
    implementation("com.mikepenz:multiplatform-markdown-renderer-m3:$mdVer")
    implementation("com.mikepenz:multiplatform-markdown-renderer-coil3:$mdVer")
    implementation("com.mikepenz:multiplatform-markdown-renderer-code:$mdVer")

    // Image loading — Coil 3
    implementation("io.coil-kt.coil3:coil-compose:3.1.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.1.0")

    // PDF export — Android built-in, no extra dependency needed
    // PdfDocument / PrintedPdfDocument available in android.graphics.pdf

    // Custom fonts — bundled in res/font/, no extra dependency
    // OpenDyslexic .ttf placed in app/src/main/res/font/open_dyslexic.ttf

    // Navigation — type-safe
    implementation("androidx.navigation:navigation-compose:2.8.9")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.56")
    ksp("com.google.dagger:hilt-compiler:2.56")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Room
    implementation("androidx.room:room-runtime:2.7.1")
    implementation("androidx.room:room-ktx:2.7.1")
    ksp("androidx.room:room-compiler:2.7.1")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.4")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.0")
}
```

---

## Key Implementation Notes

1. **Kotlin 2.x + `kotlin.plugin.compose`** — no `composeOptions.kotlinCompilerExtensionVersion` needed
2. **`compileSdk = 36`** — required by `multiplatform-markdown-renderer` 0.39+
3. **`LazyMarkdownSuccess`** — use instead of default `Column` layout for large files; pass `lazyListState` for scroll control
4. **TOC scroll** — call `lazyListState.animateScrollToItem(index)` where `index` is the position of the heading block in the parsed node list
5. **Type-safe navigation** — use `@Serializable` data classes + `toRoute<T>()`, no string routes
6. **Reading progress** — debounce scroll save with `Flow.debounce(500)` or manual `delay` in coroutine; store `firstVisibleItemIndex` + `firstVisibleItemScrollOffset`
7. **Tables** — horizontal scroll built-in by the library, no custom wrapper needed
8. **Code blocks** — `showHeader = true` gives language label + copy-to-clipboard button automatically

---

## Non-Goals

These are explicitly **out of scope** — do not implement:

- **WebView rendering** — no Markwon, no HTML bridge, no `AndroidView`
- **DOCX / HTML export** — only PDF export is supported
- **File sync / cloud storage backend** — the app opens files from cloud via SAF, but does not sync or manage them
- **Multi-window / split-screen** — not optimized for tablet split view
- **Bookmarks / annotations** — no highlight or note-taking features
- **Offline image caching** — Coil handles disk cache automatically, no manual management
- **File format conversion** — only `.md` / plain text input
- **Collaborative editing** — single-user only, no real-time sync
- **Version history / git integration** — no file versioning

---

## Error Handling

### File Reading Errors

| Error Type | Cause | User-Facing Message |
|---|---|---|
| `PERMISSION_DENIED` | URI permission expired or never granted | "Cannot open file. Please open it again using the + button." |
| `FILE_NOT_FOUND` | Cached file deleted / URI invalid | "File no longer exists. Please re-open from your file manager." |
| `IO_ERROR` | Disk read failure | "Failed to read file. The file may be corrupted." |
| `EMPTY_FILE` | File exists but has 0 bytes | "This file is empty." |
| `UNKNOWN` | Unexpected exception | "An unexpected error occurred. Please try again." |

### Error Handling Rules

- **Never crash** — all `readFileUseCase` paths wrapped in `try-catch`, emit `Result.Error`
- **`ViewerUiState.Error`** — show `ErrorCard` composable with icon + message + optional retry button
- **`SecurityException`** — caught specifically, mapped to `PERMISSION_DENIED`, never re-thrown
- **Empty content** — treat as `EMPTY_FILE` error, not success with empty string
- **Navigation to viewer with bad URI** — show error state immediately, do not navigate back automatically

### Image Loading Errors

- Failed images show a placeholder — never throw, never crash the list
- Use `Coil3ImageTransformerImpl` error placeholder via Coil's `error()` builder

---

## Definition of Done

A feature is **done** when all of the following are true:

### Rendering
- [ ] All standard CommonMark elements render correctly: headings (H1–H6), paragraphs, bold, italic, strikethrough, blockquote, ordered/unordered lists, task lists, horizontal rule
- [ ] Tables render as a proper grid — not as plain text — with horizontal scroll when wider than screen
- [ ] Code blocks render with syntax highlighting (Highlights library), language label, copy button, and horizontal scroll with 1-touch gesture
- [ ] Images load asynchronously via Coil 3, with placeholder on loading and error
- [ ] Links are tappable and open in browser

### Themes
- [ ] All 5 themes (System, Light, Dark, Sepia, AMOLED) render with correct background/text/code colors
- [ ] Theme persists across app restarts
- [ ] System theme follows OS dark mode toggle in real-time

### File Opening
- [ ] App appears in "Open with" dialog for `.md` files from Files, Drive, Dropbox, Gmail
- [ ] Share sheet receives `.md` files from other apps
- [ ] File can be re-opened from recents list without permission error (URI persisted)
- [ ] Cache strategy prevents stale permission errors

### TOC
- [ ] FAB hidden when document has no headings
- [ ] BottomSheet lists all headings with correct indentation per level
- [ ] Tapping a TOC entry scrolls to the correct heading smoothly

### Reading Progress
- [ ] Scroll position saved within 500ms of stopping scroll
- [ ] Position restored exactly when re-opening the same file
- [ ] Progress saved per URI, independent across files

### Performance
- [ ] Files up to 500KB open in under 1 second on mid-range device
- [ ] Scrolling through 1000-line document is smooth (no jank)
- [ ] `LazyColumn` used — off-screen items not composed

### Editor
- [ ] Toggle between READ / EDIT / SPLIT modes without losing scroll position
- [ ] Editor toolbar inserts correct markdown syntax at cursor position
- [ ] Auto-save triggers within 1s of last keystroke, dirty indicator shown/hidden correctly
- [ ] Edited content persists across app kill + restore (via file write, not just memory)
- [ ] Split mode renders preview in sync as user types

### PDF Export
- [ ] Exported PDF contains all text content with correct headings, lists, and paragraphs
- [ ] Code blocks in PDF use monospace font
- [ ] Export completes in under 5s for a 200-line document
- [ ] File appears in Downloads folder and is shareable immediately

### Font Customization
- [ ] All 4 font families render visibly different in both reader and editor
- [ ] Font size change applies to all text immediately without restart
- [ ] Settings persist across cold start

### Code Quality
- [ ] No `@Suppress("UNCHECKED_CAST")` or similar suppressions without comment
- [ ] No hardcoded strings in UI — use `stringResource` or constants
- [ ] No `Thread.sleep` or blocking calls on main thread
- [ ] All `Flow` collections use `collectAsStateWithLifecycle`, not `collectAsState`

---

## Implementation Phases

### Phase 1 — Foundation *(~1 day)*
**Goal:** App builds, runs, opens a hardcoded markdown string.

- [ ] Create project with AGP 8.7, Kotlin 2.1.x, Compose BOM 2025
- [ ] Add all dependencies (renderer, Coil, Hilt, Room, DataStore, Navigation)
- [ ] Set up Hilt application class + `@HiltAndroidApp`
- [ ] Create `AppTheme` enum + `MdReaderTheme` with 5 color schemes
- [ ] Implement `MarkdownViewer` composable with hardcoded sample content
- [ ] Verify: syntax highlighting, table, code block horizontal scroll all work

### Phase 2 — File Opening *(~1 day)*
**Goal:** App opens real `.md` files from the file picker and share sheet.

- [ ] Implement `ReadFileUseCase` with 4-strategy fallback chain
- [ ] Add `ActivityResultContracts.OpenDocument` launcher in `HomeScreen`
- [ ] Handle `ACTION_VIEW` and `ACTION_SEND` in `MainActivity`
- [ ] Add `takePersistableUriPermission` + cache-to-internal-storage strategy
- [ ] Add `AndroidManifest` intent filters
- [ ] Verify: open from Files app, Google Drive, Gmail attachment, share from browser

### Phase 3 — Viewer Screen *(~1 day)*
**Goal:** Full viewer with theme toggle, TOC, font customization.

- [ ] Implement `ViewerViewModel` — load file, parse TOC, restore progress
- [ ] Implement `ViewerScreen` — `Scaffold`, `TopAppBar`, FAB, `ModalBottomSheet`
- [ ] Wire `LazyListState` for scroll position save/restore
- [ ] Implement TOC anchor scroll via `lazyListState.animateScrollToItem()`
- [ ] Implement font size slider + font family selector in settings sheet
- [ ] Verify: theme toggle, TOC scroll, font change, progress restore all work

### Phase 4 — Markdown Editor *(~1.5 days)*
**Goal:** Users can edit markdown and see a live preview.

- [ ] Add `ViewerMode` enum + mode toggle button in `TopAppBar`
- [ ] Implement `MarkdownEditor` composable — `BasicTextField`, monospace, toolbar
- [ ] Editor toolbar: Bold, Italic, Code, Heading, Link, List buttons
- [ ] Implement split mode for landscape / large screen
- [ ] Wire `WriteFileUseCase` — save edits back to URI / cache file, debounced 1s
- [ ] Dirty state — show `•` in title bar when unsaved changes exist
- [ ] Verify: edit → preview toggle, auto-save, dirty indicator, split mode on rotation

### Phase 5 — PDF Export *(~1 day)*
**Goal:** Users can export the current document to a PDF file.

- [ ] Implement `ExportPdfUseCase` using Android `PrintedPdfDocument` or `PdfDocument`
- [ ] Add "Export as PDF" option in share bottom sheet
- [ ] Save to `MediaStore.Downloads` (API 29+) with proper MIME type
- [ ] Show export progress indicator + success/error snackbar
- [ ] Offer share intent immediately after export
- [ ] Verify: exported PDF matches rendered view, fonts correct, images included

### Phase 6 — Home & Recents *(~half day)*
**Goal:** App has a home screen showing recently opened files.

- [ ] Set up Room `AppDatabase` + `DocumentEntity` + `DocumentDao`
- [ ] Implement `DocumentRepository` — upsert on open, query recent by date
- [ ] Implement `HomeScreen` + `HomeViewModel` — recent list + open picker
- [ ] Add swipe-to-delete on recent items + quick edit/read action chips
- [ ] Verify: recent docs list updates, swipe delete works, re-open restores progress

### Phase 7 — Settings & Polish *(~half day)*
**Goal:** Settings screen, edge cases handled, app is shippable.

- [ ] Implement `SettingsScreen` — font size slider, font family picker, theme selector, PDF page size, about
- [ ] Handle all `ErrorType` cases with user-friendly `ErrorCard`
- [ ] Add `WindowInsets` padding (navigation bar, status bar) throughout
- [ ] Add `requestLegacyExternalStorage` + media permissions for Android 9–12 compatibility
- [ ] Test on: Android 9 (API 28), Android 12 (API 32), Android 14 (API 34)
- [ ] Final build: run `./gradlew assembleRelease`, verify no R8 issues

---

## Output Rules

These rules apply to **every file generated**:

### Code Style
- Kotlin only — no Java files
- All UI is pure Jetpack Compose — no XML layouts, no `View`, no `AndroidView`
- One composable per file, file name matches composable name
- `data class` with `copy()` for all state objects
- `sealed class` / `sealed interface` for all UI state and result types
- `object` for singleton use cases that have no state
- No `!!` (non-null assertion) — use `?: return`, `?: error(...)`, or `requireNotNull()`

### Architecture Rules
- ViewModels must not import any `android.view.*` or Compose classes
- UseCases must not import any ViewModel or UI classes
- Repository interfaces defined in `domain`, implementations in `data`
- All `suspend` functions called only from coroutines or other suspend functions
- `Dispatchers.IO` for all file I/O and database operations

### Compose Rules
- All state hoisted to ViewModel — composables are stateless where possible
- Use `collectAsStateWithLifecycle()` — never `collectAsState()`
- `remember { }` for expensive objects (regex, builders)
- `rememberCoroutineScope()` only for UI-triggered one-off coroutines
- `LaunchedEffect(key)` for side effects triggered by state changes
- No `GlobalScope` — use `viewModelScope` or `rememberCoroutineScope()`

### File Generation Order
When generating the full project, output files in this order:
1. `settings.gradle.kts` + root `build.gradle.kts`
2. `app/build.gradle.kts`
3. `AndroidManifest.xml`
4. Data layer: models → entities → DAOs → database → repositories
5. Domain layer: use cases (`ReadFileUseCase`, `WriteFileUseCase`, `ExportPdfUseCase`, `ParseTocUseCase`, `SaveProgressUseCase`) → parsers
6. DI modules
7. Theme: `ThemeTokens` → `Type` → `Theme`
8. Navigation: route definitions → `AppNavGraph`
9. Screens: Home → Settings → Viewer components (`MarkdownViewer`, `MarkdownEditor`, `EditorToolbar`) → ViewerScreen → ViewerViewModel
