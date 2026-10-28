package de.ebooksync.domain

import de.ebooksync.data.Anchor
import org.junit.Assert.assertEquals
import org.junit.Test

class SyncCalculatorTest {
    // 300 pages, 10 hours: 2 minutes per page without anchors.
    private val linear = SyncCalculator(totalPages = 300, durationSeconds = 36_000)

    private fun anchor(seconds: Long, page: Int?) = Anchor(bookId = 1, seconds = seconds, page = page)

    @Test
    fun withoutAnchorsIsLinear() {
        assertEquals(PagePosition(1, 0.0), linear.timeToPage(0))
        assertEquals(PagePosition(151, 0.0), linear.timeToPage(18_000))
        assertEquals(PagePosition(2, 0.5), linear.timeToPage(180))
        assertEquals(18_000L, linear.pageToTime(151))
    }

    @Test
    fun endMapsToEndOfLastPage() {
        assertEquals(PagePosition(300, 1.0), linear.timeToPage(36_000))
        assertEquals(36_000L, linear.pageToTime(300, 1.0))
    }

    @Test
    fun valuesOutsideRangeAreClamped() {
        assertEquals(PagePosition(1, 0.0), linear.timeToPage(-5))
        assertEquals(PagePosition(300, 1.0), linear.timeToPage(99_999))
        assertEquals(0L, linear.pageToTime(0))
        assertEquals(36_000L, linear.pageToTime(500))
    }

    @Test
    fun anchorsAreHitExactly() {
        val calc = SyncCalculator(300, 36_000, listOf(anchor(3_600, 10), anchor(30_000, 280)))
        assertEquals(PagePosition(10, 0.0), calc.timeToPage(3_600))
        assertEquals(PagePosition(280, 0.0), calc.timeToPage(30_000))
        assertEquals(3_600L, calc.pageToTime(10))
        assertEquals(30_000L, calc.pageToTime(280))
        // Between the anchors: halfway in time is halfway in pages.
        assertEquals(PagePosition(145, 0.0), calc.timeToPage(16_800))
    }

    @Test
    fun roundTripReturnsSameTime() {
        val calc = SyncCalculator(412, 47_123, listOf(anchor(5_000, 40), anchor(20_000, 190)))
        for (seconds in listOf(0L, 1L, 4_999L, 12_345L, 33_333L, 47_123L)) {
            val pos = calc.timeToPage(seconds)
            assertEquals(seconds.toDouble(), calc.pageToTime(pos.page, pos.fraction).toDouble(), 1.0)
        }
    }

    @Test
    fun anchorsWithoutPageAreIgnored() {
        val calc = SyncCalculator(300, 36_000, listOf(anchor(3_600, null)))
        assertEquals(linear.timeToPage(3_600), calc.timeToPage(3_600))
    }

    @Test
    fun contradictingAndDuplicateAnchorsAreIgnored() {
        val calc = SyncCalculator(
            300, 36_000,
            listOf(anchor(3_600, 10), anchor(3_600, 12), anchor(7_200, 5), anchor(40_000, 290)),
        )
        val expected = SyncCalculator(300, 36_000, listOf(anchor(3_600, 10)))
        for (seconds in listOf(1_000L, 3_600L, 7_200L, 20_000L, 36_000L)) {
            assertEquals(expected.timeToPage(seconds), calc.timeToPage(seconds))
        }
    }

    @Test
    fun parsesAndFormatsTime() {
        assertEquals(5_025L, parseTime("1:23:45"))
        assertEquals(125L, parseTime("2:05"))
        assertEquals(42L, parseTime("42"))
        assertEquals(36_000L, parseTime(" 10:00:00 "))
        assertEquals(null, parseTime("1:60:00"))
        assertEquals(null, parseTime("abc"))
        assertEquals(null, parseTime(""))
        assertEquals(null, parseTime("1:2:3:4"))
        assertEquals("1:23:45", formatTime(5_025))
        assertEquals("0:00:07", formatTime(7))
    }
}
