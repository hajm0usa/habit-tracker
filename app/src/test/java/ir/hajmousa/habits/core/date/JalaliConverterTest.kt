package ir.hajmousa.habits.core.date

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class JalaliConverterTest {
    @Test fun knownNewYearsAndCurrentDay() {
        listOf(
            JalaliDate(1400, 1, 1) to LocalDate.of(2021, 3, 21),
            JalaliDate(1402, 1, 1) to LocalDate.of(2023, 3, 21),
            JalaliDate(1403, 1, 1) to LocalDate.of(2024, 3, 20),
            JalaliDate(1404, 1, 1) to LocalDate.of(2025, 3, 21),
            JalaliDate(1405, 8, 18) to LocalDate.of(2026, 11, 9)
        ).forEach { (jalali, gregorian) ->
            assertEquals(gregorian, JalaliConverter.toGregorian(jalali))
            assertEquals(jalali, JalaliConverter.toJalali(gregorian))
        }
    }

    @Test fun leapEsfandValidation() {
        assertEquals(LocalDate.of(2025, 3, 20), JalaliConverter.toGregorian(JalaliDate(1403, 12, 30)))
        assertThrows(IllegalArgumentException::class.java) { JalaliDate(1404, 12, 30) }
    }

    @Test fun roundTripsSupportedRange() {
        var day = LocalDate.of(2010, 1, 1)
        val end = LocalDate.of(2035, 1, 1)
        while (day < end) {
            assertEquals(day, JalaliConverter.toGregorian(JalaliConverter.toJalali(day)))
            day = day.plusDays(1)
        }
    }

    @Test fun formatsPersianDigitsAndMonthName() {
        assertEquals("۱۸ آبان ۱۴۰۵", PersianFormat.date(JalaliDate(1405, 8, 18)))
        assertEquals("سال ۱۴۰۵", PersianDigits.convert("سال 1405"))
    }

    @Test fun supportedYearsHaveConsistentLengths() {
        for (year in 1300..1500) {
            val days = (1..12).sumOf { JalaliConverter.monthLength(year, it) }
            org.junit.Assert.assertTrue("invalid year length: $year has $days days", days == 365 || days == 366)
            assertEquals(days == 366, JalaliConverter.isLeapYear(year))
            for (month in 1..12) {
                for (day in 1..JalaliConverter.monthLength(year, month)) {
                    val date = JalaliDate(year, month, day)
                    assertEquals(date, JalaliConverter.toJalali(JalaliConverter.toGregorian(date)))
                }
            }
        }
    }
}
