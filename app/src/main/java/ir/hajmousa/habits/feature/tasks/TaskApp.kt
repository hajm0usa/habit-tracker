package ir.hajmousa.habits.feature.tasks

import android.util.Log
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import ir.hajmousa.habits.AppContainer
import ir.hajmousa.habits.R
import ir.hajmousa.habits.core.date.JalaliConverter
import ir.hajmousa.habits.core.date.PersianDigits
import ir.hajmousa.habits.core.date.PersianFormat
import ir.hajmousa.habits.data.db.TagEntity
import ir.hajmousa.habits.data.db.TaskEntity
import ir.hajmousa.habits.data.db.TaskWithTags
import ir.hajmousa.habits.feature.habits.HabitProgressItem
import ir.hajmousa.habits.feature.habits.HabitManagerScreen
import ir.hajmousa.habits.feature.habits.HabitProgressCard
import ir.hajmousa.habits.feature.habits.HabitEditorDialog
import ir.hajmousa.habits.feature.settings.SettingsScreen
import ir.hajmousa.habits.feature.stats.StatsHabit
import ir.hajmousa.habits.feature.stats.StatsScreen
import ir.hajmousa.habits.data.backup.BackupModel
import ir.hajmousa.habits.data.backup.BackupSummary
import ir.hajmousa.habits.feature.widget.WidgetUpdater
import ir.hajmousa.habits.data.repo.toRecord
import ir.hajmousa.habits.data.repo.toModel
import ir.hajmousa.habits.domain.model.TaskPriority
import ir.hajmousa.habits.domain.model.HabitType
import ir.hajmousa.habits.domain.model.TaskRecord
import ir.hajmousa.habits.domain.model.TaskSection
import ir.hajmousa.habits.domain.task.TaskGrouping
import ir.hajmousa.habits.domain.task.TaskTagFilter
import ir.hajmousa.habits.domain.habit.ScheduleResolver
import ir.hajmousa.habits.domain.habit.StreakCalculator
import java.time.LocalDate
import java.time.Instant
import java.time.ZoneId
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private enum class Page { TODAY, TASKS, HABITS, STATS, TAGS, SETTINGS }

private data class TagIcon(val key: String, val image: ImageVector, val label: Int)

private val tagIcons = listOf(
    TagIcon("home", Icons.Default.Home, R.string.tag_icon_home),
    TagIcon("star", Icons.Default.Star, R.string.tag_icon_star),
    TagIcon("favorite", Icons.Default.Favorite, R.string.tag_icon_favorite),
    TagIcon("check", Icons.Default.Check, R.string.tag_icon_check),
    TagIcon("cart", Icons.Default.ShoppingCart, R.string.tag_icon_cart),
    TagIcon("settings", Icons.Default.Settings, R.string.tag_icon_settings),
    TagIcon("flag", Icons.Default.Flag, R.string.tag_icon_flag),
    TagIcon("school", Icons.Default.School, R.string.tag_icon_school),
    TagIcon("work", Icons.Default.Work, R.string.tag_icon_work),
    TagIcon("book", Icons.Default.Book, R.string.tag_icon_book),
    TagIcon("fitness", Icons.Default.FitnessCenter, R.string.tag_icon_fitness),
    TagIcon("pets", Icons.Default.Pets, R.string.tag_icon_pets),
    TagIcon("food", Icons.Default.Restaurant, R.string.tag_icon_food),
    TagIcon("flight", Icons.Default.Flight, R.string.tag_icon_flight),
    TagIcon("car", Icons.Default.DirectionsCar, R.string.tag_icon_car),
    TagIcon("idea", Icons.Default.Lightbulb, R.string.tag_icon_idea),
    TagIcon("music", Icons.Default.MusicNote, R.string.tag_icon_music),
    TagIcon("camera", Icons.Default.CameraAlt, R.string.tag_icon_camera),
    TagIcon("build", Icons.Default.Build, R.string.tag_icon_build),
    TagIcon("event", Icons.Default.Event, R.string.tag_icon_event),
    TagIcon("celebration", Icons.Default.Celebration, R.string.tag_icon_celebration),
    TagIcon("flower", Icons.Default.LocalFlorist, R.string.tag_icon_flower),
    TagIcon("game", Icons.Default.SportsEsports, R.string.tag_icon_game),
    TagIcon("phone", Icons.Default.Phone, R.string.tag_icon_phone),
    TagIcon("computer", Icons.Default.Computer, R.string.tag_icon_computer),
    TagIcon("email", Icons.Default.Email, R.string.tag_icon_email),
    TagIcon("money", Icons.Default.AttachMoney, R.string.tag_icon_money),
    TagIcon("bank", Icons.Default.AccountBalance, R.string.tag_icon_bank),
    TagIcon("run", Icons.Default.DirectionsRun, R.string.tag_icon_run),
    TagIcon("calm", Icons.Default.SelfImprovement, R.string.tag_icon_calm)
)

