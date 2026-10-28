package de.ebooksync.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A known matching point between audiobook time and eBook page.
 *
 * [page] is null for imported chapter marks whose start page has not been entered yet;
 * such anchors are ignored by the calculation.
 */
@Entity(
    tableName = "anchors",
    foreignKeys = [
        ForeignKey(
            entity = Book::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("bookId")],
)
data class Anchor(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: Long,
    val seconds: Long,
    val page: Int?,
    val label: String = "",
)
