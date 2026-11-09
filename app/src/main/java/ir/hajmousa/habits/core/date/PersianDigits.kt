package ir.hajmousa.habits.core.date

object PersianDigits {
    fun convert(text: String): String = buildString(text.length) {
        text.forEach { char -> append(if (char in '0'..'9') ('۰'.code + char.code - '0'.code).toChar() else char) }
    }
}

object PersianFormat {
    private val months = arrayOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )

    fun date(date: JalaliDate): String = "${PersianDigits.convert(date.day.toString())} ${months[date.month - 1]} ${PersianDigits.convert(date.year.toString())}"
}
