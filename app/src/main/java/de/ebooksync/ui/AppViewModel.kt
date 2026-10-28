package de.ebooksync.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.ebooksync.data.Anchor
import de.ebooksync.data.AppDatabase
import de.ebooksync.data.Book
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.get(application).bookDao()

    val books: StateFlow<List<Book>> =
        dao.observeBooks().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun book(id: Long): Flow<Book?> = dao.observeBook(id)

    fun anchors(bookId: Long): Flow<List<Anchor>> = dao.observeAnchors(bookId)

    /** Inserts or updates [book]; [onSaved] receives its id. */
    fun saveBook(book: Book, onSaved: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = if (book.id == 0L) dao.insertBook(book) else book.id.also { dao.updateBook(book) }
            onSaved(id)
        }
    }

    fun deleteBook(book: Book) {
        viewModelScope.launch { dao.deleteBook(book) }
    }

    fun saveAnchor(anchor: Anchor) {
        viewModelScope.launch { dao.upsertAnchor(anchor) }
    }

    fun deleteAnchor(anchor: Anchor) {
        viewModelScope.launch { dao.deleteAnchor(anchor) }
    }
}
