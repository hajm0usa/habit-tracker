package ir.hajmousa.habits.core.date

import java.time.LocalDate

data class JalaliDate(val year: Int, val month: Int, val day: Int) {
    init {
        require(year in 1300..1500) { "Jalali year outside supported range" }
        require(month in 1..12 && day in 1..JalaliConverter.monthLength(year, month))
    }
}

object JalaliConverter {
    private val breaks = intArrayOf(-61, 9, 38, 199, 426, 686, 756, 818, 1111, 1181, 1210, 1635, 2060, 2097, 2192, 2262, 2324, 2394, 2456, 3178)

    fun toJalali(date: LocalDate): JalaliDate {
        val gy = date.year
        val jy = gy - 621
        val (march, _) = jalCal(jy)
        val firstDay = LocalDate.of(gy, 3, march)
        val offset = (date.toEpochDay() - firstDay.toEpochDay()).toInt()
        val dayOfYear = if (offset >= 0) offset else offset + if (isLeapYear(jy - 1)) 366 else 365
        val month = if (dayOfYear < 186) dayOfYear / 31 + 1 else (dayOfYear - 186) / 30 + 7
        val day = if (dayOfYear < 186) dayOfYear % 31 + 1 else (dayOfYear - 186) % 30 + 1
        return JalaliDate(if (offset >= 0) jy else jy - 1, month, day)
    }

    fun toGregorian(date: JalaliDate): LocalDate {
        val (march, _) = jalCal(date.year)
        return LocalDate.of(date.year + 621, 3, march)
            .plusDays(if (date.month <= 7) (date.month - 1) * 31L + date.day - 1 else 186L + (date.month - 7) * 30 + date.day - 1)
    }

    fun isLeapYear(year: Int): Boolean = jalCal(year).second == 0

    fun monthLength(year: Int, month: Int): Int = when (month) {
        in 1..6 -> 31
        in 7..11 -> 30
        12 -> if (isLeapYear(year)) 30 else 29
        else -> throw IllegalArgumentException("month must be in 1..12")
    }

    private fun jalCal(jy: Int): Pair<Int, Int> {
        require(jy in breaks.first() until breaks.last()) { "Jalali year outside supported range" }
        val gy = jy + 621
        var leapJ = -14
        var jp = breaks[0]
        var jump = 0
        for (index in 1 until breaks.size) {
            val jm = breaks[index]
            jump = jm - jp
            if (jy < jm) break
            leapJ += jump / 33 * 8 + (jump % 33) / 4
            jp = jm
        }
        var n = jy - jp
        leapJ += n / 33 * 8 + (n % 33 + 3) / 4
        if (jump % 33 == 4 && jump - n == 4) leapJ++
        val leapG = gy / 4 - ((gy / 100 + 1) * 3) / 4 - 150
        val march = 20 + leapJ - leapG
        if (jump - n < 6) n = n - jump + (jump + 4) / 33 * 33
        var leap = ((n + 1) % 33 - 1) % 4
        if (leap == -1) leap = 4
        return march to leap
    }
}
