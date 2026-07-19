package com.shuravianbhron.vaa

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.shuravianbhron.vaa.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

val Context.dataStore by preferencesDataStore(name = "settings")
val FIRST_LAUNCH_COMPLETE = booleanPreferencesKey("first_launch_complete")
val SHOW_LOG_FAB = booleanPreferencesKey("show_log_fab")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        LogKeeper.init(applicationContext)

        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            try {
                LogKeeper.logError("UncaughtException", "Fatal unhandled exception in thread ${thread.name}", exception)
            } catch (e: Throwable) {
                // Ignore failure in log writing
            } finally {
                defaultHandler?.uncaughtException(thread, exception)
            }
        }

        enableEdgeToEdge()

        var isReady by mutableStateOf(false)
        var startDestination by mutableStateOf<String?>(null)

        splashScreen.setKeepOnScreenCondition { !isReady }

        val flow = dataStore.data.map { preferences ->
            preferences[FIRST_LAUNCH_COMPLETE] ?: false
        }

        setContent {
            val isFirstLaunchComplete by flow.collectAsState(initial = null)

            LaunchedEffect(isFirstLaunchComplete) {
                if (isFirstLaunchComplete != null) {
                    try {
                        startDestination = if (isFirstLaunchComplete == true) "main_shell" else "welcome"
                        isReady = true
                    } catch (e: Throwable) {
                        LogKeeper.logError("MainActivity", "Failed during destination resolution", e)
                    }
                }
            }

            MyApplicationTheme {
                if (isReady && startDestination != null) {
                    AppNavigation(
                        startDestination = startDestination!!,
                        onGetStarted = {
                            startDestination = "main_shell"
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AppNavigation(startDestination: String, onGetStarted: () -> Unit) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = startDestination) {
        composable("welcome") {
            WelcomeScreen(navController = navController, onGetStarted = onGetStarted)
        }
        composable("main_shell") {
            MainShell(navController = navController)
        }
        composable("settings") {
            SettingsScreen(navController = navController)
        }
        composable("log_viewer") {
            LogViewerScreen(navController = navController)
        }
    }
}

@Composable
fun WelcomeScreen(navController: NavHostController, onGetStarted: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Vaa",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Welcome to Vaa, your offline-first companion.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(48.dp))
            Button(
                onClick = {
                    coroutineScope.launch {
                        try {
                            context.dataStore.edit { preferences ->
                                preferences[FIRST_LAUNCH_COMPLETE] = true
                            }
                            onGetStarted()
                            navController.navigate("main_shell") {
                                popUpTo("welcome") { inclusive = true }
                            }
                        } catch (e: kotlinx.coroutines.CancellationException) {
                            throw e // Not a real error — normal coroutine cancellation, let it propagate
                        } catch (e: Throwable) {
                            LogKeeper.logError("WelcomeScreen", "Failed to save first launch state", e)
                        }
                    }
                }
            ) {
                Text("Get Started")
            }
        }
    }
}

@Composable
fun MainShell(navController: NavHostController) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    
    val items = listOf("Chats", "Updates", "Loader", "Placeholder")
    val icons = listOf(Icons.AutoMirrored.Filled.Chat, Icons.Filled.Refresh, Icons.Filled.Download, Icons.Filled.MoreHoriz)

    val showFabFlow = remember {
        context.dataStore.data.map { preferences ->
            preferences[SHOW_LOG_FAB] ?: true
        }
    }
    val showFab by showFabFlow.collectAsState(initial = true)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            if (showFab) {
                FloatingActionButton(
                    onClick = { 
                        try {
                            navController.navigate("log_viewer") 
                        } catch (e: Throwable) {
                            LogKeeper.logError("MainShell", "Failed to navigate to log viewer", e)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Icon(Icons.Filled.BugReport, contentDescription = "View Logs")
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Start,
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, title ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = title) },
                        label = { Text(title) },
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                try {
                                    pagerState.animateScrollToPage(index)
                                } catch (e: kotlinx.coroutines.CancellationException) {
                                    throw e // Not a real error — normal coroutine cancellation, let it propagate
                                } catch (e: Throwable) {
                                    LogKeeper.logError("MainShell", "Failed to animate scroll to page $index", e)
                                }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { page ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${items[page]} Screen Placeholder",
                    style = MaterialTheme.typography.headlineMedium
                )
                IconButton(
                    onClick = { navController.navigate("settings") },
                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
                ) {
                    Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