private val tagColors = listOf(
    0xFF6750A4.toInt() to R.string.tag_color_purple,
    0xFFB3261E.toInt() to R.string.tag_color_red,
    0xFF386A20.toInt() to R.string.tag_color_green,
    0xFF0061A4.toInt() to R.string.tag_color_blue,
    0xFF984061.toInt() to R.string.tag_color_pink,
    0xFF695D41.toInt() to R.string.tag_color_brown
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskApp(
    container: AppContainer,
    notificationPermissionGranted: Boolean,
    notificationPermissionDenied: Boolean,
    onRequestNotifications: () -> Unit,
    onDismissPermissionPrompt: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    onOpenBatterySettings: () -> Unit
) {
    val context = LocalContext.current
    val tasks by container.tasks.observeAll().collectAsState(initial = emptyList())
    val tags by container.tags.observeAll().collectAsState(initial = emptyList())
    val habitRows by container.habits.observeAll().collectAsState(initial = emptyList())
    val scheduleRows by container.habits.observeVersions().collectAsState(initial = emptyList())
    val logRows by container.habits.observeLogs().collectAsState(initial = emptyList())
    val reminderRows by container.habits.observeReminders().collectAsState(initial = emptyList())
    val weeklyBackupEnabled by container.settings.weeklyBackupEnabled.collectAsState(initial = true)
    val weeklyBackupMinute by container.settings.weeklyBackupMinute.collectAsState(initial = 17 * 60)
    val dayStartHour by container.settings.dayStartHour.collectAsState(initial = 0)
    container.dayClock.setDayStartHour(dayStartHour)
    val today = container.dayClock.today()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var page by remember { mutableStateOf(Page.TODAY) }
    var selectedTagIds by remember { mutableStateOf(emptySet<Long>()) }
    var editingTask by remember { mutableStateOf<TaskWithTags?>(null) }
    var showTaskEditor by remember { mutableStateOf(false) }
    var editingTag by remember { mutableStateOf<TagEntity?>(null) }
    var showTagEditor by remember { mutableStateOf(false) }
    var showDone by remember { mutableStateOf(false) }
    var editingHabit by remember { mutableStateOf<HabitProgressItem?>(null) }
    var showHabitEditor by remember { mutableStateOf(false) }
    var pendingHabitDeletes by remember { mutableStateOf(emptySet<Long>()) }
    var pendingRestore by remember { mutableStateOf<Pair<BackupModel, BackupSummary>?>(null) }
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) scope.launch {
            try {
                val json = container.backup.export()
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.bufferedWriter(Charsets.UTF_8)?.use { it.write(json) }
                        ?: error("Unable to open backup destination")
                }
                snackbar.showSnackbar(context.getString(R.string.backup_exported))
            } catch (error: Exception) {
                if (error is kotlinx.coroutines.CancellationException) throw error
                snackbar.showSnackbar(context.getString(R.string.operation_failed))
            }
        }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            try {
                val content = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        val output = ByteArrayOutputStream()
                        val buffer = ByteArray(8192)
                        while (true) {
                            val count = input.read(buffer, 0, minOf(buffer.size, 20_000_001 - output.size()))
                            if (count < 0) break
                            output.write(buffer, 0, count)
                            require(output.size() <= 20_000_000) { "Backup is too large" }
                        }
                        output.toString(Charsets.UTF_8.name())
                    } ?: error("Unable to read backup")
                }
                pendingRestore = withContext(Dispatchers.Default) { container.backup.inspect(content) }
            } catch (error: Exception) {
                if (error is kotlinx.coroutines.CancellationException) throw error
                snackbar.showSnackbar(context.getString(R.string.invalid_backup))
            }
        }
    }

    val visible = tasks.filter { row -> TaskTagFilter.matches(row.tags.map { it.id }.toSet(), selectedTagIds) }
    val visibleGroups = remember(visible, today) {
        TaskGrouping.group(visible.map { it.toRecord() }, today.toEpochDay())
    }
    val visibleById = remember(visible) { visible.associateBy { it.task.id } }
    val habitItems = remember(habitRows, scheduleRows, logRows, reminderRows, selectedTagIds, today) {
        val versionsByHabit = scheduleRows.groupBy { it.habitId }
        val logsByHabit = logRows.groupBy { it.habitId }.mapValues { (_, rows) -> rows.map { it.toModel() }.associateBy { it.epochDay } }
        val remindersByHabit = reminderRows.groupBy { it.habitId }.mapValues { (_, rows) -> rows.map { it.minuteOfDay }.sorted() }
        habitRows.filter { row -> selectedTagIds.all { id -> row.tags.any { it.id == id } } }.mapNotNull { row ->
            val versions = versionsByHabit[row.habit.id].orEmpty().map { it.toModel() }
            val currentVersion = ScheduleResolver.versionOn(versions, today.toEpochDay()) ?: return@mapNotNull null
            val logs = logsByHabit[row.habit.id].orEmpty()
            HabitProgressItem(
                row = row,
                version = currentVersion,
                log = logs[today.toEpochDay()],
                scheduledToday = ScheduleResolver.isScheduled(today.toEpochDay(), versions),
                currentStreak = StreakCalculator.current(row.habit.createdEpochDay, today.toEpochDay(), versions, logs),
                reminderMinutes = remindersByHabit[row.habit.id].orEmpty()
            )
        }
    }
    androidx.compose.runtime.LaunchedEffect(tasks, habitRows, scheduleRows, logRows) {
        try { WidgetUpdater.update(context) } catch (error: Exception) {
            if (error is kotlinx.coroutines.CancellationException) throw error
            Log.w("KarHa", "Widget refresh failed", error)
        }
    }
    fun deleteTask(id: Long) {
        scope.launch {
            try {
                val deleted = container.tasks.delete(id) ?: return@launch
                try {
                    container.reminders.cancelTask(id, deleted.task.dueMinuteOfDay)
                    container.reminders.scheduleAll()
                } catch (syncError: Exception) {
                    if (syncError is kotlinx.coroutines.CancellationException) throw syncError
                    Log.e("KarHa", "Reminder rescheduling failed", syncError)
                }
                if (snackbar.showSnackbar(
                        context.getString(R.string.task_deleted),
                        context.getString(R.string.undo),
                        withDismissAction = true,
                        duration = SnackbarDuration.Long
                ) == androidx.compose.material3.SnackbarResult.ActionPerformed
                ) {
                    container.tasks.restore(deleted)
                    try { container.reminders.scheduleAll() } catch (syncError: Exception) {
                        if (syncError is kotlinx.coroutines.CancellationException) throw syncError
                        Log.e("KarHa", "Reminder rescheduling failed after undo", syncError)
                    }
                }
            } catch (error: Exception) {
                if (error is kotlinx.coroutines.CancellationException) throw error
                snackbar.showSnackbar(context.getString(R.string.operation_failed))
            }
        }
    }
    // shortcut: the five-second undo window is process-scoped; persist it only if deletion must survive process death.
    fun deleteHabit(item: HabitProgressItem) {
        val id = item.row.habit.id
        if (id in pendingHabitDeletes) return
        pendingHabitDeletes += id
        scope.launch {
            var timer: Job? = null
            var deletionFailed = false
            try {
                val historyDays = container.habits.logCount(id)
                val deleteTimer = launch {
                    delay(5_000)
                    try {
                        container.reminders.cancelHabit(id, item.reminderMinutes)
                        container.habits.delete(id)
                        container.reminders.scheduleAll()
                    } catch (error: Exception) {
                        if (error is kotlinx.coroutines.CancellationException) throw error
                        deletionFailed = true
                    } finally {
                        snackbar.currentSnackbarData?.dismiss()
                    }
                }
                timer = deleteTimer
                val result = snackbar.showSnackbar(
                    context.getString(R.string.habit_delete_pending, PersianDigits.convert(historyDays.toString())),
                    context.getString(R.string.undo),
                    withDismissAction = true,
                    duration = SnackbarDuration.Indefinite
                )
                if (result == SnackbarResult.ActionPerformed) deleteTimer.cancel() else deleteTimer.join()
                if (deletionFailed) snackbar.showSnackbar(context.getString(R.string.operation_failed))
            } catch (error: Exception) {
                if (error is kotlinx.coroutines.CancellationException) throw error
                snackbar.showSnackbar(context.getString(R.string.operation_failed))
            } finally {
                timer?.cancel()
                pendingHabitDeletes -= id
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(page.titleRes()))
                        if (page == Page.TODAY) Text(
                            PersianFormat.date(JalaliConverter.toJalali(today)),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                actions = {
                    if (page == Page.TAGS || page == Page.SETTINGS) {
                        TextButton(onClick = { page = Page.TASKS }) { Text(stringResource(R.string.tasks)) }
                    } else {
                        TextButton(onClick = { page = Page.TAGS }) { Text(stringResource(R.string.tags)) }
                        TextButton(onClick = { page = Page.SETTINGS }) { Text(stringResource(R.string.settings)) }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            if (page == Page.TASKS || page == Page.TODAY) FloatingActionButton(
                onClick = { editingTask = null; showTaskEditor = true },
                content = { Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_task_action)) }
            )
            if (page == Page.HABITS) FloatingActionButton(onClick = { editingHabit = null; showHabitEditor = true }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_habit))
            }
            if (page == Page.TAGS) FloatingActionButton(onClick = {
                editingTag = null
                showTagEditor = true
            }) { Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_tag_action)) }
        },
        bottomBar = {
            if (page != Page.TAGS && page != Page.SETTINGS) NavigationBar {
                listOf(Page.TODAY, Page.TASKS, Page.HABITS, Page.STATS).forEach { item ->
                    NavigationBarItem(
                        selected = page == item,
                        onClick = { page = item },
                        icon = { Icon(item.icon(), contentDescription = stringResource(item.titleRes())) },
                        label = { Text(stringResource(item.titleRes())) }
                    )
                }
            }
        }
    ) { padding ->
        when (page) {
            Page.TODAY -> TaskList(
                modifier = Modifier.padding(padding),
                groups = visibleGroups,
                rows = visibleById,
                sections = listOf(TaskSection.OVERDUE, TaskSection.TODAY),
                tags = tags,
                selectedTagIds = selectedTagIds,
                onTagToggle = { id -> selectedTagIds = selectedTagIds.toggle(id) },
                onClearFilter = { selectedTagIds = emptySet() },
                onEdit = { editingTask = it; showTaskEditor = true },
                onDone = { id, done -> scope.launch {
                    try { container.tasks.setDone(id, done); container.reminders.scheduleAll() } catch (error: Exception) {
                        if (error is kotlinx.coroutines.CancellationException) throw error
                        snackbar.showSnackbar(context.getString(R.string.operation_failed))
                    }
                } },
                onDelete = ::deleteTask,
                showDone = false,
                onToggleDone = {},
        habits = habitItems.filter { it.scheduledToday && !it.row.habit.archived },
                onHabitValue = { id, value -> scope.launch {
                    try { container.habits.setValue(id, today.toEpochDay(), value); container.reminders.scheduleAll() } catch (error: Exception) {
                        if (error is kotlinx.coroutines.CancellationException) throw error
                        snackbar.showSnackbar(context.getString(R.string.operation_failed))
                    }
                } },
                onHabitAdjust = { id, direction -> scope.launch {
                    try { container.habits.increment(id, today.toEpochDay(), direction); container.reminders.scheduleAll() } catch (error: Exception) {
                        if (error is kotlinx.coroutines.CancellationException) throw error
                        snackbar.showSnackbar(context.getString(R.string.operation_failed))
                    }
                } },
                onHabitSkip = { id, skipped -> scope.launch {
                    try { container.habits.setSkipped(id, today.toEpochDay(), skipped); container.reminders.scheduleAll() } catch (error: Exception) {
                        if (error is kotlinx.coroutines.CancellationException) throw error
                        snackbar.showSnackbar(context.getString(R.string.operation_failed))
                    }
                } }
            )
            Page.TASKS -> TaskList(
                modifier = Modifier.padding(padding),
                groups = visibleGroups,
                rows = visibleById,
                sections = listOf(TaskSection.OVERDUE, TaskSection.TODAY, TaskSection.UPCOMING, TaskSection.NO_DATE, TaskSection.DONE),
                tags = tags,
                selectedTagIds = selectedTagIds,
                onTagToggle = { id -> selectedTagIds = selectedTagIds.toggle(id) },
                onClearFilter = { selectedTagIds = emptySet() },
                onEdit = { editingTask = it; showTaskEditor = true },
                onDone = { id, done -> scope.launch {
                    try { container.tasks.setDone(id, done); container.reminders.scheduleAll() } catch (error: Exception) {
                        if (error is kotlinx.coroutines.CancellationException) throw error
                        snackbar.showSnackbar(context.getString(R.string.operation_failed))
                    }
                } },
                onDelete = ::deleteTask,
                showDone = showDone,
                onToggleDone = { showDone = !showDone }
            )
            Page.HABITS -> HabitManagerScreen(
                items = habitItems,
                tags = tags,
                selectedTagIds = selectedTagIds,
                onToggleTag = { id -> selectedTagIds = selectedTagIds.toggle(id) },
                onClearFilter = { selectedTagIds = emptySet() },
                onValue = { id, value -> scope.launch {
                    try { container.habits.setValue(id, today.toEpochDay(), value); container.reminders.scheduleAll() } catch (error: Exception) {
                        if (error is kotlinx.coroutines.CancellationException) throw error
                        snackbar.showSnackbar(context.getString(R.string.operation_failed))
                    }
                } },
                onAdjust = { id, direction -> scope.launch {
                    try { container.habits.increment(id, today.toEpochDay(), direction); container.reminders.scheduleAll() } catch (error: Exception) {
                        if (error is kotlinx.coroutines.CancellationException) throw error
                        snackbar.showSnackbar(context.getString(R.string.operation_failed))
                    }
                } },
                onSkip = { id, skipped -> scope.launch {
                    try { container.habits.setSkipped(id, today.toEpochDay(), skipped); container.reminders.scheduleAll() } catch (error: Exception) {
                        if (error is kotlinx.coroutines.CancellationException) throw error
                        snackbar.showSnackbar(context.getString(R.string.operation_failed))
                    }
                } },
                onEdit = { editingHabit = it; showHabitEditor = true },
                onArchive = { id, archived -> scope.launch {
                    try { container.habits.setArchived(id, archived); container.reminders.scheduleAll() } catch (error: Exception) {
                        if (error is kotlinx.coroutines.CancellationException) throw error
                        snackbar.showSnackbar(context.getString(R.string.operation_failed))
                    }
                } },
                onDelete = ::deleteHabit,
                onReorder = { ids -> scope.launch {
                    try { container.habits.reorder(ids) } catch (error: Exception) {
                        if (error is kotlinx.coroutines.CancellationException) throw error
                        snackbar.showSnackbar(context.getString(R.string.operation_failed))
                    }
                } },
                modifier = Modifier.padding(padding)
            )
            Page.TAGS -> TagList(
                modifier = Modifier.padding(padding),
                tags = tags,
                onEdit = { editingTag = it; showTagEditor = true },
                onDelete = { tag -> scope.launch {
                    try { container.tags.delete(tag.id); selectedTagIds -= tag.id } catch (error: Exception) {
                        if (error is kotlinx.coroutines.CancellationException) throw error
                        snackbar.showSnackbar(context.getString(R.string.operation_failed))
                    }
                } }
            )
            Page.SETTINGS -> SettingsScreen(
                weeklyBackupEnabled = weeklyBackupEnabled,
                weeklyBackupMinute = weeklyBackupMinute,
                dayStartHour = dayStartHour,
                notificationPermissionGranted = notificationPermissionGranted,
                onWeeklyBackupEnabled = { enabled -> scope.launch {
                    try {
                        container.settings.setWeeklyBackupEnabled(enabled)
                        container.reminders.scheduleAll()
                    } catch (error: Exception) {
                        if (error is kotlinx.coroutines.CancellationException) throw error
                        try { container.reminders.scheduleAll() } catch (syncError: Exception) {
                            if (syncError is kotlinx.coroutines.CancellationException) throw syncError
                            Log.e("KarHa", "Reminder rescheduling failed", syncError)
                        }
                        snackbar.showSnackbar(context.getString(R.string.operation_failed))
                    }
                } },
                onSaveBackupMinute = { minute -> scope.launch {
                    try {
                        container.reminders.cancelBackup(weeklyBackupMinute)
                        container.settings.setWeeklyBackupMinute(minute)
                        container.reminders.scheduleAll()
                    } catch (error: Exception) {
                        if (error is kotlinx.coroutines.CancellationException) throw error
                        try { container.reminders.scheduleAll() } catch (syncError: Exception) {
                            if (syncError is kotlinx.coroutines.CancellationException) throw syncError
                            Log.e("KarHa", "Reminder rescheduling failed", syncError)
                        }
                        snackbar.showSnackbar(context.getString(R.string.operation_failed))
                    }
                } },
                onSaveDayStartHour = { hour -> scope.launch {
                    try {
                        container.settings.setDayStartHour(hour)
                        container.dayClock.setDayStartHour(hour)
                        container.reminders.scheduleAll()
                    } catch (error: Exception) {
                        if (error is kotlinx.coroutines.CancellationException) throw error
                        snackbar.showSnackbar(context.getString(R.string.operation_failed))
                    }
                } },
                onRequestNotifications = onRequestNotifications,
                onOpenNotificationSettings = onOpenNotificationSettings,
                onOpenBatterySettings = onOpenBatterySettings,
                onExportBackup = { exportLauncher.launch("kar-ha-backup.json") },
                onImportBackup = { importLauncher.launch(arrayOf("application/json", "text/*")) },
                modifier = Modifier.padding(padding)
            )
            Page.STATS -> StatsScreen(
                habits = habitRows.mapNotNull { row ->
                    val versions = scheduleRows.filter { it.habitId == row.habit.id }.map { it.toModel() }
                    if (versions.isEmpty()) return@mapNotNull null
                    val logs = logRows.filter { it.habitId == row.habit.id }.map { it.toModel() }.associateBy { it.epochDay }
                    StatsHabit(row.habit.id, row.habit.name, runCatching { HabitType.valueOf(row.habit.type) }.getOrDefault(HabitType.BOOLEAN), row.habit.createdEpochDay, versions, logs, row.habit.archived)
                },
                today = today,
                onSetValue = { id, day, value -> scope.launch {
                    try { container.habits.setValue(id, day, value); container.reminders.scheduleAll() } catch (error: Exception) {
                        if (error is kotlinx.coroutines.CancellationException) throw error
                        snackbar.showSnackbar(context.getString(R.string.operation_failed))
                    }
                } },
                onSkip = { id, day, skipped -> scope.launch {
                    try { container.habits.setSkipped(id, day, skipped); container.reminders.scheduleAll() } catch (error: Exception) {
                        if (error is kotlinx.coroutines.CancellationException) throw error
                        snackbar.showSnackbar(context.getString(R.string.operation_failed))
                    }
                } },
                modifier = Modifier.padding(padding)
            )
        }
    }

    if (showTaskEditor) TaskEditorDialog(
        task = editingTask,
        tags = tags,
        onDismiss = { showTaskEditor = false },
        onSave = { task, tagIds ->
            scope.launch {
                try {
                    editingTask?.task?.let { container.reminders.cancelTask(it.id, it.dueMinuteOfDay) }
                    container.tasks.save(task, tagIds)
                    container.reminders.scheduleAll()
                    showTaskEditor = false
                } catch (error: Exception) {
                    if (error is kotlinx.coroutines.CancellationException) throw error
                    try { container.reminders.scheduleAll() } catch (syncError: Exception) {
                        if (syncError is kotlinx.coroutines.CancellationException) throw syncError
                        Log.e("KarHa", "Reminder rescheduling failed", syncError)
                    }
                    snackbar.showSnackbar(context.getString(R.string.operation_failed))
                }
            }
        }
    )
    if (showTagEditor) TagEditorDialog(
        tag = editingTag,
        onDismiss = { showTagEditor = false },
        onSave = { tag -> scope.launch {
            try {
                container.tags.save(tag)
                showTagEditor = false
            } catch (error: Exception) {
                if (error is kotlinx.coroutines.CancellationException) throw error
                snackbar.showSnackbar(context.getString(R.string.operation_failed))
            }
        } }
    )
    if (showHabitEditor) HabitEditorDialog(
        item = editingHabit,
        tags = tags,
        reminderMinutes = editingHabit?.reminderMinutes.orEmpty(),
        today = today,
        onDismiss = { showHabitEditor = false },
        onSave = { habit, version, tagIds, reminderMinutes ->
            scope.launch {
                try {
                    editingHabit?.let { container.reminders.cancelHabit(habit.id, it.reminderMinutes) }
                    container.habits.save(habit, version, tagIds, reminderMinutes)
                    container.reminders.scheduleAll()
                    showHabitEditor = false
                } catch (error: Exception) {
                    if (error is kotlinx.coroutines.CancellationException) throw error
                    try { container.reminders.scheduleAll() } catch (syncError: Exception) {
                        if (syncError is kotlinx.coroutines.CancellationException) throw syncError
                        Log.e("KarHa", "Reminder rescheduling failed", syncError)
                    }
                    snackbar.showSnackbar(context.getString(R.string.operation_failed))
                }
            }
        }
    )
    if (notificationPermissionDenied) AlertDialog(
        onDismissRequest = onDismissPermissionPrompt,
        title = { Text(stringResource(R.string.notification_permission_title)) },
        text = { Text(stringResource(R.string.notification_permission_message)) },
        confirmButton = {
            TextButton(onClick = onOpenNotificationSettings) { Text(stringResource(R.string.open_notification_settings)) }
        },
        dismissButton = { TextButton(onClick = onDismissPermissionPrompt) { Text(stringResource(R.string.cancel)) } }
    )
    pendingRestore?.let { (backup, summary) ->
        val exportedDate = JalaliConverter.toJalali(Instant.ofEpochMilli(summary.exportedAtEpochMillis).atZone(ZoneId.systemDefault()).toLocalDate())
        val dateText = PersianDigits.convert("${exportedDate.year}/${exportedDate.month}/${exportedDate.day}")
        AlertDialog(
            onDismissRequest = { pendingRestore = null },
            title = { Text(stringResource(R.string.import_backup)) },
            text = { Column {
                Text(stringResource(R.string.backup_summary, PersianDigits.convert(summary.habitCount.toString()), PersianDigits.convert(summary.taskCount.toString()), PersianDigits.convert(summary.tagCount.toString()), dateText))
                Text(stringResource(R.string.confirm_restore))
            } },
            confirmButton = { TextButton(onClick = {
                scope.launch {
                    try {
                        container.backup.restore(backup)
                        container.dayClock.setDayStartHour(backup.settings.dayStartHour)
                        pendingRestore = null
                        try { container.reminders.scheduleAll() } catch (error: Exception) {
                            if (error is kotlinx.coroutines.CancellationException) throw error
                            Log.e("KarHa", "Reminder rescheduling after restore failed", error)
                        }
                        try { WidgetUpdater.update(context) } catch (error: Exception) {
                            if (error is kotlinx.coroutines.CancellationException) throw error
                            Log.e("KarHa", "Widget refresh after restore failed", error)
                        }
                        snackbar.showSnackbar(context.getString(R.string.backup_restored))
                    } catch (error: Exception) {
                        if (error is kotlinx.coroutines.CancellationException) throw error
                        snackbar.showSnackbar(context.getString(R.string.operation_failed))
                    }
                }
            }) { Text(stringResource(R.string.restore)) } },
            dismissButton = { TextButton(onClick = { pendingRestore = null }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}

@Composable
private fun TaskList(
    modifier: Modifier,
    groups: Map<TaskSection, List<TaskRecord>>,
    rows: Map<Long, TaskWithTags>,
    sections: List<TaskSection>,
    tags: List<TagEntity>,
    selectedTagIds: Set<Long>,
    onTagToggle: (Long) -> Unit,
    onClearFilter: () -> Unit,
    onEdit: (TaskWithTags) -> Unit,
    onDone: (Long, Boolean) -> Unit,
    onDelete: (Long) -> Unit,
    showDone: Boolean,
    onToggleDone: () -> Unit,
    habits: List<HabitProgressItem> = emptyList(),
    onHabitValue: (Long, Int) -> Unit = { _, _ -> },
    onHabitAdjust: (Long, Int) -> Unit = { _, _ -> },
    onHabitSkip: (Long, Boolean) -> Unit = { _, _ -> }
) {
    val noResults = sections.none { groups[it].orEmpty().isNotEmpty() } && habits.isEmpty()
    LazyColumn(modifier.fillMaxSize(), contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)) {
        if (tags.isNotEmpty()) item {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), verticalAlignment = Alignment.CenterVertically) {
                tags.forEach { tag ->
                    FilterChip(
                        selected = tag.id in selectedTagIds,
                        onClick = { onTagToggle(tag.id) },
                        label = { Text(tag.name) },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }
        }
        if (noResults) item {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(32.dp)) {
                Text(stringResource(if (selectedTagIds.isEmpty()) R.string.empty_tasks else R.string.empty_filtered_tasks))
                if (selectedTagIds.isNotEmpty()) TextButton(onClick = onClearFilter) { Text(stringResource(R.string.clear_filter)) }
            }
        }
        sections.forEach { section ->
            val records = groups[section].orEmpty()
            if (records.isNotEmpty()) {
                item(key = "header-${section.name}") {
                    if (section == TaskSection.DONE) {
                        TextButton(onClick = onToggleDone) {
                            Text(stringResource(if (showDone) R.string.done_expanded else R.string.done_collapsed, PersianDigits.convert(records.size.toString())))
                        }
                    } else Text(
                        stringResource(section.titleRes()),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (section == TaskSection.OVERDUE) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }
                if (section != TaskSection.DONE) items(records, key = { "${section.name}-${it.id}" }) { record ->
                    rows[record.id]?.let { row ->
                        TaskCard(row, onDone, onEdit, onDelete)
                    }
                }
                if (section == TaskSection.DONE && showDone) {
                    records.mapNotNull { record -> record.doneEpochDay?.let { it to record } }
                        .groupBy({ it.first }, { it.second })
                        .toSortedMap(compareByDescending { it })
                        .forEach { (epochDay, dayRecords) ->
                            item(key = "done-day-$epochDay") {
                                Text(
                                    PersianFormat.date(JalaliConverter.toJalali(LocalDate.ofEpochDay(epochDay))),
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            items(dayRecords, key = { "done-${it.id}" }) { record ->
                                rows[record.id]?.let { row -> TaskCard(row, onDone, onEdit, onDelete) }
                            }
                        }
                }
            }
        }
        if (habits.isNotEmpty()) {
            item(key = "today-habits-header") {
                Text(stringResource(R.string.habits), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
            }
            items(habits, key = { "habit-${it.row.habit.id}" }) { habit ->
                HabitProgressCard(habit, onHabitValue, onHabitAdjust, onHabitSkip)
            }
        }
    }
}

@Composable
private fun TaskCard(
    row: TaskWithTags,
    onDone: (Long, Boolean) -> Unit,
    onEdit: (TaskWithTags) -> Unit,
    onDelete: (Long) -> Unit
) {
    val task = row.task
    val context = LocalContext.current
    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = task.isDone, onCheckedChange = { onDone(task.id, it) })
            Column(Modifier.weight(1f)) {
                Text(task.title, textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None)
                val details = buildList {
                    task.dueEpochDay?.let { add(PersianFormat.date(JalaliConverter.toJalali(LocalDate.ofEpochDay(it)))) }
                    task.dueMinuteOfDay?.let { minute -> add(PersianDigits.convert("%02d:%02d".format(java.util.Locale.ROOT, minute / 60, minute % 60))) }
                    add(context.getString(runCatching { TaskPriority.valueOf(task.priority).resourceId() }.getOrDefault(R.string.priority_medium)))
                    row.tags.forEach { add(it.name) }
                }
                Text(details.joinToString(" · "), style = MaterialTheme.typography.bodySmall)
            }
            Column {
                TextButton(onClick = { onEdit(row) }) { Text(stringResource(R.string.edit_task)) }
                TextButton(onClick = { onDelete(task.id) }) { Text(stringResource(R.string.delete)) }
            }
        }
    }
}

@Composable
private fun TagList(modifier: Modifier, tags: List<TagEntity>, onEdit: (TagEntity) -> Unit, onDelete: (TagEntity) -> Unit) {
    LazyColumn(modifier.fillMaxSize(), contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)) {
        if (tags.isEmpty()) item { Text(stringResource(R.string.empty_tags)) }
        items(tags, key = { it.id }) { tag ->
            Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(tag.iconKey.image(), contentDescription = null, tint = Color(tag.colorArgb), modifier = Modifier.size(24.dp))
                    Text(tag.name, Modifier.weight(1f).padding(horizontal = 12.dp))
                    TextButton(onClick = { onEdit(tag) }) { Text(stringResource(R.string.edit_tag)) }
                    TextButton(onClick = { onDelete(tag) }) { Text(stringResource(R.string.delete)) }
                }
            }
        }
    }
}

