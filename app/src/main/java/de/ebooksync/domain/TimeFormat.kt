package de.ebooksync.domain

/** Parses "h:mm:ss", "mm:ss" or "ss" into seconds; returns null for invalid input. */
fun parseTime(text: String): Long? {
    val parts = text.trim().split(":")
    if (parts.isEmpty() || parts.size > 3) return null
    val numbers = parts.map { it.trim().toLongOrNull()?.takeIf { n -> n >= 0 } ?: return null }
    // Minutes and seconds after the first field must stay below 60.
    if (numbers.drop(1).any { it >= 60 }) return null
    return numbers.fold(0L) { total, n -> total * 60 + n }
}

/** Formats seconds as "h:mm:ss". */
fun formatTime(seconds: Long): String {
    val s = seconds.coerceAtLeast(0)
    return "%d:%02d:%02d".format(s / 3600, s % 3600 / 60, s % 60)
}
