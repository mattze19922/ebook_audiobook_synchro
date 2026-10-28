package de.ebooksync.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.ebooksync.data.Anchor
import de.ebooksync.data.Book
import de.ebooksync.domain.formatTime
import de.ebooksync.domain.parseTime

/** Create ([initial] == null) or edit a book. */
@Composable
fun BookDialog(
    initial: Book?,
    onDismiss: () -> Unit,
    onSave: (Book) -> Unit,
) {
    var title by rememberSaveable { mutableStateOf(initial?.title.orEmpty()) }
    var pages by rememberSaveable { mutableStateOf(initial?.totalPages?.toString().orEmpty()) }
    var duration by rememberSaveable { mutableStateOf(initial?.durationSeconds?.let(::formatTime).orEmpty()) }

    val parsedPages = pages.trim().toIntOrNull()?.takeIf { it > 0 }
    val parsedDuration = parseTime(duration)?.takeIf { it > 0 }
    val valid = title.isNotBlank() && parsedPages != null && parsedDuration != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Buch hinzufügen" else "Buch bearbeiten") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titel") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = pages,
                    onValueChange = { pages = it },
                    label = { Text("Seiten im eBook") },
                    isError = pages.isNotEmpty() && parsedPages == null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text("Hörbuch-Dauer") },
                    placeholder = { Text("h:mm:ss") },
                    isError = duration.isNotEmpty() && parsedDuration == null,
                    supportingText = { Text("z. B. 12:34:56") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = valid,
                onClick = {
                    val book = (initial ?: Book(title = "", totalPages = 0, durationSeconds = 0)).copy(
                        title = title.trim(),
                        totalPages = parsedPages!!,
                        durationSeconds = parsedDuration!!,
                    )
                    onSave(book)
                },
            ) { Text("Speichern") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Abbrechen") } },
    )
}

/** Create or edit an anchor; the page may stay empty for chapter marks without a known page. */
@Composable
fun AnchorDialog(
    initial: Anchor,
    book: Book,
    onDismiss: () -> Unit,
    onSave: (Anchor) -> Unit,
) {
    var time by rememberSaveable { mutableStateOf(formatTime(initial.seconds)) }
    var page by rememberSaveable { mutableStateOf(initial.page?.toString().orEmpty()) }
    var label by rememberSaveable { mutableStateOf(initial.label) }

    val parsedTime = parseTime(time)?.takeIf { it <= book.durationSeconds }
    val parsedPage = page.trim().toIntOrNull()?.takeIf { it in 1..book.totalPages }
    val pageValid = page.isBlank() || parsedPage != null
    val valid = parsedTime != null && pageValid

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial.id == 0L) "Ankerpunkt hinzufügen" else "Ankerpunkt bearbeiten") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Hörbuch-Zeit") },
                    placeholder = { Text("h:mm:ss") },
                    isError = parsedTime == null,
                    supportingText = { Text("Höchstens ${formatTime(book.durationSeconds)}") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = page,
                    onValueChange = { page = it },
                    label = { Text("Seite im eBook") },
                    isError = !pageValid,
                    supportingText = { Text("1–${book.totalPages}, leer = noch unbekannt") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Notiz (z. B. Kapitel 5)") },
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = valid,
                onClick = { onSave(initial.copy(seconds = parsedTime!!, page = parsedPage, label = label.trim())) },
            ) { Text("Speichern") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Abbrechen") } },
    )
}
