package com.shuravianbhron.vaa

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.Locale

enum class LogTimeFilter(val label: String, val hours: Int?) {
    ONE_HOUR("1h", 1),
    SIX_HOURS("6h", 6),
    TWELVE_HOURS("12h", 12),
    TWENTY_FOUR_HOURS("24h", 24),
    ALL("All", null)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogViewerScreen(navController: NavController) {
    val context = LocalContext.current
    var rawLogs by remember { mutableStateOf(LogKeeper.getLogs()) }
    var selectedFilter by remember { mutableStateOf(LogTimeFilter.ALL) }
    
    val logContent = remember(rawLogs, selectedFilter) {
        filterAndReverseLogs(rawLogs, selectedFilter)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Log Viewer") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        try {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Vaa Logs", logContent)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                        } catch (e: Throwable) {
                            LogKeeper.logError("LogViewer", "Failed to copy logs to clipboard", e)
                        }
                    }) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = "Copy")
                    }
                    IconButton(onClick = {
                        try {
                            LogKeeper.clearLogs()
                            rawLogs = LogKeeper.getLogs()
                        } catch (e: Throwable) {
                            LogKeeper.logError("LogViewer", "Failed to clear logs", e)
                        }
                    }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Clear")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LogTimeFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter.label) }
                    )
                }
            }

            Button(
                onClick = {
                    try {
                        throw RuntimeException("Test exception triggered from Log Viewer")
                    } catch (e: Throwable) {
                        LogKeeper.logError("TestLog", "User manually triggered a test exception", e)
                        rawLogs = LogKeeper.getLogs() // Refresh view
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                Text("Generate Test Exception Log")
            }
            
            Button(
                onClick = {
                    try {
                        // Simulating the exact wrapper pattern used for navigation across the app
                        navController.navigate("this_route_does_not_exist_triggering_automatic_capture")
                    } catch (e: Throwable) {
                        LogKeeper.logError("Navigation", "Failed to navigate to route", e)
                        rawLogs = LogKeeper.getLogs() // Refresh view
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Text("Test Automatic Capture (Navigation path)")
            }

            SelectionContainer {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        Text(
                            text = logContent,
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

fun filterAndReverseLogs(logs: String, filter: LogTimeFilter): String {
    if (logs.isBlank() || logs == "No logs available.") return logs

    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
    val now = System.currentTimeMillis()
    val cutoffTime = filter.hours?.let { now - (it * 60 * 60 * 1000L) }

    val entries = logs.split("\n\n").filter { it.isNotBlank() }
    
    val filteredEntries = entries.filter { entry ->
        if (cutoffTime == null) {
            true
        } else {
            try {
                // Parse timestamp from "[2026-07-18 12:34:56.789] [Tag]: ..."
                val timestampStr = entry.substringAfter("[").substringBefore("]")
                val date = dateFormat.parse(timestampStr)
                if (date != null) {
                    date.time >= cutoffTime
                } else {
                    true // If we can't parse, include it just in case
                }
            } catch (e: Exception) {
                true // Keep if malformed
            }
        }
    }

    val result = filteredEntries.reversed().joinToString("\n\n")
    return result.ifEmpty { "No logs in this time period." }
}
