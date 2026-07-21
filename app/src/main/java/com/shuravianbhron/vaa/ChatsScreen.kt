package com.shuravianbhron.vaa

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException

enum class ThreadType(val label: String) {
    CHAT("Chat"), PAGE("Page"), LOCAL("Local")
}

data class DummyThread(val id: String, val name: String, val type: ThreadType)

val dummyThreads = listOf(
    DummyThread("1", "General Chat", ThreadType.CHAT),
    DummyThread("2", "Project Planning", ThreadType.CHAT),
    DummyThread("3", "API Documentation", ThreadType.PAGE),
    DummyThread("4", "Local Model Context", ThreadType.LOCAL),
    DummyThread("5", "Random Ideas", ThreadType.CHAT)
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChatsScreen(navController: NavController, onLongClickThread: (DummyThread) -> Unit) {
    val context = LocalContext.current
    var expandedMenu by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("All") }

    val filters = listOf("All", "Chat", "Page", "Local")

    val filteredThreads = remember(selectedFilter) {
        if (selectedFilter == "All") dummyThreads
        else dummyThreads.filter { it.type.label == selectedFilter }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Vaa") },
            actions = {
                IconButton(onClick = { expandedMenu = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "Menu")
                }
                DropdownMenu(
                    expanded = expandedMenu,
                    onDismissRequest = { expandedMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Settings") },
                        onClick = {
                            expandedMenu = false
                            try {
                                navController.navigate("settings")
                            } catch (e: CancellationException) {
                                throw e
                            } catch (e: Throwable) {
                                LogKeeper.logError("ChatsScreen", "Failed to navigate to settings", e)
                            }
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Add New (Stub)") },
                        onClick = {
                            expandedMenu = false
                            Toast.makeText(context, "Add New tapped", Toast.LENGTH_SHORT).show()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("All Threads (Stub)") },
                        onClick = {
                            expandedMenu = false
                            Toast.makeText(context, "All Threads tapped", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filters.forEach { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter) }
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(filteredThreads) { thread ->
                ListItem(
                    headlineContent = { Text(thread.name) },
                    supportingContent = { Text(thread.type.label) },
                    modifier = Modifier.combinedClickable(
                        onClick = {
                            try {
                                navController.navigate("dummy_detail/${thread.id}")
                            } catch (e: CancellationException) {
                                throw e
                            } catch (e: Throwable) {
                                LogKeeper.logError("ChatsScreen", "Failed to navigate to dummy detail", e)
                            }
                        },
                        onLongClick = {
                            try {
                                onLongClickThread(thread)
                                Toast.makeText(context, "Opened in new tab", Toast.LENGTH_SHORT).show()
                            } catch (e: CancellationException) {
                                throw e
                            } catch (e: Throwable) {
                                LogKeeper.logError("ChatsScreen", "Failed on long press", e)
                            }
                        }
                    )
                )
                HorizontalDivider()
            }
        }
    }
}
