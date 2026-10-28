package de.ebooksync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import de.ebooksync.ui.AppViewModel
import de.ebooksync.ui.BookListScreen
import de.ebooksync.ui.BookScreen
import de.ebooksync.ui.theme.EbookSyncTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EbookSyncTheme {
                val appViewModel: AppViewModel = viewModel()
                var selectedBookId by rememberSaveable { mutableStateOf<Long?>(null) }

                BackHandler(enabled = selectedBookId != null) { selectedBookId = null }

                when (val id = selectedBookId) {
                    null -> BookListScreen(appViewModel, onOpenBook = { selectedBookId = it })
                    else -> BookScreen(id, appViewModel, onBack = { selectedBookId = null })
                }
            }
        }
    }
}
