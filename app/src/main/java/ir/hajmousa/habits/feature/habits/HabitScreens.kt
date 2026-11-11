package ir.hajmousa.habits.feature.habits

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import ir.hajmousa.habits.R
import ir.hajmousa.habits.core.date.PersianDigits
import ir.hajmousa.habits.core.ui.AppSpacing
import ir.hajmousa.habits.core.ui.EmptyState
import ir.hajmousa.habits.core.ui.ItemMenu
import ir.hajmousa.habits.data.db.HabitWithTags
import ir.hajmousa.habits.data.db.TagEntity
import ir.hajmousa.habits.domain.habit.CompletionRules
import ir.hajmousa.habits.domain.model.HabitLogRecord
import ir.hajmousa.habits.domain.model.HabitSchedule
import ir.hajmousa.habits.domain.model.HabitType

data class HabitProgressItem(
    val row: HabitWithTags,
    val version: HabitSchedule,
    val log: HabitLogRecord?,
    val scheduledToday: Boolean,
    val currentStreak: Int,
    val reminderMinutes: List<Int> = emptyList()
)

@Composable
fun HabitManagerScreen(
    items: List<HabitProgressItem>,
    tags: List<TagEntity>,
    selectedTagIds: Set<Long>,
    onToggleTag: (Long) -> Unit,
    onClearFilter: () -> Unit,
    onValue: (Long, Int) -> Unit,
    onAdjust: (Long, Int) -> Unit,
    onSkip: (Long, Boolean) -> Unit,
    onEdit: (HabitProgressItem) -> Unit,
    onArchive: (Long, Boolean) -> Unit,
    onDelete: (HabitProgressItem) -> Unit,
    onReorder: (List<Long>) -> Unit,
    modifier: Modifier = Modifier
) {
    var showArchived by rememberSaveable { mutableStateOf(false) }
    val shown = if (showArchived) items else items.filterNot { it.row.habit.archived }
    val latestShown by rememberUpdatedState(shown)
    val latestOnReorder by rememberUpdatedState(onReorder)
    val listState = rememberLazyListState()
    val canReorder = selectedTagIds.isEmpty() && (showArchived || items.none { it.row.habit.archived })

    Column(modifier.fillMaxSize()) {
        if (tags.isNotEmpty()) Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selectedTagIds.isNotEmpty()) TextButton(onClick = onClearFilter) { Text(stringResource(R.string.clear_filter)) }
            tags.forEach { tag ->
                FilterChip(tag.id in selectedTagIds, { onToggleTag(tag.id) }, label = { Text(tag.name) }, modifier = Modifier.padding(end = 8.dp))
            }
        }
        if (showArchived || items.any { it.row.habit.archived }) TextButton(onClick = { showArchived = !showArchived }, modifier = Modifier.align(Alignment.Start).padding(horizontal = AppSpacing.medium)) {
            Text(stringResource(if (showArchived) R.string.hide_archived else R.string.show_archived))
        }
        if (shown.isEmpty()) {
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(AppSpacing.medium), horizontalAlignment = Alignment.CenterHorizontally) {
                EmptyState(Icons.Default.LocalFlorist, stringResource(if (selectedTagIds.isEmpty()) R.string.empty_habits else R.string.empty_filtered_habits), stringResource(if (selectedTagIds.isEmpty()) R.string.empty_habits_hint else R.string.filter_hint))
                if (selectedTagIds.isNotEmpty()) TextButton(onClick = onClearFilter) { Text(stringResource(R.string.clear_filter)) }
            }
        } else LazyColumn(
            state = listState, modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = AppSpacing.medium, end = AppSpacing.medium, top = AppSpacing.small, bottom = AppSpacing.listBottom),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
        ) {
            items(shown, key = { it.row.habit.id }) { item ->
                var dragDistance by remember(item.row.habit.id) { mutableStateOf(0f) }
                var draggedIndex by remember(item.row.habit.id) { mutableStateOf(-1) }
                val dragModifier = if (!canReorder) Modifier else Modifier.pointerInput(item.row.habit.id) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = { dragDistance = 0f },
                        onDragEnd = { draggedIndex = -1 },
                        onDragCancel = { draggedIndex = -1 },
                        onDrag = { change, delta ->
                            change.consume()
                            dragDistance += delta.y
                            val visible = listState.layoutInfo.visibleItemsInfo
                            val dragged = visible.firstOrNull { it.key == item.row.habit.id } ?: return@detectDragGesturesAfterLongPress
                            if (draggedIndex < 0) draggedIndex = latestShown.indexOfFirst { it.row.habit.id == item.row.habit.id }
                            val center = dragged.offset + dragged.size / 2 + dragDistance
                            val target = visible.firstOrNull { center >= it.offset && center <= it.offset + it.size } ?: return@detectDragGesturesAfterLongPress
                            val to = target.index
                            val from = draggedIndex
                            if (from >= 0 && to in latestShown.indices && to != from) {
                                val reordered = latestShown.toMutableList().apply { add(to, removeAt(from)) }
                                latestOnReorder(reordered.map { it.row.habit.id })
                                draggedIndex = to
                                dragDistance = 0f
                            }
                        }
                    )
                }
                HabitProgressCard(item, onValue, onAdjust, onSkip, dragModifier, editable = !item.row.habit.archived, menuItems = { close ->
                    DropdownMenuItem(text = { Text(stringResource(R.string.edit_habit)) }, onClick = { close(); onEdit(item) })
                    DropdownMenuItem(text = { Text(stringResource(if (item.row.habit.archived) R.string.unarchive_habit else R.string.archive_habit)) }, onClick = { close(); onArchive(item.row.habit.id, !item.row.habit.archived) })
                    if (canReorder) {
                        val index = shown.indexOf(item)
                        DropdownMenuItem(enabled = index > 0, text = { Text(stringResource(R.string.move_up)) }, onClick = {
                            close()
                            onReorder(shown.toMutableList().apply { add(index - 1, removeAt(index)) }.map { it.row.habit.id })
                        })
                        DropdownMenuItem(enabled = index < shown.lastIndex, text = { Text(stringResource(R.string.move_down)) }, onClick = {
                            close()
                            onReorder(shown.toMutableList().apply { add(index + 1, removeAt(index)) }.map { it.row.habit.id })
                        })
                    }
                    DropdownMenuItem(text = { Text(stringResource(R.string.delete_habit), color = MaterialTheme.colorScheme.error) }, onClick = { close(); onDelete(item) })
                })
            }
        }
    }
}

