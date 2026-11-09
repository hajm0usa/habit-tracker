package ir.hajmousa.habits.feature.habits

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ir.hajmousa.habits.R
import ir.hajmousa.habits.core.date.JalaliConverter
import ir.hajmousa.habits.core.date.PersianDigits
import ir.hajmousa.habits.core.date.PersianFormat
import ir.hajmousa.habits.data.db.HabitEntity
import ir.hajmousa.habits.data.db.HabitScheduleVersionEntity
import ir.hajmousa.habits.data.db.TagEntity
import ir.hajmousa.habits.domain.model.HabitType
import ir.hajmousa.habits.domain.model.ScheduleType
import java.time.LocalDate

@Composable
fun HabitEditorDialog(
    item: HabitProgressItem?,
    tags: List<TagEntity>,
    today: LocalDate,
    onDismiss: () -> Unit,
    onSave: (HabitEntity, HabitScheduleVersionEntity, Set<Long>) -> Unit
) {
    val habit = item?.row?.habit
    val version = item?.version
    var name by remember(habit?.id) { mutableStateOf(habit?.name.orEmpty()) }
    var note by remember(habit?.id) { mutableStateOf(habit?.note.orEmpty()) }
    var unit by remember(habit?.id) { mutableStateOf(habit?.unit.orEmpty()) }
    var type by remember(habit?.id) { mutableStateOf(runCatching { HabitType.valueOf(habit?.type ?: "BOOLEAN") }.getOrDefault(HabitType.BOOLEAN)) }
    var scheduleType by remember(habit?.id) { mutableStateOf(runCatching { ScheduleType.valueOf(version?.type?.name ?: "DAILY") }.getOrDefault(ScheduleType.DAILY)) }
    var weekdaysMask by remember(habit?.id) { mutableStateOf(version?.weekdaysMask ?: 127) }
    var intervalText by remember(habit?.id) { mutableStateOf(PersianDigits.convert((version?.intervalN ?: 2).toString())) }
    var anchorText by remember(habit?.id) {
        mutableStateOf(version?.anchorEpochDay?.let { epochDay ->
            val date = JalaliConverter.toJalali(LocalDate.ofEpochDay(epochDay))
            jalaliInput(date.year, date.month, date.day)
        } ?: JalaliConverter.toJalali(today).let { date ->
            jalaliInput(date.year, date.month, date.day)
        })
    }
    var targetText by remember(habit?.id) { mutableStateOf(PersianDigits.convert((version?.targetValue ?: 1).toString())) }
    var stepText by remember(habit?.id) { mutableStateOf(PersianDigits.convert((version?.step ?: 1).toString())) }
    var selectedTagIds by remember(habit?.id, tags) { mutableStateOf(item?.row?.tags?.map { it.id }?.toSet().orEmpty()) }

    val anchorDate = remember(anchorText) { PersianFormat.parseDate(anchorText) }
    val interval = PersianDigits.toAscii(intervalText).toIntOrNull()
    val target = if (type == HabitType.BOOLEAN) 1 else PersianDigits.toAscii(targetText).toIntOrNull()
    val step = if (type == HabitType.BOOLEAN) 1 else PersianDigits.toAscii(stepText).toIntOrNull()
    val validSchedule = when (scheduleType) {
        ScheduleType.DAILY -> true
        ScheduleType.WEEKDAYS -> weekdaysMask != 0
        ScheduleType.EVERY_N -> interval != null && interval > 0 && anchorDate != null
    }
    val valid = name.isNotBlank() && validSchedule && (target ?: 0) > 0 && (step ?: 0) > 0
    val weekdayIds = listOf(
        R.string.weekday_saturday, R.string.weekday_sunday, R.string.weekday_monday,
        R.string.weekday_tuesday, R.string.weekday_wednesday, R.string.weekday_thursday, R.string.weekday_friday
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(if (habit == null) R.string.add_habit else R.string.edit_habit)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text(stringResource(R.string.habit_name)) }, singleLine = true)
                OutlinedTextField(note, { note = it }, label = { Text(stringResource(R.string.habit_note)) })
                if (habit == null) Row(Modifier.horizontalScroll(rememberScrollState())) {
                    HabitType.entries.forEach { option ->
                        FilterChip(type == option, { type = option }, label = {
                            Text(stringResource(if (option == HabitType.BOOLEAN) R.string.habit_boolean else R.string.habit_numeric))
                        }, modifier = Modifier.padding(end = 6.dp))
                    }
                } else Text(stringResource(if (type == HabitType.BOOLEAN) R.string.habit_boolean else R.string.habit_numeric))
                Text(stringResource(R.string.schedule), style = MaterialTheme.typography.titleSmall)
                Row(Modifier.horizontalScroll(rememberScrollState())) {
                    ScheduleType.entries.forEach { option ->
                        FilterChip(scheduleType == option, { scheduleType = option }, label = {
                            Text(stringResource(option.labelResource()))
                        }, modifier = Modifier.padding(end = 6.dp))
                    }
                }
                if (scheduleType == ScheduleType.WEEKDAYS) Row(Modifier.horizontalScroll(rememberScrollState())) {
                    weekdayIds.forEachIndexed { index, label ->
                        val bit = 1 shl index
                        FilterChip(weekdaysMask and bit != 0, { weekdaysMask = weekdaysMask xor bit }, label = { Text(stringResource(label)) }, modifier = Modifier.padding(end = 4.dp))
                    }
                }
                if (scheduleType == ScheduleType.EVERY_N) {
                    OutlinedTextField(intervalText, { intervalText = PersianDigits.convert(PersianDigits.toAscii(it)) }, label = { Text(stringResource(R.string.interval_days)) }, singleLine = true)
                    OutlinedTextField(anchorText, { anchorText = PersianDigits.convert(PersianDigits.toAscii(it)) }, label = { Text(stringResource(R.string.anchor_date)) }, isError = anchorDate == null, singleLine = true)
                }
                if (type == HabitType.NUMERIC) {
                    OutlinedTextField(targetText, { targetText = PersianDigits.convert(PersianDigits.toAscii(it)) }, label = { Text(stringResource(R.string.habit_target)) }, singleLine = true)
                    OutlinedTextField(stepText, { stepText = PersianDigits.convert(PersianDigits.toAscii(it)) }, label = { Text(stringResource(R.string.habit_step)) }, singleLine = true)
                    OutlinedTextField(unit, { unit = it }, label = { Text(stringResource(R.string.habit_unit)) }, singleLine = true)
                }
                if (tags.isNotEmpty()) Text(stringResource(R.string.tags), style = MaterialTheme.typography.titleSmall)
                Row(Modifier.horizontalScroll(rememberScrollState())) {
                    tags.forEach { tag ->
                        FilterChip(tag.id in selectedTagIds, { selectedTagIds = selectedTagIds.toggle(tag.id) }, label = { Text(tag.name) }, modifier = Modifier.padding(end = 6.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(enabled = valid, onClick = {
                val habitEntity = HabitEntity(
                    id = habit?.id ?: 0,
                    name = name.trim(),
                    note = note.trim().ifBlank { null },
                    unit = if (type == HabitType.NUMERIC) unit.trim().ifBlank { null } else null,
                    type = type.name,
                    sortOrder = habit?.sortOrder ?: 0,
                    createdEpochDay = habit?.createdEpochDay ?: today.toEpochDay(),
                    archived = habit?.archived ?: false
                )
                val scheduleEntity = HabitScheduleVersionEntity(
                    habitId = habit?.id ?: 0,
                    effectiveFromEpochDay = today.toEpochDay(),
                    scheduleType = scheduleType.name,
                    weekdaysMask = if (scheduleType == ScheduleType.WEEKDAYS) weekdaysMask else 0,
                    intervalN = if (scheduleType == ScheduleType.EVERY_N) interval else null,
                    anchorEpochDay = if (scheduleType == ScheduleType.EVERY_N) anchorDate?.toEpochDay() else null,
                    targetValue = target ?: 1,
                    step = step ?: 1
                )
                onSave(habitEntity, scheduleEntity, selectedTagIds)
            }) { Text(stringResource(R.string.save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}

private fun ScheduleType.labelResource() = when (this) {
    ScheduleType.DAILY -> R.string.schedule_daily
    ScheduleType.WEEKDAYS -> R.string.schedule_weekdays
    ScheduleType.EVERY_N -> R.string.schedule_every_n
}

private fun Set<Long>.toggle(id: Long): Set<Long> = if (id in this) this - id else this + id

private fun jalaliInput(year: Int, month: Int, day: Int) =
    PersianDigits.convert("%04d/%02d/%02d".format(java.util.Locale.ROOT, year, month, day))
