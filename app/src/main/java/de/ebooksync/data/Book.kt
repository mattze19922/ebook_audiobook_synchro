package de.ebooksync.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** An eBook/audiobook pair. Only numbers are stored, never the book files themselves. */
@Entity(tableName = "books")
data class Book(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val totalPages: Int,
    val durationSeconds: Long,
    /** Audible ASIN, used for the optional chapter import. */
    val asin: String? = null,
    /** eBook ISBN, used for the optional page-count import. */
    val isbn: String? = null,
)
