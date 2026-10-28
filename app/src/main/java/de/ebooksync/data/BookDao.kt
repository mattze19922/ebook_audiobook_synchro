package de.ebooksync.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Query("SELECT * FROM books ORDER BY title COLLATE NOCASE")
    fun observeBooks(): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE id = :id")
    fun observeBook(id: Long): Flow<Book?>

    @Insert
    suspend fun insertBook(book: Book): Long

    @Update
    suspend fun updateBook(book: Book)

    @Delete
    suspend fun deleteBook(book: Book)

    @Query("SELECT * FROM anchors WHERE bookId = :bookId ORDER BY seconds")
    fun observeAnchors(bookId: Long): Flow<List<Anchor>>

    @Upsert
    suspend fun upsertAnchor(anchor: Anchor): Long

    @Insert
    suspend fun insertAnchors(anchors: List<Anchor>)

    @Delete
    suspend fun deleteAnchor(anchor: Anchor)
}
