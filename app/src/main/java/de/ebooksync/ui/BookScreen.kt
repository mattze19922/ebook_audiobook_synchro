package de.ebooksync.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.ebooksync.data.Anchor
import de.ebooksync.data.Book
import de.ebooksync.domain.SyncCalculator
import de.ebooksync.domain.formatTime
import de.ebooksync.domain.parseTime
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookScreen(
    bookId: Long,
    viewModel: AppViewModel,
    onBack: () -> Unit,
) {
    val book by remember(bookId) { viewModel.book(bookId) }.collectAsStateWithLifecycle(initialValue = null)
    val anchors by remember(bookId) { viewModel.anchors(bookId) }.collectAsStateWithLifecycle(initialValue = emptyList())
    val current = book ?: return

    var tab by rememberSaveable { mutableIntStateOf(0) }
    var menuOpen by remember { mutableStateOf(false) }
    var editBook by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    var anchorToEdit by remember { mutableStateOf<Anchor?>(null) }

    val calculator = remember(current, anchors) {
        SyncCalculator(current.totalPages, current.durationSeconds, anchors)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(current.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                },
                actions = {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Mehr")
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text("Buch bearbeiten") },
                            onClick = { menuOpen = false; editBook = true },
                        )
                        DropdownMenuItem(
                            text = { Text("Buch löschen") },
                            onClick = { menuOpen = false; confirmDelete = true },
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            if (tab == 1) {
                ExtendedFloatingActionButton(
                    onClick = { anchorToEdit = Anchor(bookId = bookId, seconds = 0, page = null) },
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Anker hinzufügen") },
                )
            }
        },
    ) { padding ->
        Column(Modifier.padding(padding)) {
            PrimaryTabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Umrechnen") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Ankerpunkte (${anchors.size})") })
            }
            when (tab) {
                0 -> ConvertTab(
                    book = current,
                    calculator = calculator,
                    knownAnchors = anchors.count { it.page != null },
                    onSaveAsAnchor = { seconds, page ->
                        anchorToEdit = Anchor(bookId = bookId, seconds = seconds, page = page)
                    },
                )
                else -> AnchorsTab(
                    anchors = anchors,
                    onEdit = { anchorToEdit = it },
                    onDelete = viewModel::deleteAnchor,
                )
            }
        }
    }

    anchorToEdit?.let { anchor ->
        AnchorDialog(
            initial = anchor,
            book = current,
            onDismiss = { anchorToEdit = null },
            onSave = {
                viewModel.saveAnchor(it)
                anchorToEdit = null
            },
        )
    }
    if (editBook) {
        BookDialog(
            initial = current,
            onDismiss = { editBook = false },
            onSave = {
                viewModel.saveBook(it)
                editBook = false
            },
        )
    }
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Buch löschen?") },
            text = { Text("„${current.title}“ und alle Ankerpunkte werden gelöscht.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    viewModel.deleteBook(current)
                    onBack()
                }) { Text("Löschen") }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Abbrechen") } },
        )
    }
}

@Composable
private fun ConvertTab(
    book: Book,
    calculator: SyncCalculator,
    knownAnchors: Int,
    onSaveAsAnchor: (seconds: Long, page: Int) -> Unit,
) {
    var timeToPage by rememberSaveable { mutableStateOf(true) }
    var input by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = timeToPage,
                onClick = { timeToPage = true; input = "" },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
            ) { Text("Zeit → Seite") }
            SegmentedButton(
                selected = !timeToPage,
                onClick = { timeToPage = false; input = "" },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
            ) { Text("Seite → Zeit") }
        }

        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            label = { Text(if (timeToPage) "Hörbuch-Zeit (h:mm:ss)" else "Seite (1–${book.totalPages})") },
            keyboardOptions = KeyboardOptions(keyboardType = if (timeToPage) KeyboardType.Uri else KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        // Pair of (seconds, page) for the current input, or null if the input is invalid.
        val result: Pair<Long, Int>? = if (timeToPage) {
            parseTime(input)?.takeIf { it <= book.durationSeconds }?.let { it to calculator.timeToPage(it).page }
        } else {
            input.trim().toIntOrNull()?.takeIf { it in 1..book.totalPages }?.let { calculator.pageToTime(it) to it }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                when {
                    result == null -> Text(
                        if (input.isBlank()) "Gib oben einen Wert ein" else "Ungültige Eingabe",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    timeToPage -> {
                        val position = calculator.timeToPage(result.first)
                        Text("S. ${position.page}", style = MaterialTheme.typography.displayMedium)
                        Text(
                            "ca. ${(position.fraction * 100).roundToInt()} % der Seite · von ${book.totalPages}",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                    else -> {
                        Text(formatTime(result.first), style = MaterialTheme.typography.displayMedium)
                        Text("Anfang von Seite ${result.second}", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }

        Button(
            onClick = { result?.let { onSaveAsAnchor(it.first, it.second) } },
            enabled = result != null,
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Als Ankerpunkt speichern") }

        Text(
            if (knownAnchors == 0) {
                "Berechnet aus Seitenzahl und Dauer. Mit Ankerpunkten wird es genauer."
            } else {
                "Berechnet mit $knownAnchors Ankerpunkt(en)."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AnchorsTab(
    anchors: List<Anchor>,
    onEdit: (Anchor) -> Unit,
    onDelete: (Anchor) -> Unit,
) {
    if (anchors.isEmpty()) {
        Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Text(
                "Noch keine Ankerpunkte.\nTrage bekannte Stellen ein, z. B. Kapitelanfänge.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
        }
        return
    }
    LazyColumn(contentPadding = PaddingValues(bottom = 96.dp)) {
        items(anchors, key = { it.id }) { anchor ->
            ListItem(
                headlineContent = {
                    Text("${formatTime(anchor.seconds)} · ${anchor.page?.let { "S. $it" } ?: "Seite fehlt"}")
                },
                supportingContent = anchor.label.takeIf { it.isNotBlank() }?.let { { Text(it) } },
                trailingContent = {
                    IconButton(onClick = { onDelete(anchor) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Löschen")
                    }
                },
                modifier = Modifier.clickable { onEdit(anchor) },
            )
        }
    }
}
