package ir.hajmousa.habits.feature.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import ir.hajmousa.habits.R
import ir.hajmousa.habits.core.date.JalaliConverter
import ir.hajmousa.habits.core.date.JalaliDate
import ir.hajmousa.habits.core.date.PersianDigits
import ir.hajmousa.habits.core.date.PersianFormat
import ir.hajmousa.habits.core.ui.AppSpacing
import ir.hajmousa.habits.core.ui.EmptyState
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
    var selectedId by rememberSaveable { mutableStateOf<Long?>(null) }
    var month by rememberSaveable(today) { mutableStateOf(JalaliConverter.toJalali(today).let { it.year to it.month }) }
    var editDay by remember { mutableStateOf<Long?>(null) }
    val habit = habits.firstOrNull { it.id == selectedId } ?: habits.firstOrNull()
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = AppSpacing.small, vertical = AppSpacing.medium), verticalArrangement = Arrangement.spacedBy(AppSpacing.large)) {
        if (habit == null) {
            EmptyState(Icons.Default.BarChart, stringResource(R.string.empty_stats), stringResource(R.string.empty_stats_hint))
            return@Column
        }
        Surface(shape = MaterialTheme.shapes.extraLarge, color = MaterialTheme.colorScheme.primaryContainer) {
            Row(Modifier.fillMaxWidth().padding(AppSpacing.small), verticalAlignment = Alignment.CenterVertically) {
                IconButton(enabled = habits.size > 1, onClick = { selectedId = habits[(habits.indexOf(habit) - 1).mod(habits.size)].id }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = stringResource(R.string.previous_habit))
                }
                Column(Modifier.weight(1f).padding(vertical = AppSpacing.medium)) {
                    Text(habit.name, style = MaterialTheme.typography.headlineSmall)
                    if (habit.archived) Text(stringResource(R.string.habit_archived), style = MaterialTheme.typography.bodyMedium)
                }
                IconButton(enabled = habits.size > 1, onClick = { selectedId = habits[(habits.indexOf(habit) + 1).mod(habits.size)].id }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = stringResource(R.string.next_habit))
                }
            }
        }
        val epochToday = today.toEpochDay()
        val current = ir.hajmousa.habits.domain.habit.StreakCalculator.current(habit.createdEpochDay, epochToday, habit.versions, habit.logs)
        val best = ir.hajmousa.habits.domain.habit.StreakCalculator.best(habit.createdEpochDay, epochToday, habit.versions, habit.logs)
        FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppSpacing.small), verticalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
            MetricCard(stringResource(R.string.current_streak_label), PersianDigits.convert(current.toString()), Modifier.weight(1f))
            MetricCard(stringResource(R.string.best_streak_label), PersianDigits.convert(best.toString()), Modifier.weight(1f))
        }
        val week = HabitStats.successRate(epochToday - 6, epochToday, epochToday, habit.versions, habit.logs)
        val jalaliToday = JalaliConverter.toJalali(today)
        val monthStart = JalaliConverter.toGregorian(JalaliDate(jalaliToday.year, jalaliToday.month, 1)).toEpochDay()
        val monthRate = HabitStats.successRate(monthStart, epochToday, epochToday, habit.versions, habit.logs)
        val allRate = HabitStats.successRate(habit.createdEpochDay, epochToday, epochToday, habit.versions, habit.logs)
        FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppSpacing.small), verticalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
            MetricCard(stringResource(R.string.week_rate_label), formatRate(week.percent), Modifier.weight(1f))
            MetricCard(stringResource(R.string.month_rate_label), formatRate(monthRate.percent), Modifier.weight(1f))
            MetricCard(stringResource(R.string.all_rate_label), formatRate(allRate.percent), Modifier.weight(1f))
        }
        Text(stringResource(R.string.calendar_title), style = MaterialTheme.typography.titleLarge)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            IconButton(enabled = month != (1300 to 1), onClick = { month = if (month.second == 1) (month.first - 1) to 12 else month.first to month.second - 1 }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = stringResource(R.string.previous_month))
            }
            Text("${stringResource(monthNames[month.second - 1])} ${PersianDigits.convert(month.first.toString())}", Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            IconButton(enabled = month != (1500 to 12), onClick = { month = if (month.second == 12) (month.first + 1) to 1 else month.first to month.second + 1 }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = stringResource(R.string.next_month))
            }
        }
        val days = JalaliConverter.monthLength(month.first, month.second)
        val firstEpoch = JalaliConverter.toGregorian(JalaliDate(month.first, month.second, 1)).toEpochDay()
        val offset = (LocalDate.ofEpochDay(firstEpoch).dayOfWeek.value + 1) % 7
        val weekdays = listOf(R.string.weekday_saturday, R.string.weekday_sunday, R.string.weekday_monday, R.string.weekday_tuesday, R.string.weekday_wednesday, R.string.weekday_thursday, R.string.weekday_friday)
        val shortWeekdays = listOf(R.string.weekday_short_saturday, R.string.weekday_short_sunday, R.string.weekday_short_monday, R.string.weekday_short_tuesday, R.string.weekday_short_wednesday, R.string.weekday_short_thursday, R.string.weekday_short_friday)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row {
                weekdays.forEachIndexed { index, label ->
                    val dayName = stringResource(label)
                    Text(stringResource(shortWeekdays[index]), Modifier.weight(1f).padding(vertical = AppSpacing.small).clearAndSetSemantics { contentDescription = dayName }, style = MaterialTheme.typography.labelLarge, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            }
            repeat((offset + days + 6) / 7) { weekIndex ->
                Row {
                    repeat(7) { weekday ->
                        val index = weekIndex * 7 + weekday - offset
                        if (index !in 0 until days) Box(Modifier.weight(1f)) else {
                            val epoch = firstEpoch + index
                            val status = HabitStats.dayStatus(epoch, epochToday, habit.versions, habit.logs)
                            val dayDescription = stringResource(R.string.calendar_day_description, PersianFormat.date(JalaliConverter.toJalali(LocalDate.ofEpochDay(epoch))), stringResource(status.labelRes()))
                            Card(
                                onClick = { editDay = epoch },
                                modifier = Modifier.weight(1f).heightIn(min = 56.dp).semantics { contentDescription = dayDescription },
                                shape = MaterialTheme.shapes.small,
                                colors = CardDefaults.cardColors(containerColor = status.background(), contentColor = status.foreground())
                            ) {
                                Text(
                                    stringResource(R.string.calendar_cell, PersianDigits.convert((index + 1).toString()), stringResource(status.markerRes())),
                                    Modifier.fillMaxWidth().padding(vertical = AppSpacing.small).clearAndSetSemantics {},
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.small), verticalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
            HabitDayStatus.entries.forEach { status ->
                Surface(shape = MaterialTheme.shapes.small, color = status.background(), contentColor = status.foreground()) {
                    Text(stringResource(R.string.calendar_legend, stringResource(status.markerRes()), stringResource(status.labelRes())), Modifier.padding(AppSpacing.small), style = MaterialTheme.typography.labelMedium)
                }
            }
        }
        if (habit.type == HabitType.NUMERIC) {
            val trend = HabitStats.numericTrend(epochToday, habit.logs)
            val trendColor = MaterialTheme.colorScheme.primary
            Text(stringResource(R.string.trend_30_days), style = MaterialTheme.typography.titleLarge)
            val description = stringResource(R.string.trend_values, PersianDigits.convert(trend.joinToString("، ")))
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)) {
                Canvas(Modifier.fillMaxWidth().padding(AppSpacing.medium).height(120.dp).semantics { contentDescription = description }) {
                    val max = (trend.maxOrNull() ?: 1).coerceAtLeast(1).toFloat()
                    val points = trend.mapIndexed { index, value -> Offset(size.width * index / (trend.size - 1), size.height * (1f - value / max)) }
                    points.zipWithNext().forEach { (a, b) -> drawLine(trendColor, a, b, 3.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round) }
                }
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
private fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier.widthIn(min = 96.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)) {
        Column(Modifier.padding(AppSpacing.medium), verticalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
            Text(value, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun HabitDayStatus.labelRes() = when (this) {
    HabitDayStatus.DONE -> R.string.calendar_done
    HabitDayStatus.MISSED -> R.string.calendar_missed
    HabitDayStatus.SKIPPED -> R.string.calendar_skipped
    HabitDayStatus.PENDING -> R.string.calendar_pending
    HabitDayStatus.UNSCHEDULED -> R.string.calendar_unscheduled
}

private fun HabitDayStatus.markerRes() = when (this) {
    HabitDayStatus.DONE -> R.string.marker_done
    HabitDayStatus.MISSED -> R.string.marker_missed
    HabitDayStatus.SKIPPED -> R.string.marker_skipped
    HabitDayStatus.PENDING -> R.string.marker_pending
    HabitDayStatus.UNSCHEDULED -> R.string.marker_unscheduled
}

@Composable
private fun HabitDayStatus.background() = when (this) {
    HabitDayStatus.DONE -> Color(0xFF2E7D32)
    HabitDayStatus.MISSED -> MaterialTheme.colorScheme.errorContainer
    HabitDayStatus.SKIPPED -> MaterialTheme.colorScheme.tertiaryContainer
    HabitDayStatus.PENDING -> MaterialTheme.colorScheme.secondaryContainer
    HabitDayStatus.UNSCHEDULED -> MaterialTheme.colorScheme.surfaceContainerHighest
}

@Composable
private fun HabitDayStatus.foreground() = when (this) {
    HabitDayStatus.DONE -> Color.White
    HabitDayStatus.MISSED -> MaterialTheme.colorScheme.onErrorContainer
    HabitDayStatus.SKIPPED -> MaterialTheme.colorScheme.onTertiaryContainer
    HabitDayStatus.PENDING -> MaterialTheme.colorScheme.onSecondaryContainer
    HabitDayStatus.UNSCHEDULED -> MaterialTheme.colorScheme.onSurface
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
