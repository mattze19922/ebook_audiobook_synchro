package de.ebooksync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.ebooksync.ui.theme.EbookSyncTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EbookSyncTheme {
                Scaffold(
                    topBar = {
                        LargeFlexibleTopAppBar(title = { Text(stringResource(R.string.app_name)) })
                    },
                ) { padding ->
                    Box(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("Bald: Zeit ↔ Seite umrechnen", style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }
    }
}
