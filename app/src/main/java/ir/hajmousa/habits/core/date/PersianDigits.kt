package ir.hajmousa.habits.core.date

object PersianDigits {
    fun convert(text: String): String = buildString(text.length) {
        text.forEach { char -> append(if (char in '0'..'9') ('۰'.code + char.code - '0'.code).toChar() else char) }
    }

    fun toAscii(text: String): String = buildString(text.length) {
        text.forEach { char ->
            val digit = when (char) {
                in '۰'..'۹' -> char.code - '۰'.code
                in '٠'..'٩' -> char.code - '٠'.code
                else -> -1
            }
            append(if (digit >= 0) ('0'.code + digit).toChar() else char)
        }
    }
}

object PersianFormat {
    private val months = arrayOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )

    fun date(date: JalaliDate): String = "${PersianDigits.convert(date.day.toString())} ${months[date.month - 1]} ${PersianDigits.convert(date.year.toString())}"

    fun parseDate(text: String): java.time.LocalDate? = runCatching {
        val parts = PersianDigits.toAscii(text.trim()).split(Regex("[/.-]"))
        require(parts.size == 3)
        JalaliConverter.toGregorian(JalaliDate(parts[0].toInt(), parts[1].toInt(), parts[2].toInt()))
    }.getOrNull()

    fun parseTime(text: String): Int? = runCatching {
        val (hour, minute) = PersianDigits.toAscii(text.trim()).split(":").map(String::toInt)
        require(hour in 0..23 && minute in 0..59)
        hour * 60 + minute
    }.getOrNull()
}
