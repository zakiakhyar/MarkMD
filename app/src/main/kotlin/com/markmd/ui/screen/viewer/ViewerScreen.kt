package com.markmd.ui.screen.viewer

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
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
                        modifier      = Modifier.fillMaxSize(),
                    )
                }
            }
        }
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
