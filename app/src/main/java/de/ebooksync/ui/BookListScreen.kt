package de.ebooksync.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.ebooksync.domain.formatTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BookListScreen(
    viewModel: AppViewModel,
    onOpenBook: (Long) -> Unit,
) {
    val books by viewModel.books.collectAsStateWithLifecycle()
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("Hörbuch-Abgleich") },
                subtitle = { Text("eBook-Seite ↔ Hörbuch-Zeit") },
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Buch hinzufügen") },
            )
        },
    ) { padding ->
        if (books.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding).padding(32.dp), contentAlignment = Alignment.Center) {
                Text(
                    "Noch keine Bücher.\nLege dein erstes Buch mit Seitenzahl und Hörbuch-Dauer an.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = padding.calculateTopPadding(), bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(books, key = { it.id }) { book ->
                    ElevatedCard(onClick = { onOpenBook(book.id) }, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(20.dp)) {
                            Text(book.title, style = MaterialTheme.typography.titleLarge)
                            Text(
                                "${book.totalPages} Seiten · ${formatTime(book.durationSeconds)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        BookDialog(
            initial = null,
            onDismiss = { showAddDialog = false },
            onSave = { book ->
                showAddDialog = false
                viewModel.saveBook(book, onSaved = onOpenBook)
            },
        )
    }
}
