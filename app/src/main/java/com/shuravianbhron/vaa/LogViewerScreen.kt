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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogViewerScreen(navController: NavController) {
    val context = LocalContext.current
    var logContent by remember { mutableStateOf(getReversedLogs()) }

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
                            logContent = getReversedLogs()
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
            Button(
                onClick = {
                    try {
                        throw RuntimeException("Test exception triggered from Log Viewer")
                    } catch (e: Throwable) {
                        LogKeeper.logError("TestLog", "User manually triggered a test exception", e)
                        logContent = getReversedLogs() // Refresh view
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Text("Generate Test Exception Log")
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

fun getReversedLogs(): String {
    val logs = LogKeeper.getLogs()
    if (logs.isBlank() || logs == "No logs available.") return logs
    
    // Split by our double newline separator, filter blanks, reverse, and rejoin
    return logs.split("\n\n")
        .filter { it.isNotBlank() }
        .reversed()
        .joinToString("\n\n")
}
