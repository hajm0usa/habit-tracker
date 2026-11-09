package ir.hajmousa.habits.feature.habits

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import ir.hajmousa.habits.R
import ir.hajmousa.habits.core.date.PersianDigits
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
    val currentStreak: Int
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
    var showArchived by remember { mutableStateOf(false) }
    val shown = if (showArchived) items else items.filterNot { it.row.habit.archived }
    val latestShown by rememberUpdatedState(shown)
    val latestOnReorder by rememberUpdatedState(onReorder)
    val listState = rememberLazyListState()

    Column(modifier.fillMaxSize()) {
        if (tags.isNotEmpty()) Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tags.forEach { tag ->
                FilterChip(tag.id in selectedTagIds, { onToggleTag(tag.id) }, label = { Text(tag.name) }, modifier = Modifier.padding(end = 8.dp))
            }
        }
        TextButton(onClick = { showArchived = !showArchived }, modifier = Modifier.align(Alignment.Start).padding(horizontal = 16.dp)) {
            Text(stringResource(if (showArchived) R.string.hide_archived else R.string.show_archived))
        }
        if (shown.isEmpty()) {
            Column(Modifier.weight(1f).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(if (selectedTagIds.isEmpty()) R.string.empty_habits else R.string.empty_filtered_habits))
                if (selectedTagIds.isNotEmpty()) TextButton(onClick = onClearFilter) { Text(stringResource(R.string.clear_filter)) }
            }
        } else LazyColumn(state = listState, modifier = Modifier.weight(1f), contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp)) {
            items(shown, key = { it.row.habit.id }) { item ->
                var dragDistance by remember(item.row.habit.id) { mutableStateOf(0f) }
                var draggedIndex by remember(item.row.habit.id) { mutableStateOf(-1) }
                val dragModifier = if (selectedTagIds.isNotEmpty() || (items.any { it.row.habit.archived } && !showArchived)) Modifier else Modifier.pointerInput(item.row.habit.id) {
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
                Column {
                    HabitProgressCard(item, onValue, onAdjust, onSkip, dragModifier, editable = !item.row.habit.archived)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { onEdit(item) }) { Text(stringResource(R.string.edit_habit)) }
                        TextButton(onClick = { onArchive(item.row.habit.id, !item.row.habit.archived) }) {
                            Text(stringResource(if (item.row.habit.archived) R.string.unarchive_habit else R.string.archive_habit))
                        }
                        TextButton(onClick = { onDelete(item) }) { Text(stringResource(R.string.delete_habit)) }
                    }
                }
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
    editable: Boolean = true
) {
    val habit = item.row.habit
    val skipped = item.log?.skipped == true
    val value = item.log?.value ?: 0
    val complete = CompletionRules.isCompleted(item.log, item.version.targetValue)
    var editExact by remember(habit.id) { mutableStateOf(false) }
    Card(modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (habit.type == HabitType.BOOLEAN.name) {
                    Checkbox(checked = complete, enabled = editable, onCheckedChange = { onValue(habit.id, if (it) 1 else 0) })
                }
                Column(Modifier.weight(1f)) {
                    Text(habit.name, style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.habit_streak, PersianDigits.convert(item.currentStreak.toString())), style = MaterialTheme.typography.bodySmall)
                }
                if (habit.type == HabitType.NUMERIC.name) {
                    val valueDescription = stringResource(R.string.habit_value_description, habit.name, PersianDigits.convert(value.toString()))
                    TextButton(enabled = editable, onClick = { editExact = true }, modifier = Modifier.semantics { contentDescription = valueDescription }) {
                        Text("${PersianDigits.convert(value.toString())} / ${PersianDigits.convert(item.version.targetValue.toString())}${habit.unit?.let { " $it" } ?: ""}")
                    }
                }
            }
            if (habit.type == HabitType.NUMERIC.name) {
                LinearProgressIndicator(
                    progress = { (value.toFloat() / item.version.targetValue).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val decreaseDescription = stringResource(R.string.decrease_value)
                    val increaseDescription = stringResource(R.string.increase_value)
                    TextButton(enabled = editable, onClick = { onAdjust(habit.id, -1) }, modifier = Modifier.semantics {
                        contentDescription = decreaseDescription
                    }) { Icon(Icons.Default.Remove, contentDescription = null) }
                    TextButton(enabled = editable, onClick = { onAdjust(habit.id, 1) }, modifier = Modifier.semantics { contentDescription = increaseDescription }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                    }
                }
            }
            if (!item.scheduledToday) Text(stringResource(R.string.not_scheduled_today), style = MaterialTheme.typography.bodySmall)
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (skipped) Text(stringResource(R.string.habit_skipped), color = MaterialTheme.colorScheme.error)
                if (editable && item.scheduledToday) TextButton(onClick = { onSkip(habit.id, !skipped) }) {
                    Text(stringResource(if (skipped) R.string.unskip_today else R.string.skip_today))
                }
            }
            if (item.row.tags.isNotEmpty()) Text(item.row.tags.joinToString(" · ") { it.name }, style = MaterialTheme.typography.bodySmall)
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
    text = { OutlinedTextField(text, { text = PersianDigits.convert(PersianDigits.toAscii(it)) }, label = { Text(stringResource(R.string.habit_value)) }, singleLine = true) },
        confirmButton = { TextButton(enabled = value != null, onClick = { value?.let(onSave) }) { Text(stringResource(R.string.save)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}
