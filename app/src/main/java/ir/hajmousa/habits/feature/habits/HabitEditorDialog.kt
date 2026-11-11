package ir.hajmousa.habits.feature.habits

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import ir.hajmousa.habits.R
import ir.hajmousa.habits.core.date.JalaliConverter
import ir.hajmousa.habits.core.date.PersianDigits
import ir.hajmousa.habits.core.date.PersianFormat
import ir.hajmousa.habits.core.ui.AppSpacing
import ir.hajmousa.habits.core.ui.EditorDialog
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
    reminderMinutes: List<Int>,
    today: LocalDate,
    initialTagIds: Set<Long>,
    saving: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onSave: (HabitEntity, HabitScheduleVersionEntity, Set<Long>, Set<Int>) -> Unit
) {
    val habit = item?.row?.habit
    val version = item?.version
    var name by rememberSaveable(habit?.id) { mutableStateOf(habit?.name.orEmpty()) }
    var note by rememberSaveable(habit?.id) { mutableStateOf(habit?.note.orEmpty()) }
    var unit by rememberSaveable(habit?.id) { mutableStateOf(habit?.unit.orEmpty()) }
    var type by rememberSaveable(habit?.id) { mutableStateOf(runCatching { HabitType.valueOf(habit?.type ?: "BOOLEAN") }.getOrDefault(HabitType.BOOLEAN)) }
    var scheduleType by rememberSaveable(habit?.id) { mutableStateOf(runCatching { ScheduleType.valueOf(version?.type?.name ?: "DAILY") }.getOrDefault(ScheduleType.DAILY)) }
    var weekdaysMask by rememberSaveable(habit?.id) { mutableStateOf(version?.weekdaysMask ?: 127) }
    var intervalText by rememberSaveable(habit?.id) { mutableStateOf(PersianDigits.convert((version?.intervalN ?: 2).toString())) }
    var anchorText by rememberSaveable(habit?.id) {
        mutableStateOf(version?.anchorEpochDay?.let { epochDay ->
            val date = JalaliConverter.toJalali(LocalDate.ofEpochDay(epochDay))
            jalaliInput(date.year, date.month, date.day)
        } ?: JalaliConverter.toJalali(today).let { date ->
            jalaliInput(date.year, date.month, date.day)
        })
    }
    var targetText by rememberSaveable(habit?.id) { mutableStateOf(PersianDigits.convert((version?.targetValue ?: 1).toString())) }
    var stepText by rememberSaveable(habit?.id) { mutableStateOf(PersianDigits.convert((version?.step ?: 1).toString())) }
    var reminderText by rememberSaveable(habit?.id) {
        mutableStateOf(reminderMinutes.joinToString("، ") { minute ->
            PersianDigits.convert("%02d:%02d".format(java.util.Locale.ROOT, minute / 60, minute % 60))
        })
    }
    var selectedTagIds by rememberSaveable(habit?.id) { mutableStateOf(item?.row?.tags?.map { it.id }?.toSet() ?: initialTagIds) }
    var details by rememberSaveable(habit?.id) { mutableStateOf(note.isNotBlank() || reminderText.isNotBlank() || selectedTagIds.isNotEmpty()) }
    val draft = listOf(name, note, unit, type.name, scheduleType.name, weekdaysMask.toString(), intervalText, anchorText, targetText, stepText, reminderText, selectedTagIds.sorted().joinToString())
    val initialDraft = rememberSaveable(habit?.id) { draft }

    val anchorDate = remember(anchorText) { PersianFormat.parseDate(anchorText) }
    val interval = PersianDigits.toAscii(intervalText).toIntOrNull()
    val target = if (type == HabitType.BOOLEAN) 1 else PersianDigits.toAscii(targetText).toIntOrNull()
    val step = if (type == HabitType.BOOLEAN) 1 else PersianDigits.toAscii(stepText).toIntOrNull()
    val parsedReminderTimes = reminderText.split(Regex("[,،;\\s]+")).filter(String::isNotBlank).map(PersianFormat::parseTime)
    val parsedRemindersValid = parsedReminderTimes.all { it != null }
    val validSchedule = when (scheduleType) {
        ScheduleType.DAILY -> true
        ScheduleType.WEEKDAYS -> weekdaysMask != 0
        ScheduleType.EVERY_N -> interval != null && interval > 0 && anchorDate != null
    }
    val valid = name.isNotBlank() && validSchedule && (target ?: 0) > 0 && (step ?: 0) > 0 && parsedRemindersValid
    val weekdayIds = listOf(
        R.string.weekday_saturday, R.string.weekday_sunday, R.string.weekday_monday,
        R.string.weekday_tuesday, R.string.weekday_wednesday, R.string.weekday_thursday, R.string.weekday_friday
    )

    EditorDialog(
        title = stringResource(if (habit == null) R.string.add_habit else R.string.edit_habit),
        valid = valid, dirty = draft != initialDraft, saving = saving, error = error, onDismiss = onDismiss,
        onSave = {
            val habitEntity = HabitEntity(
                id = habit?.id ?: 0, name = name.trim(), note = note.trim().ifBlank { null },
                unit = if (type == HabitType.NUMERIC) unit.trim().ifBlank { null } else null,
                type = type.name, sortOrder = habit?.sortOrder ?: 0,
                createdEpochDay = habit?.createdEpochDay ?: today.toEpochDay(), archived = habit?.archived ?: false
            )
            val scheduleEntity = HabitScheduleVersionEntity(
                habitId = habit?.id ?: 0, effectiveFromEpochDay = today.toEpochDay(), scheduleType = scheduleType.name,
                weekdaysMask = if (scheduleType == ScheduleType.WEEKDAYS) weekdaysMask else 0,
                intervalN = if (scheduleType == ScheduleType.EVERY_N) interval else null,
                anchorEpochDay = if (scheduleType == ScheduleType.EVERY_N) anchorDate?.toEpochDay() else null,
                targetValue = target ?: 1, step = step ?: 1
            )
            onSave(habitEntity, scheduleEntity, selectedTagIds, parsedReminderTimes.filterNotNull().toSet())
        }
    ) {
        OutlinedTextField(name, { name = it }, label = { Text(stringResource(R.string.habit_name)) }, singleLine = true, modifier = Modifier.fillMaxWidth(), enabled = !saving, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next))
        Text(stringResource(R.string.habit_type), style = MaterialTheme.typography.titleSmall)
        if (habit == null) FlowRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
            HabitType.entries.forEach { option ->
                FilterChip(type == option, { type = option }, enabled = !saving, label = {
                    Text(stringResource(if (option == HabitType.BOOLEAN) R.string.habit_boolean else R.string.habit_numeric))
                })
            }
        } else Text(stringResource(if (type == HabitType.BOOLEAN) R.string.habit_boolean else R.string.habit_numeric))
        if (type == HabitType.NUMERIC) {
            OutlinedTextField(
                targetText, { targetText = PersianDigits.convert(PersianDigits.toAscii(it)) },
                label = { Text(stringResource(R.string.habit_target)) }, singleLine = true,
                modifier = Modifier.fillMaxWidth(), enabled = !saving,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                isError = (target ?: 0) <= 0,
                supportingText = { if ((target ?: 0) <= 0) Text(stringResource(R.string.positive_number_hint)) }
            )
            OutlinedTextField(
                stepText, { stepText = PersianDigits.convert(PersianDigits.toAscii(it)) },
                label = { Text(stringResource(R.string.habit_step)) }, singleLine = true,
                modifier = Modifier.fillMaxWidth(), enabled = !saving,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                isError = (step ?: 0) <= 0,
                supportingText = { if ((step ?: 0) <= 0) Text(stringResource(R.string.positive_number_hint)) }
            )
            OutlinedTextField(unit, { unit = it }, label = { Text(stringResource(R.string.habit_unit)) }, singleLine = true, modifier = Modifier.fillMaxWidth(), enabled = !saving)
        }
        Text(stringResource(R.string.schedule), style = MaterialTheme.typography.titleSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
            ScheduleType.entries.forEach { option ->
                FilterChip(scheduleType == option, { scheduleType = option }, enabled = !saving, label = {
                    Text(stringResource(option.labelResource()))
                })
            }
        }
        if (scheduleType == ScheduleType.WEEKDAYS) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
                weekdayIds.forEachIndexed { index, label ->
                    val bit = 1 shl index
                    FilterChip(weekdaysMask and bit != 0, { weekdaysMask = weekdaysMask xor bit }, enabled = !saving, label = { Text(stringResource(label)) })
                }
            }
            if (weekdaysMask == 0) Text(stringResource(R.string.weekdays_required), color = MaterialTheme.colorScheme.error)
        }
        if (scheduleType == ScheduleType.EVERY_N) {
            OutlinedTextField(
                intervalText, { intervalText = PersianDigits.convert(PersianDigits.toAscii(it)) },
                label = { Text(stringResource(R.string.interval_days)) }, singleLine = true,
                modifier = Modifier.fillMaxWidth(), enabled = !saving,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                isError = (interval ?: 0) <= 0,
                supportingText = { if ((interval ?: 0) <= 0) Text(stringResource(R.string.positive_number_hint)) }
            )
            OutlinedTextField(
                anchorText, { anchorText = PersianDigits.convert(PersianDigits.toAscii(it)) },
                label = { Text(stringResource(R.string.anchor_date)) }, isError = anchorDate == null, singleLine = true,
                modifier = Modifier.fillMaxWidth(), enabled = !saving,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Next),
                supportingText = { Text(stringResource(if (anchorDate == null) R.string.due_date_invalid else R.string.date_hint)) }
            )
        }
        if (!details) TextButton(onClick = { details = true }) { Text(stringResource(R.string.more_details)) }
        if (details) {
            OutlinedTextField(note, { note = it }, label = { Text(stringResource(R.string.habit_note)) }, modifier = Modifier.fillMaxWidth(), enabled = !saving)
            OutlinedTextField(
                reminderText,
                { reminderText = PersianDigits.convert(PersianDigits.toAscii(it)) },
                label = { Text(stringResource(R.string.habit_reminder_times)) },
                isError = !parsedRemindersValid,
                singleLine = true, modifier = Modifier.fillMaxWidth(), enabled = !saving,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                supportingText = { if (!parsedRemindersValid) Text(stringResource(R.string.reminder_times_invalid)) }
            )
            if (tags.isNotEmpty()) Text(stringResource(R.string.tags), style = MaterialTheme.typography.titleSmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
                tags.forEach { tag ->
                    FilterChip(tag.id in selectedTagIds, { selectedTagIds = selectedTagIds.toggle(tag.id) }, enabled = !saving, label = { Text(tag.name) })
                }
            }
        }
    }
}

private fun ScheduleType.labelResource() = when (this) {
    ScheduleType.DAILY -> R.string.schedule_daily
    ScheduleType.WEEKDAYS -> R.string.schedule_weekdays
    ScheduleType.EVERY_N -> R.string.schedule_every_n
}

private fun Set<Long>.toggle(id: Long): Set<Long> = if (id in this) this - id else this + id

private fun jalaliInput(year: Int, month: Int, day: Int) =
    PersianDigits.convert("%04d/%02d/%02d".format(java.util.Locale.ROOT, year, month, day))
