package com.markmd.ui.navigation

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.markmd.ui.screen.editor.EditorScreen
import com.markmd.ui.screen.home.HomeScreen
import com.markmd.ui.screen.settings.SettingsScreen
import com.markmd.ui.screen.viewer.ViewerScreen
import com.markmd.ui.screen.viewer.ViewerViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current

    // Handle incoming intents
    LaunchedEffect(Unit) {
        val intent = (context as? android.app.Activity)?.intent
        handleIntent(intent, navController)
    }

    NavHost(
        navController = navController,
        startDestination = Routes.Home
    ) {
        composable<Routes.Home> {
            HomeScreen(
                onNavigateToViewer = { uri ->
                    navController.navigate(Routes.Viewer(uri.toString()))
                },
                onNavigateToSettings = {
                    navController.navigate(Routes.Settings)
                }
            )
        }

        composable<Routes.Viewer> { backStackEntry ->
            val route = backStackEntry.toRoute<Routes.Viewer>()
            val uri = route.toUri()

            ViewerScreen(
                uri = uri,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEditor = {
                    navController.navigate(Routes.Editor(uri.toString()))
                },
                onNavigateToSettings = {
                    navController.navigate(Routes.Settings)
                }
            )
        }

        composable<Routes.Editor> { backStackEntry ->
            val route = backStackEntry.toRoute<Routes.Editor>()
            EditorScreen(
                uri = route.toUri(),
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Routes.Settings> {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

private fun handleIntent(intent: Intent?, navController: NavHostController) {
    when (intent?.action) {
        Intent.ACTION_VIEW -> {
            val uri = intent.data
            if (uri != null) {
                navController.navigate(Routes.Viewer(uri.toString())) {
                    popUpTo(Routes.Home) { inclusive = false }
                }
            }
        }
        Intent.ACTION_SEND -> {
            val uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
            if (uri != null) {
                navController.navigate(Routes.Viewer(uri.toString())) {
                    popUpTo(Routes.Home) { inclusive = false }
                }
            }
        }
    }
}
