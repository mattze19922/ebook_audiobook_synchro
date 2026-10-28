package de.ebooksync.domain

import de.ebooksync.data.Anchor
import kotlin.math.floor
import kotlin.math.roundToLong

/** A position in the eBook: [page] plus how far into that page ([fraction], 0.0–1.0). */
data class PagePosition(val page: Int, val fraction: Double)

/**
 * Converts between audiobook time and eBook page by piecewise linear interpolation.
 *
 * Fixed points are the start (0 s = start of page 1), the end (full duration = end of the last
 * page) and every anchor with a known page. Anchors that contradict the order of the others
 * (page goes down while time goes up) are ignored, so a typo cannot break the calculation.
 */
class SyncCalculator(
    private val totalPages: Int,
    private val durationSeconds: Long,
    anchors: List<Anchor> = emptyList(),
) {
    /** Pairs of (seconds, continuous page position); page position 1.0 = start of page 1. */
    private val points: List<Pair<Double, Double>>

    init {
        require(totalPages > 0) { "totalPages must be positive" }
        require(durationSeconds > 0) { "durationSeconds must be positive" }

        val end = durationSeconds.toDouble() to (totalPages + 1).toDouble()
        val result = mutableListOf(0.0 to 1.0)
        anchors
            .filter { it.page != null }
            .sortedWith(compareBy({ it.seconds }, { it.page }))
            .forEach { anchor ->
                val point = anchor.seconds.toDouble() to anchor.page!!.toDouble()
                val last = result.last()
                if (point.first > last.first && point.second > last.second &&
                    point.first < end.first && point.second < end.second
                ) {
                    result += point
                }
            }
        result += end
        points = result
    }

    fun timeToPage(seconds: Long): PagePosition {
        val position = interpolate(seconds.toDouble(), from = { it.first }, to = { it.second })
        val page = floor(position).toInt()
        return if (page > totalPages) {
            PagePosition(totalPages, 1.0)
        } else {
            PagePosition(page, position - page)
        }
    }

    fun pageToTime(page: Int, fraction: Double = 0.0): Long {
        val position = page + fraction.coerceIn(0.0, 1.0)
        return interpolate(position, from = { it.second }, to = { it.first }).roundToLong()
    }

    private inline fun interpolate(
        value: Double,
        from: (Pair<Double, Double>) -> Double,
        to: (Pair<Double, Double>) -> Double,
    ): Double {
        val clamped = value.coerceIn(from(points.first()), from(points.last()))
        val upperIndex = points.indexOfFirst { from(it) >= clamped }.coerceAtLeast(1)
        val a = points[upperIndex - 1]
        val b = points[upperIndex]
        val t = (clamped - from(a)) / (from(b) - from(a))
        return to(a) + t * (to(b) - to(a))
    }
}
