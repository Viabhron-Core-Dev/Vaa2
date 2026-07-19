package com.shuravianbhron.vaa

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import androidx.navigation.NavController
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val showFabFlow = remember {
        context.dataStore.data.map { preferences ->
            preferences[SHOW_LOG_FAB] ?: true // Default is true
        }
    }
    val showFab by showFabFlow.collectAsState(initial = true)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Show Log Viewer FAB", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "Displays a floating action button on main tabs to quickly access logs.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = showFab,
                    onCheckedChange = { isChecked ->
                        coroutineScope.launch {
                            try {
                                context.dataStore.edit { preferences ->
                                    preferences[SHOW_LOG_FAB] = isChecked
                                }
                            } catch (e: kotlinx.coroutines.CancellationException) {
                                throw e // Not a real error — normal coroutine cancellation, let it propagate
                            } catch (e: Throwable) {
                                LogKeeper.logError("SettingsScreen", "Failed to update FAB preference", e)
                            }
                        }
                    }
                )
            }
        }
    }
}
