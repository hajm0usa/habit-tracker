package ir.hajmousa.habits.feature.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import ir.hajmousa.habits.R
import ir.hajmousa.habits.core.date.JalaliConverter
import ir.hajmousa.habits.core.date.JalaliDate
import ir.hajmousa.habits.core.date.PersianDigits
import ir.hajmousa.habits.domain.habit.HabitDayStatus
import ir.hajmousa.habits.domain.habit.HabitStats
import ir.hajmousa.habits.domain.model.HabitLogRecord
import ir.hajmousa.habits.domain.model.HabitSchedule
import ir.hajmousa.habits.domain.model.HabitType
import java.time.LocalDate

data class StatsHabit(
    val id: Long,
    val name: String,
    val type: HabitType,
    val createdEpochDay: Long,
    val versions: List<HabitSchedule>,
    val logs: Map<Long, HabitLogRecord>,
    val archived: Boolean
)

private val monthNames = listOf(R.string.month_1, R.string.month_2, R.string.month_3, R.string.month_4, R.string.month_5, R.string.month_6, R.string.month_7, R.string.month_8, R.string.month_9, R.string.month_10, R.string.month_11, R.string.month_12)

@Composable
fun StatsScreen(habits: List<StatsHabit>, today: LocalDate, onSetValue: (Long, Long, Int) -> Unit, onSkip: (Long, Long, Boolean) -> Unit, modifier: Modifier = Modifier) {
    var selectedId by remember(habits) { mutableStateOf(habits.firstOrNull()?.id) }
    var month by remember(today) { mutableStateOf(JalaliConverter.toJalali(today).let { it.year to it.month }) }
    var editDay by remember { mutableStateOf<Long?>(null) }
    val habit = habits.firstOrNull { it.id == selectedId }
    Column(modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (habit == null) {
            Text(stringResource(R.string.empty_stats))
            return@Column
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            val previousHabitDescription = stringResource(R.string.previous_habit)
            val nextHabitDescription = stringResource(R.string.next_habit)
            TextButton(onClick = { selectedId = habits.getOrNull((habits.indexOf(habit) - 1).mod(habits.size))?.id }, modifier = Modifier.semantics { contentDescription = previousHabitDescription }) { Text("‹") }
            Text(habit.name, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = { selectedId = habits.getOrNull((habits.indexOf(habit) + 1).mod(habits.size))?.id }, modifier = Modifier.semantics { contentDescription = nextHabitDescription }) { Text("›") }
        }
        val epochToday = today.toEpochDay()
        val current = ir.hajmousa.habits.domain.habit.StreakCalculator.current(habit.createdEpochDay, epochToday, habit.versions, habit.logs)
        val best = ir.hajmousa.habits.domain.habit.StreakCalculator.best(habit.createdEpochDay, epochToday, habit.versions, habit.logs)
        Text(stringResource(R.string.streak_summary, PersianDigits.convert(current.toString()), PersianDigits.convert(best.toString())))
        val week = HabitStats.successRate(epochToday - 6, epochToday, epochToday, habit.versions, habit.logs)
        val jalaliToday = JalaliConverter.toJalali(today)
        val monthStart = JalaliConverter.toGregorian(JalaliDate(jalaliToday.year, jalaliToday.month, 1)).toEpochDay()
        val monthRate = HabitStats.successRate(monthStart, epochToday, epochToday, habit.versions, habit.logs)
        val allRate = HabitStats.successRate(habit.createdEpochDay, epochToday, epochToday, habit.versions, habit.logs)
        Text(stringResource(R.string.success_rates, formatRate(week.percent), formatRate(monthRate.percent), formatRate(allRate.percent)))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(enabled = month != (1300 to 1), onClick = { month = if (month.second == 1) (month.first - 1) to 12 else month.first to month.second - 1 }) { Text(stringResource(R.string.previous_month)) }
            Text("${stringResource(monthNames[month.second - 1])} ${PersianDigits.convert(month.first.toString())}")
            TextButton(enabled = month != (1500 to 12), onClick = { month = if (month.second == 12) (month.first + 1) to 1 else month.first to month.second + 1 }) { Text(stringResource(R.string.next_month)) }
        }
        val days = JalaliConverter.monthLength(month.first, month.second)
        val firstEpoch = JalaliConverter.toGregorian(JalaliDate(month.first, month.second, 1)).toEpochDay()
        val offset = (LocalDate.ofEpochDay(firstEpoch).dayOfWeek.value + 1) % 7
        val weekdays = listOf(R.string.weekday_saturday, R.string.weekday_sunday, R.string.weekday_monday, R.string.weekday_tuesday, R.string.weekday_wednesday, R.string.weekday_thursday, R.string.weekday_friday)
        LazyVerticalGrid(GridCells.Fixed(7), modifier = Modifier.height(300.dp)) {
            items(7) { index -> Text(stringResource(weekdays[index]), Modifier.fillMaxWidth().padding(vertical = 4.dp), style = MaterialTheme.typography.labelSmall, textAlign = androidx.compose.ui.text.style.TextAlign.Center) }
            items(offset) { Text("") }
            items(days) { index ->
                val day = index + 1
                val epoch = firstEpoch + index
                val status = HabitStats.dayStatus(epoch, epochToday, habit.versions, habit.logs)
                val background = when (status) {
                    HabitDayStatus.DONE -> Color(0xFF2E7D32)
                    HabitDayStatus.MISSED -> MaterialTheme.colorScheme.errorContainer
                    HabitDayStatus.SKIPPED -> MaterialTheme.colorScheme.tertiaryContainer
                    HabitDayStatus.PENDING -> MaterialTheme.colorScheme.secondaryContainer
                    HabitDayStatus.UNSCHEDULED -> MaterialTheme.colorScheme.surfaceVariant
                }
                Card(Modifier.padding(2.dp).clickable { editDay = epoch }, colors = CardDefaults.cardColors(containerColor = background)) {
                    val marker = when (status) {
                        HabitDayStatus.DONE -> "✓"
                        HabitDayStatus.MISSED -> "×"
                        HabitDayStatus.SKIPPED -> "−"
                        HabitDayStatus.PENDING -> "○"
                        HabitDayStatus.UNSCHEDULED -> "·"
                    }
                    Text("${PersianDigits.convert(day.toString())}\n$marker", Modifier.fillMaxWidth().padding(vertical = 3.dp), color = if (status == HabitDayStatus.DONE) Color.White else MaterialTheme.colorScheme.onSurface, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            }
        }
        listOf(R.string.calendar_done, R.string.calendar_missed, R.string.calendar_skipped, R.string.calendar_unscheduled).forEach { Text("●  ${stringResource(it)}") }
        if (habit.type == HabitType.NUMERIC) {
            val trend = HabitStats.numericTrend(epochToday, habit.logs)
            val trendColor = MaterialTheme.colorScheme.primary
            Text(stringResource(R.string.trend_30_days))
        Canvas(Modifier.fillMaxWidth().height(90.dp)) {
                val max = (trend.maxOrNull() ?: 1).coerceAtLeast(1).toFloat()
                val points = trend.mapIndexed { index, value -> Offset(size.width * index / (trend.size - 1), size.height * (1f - value / max)) }
                points.zipWithNext().forEach { (a, b) -> drawLine(trendColor, a, b, 3.dp.toPx()) }
            }
        }
    }
    habit?.let { selected -> editDay?.let { epoch ->
        val allowed = !selected.archived && epoch in (today.toEpochDay() - 7)..today.toEpochDay() && HabitStats.dayStatus(epoch, today.toEpochDay(), selected.versions, selected.logs) != HabitDayStatus.UNSCHEDULED
        if (allowed) EditDayDialog(selected, epoch, onDismiss = { editDay = null }, onSet = { onSetValue(selected.id, epoch, it); editDay = null }, onSkip = { onSkip(selected.id, epoch, it); editDay = null })
        else AlertDialog(onDismissRequest = { editDay = null }, title = { Text(stringResource(R.string.read_only_day)) }, confirmButton = { TextButton(onClick = { editDay = null }) { Text(stringResource(R.string.cancel)) } })
    }
    }
}

@Composable
private fun EditDayDialog(habit: StatsHabit, epoch: Long, onDismiss: () -> Unit, onSet: (Int) -> Unit, onSkip: (Boolean) -> Unit) {
    val version = ir.hajmousa.habits.domain.habit.ScheduleResolver.versionOn(habit.versions, epoch) ?: return
    val previous = habit.logs[epoch]
    var valueText by remember(epoch) { mutableStateOf(PersianDigits.convert((previous?.value ?: 0).toString())) }
    val value = PersianDigits.toAscii(valueText).toIntOrNull()?.takeIf { it >= 0 && (habit.type == HabitType.NUMERIC || it <= 1) }
    val date = JalaliConverter.toJalali(LocalDate.ofEpochDay(epoch))
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(PersianDigits.convert("${date.year}/${date.month}/${date.day}")) },
        text = { if (habit.type == HabitType.NUMERIC) OutlinedTextField(valueText, { valueText = PersianDigits.convert(PersianDigits.toAscii(it)) }, label = { Text(stringResource(R.string.habit_value)) }) else Text(stringResource(if (previous?.value == 1 && previous.skipped.not()) R.string.day_done else R.string.day_not_done)) },
        confirmButton = { Button(enabled = value != null, onClick = { value?.let { onSet(if (habit.type == HabitType.BOOLEAN) if (previous?.value == 1) 0 else 1 else it) } }) { Text(stringResource(R.string.save)) } },
        dismissButton = { Row { TextButton(onClick = { onSkip(previous?.skipped != true) }) { Text(stringResource(R.string.habit_skipped)) }; TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } } }
    )
}

private fun formatRate(value: Int?) = value?.let { "${PersianDigits.convert(it.toString())}٪" } ?: "—"
