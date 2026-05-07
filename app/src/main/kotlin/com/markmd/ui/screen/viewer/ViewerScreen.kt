package com.markmd.ui.screen.viewer

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.markmd.R
import com.markmd.ui.screen.viewer.components.MarkdownViewer
import com.markmd.ui.screen.viewer.components.ReaderSettingsSheetContent
import com.markmd.ui.screen.viewer.components.TocSheetContent
import com.markmd.ui.screen.viewer.components.rememberMarkdownSections
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewerScreen(
    uri: Uri,
    onNavigateBack: () -> Unit,
    onNavigateToEditor: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: ViewerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val readerSettings by viewModel.readerSettings.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val tocSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val readerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val lazyListState = rememberLazyListState()
    val sections = rememberMarkdownSections(uiState.document?.content ?: "")
    val currentSections by rememberUpdatedState(sections)

    var showMenu by remember { mutableStateOf(false) }
    var showTocSheet by remember { mutableStateOf(false) }
    var showReaderSheet by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }
    val searchFocusRequester = remember { FocusRequester() }

    LaunchedEffect(uri) {
        viewModel.loadDocument(uri)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is ViewerEvent.ScrollToAnchor -> {
                    val index = currentSections.indexOfFirst { it.anchor == event.anchor }
                    if (index >= 0) {
                        lazyListState.animateScrollToItem(index)
                    }
                }
                is ViewerEvent.NavigateToEditor -> onNavigateToEditor()
                is ViewerEvent.NavigateToSettings -> onNavigateToSettings()
                is ViewerEvent.ShareDocument -> {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/markdown"
                        putExtra(Intent.EXTRA_STREAM, event.uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share document"))
                }
                is ViewerEvent.ExportPdf -> {
                    snackbarHostState.showSnackbar("PDF export coming soon")
                }
                is ViewerEvent.ScrollToMatch -> {
                    // Find which section contains this char offset
                    var cumulative = 0
                    var targetIndex = 0
                    for ((i, section) in currentSections.withIndex()) {
                        val sectionLen = section.content.length + 1
                        if (cumulative + sectionLen > event.charOffset) {
                            targetIndex = i
                            break
                        }
                        cumulative += sectionLen
                        targetIndex = i
                    }
                    lazyListState.animateScrollToItem(targetIndex)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.document?.title ?: stringResource(R.string.viewer_title),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            showSearch = !showSearch
                            if (!showSearch) viewModel.onSearchClose()
                        },
                        enabled = uiState.document != null
                    ) {
                        Icon(Icons.Default.Search, contentDescription = stringResource(R.string.cd_search))
                    }

                    IconButton(
                        onClick = { viewModel.onEditClick() },
                        enabled = uiState.document != null
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.viewer_edit))
                    }

                    IconButton(onClick = { showReaderSheet = true }) {
                        Icon(
                            Icons.Default.FormatSize,
                            contentDescription = "Reader settings"
                        )
                    }

                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.cd_more))
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.viewer_share)) },
                            leadingIcon = { Icon(Icons.Default.Share, null) },
                            onClick = {
                                showMenu = false
                                viewModel.onShareClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.viewer_export_pdf)) },
                            onClick = {
                                showMenu = false
                                viewModel.onExportPdfClick()
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            if (uiState.toc.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { showTocSheet = true },
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.List,
                        contentDescription = stringResource(R.string.viewer_toc)
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            AnimatedVisibility(
                visible = showSearch,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                LaunchedEffect(showSearch) {
                    if (showSearch) searchFocusRequester.requestFocus()
                }
                SearchBar(
                    query = uiState.searchQuery,
                    matchCurrent = uiState.currentMatch,
                    matchTotal = uiState.matchCount,
                    onQueryChange = viewModel::onSearchQueryChange,
                    onNext = viewModel::onSearchNext,
                    onPrev = viewModel::onSearchPrev,
                    onClose = {
                        showSearch = false
                        viewModel.onSearchClose()
                    },
                    focusRequester = searchFocusRequester,
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.error != null -> {
                    Text(
                        text = uiState.error!!,
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                uiState.document != null -> {
                    MarkdownViewer(
                        sections      = sections,
                        fontSize      = readerSettings.fontSize,
                        theme         = readerSettings.theme,
                        onAnchorClick = { anchor -> viewModel.onTocClick(anchor) },
                        lazyListState = lazyListState,
                        searchQuery   = uiState.searchQuery,
                        modifier      = Modifier.fillMaxSize(),
                    )
                }
            }
            } // end Box
        } // end Column
    }

    if (showTocSheet && uiState.toc.isNotEmpty()) {
        ModalBottomSheet(
            onDismissRequest = { showTocSheet = false },
            sheetState = tocSheetState,
        ) {
            TocSheetContent(
                toc = uiState.toc,
                onTocEntryClick = { anchor ->
                    viewModel.onTocClick(anchor)
                    scope.launch {
                        tocSheetState.hide()
                        showTocSheet = false
                    }
                },
            )
        }
    }

    if (showReaderSheet) {
        ModalBottomSheet(
            onDismissRequest = { showReaderSheet = false },
            sheetState = readerSheetState,
        ) {
            ReaderSettingsSheetContent(
                fontSize = readerSettings.fontSize,
                theme = readerSettings.theme,
                onFontSizeChange = viewModel::setFontSize,
                onThemeChange = viewModel::setTheme,
            )
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    matchCurrent: Int,
    matchTotal: Int,
    onQueryChange: (String) -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onClose: () -> Unit,
    focusRequester: FocusRequester,
) {
    Surface(
        tonalElevation = 4.dp,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    BasicTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { onNext() }),
                        decorationBox = { inner ->
                            if (query.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.search_hint),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            inner()
                        },
                    )
                    if (matchTotal > 0) {
                        Text(
                            text = "$matchCurrent/$matchTotal",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    } else if (query.isNotEmpty()) {
                        Text(
                            text = "0/0",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }
            }
            IconButton(onClick = onPrev) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Previous match")
            }
            IconButton(onClick = onNext) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Next match")
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close search")
            }
        }
    }
}