@Composable
fun HabitProgressCard(
    item: HabitProgressItem,
    onValue: (Long, Int) -> Unit,
    onAdjust: (Long, Int) -> Unit,
    onSkip: (Long, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    editable: Boolean = true,
    menuItems: @Composable ColumnScope.(close: () -> Unit) -> Unit = {}
) {
    val habit = item.row.habit
    val skipped = item.log?.skipped == true
    val value = item.log?.value ?: 0
    val complete = CompletionRules.isCompleted(item.log, item.version.targetValue)
    val cardColor by animateColorAsState(
        if (complete) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerLowest,
        label = "habitCompletion"
    )
    val progress by animateFloatAsState((value.toFloat() / item.version.targetValue).coerceIn(0f, 1f), label = "habitProgress")
    var editExact by remember(habit.id) { mutableStateOf(false) }
    Card(modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = cardColor, contentColor = if (complete) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface)) {
        Column(Modifier.padding(AppSpacing.small), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (habit.type == HabitType.BOOLEAN.name) {
                    Checkbox(checked = complete, enabled = editable, onCheckedChange = { onValue(habit.id, if (it) 1 else 0) }, modifier = Modifier.semantics { contentDescription = habit.name })
                }
                Column(Modifier.weight(1f)) {
                    Text(habit.name, style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.habit_streak, PersianDigits.convert(item.currentStreak.toString())), style = MaterialTheme.typography.bodySmall)
                }
                if (complete && habit.type == HabitType.NUMERIC.name) {
                    Icon(Icons.Default.CheckCircle, contentDescription = stringResource(R.string.day_done))
                }
                ItemMenu(stringResource(R.string.item_actions, habit.name)) { close ->
                    if (editable && item.scheduledToday) DropdownMenuItem(
                        text = { Text(stringResource(if (skipped) R.string.unskip_today else R.string.skip_today)) },
                        onClick = { close(); onSkip(habit.id, !skipped) }
                    )
                    menuItems(close)
                }
            }
            if (habit.type == HabitType.NUMERIC.name) {
                val valueDescription = stringResource(R.string.habit_value_description, habit.name, PersianDigits.convert(value.toString()))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
                    val decreaseDescription = stringResource(R.string.decrease_value)
                    val increaseDescription = stringResource(R.string.increase_value)
                    FilledTonalIconButton(enabled = editable, onClick = { onAdjust(habit.id, -1) }, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Default.Remove, contentDescription = decreaseDescription)
                    }
                    TextButton(enabled = editable, onClick = { editExact = true }, modifier = Modifier.weight(1f).semantics { contentDescription = valueDescription }) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(stringResource(R.string.habit_progress_value, PersianDigits.convert(value.toString()), PersianDigits.convert(item.version.targetValue.toString())), style = MaterialTheme.typography.titleMedium)
                            habit.unit?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                        }
                    }
                    FilledTonalIconButton(enabled = editable, onClick = { onAdjust(habit.id, 1) }, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Default.Add, contentDescription = increaseDescription)
                    }
                }
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.small))
            }
            if (!item.scheduledToday) Text(stringResource(R.string.not_scheduled_today), style = MaterialTheme.typography.bodySmall)
            if (skipped) Text(stringResource(R.string.habit_skipped), modifier = Modifier.padding(horizontal = AppSpacing.small), style = MaterialTheme.typography.bodySmall)
            if (item.row.tags.isNotEmpty()) Text(item.row.tags.joinToString(" · ") { it.name }, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(horizontal = AppSpacing.small))
        }
    }
    if (editExact) ExactValueDialog(
        initialValue = value,
        onDismiss = { editExact = false },
        onSave = { onValue(habit.id, it); editExact = false }
    )
}

@Composable
private fun ExactValueDialog(initialValue: Int, onDismiss: () -> Unit, onSave: (Int) -> Unit) {
    var text by remember(initialValue) { mutableStateOf(PersianDigits.convert(initialValue.toString())) }
    val value = PersianDigits.toAscii(text).toIntOrNull()?.takeIf { it >= 0 }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.exact_value)) },
        text = { OutlinedTextField(text, { text = PersianDigits.convert(PersianDigits.toAscii(it)) }, label = { Text(stringResource(R.string.habit_value)) }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)) },
        confirmButton = { TextButton(enabled = value != null, onClick = { value?.let(onSave) }) { Text(stringResource(R.string.save)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}