@Composable
private fun TaskEditorDialog(task: TaskWithTags?, tags: List<TagEntity>, onDismiss: () -> Unit, onSave: (TaskEntity, Set<Long>) -> Unit) {
    val entity = task?.task
    var title by remember(entity?.id) { mutableStateOf(entity?.title.orEmpty()) }
    var description by remember(entity?.id) { mutableStateOf(entity?.description.orEmpty()) }
    var dateText by remember(entity?.id) {
        mutableStateOf(entity?.dueEpochDay?.let { day ->
            val date = JalaliConverter.toJalali(LocalDate.ofEpochDay(day))
            PersianDigits.convert("%04d/%02d/%02d".format(java.util.Locale.ROOT, date.year, date.month, date.day))
        }.orEmpty())
    }
    var timeText by remember(entity?.id) {
        mutableStateOf(entity?.dueMinuteOfDay?.let { PersianDigits.convert("%02d:%02d".format(java.util.Locale.ROOT, it / 60, it % 60)) }.orEmpty())
    }
    var reminderEnabled by remember(entity?.id) { mutableStateOf(entity?.reminderEnabled == true) }
    var priority by remember(entity?.id) { mutableStateOf(runCatching { TaskPriority.valueOf(entity?.priority ?: "MEDIUM") }.getOrDefault(TaskPriority.MEDIUM)) }
    var selectedTagIds by remember(entity?.id, tags) { mutableStateOf(task?.tags?.map { it.id }?.toSet().orEmpty()) }
    val dueDate = remember(dateText) { if (dateText.isBlank()) null else PersianFormat.parseDate(dateText) }
    val dueMinute = remember(timeText) { if (timeText.isBlank()) null else PersianFormat.parseTime(timeText) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(if (task == null) R.string.add_task else R.string.edit_task)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(title, { title = it }, label = { Text(stringResource(R.string.task_title)) }, singleLine = true)
                OutlinedTextField(description, { description = it }, label = { Text(stringResource(R.string.task_description)) })
                OutlinedTextField(
                    dateText,
                    { dateText = PersianDigits.convert(PersianDigits.toAscii(it)) },
                    label = { Text(stringResource(R.string.due_date)) },
                    isError = dateText.isNotBlank() && dueDate == null,
                    supportingText = { if (dateText.isNotBlank() && dueDate == null) Text(stringResource(R.string.due_date_invalid)) }
                )
                OutlinedTextField(
                    timeText,
                    { timeText = PersianDigits.convert(PersianDigits.toAscii(it)) },
                    label = { Text(stringResource(R.string.due_time)) },
                    isError = timeText.isNotBlank() && (dueMinute == null || dueDate == null),
                    supportingText = {
                        if (timeText.isNotBlank() && (dueMinute == null || dueDate == null)) Text(stringResource(R.string.due_time_invalid))
                    }
                )
                if (dueDate != null && dueMinute != null) Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(reminderEnabled, { reminderEnabled = it })
                    Text(stringResource(R.string.task_reminder))
                }
                Row(Modifier.horizontalScroll(rememberScrollState())) {
                    TaskPriority.entries.forEach { option ->
                        FilterChip(priority == option, { priority = option }, label = { Text(stringResource(option.resourceId())) }, modifier = Modifier.padding(end = 6.dp))
                    }
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
            TextButton(
                enabled = title.isNotBlank() && (dateText.isBlank() || dueDate != null) &&
                    (timeText.isBlank() || (dueMinute != null && dueDate != null)),
                onClick = {
                    onSave(
                        (entity ?: TaskEntity(title = title)).copy(
                            title = title.trim(),
                            description = description.trim().ifBlank { null },
                            dueEpochDay = dueDate?.toEpochDay(),
                            dueMinuteOfDay = dueMinute,
                            reminderEnabled = reminderEnabled && dueDate != null && dueMinute != null,
                            priority = priority.name
                        ),
                        selectedTagIds
                    )
                }
            ) { Text(stringResource(R.string.save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}

@Composable
private fun TagEditorDialog(tag: TagEntity?, onDismiss: () -> Unit, onSave: (TagEntity) -> Unit) {
    var name by remember(tag?.id) { mutableStateOf(tag?.name.orEmpty()) }
    var color by remember(tag?.id) { mutableStateOf(tag?.colorArgb ?: tagColors.first().first) }
    var iconKey by remember(tag?.id) { mutableStateOf(tag?.iconKey ?: tagIcons.first().key) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(if (tag == null) R.string.add_tag else R.string.edit_tag)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text(stringResource(R.string.tag_name)) }, singleLine = true)
                Text(stringResource(R.string.tag_color))
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    tagColors.forEach { (argb, label) ->
                        FilterChip(color == argb, { color = argb }, label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(Modifier.size(16.dp), color = Color(argb)) {}
                                Text(stringResource(label), Modifier.padding(start = 4.dp))
                            }
                        })
                    }
                }
                Text(stringResource(R.string.tag_icon))
                Row(Modifier.horizontalScroll(rememberScrollState())) {
                    tagIcons.forEach { choice ->
                        FilterChip(iconKey == choice.key, { iconKey = choice.key }, label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(choice.image, contentDescription = null)
                                Text(stringResource(choice.label), Modifier.padding(start = 4.dp))
                            }
                        }, modifier = Modifier.padding(end = 4.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(enabled = name.isNotBlank(), onClick = { onSave(TagEntity(tag?.id ?: 0, name.trim(), color, iconKey)) }) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}

private fun Page.titleRes() = when (this) {
    Page.TODAY -> R.string.today
    Page.TASKS -> R.string.tasks
    Page.HABITS -> R.string.habits
    Page.STATS -> R.string.stats
    Page.TAGS -> R.string.tags
    Page.SETTINGS -> R.string.settings
}

private fun Page.icon(): ImageVector = when (this) {
    Page.TODAY -> Icons.Default.Home
    Page.TASKS -> Icons.Default.Check
    Page.HABITS -> Icons.Default.Favorite
    Page.STATS -> Icons.Default.Star
    Page.TAGS -> Icons.Default.Settings
    Page.SETTINGS -> Icons.Default.Settings
}

private fun TaskSection.titleRes() = when (this) {
    TaskSection.OVERDUE -> R.string.overdue_section
    TaskSection.TODAY -> R.string.today_section
    TaskSection.UPCOMING -> R.string.upcoming_section
    TaskSection.NO_DATE -> R.string.no_date_section
    TaskSection.DONE -> R.string.done_expanded
}

private fun TaskPriority.resourceId() = when (this) {
    TaskPriority.LOW -> R.string.priority_low
    TaskPriority.MEDIUM -> R.string.priority_medium
    TaskPriority.HIGH -> R.string.priority_high
}

private fun String.image(): ImageVector = tagIcons.firstOrNull { it.key == this }?.image ?: Icons.Default.Star

private fun Set<Long>.toggle(id: Long): Set<Long> = if (id in this) this - id else this + id
