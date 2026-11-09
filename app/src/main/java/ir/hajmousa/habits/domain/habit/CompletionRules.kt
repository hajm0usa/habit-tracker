package ir.hajmousa.habits.domain.habit

import ir.hajmousa.habits.domain.model.HabitLogRecord

object CompletionRules {
    fun isCompleted(log: HabitLogRecord?, targetValue: Int): Boolean =
        log != null && !log.skipped && log.value >= targetValue

    fun adjustValue(current: Int, step: Int, direction: Int): Int {
        require(current >= 0 && step > 0 && (direction == -1 || direction == 1))
        return (current.toLong() + direction.toLong() * step)
            .coerceIn(0, Int.MAX_VALUE.toLong())
            .toInt()
    }
}
}
