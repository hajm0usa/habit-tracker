package ir.hajmousa.habits.feature.tasks

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Label
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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import ir.hajmousa.habits.AppContainer
import ir.hajmousa.habits.R
import ir.hajmousa.habits.core.date.JalaliConverter
import ir.hajmousa.habits.core.date.PersianDigits
import ir.hajmousa.habits.core.date.PersianFormat
import ir.hajmousa.habits.core.ui.AppSpacing
import ir.hajmousa.habits.core.ui.EmptyState
import ir.hajmousa.habits.core.ui.ItemMenu
import ir.hajmousa.habits.core.ui.EditorDialog
import ir.hajmousa.habits.domain.task.ProgressHabit
import ir.hajmousa.habits.domain.task.ProgressTask
import ir.hajmousa.habits.domain.task.TodayProgress
import ir.hajmousa.habits.domain.task.TodayProgressCalculator
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
    val reminderSnapshot by container.habits.observeReminders().collectAsState(initial = null)
    val reminderRows = reminderSnapshot.orEmpty()
    val weeklyBackupEnabled by container.settings.weeklyBackupEnabled.collectAsState(initial = true)
    val weeklyBackupMinute by container.settings.weeklyBackupMinute.collectAsState(initial = 17 * 60)
    val dayStartHour by container.settings.dayStartHour.collectAsState(initial = 0)
    container.dayClock.setDayStartHour(dayStartHour)
    val today = container.dayClock.today()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var page by rememberSaveable { mutableStateOf(Page.TODAY) }
    var returnPage by rememberSaveable { mutableStateOf(Page.TODAY) }
    val pageState = rememberSaveableStateHolder()
    BackHandler(enabled = page != Page.TODAY) {
        page = if (page == Page.TAGS || page == Page.SETTINGS) returnPage else Page.TODAY
    }
    var tagFilters by rememberSaveable { mutableStateOf(mapOf<Page, Set<Long>>()) }
    val selectedTagIds = tagFilters[page].orEmpty()
    fun setTagFilter(ids: Set<Long>) { tagFilters = tagFilters + (page to ids) }
    var editingTaskId by rememberSaveable { mutableStateOf<Long?>(null) }
    val editingTask = tasks.firstOrNull { it.task.id == editingTaskId }
    var showTaskEditor by rememberSaveable { mutableStateOf(false) }
    var editingTagId by rememberSaveable { mutableStateOf<Long?>(null) }
    val editingTag = tags.firstOrNull { it.id == editingTagId }
    var showTagEditor by rememberSaveable { mutableStateOf(false) }
    var showDone by rememberSaveable { mutableStateOf(false) }
    var editingHabitId by rememberSaveable { mutableStateOf<Long?>(null) }
    var showHabitEditor by rememberSaveable { mutableStateOf(false) }
    var showAddMenu by remember { mutableStateOf(false) }
    var savingEditor by remember { mutableStateOf(false) }
    var editorError by remember { mutableStateOf<String?>(null) }
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
    val habitItems = remember(habitRows, scheduleRows, logRows, reminderRows, today) {
        val versionsByHabit = scheduleRows.groupBy { it.habitId }
        val logsByHabit = logRows.groupBy { it.habitId }.mapValues { (_, rows) -> rows.map { it.toModel() }.associateBy { it.epochDay } }
        val remindersByHabit = reminderRows.groupBy { it.habitId }.mapValues { (_, rows) -> rows.map { it.minuteOfDay }.sorted() }
        habitRows.mapNotNull { row ->
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
    val editingHabit = habitItems.firstOrNull { it.row.habit.id == editingHabitId }
    val filteredHabitItems = habitItems.filter { item -> selectedTagIds.all { id -> item.row.tags.any { it.id == id } } }
    val todayProgress = TodayProgressCalculator.calculate(
        today.toEpochDay(),
        habitItems.map { ProgressHabit(it.scheduledToday, it.row.habit.archived, it.version.targetValue, it.log?.value, it.log?.skipped == true) },
        tasks.map { ProgressTask(it.task.dueEpochDay, it.task.isDone) }
    )
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
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        topBar = {
            TopAppBar(
                expandedHeight = TopAppBarDefaults.TopAppBarExpandedHeight * LocalDensity.current.fontScale.coerceAtLeast(1f),
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                title = {
                    Column {
                        Text(stringResource(page.titleRes()), style = MaterialTheme.typography.titleLarge)
                        if (page == Page.TODAY) Text(
                            PersianFormat.date(JalaliConverter.toJalali(today)),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    if (page == Page.TAGS || page == Page.SETTINGS) IconButton(onClick = { page = returnPage }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    if (page != Page.TAGS && page != Page.SETTINGS) {
                        IconButton(onClick = { returnPage = page; page = Page.TAGS }) {
                            Icon(Icons.Default.Label, contentDescription = stringResource(R.string.tags))
                        }
                        IconButton(onClick = { returnPage = page; page = Page.SETTINGS }) {
                            Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings))
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            if (page == Page.TODAY) Box {
                ExtendedFloatingActionButton(
                    onClick = { showAddMenu = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text(stringResource(R.string.add_item)) }
                )
                DropdownMenu(expanded = showAddMenu, onDismissRequest = { showAddMenu = false }) {
                    DropdownMenuItem(text = { Text(stringResource(R.string.add_task)) }, onClick = {
                        showAddMenu = false; editorError = null; editingTaskId = null; showTaskEditor = true
                    })
                    DropdownMenuItem(text = { Text(stringResource(R.string.add_habit)) }, onClick = {
                        showAddMenu = false; editorError = null; editingHabitId = null; showHabitEditor = true
                    })
                }
            }
            if (page == Page.TASKS) ExtendedFloatingActionButton(
                onClick = { editorError = null; editingTaskId = null; showTaskEditor = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.add_task_action)) }
            )
            if (page == Page.HABITS) ExtendedFloatingActionButton(
                onClick = { editorError = null; editingHabitId = null; showHabitEditor = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.add_habit)) }
            )
            if (page == Page.TAGS) ExtendedFloatingActionButton(
                onClick = { editorError = null; editingTagId = null; showTagEditor = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.add_tag_action)) }
            )
        },
        bottomBar = {
            if (page != Page.TAGS && page != Page.SETTINGS) NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
                listOf(Page.TODAY, Page.TASKS, Page.HABITS, Page.STATS).forEach { item ->
                    NavigationBarItem(
                        selected = page == item,
                        onClick = { page = item },
                        icon = { Icon(item.icon(), contentDescription = null) },
                        label = { Text(stringResource(item.titleRes())) }
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
        pageState.SaveableStateProvider(page.name) {
        when (page) {
            Page.TODAY -> TaskList(
                modifier = Modifier.widthIn(max = AppSpacing.contentWidth),
                todayProgress = todayProgress,
                groups = visibleGroups,
                rows = visibleById,
                sections = listOf(TaskSection.OVERDUE, TaskSection.TODAY),
                tags = tags,
                selectedTagIds = selectedTagIds,
                onTagToggle = { id -> setTagFilter(selectedTagIds.toggle(id)) },
                onClearFilter = { setTagFilter(emptySet()) },
                onEdit = { editorError = null; editingTaskId = it.task.id; showTaskEditor = true },
                onDone = { id, done -> scope.launch {
                    try { container.tasks.setDone(id, done); container.reminders.scheduleAll() } catch (error: Exception) {
                        if (error is kotlinx.coroutines.CancellationException) throw error
                        snackbar.showSnackbar(context.getString(R.string.operation_failed))
                    }
                } },
                onDelete = ::deleteTask,
                showDone = false,
                onToggleDone = {},
                habits = filteredHabitItems.filter { it.scheduledToday && !it.row.habit.archived },
                onHabitEdit = { editorError = null; editingHabitId = it.row.habit.id; showHabitEditor = true },
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
                modifier = Modifier.widthIn(max = AppSpacing.contentWidth),
                groups = visibleGroups,
                rows = visibleById,
                sections = listOf(TaskSection.OVERDUE, TaskSection.TODAY, TaskSection.UPCOMING, TaskSection.NO_DATE, TaskSection.DONE),
                tags = tags,
                selectedTagIds = selectedTagIds,
                onTagToggle = { id -> setTagFilter(selectedTagIds.toggle(id)) },
                onClearFilter = { setTagFilter(emptySet()) },
                onEdit = { editorError = null; editingTaskId = it.task.id; showTaskEditor = true },
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
                items = filteredHabitItems,
                tags = tags,
                selectedTagIds = selectedTagIds,
                onToggleTag = { id -> setTagFilter(selectedTagIds.toggle(id)) },
                onClearFilter = { setTagFilter(emptySet()) },
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
                onEdit = { editorError = null; editingHabitId = it.row.habit.id; showHabitEditor = true },
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
                modifier = Modifier.widthIn(max = AppSpacing.contentWidth)
            )
            Page.TAGS -> TagList(
                modifier = Modifier.widthIn(max = AppSpacing.contentWidth),
                tags = tags,
                onEdit = { editorError = null; editingTagId = it.id; showTagEditor = true },
                onDelete = { tag -> scope.launch {
                    try { container.tags.delete(tag.id); tagFilters = tagFilters.mapValues { (_, ids) -> ids - tag.id } } catch (error: Exception) {
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
                modifier = Modifier.widthIn(max = AppSpacing.contentWidth)
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
                modifier = Modifier.widthIn(max = AppSpacing.contentWidth)
            )
        }
        }
        }
    }

    if (showTaskEditor && (editingTaskId == null || editingTask != null)) TaskEditorDialog(
        task = editingTask,
        tags = tags,
        today = today,
        defaultToToday = page == Page.TODAY,
        initialTagIds = selectedTagIds,
        saving = savingEditor,
        error = editorError,
        onDismiss = { showTaskEditor = false },
        onSave = saveTask@{ task, tagIds ->
            if (savingEditor) return@saveTask
            savingEditor = true
            editorError = null
            scope.launch {
                try {
                    editingTask?.task?.let { container.reminders.cancelTask(it.id, it.dueMinuteOfDay) }
                    container.tasks.save(task, tagIds)
                    showTaskEditor = false
                } catch (error: Exception) {
                    if (error is kotlinx.coroutines.CancellationException) throw error
                    try { container.reminders.scheduleAll() } catch (syncError: Exception) {
                        if (syncError is kotlinx.coroutines.CancellationException) throw syncError
                        Log.e("KarHa", "Reminder rescheduling failed", syncError)
                    }
                    editorError = context.getString(R.string.operation_failed)
                    return@launch
                } finally {
                    savingEditor = false
                }
                try { container.reminders.scheduleAll() } catch (error: Exception) {
                    if (error is kotlinx.coroutines.CancellationException) throw error
                    snackbar.showSnackbar(context.getString(R.string.saved_reminder_failed))
                }
            }
        }
    )
    if (showTagEditor && (editingTagId == null || editingTag != null)) TagEditorDialog(
        tag = editingTag,
        saving = savingEditor,
        error = editorError,
        onDismiss = { showTagEditor = false },
        onSave = saveTag@{ tag ->
            if (savingEditor) return@saveTag
            savingEditor = true
            editorError = null
            scope.launch {
                try {
                    container.tags.save(tag)
                    showTagEditor = false
                } catch (error: Exception) {
                    if (error is kotlinx.coroutines.CancellationException) throw error
                    editorError = context.getString(R.string.operation_failed)
                } finally {
                    savingEditor = false
                }
            }
        }
    )
    if (showHabitEditor && reminderSnapshot != null && (editingHabitId == null || editingHabit != null)) HabitEditorDialog(
        item = editingHabit,
        tags = tags,
        reminderMinutes = editingHabit?.reminderMinutes.orEmpty(),
        today = today,
        initialTagIds = selectedTagIds,
        saving = savingEditor,
        error = editorError,
        onDismiss = { showHabitEditor = false },
        onSave = saveHabit@{ habit, version, tagIds, reminderMinutes ->
            if (savingEditor) return@saveHabit
            savingEditor = true
            editorError = null
            scope.launch {
                try {
                    editingHabit?.let { container.reminders.cancelHabit(habit.id, it.reminderMinutes) }
                    container.habits.save(habit, version, tagIds, reminderMinutes)
                    showHabitEditor = false
                } catch (error: Exception) {
                    if (error is kotlinx.coroutines.CancellationException) throw error
                    try { container.reminders.scheduleAll() } catch (syncError: Exception) {
                        if (syncError is kotlinx.coroutines.CancellationException) throw syncError
                        Log.e("KarHa", "Reminder rescheduling failed", syncError)
                    }
                    editorError = context.getString(R.string.operation_failed)
                    return@launch
                } finally {
                    savingEditor = false
                }
                try { container.reminders.scheduleAll() } catch (error: Exception) {
                    if (error is kotlinx.coroutines.CancellationException) throw error
                    snackbar.showSnackbar(context.getString(R.string.saved_reminder_failed))
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
    todayProgress: TodayProgress? = null,
    habits: List<HabitProgressItem> = emptyList(),
    onHabitEdit: (HabitProgressItem) -> Unit = {},
    onHabitValue: (Long, Int) -> Unit = { _, _ -> },
    onHabitAdjust: (Long, Int) -> Unit = { _, _ -> },
    onHabitSkip: (Long, Boolean) -> Unit = { _, _ -> }
) {
    val noResults = sections.none { groups[it].orEmpty().isNotEmpty() } && habits.isEmpty()
    LazyColumn(
        modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = AppSpacing.medium, end = AppSpacing.medium, top = AppSpacing.medium, bottom = AppSpacing.listBottom),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
    ) {
        if (todayProgress != null && todayProgress.total > 0) item(key = "today-progress") { TodaySummary(todayProgress) }
        if (tags.isNotEmpty()) item {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), verticalAlignment = Alignment.CenterVertically) {
                if (selectedTagIds.isNotEmpty()) TextButton(onClick = onClearFilter) { Text(stringResource(R.string.clear_filter)) }
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
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                EmptyState(
                    Icons.Default.LocalFlorist,
                    stringResource(if (selectedTagIds.isNotEmpty()) R.string.empty_filtered_tasks else if (todayProgress != null) R.string.empty_today else R.string.empty_tasks),
                    stringResource(if (selectedTagIds.isNotEmpty()) R.string.filter_hint else if (todayProgress != null) R.string.empty_today_hint else R.string.empty_tasks_hint)
                )
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
                Text(stringResource(R.string.habits), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = AppSpacing.small))
            }
            items(habits, key = { "habit-${it.row.habit.id}" }) { habit ->
                HabitProgressCard(habit, onHabitValue, onHabitAdjust, onHabitSkip, menuItems = { close ->
                    DropdownMenuItem(text = { Text(stringResource(R.string.edit_habit)) }, onClick = { close(); onHabitEdit(habit) })
                })
            }
        }
    }
}

@Composable
private fun TodaySummary(progress: TodayProgress) {
    val fraction by animateFloatAsState(
        targetValue = if (progress.total == 0) 0f else progress.done.toFloat() / progress.total,
        label = "todayProgress"
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.padding(AppSpacing.medium), verticalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
                Text(stringResource(R.string.widget_progress), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                Text(
                    stringResource(R.string.widget_progress_count, PersianDigits.convert(progress.done.toString()), PersianDigits.convert(progress.total.toString())),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            LinearProgressIndicator(
                progress = { fraction }, modifier = Modifier.fillMaxWidth().height(AppSpacing.small),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f)
            )
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
    Card(
        onClick = { onEdit(row) },
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
    ) {
        Row(Modifier.padding(AppSpacing.small), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = task.isDone, onCheckedChange = { onDone(task.id, it) }, modifier = Modifier.semantics { contentDescription = task.title })
            Column(Modifier.weight(1f).padding(vertical = AppSpacing.small)) {
                Text(task.title, style = MaterialTheme.typography.titleMedium, textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None)
                val details = buildList {
                    task.dueEpochDay?.let { add(PersianFormat.date(JalaliConverter.toJalali(LocalDate.ofEpochDay(it)))) }
                    task.dueMinuteOfDay?.let { minute -> add(PersianDigits.convert("%02d:%02d".format(java.util.Locale.ROOT, minute / 60, minute % 60))) }
                    if (task.priority != TaskPriority.MEDIUM.name) add(context.getString(runCatching { TaskPriority.valueOf(task.priority).resourceId() }.getOrDefault(R.string.priority_medium)))
                    row.tags.forEach { add(it.name) }
                }
                if (details.isNotEmpty()) Text(details.joinToString(" · "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            ItemMenu(stringResource(R.string.item_actions, task.title)) { close ->
                DropdownMenuItem(text = { Text(stringResource(R.string.edit_task)) }, onClick = { close(); onEdit(row) })
                DropdownMenuItem(text = { Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error) }, onClick = { close(); onDelete(task.id) })
            }
        }
    }
}

@Composable
private fun TagList(modifier: Modifier, tags: List<TagEntity>, onEdit: (TagEntity) -> Unit, onDelete: (TagEntity) -> Unit) {
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(start = AppSpacing.medium, end = AppSpacing.medium, top = AppSpacing.medium, bottom = AppSpacing.listBottom), verticalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
        if (tags.isEmpty()) item { EmptyState(Icons.Default.Label, stringResource(R.string.empty_tags), stringResource(R.string.empty_tags_hint)) }
        items(tags, key = { it.id }) { tag ->
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)) {
                Row(Modifier.padding(AppSpacing.medium), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = MaterialTheme.shapes.medium, color = Color(tag.colorArgb)) {
                        Icon(tag.iconKey.image(), contentDescription = null, tint = if (Color(tag.colorArgb).luminance() > 0.179f) Color.Black else Color.White, modifier = Modifier.padding(AppSpacing.small).size(24.dp))
                    }
                    Text(tag.name, Modifier.weight(1f).padding(horizontal = AppSpacing.medium), style = MaterialTheme.typography.titleMedium)
                    ItemMenu(stringResource(R.string.item_actions, tag.name)) { close ->
                        DropdownMenuItem(text = { Text(stringResource(R.string.edit_tag)) }, onClick = { close(); onEdit(tag) })
                        DropdownMenuItem(text = { Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error) }, onClick = { close(); onDelete(tag) })
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskEditorDialog(
    task: TaskWithTags?, tags: List<TagEntity>, today: LocalDate, defaultToToday: Boolean, initialTagIds: Set<Long>,
    saving: Boolean, error: String?, onDismiss: () -> Unit, onSave: (TaskEntity, Set<Long>) -> Unit
) {
    val entity = task?.task
    fun dateInput(date: LocalDate): String {
        val jalali = JalaliConverter.toJalali(date)
        return PersianDigits.convert("%04d/%02d/%02d".format(java.util.Locale.ROOT, jalali.year, jalali.month, jalali.day))
    }
    var title by rememberSaveable(entity?.id) { mutableStateOf(entity?.title.orEmpty()) }
    var description by rememberSaveable(entity?.id) { mutableStateOf(entity?.description.orEmpty()) }
    var dateText by rememberSaveable(entity?.id) {
        mutableStateOf(entity?.dueEpochDay?.let { dateInput(LocalDate.ofEpochDay(it)) } ?: if (entity == null && defaultToToday) dateInput(today) else "")
    }
    var timeText by rememberSaveable(entity?.id) {
        mutableStateOf(entity?.dueMinuteOfDay?.let { PersianDigits.convert("%02d:%02d".format(java.util.Locale.ROOT, it / 60, it % 60)) }.orEmpty())
    }
    var reminderEnabled by rememberSaveable(entity?.id) { mutableStateOf(entity?.reminderEnabled == true) }
    var priority by rememberSaveable(entity?.id) { mutableStateOf(runCatching { TaskPriority.valueOf(entity?.priority ?: "MEDIUM") }.getOrDefault(TaskPriority.MEDIUM)) }
    var selectedTagIds by rememberSaveable(entity?.id) { mutableStateOf(task?.tags?.map { it.id }?.toSet() ?: initialTagIds) }
    var details by rememberSaveable(entity?.id) { mutableStateOf(description.isNotBlank() || timeText.isNotBlank() || selectedTagIds.isNotEmpty() || priority != TaskPriority.MEDIUM) }
    val draft = listOf(title, description, dateText, timeText, reminderEnabled.toString(), priority.name, selectedTagIds.sorted().joinToString())
    val initialDraft = rememberSaveable(entity?.id) { draft }
    val dueDate = remember(dateText) { if (dateText.isBlank()) null else PersianFormat.parseDate(dateText) }
    val dueMinute = remember(timeText) { if (timeText.isBlank()) null else PersianFormat.parseTime(timeText) }
    EditorDialog(
        title = stringResource(if (task == null) R.string.add_task else R.string.edit_task),
        valid = title.isNotBlank() && (dateText.isBlank() || dueDate != null) && (timeText.isBlank() || (dueMinute != null && dueDate != null)),
        dirty = draft != initialDraft, saving = saving, error = error, onDismiss = onDismiss,
        onSave = {
            onSave(
                (entity ?: TaskEntity(title = title)).copy(
                    title = title.trim(), description = description.trim().ifBlank { null },
                    dueEpochDay = dueDate?.toEpochDay(), dueMinuteOfDay = dueMinute,
                    reminderEnabled = reminderEnabled && dueDate != null && dueMinute != null, priority = priority.name
                ), selectedTagIds
            )
        }
    ) {
        OutlinedTextField(title, { title = it }, label = { Text(stringResource(R.string.task_title)) }, singleLine = true, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next), enabled = !saving)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
            FilterChip(dueDate == today, { dateText = dateInput(today) }, enabled = !saving, label = { Text(stringResource(R.string.today)) })
            FilterChip(dueDate == today.plusDays(1), { dateText = dateInput(today.plusDays(1)) }, enabled = !saving, label = { Text(stringResource(R.string.tomorrow)) })
            FilterChip(dateText.isBlank(), { dateText = ""; timeText = ""; reminderEnabled = false }, enabled = !saving, label = { Text(stringResource(R.string.no_date_section)) })
        }
        OutlinedTextField(
            dateText,
            { dateText = PersianDigits.convert(PersianDigits.toAscii(it)) },
            label = { Text(stringResource(R.string.due_date)) },
            isError = dateText.isNotBlank() && dueDate == null,
            supportingText = { Text(stringResource(if (dateText.isNotBlank() && dueDate == null) R.string.due_date_invalid else R.string.date_hint)) },
            modifier = Modifier.fillMaxWidth(), singleLine = true, enabled = !saving,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Next)
        )
        if (!details) TextButton(onClick = { details = true }) { Text(stringResource(R.string.more_details)) }
        if (details) {
            OutlinedTextField(description, { description = it }, label = { Text(stringResource(R.string.task_description)) }, modifier = Modifier.fillMaxWidth(), enabled = !saving)
            OutlinedTextField(
                timeText,
                { timeText = PersianDigits.convert(PersianDigits.toAscii(it)) },
                label = { Text(stringResource(R.string.due_time)) },
                isError = timeText.isNotBlank() && (dueMinute == null || dueDate == null),
                supportingText = {
                    Text(stringResource(if (timeText.isNotBlank() && (dueMinute == null || dueDate == null)) R.string.due_time_invalid else R.string.time_hint))
                }, modifier = Modifier.fillMaxWidth(), singleLine = true, enabled = !saving,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Next)
            )
            if (dueDate != null && dueMinute != null) Row(verticalAlignment = Alignment.CenterVertically) {
                val reminderLabel = stringResource(R.string.task_reminder)
                Checkbox(reminderEnabled, { reminderEnabled = it }, enabled = !saving, modifier = Modifier.semantics { contentDescription = reminderLabel })
                Text(stringResource(R.string.task_reminder))
            }
            Text(stringResource(R.string.task_priority), style = MaterialTheme.typography.titleSmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
                TaskPriority.entries.forEach { option ->
                    FilterChip(priority == option, { priority = option }, enabled = !saving, label = { Text(stringResource(option.resourceId())) })
                }
            }
            if (tags.isNotEmpty()) Text(stringResource(R.string.tags), style = MaterialTheme.typography.titleSmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
                tags.forEach { tag ->
                    FilterChip(tag.id in selectedTagIds, { selectedTagIds = selectedTagIds.toggle(tag.id) }, enabled = !saving, label = { Text(tag.name) })
                }
            }
        }
    }
}

@Composable
private fun TagEditorDialog(tag: TagEntity?, saving: Boolean, error: String?, onDismiss: () -> Unit, onSave: (TagEntity) -> Unit) {
    var name by rememberSaveable(tag?.id) { mutableStateOf(tag?.name.orEmpty()) }
    var color by rememberSaveable(tag?.id) { mutableStateOf(tag?.colorArgb ?: tagColors.first().first) }
    var iconKey by rememberSaveable(tag?.id) { mutableStateOf(tag?.iconKey ?: tagIcons.first().key) }
    val dirty = name != tag?.name.orEmpty() || color != (tag?.colorArgb ?: tagColors.first().first) || iconKey != (tag?.iconKey ?: tagIcons.first().key)
    EditorDialog(
        title = stringResource(if (tag == null) R.string.add_tag else R.string.edit_tag),
        valid = name.isNotBlank(), dirty = dirty, saving = saving, error = error, onDismiss = onDismiss,
        onSave = { onSave(TagEntity(tag?.id ?: 0, name.trim(), color, iconKey)) }
    ) {
        OutlinedTextField(name, { name = it }, label = { Text(stringResource(R.string.tag_name)) }, singleLine = true, modifier = Modifier.fillMaxWidth(), enabled = !saving)
        Text(stringResource(R.string.tag_color))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
            tagColors.forEach { (argb, label) ->
                FilterChip(color == argb, { color = argb }, enabled = !saving, label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(Modifier.size(16.dp), color = Color(argb)) {}
                        Text(stringResource(label), Modifier.padding(start = 4.dp))
                    }
                })
            }
        }
        Text(stringResource(R.string.tag_icon))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
            tagIcons.forEach { choice ->
                FilterChip(iconKey == choice.key, { iconKey = choice.key }, enabled = !saving, label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(choice.image, contentDescription = null)
                        Text(stringResource(choice.label), Modifier.padding(start = 4.dp))
                    }
                })
            }
        }
    }
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
    Page.TASKS -> Icons.AutoMirrored.Filled.ListAlt
    Page.HABITS -> Icons.Default.Favorite
    Page.STATS -> Icons.Default.BarChart
    Page.TAGS -> Icons.Default.Label
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
