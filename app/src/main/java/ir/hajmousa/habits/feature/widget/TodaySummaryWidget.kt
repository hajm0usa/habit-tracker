package ir.hajmousa.habits.feature.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.text.FontWeight
import androidx.glance.unit.dp
import androidx.glance.unit.sp
import androidx.glance.unit.ColorProvider
import ir.hajmousa.habits.HabitsApp
import ir.hajmousa.habits.MainActivity
import ir.hajmousa.habits.R
import ir.hajmousa.habits.domain.habit.ScheduleResolver
import ir.hajmousa.habits.domain.task.ProgressHabit
import ir.hajmousa.habits.domain.task.ProgressTask
import ir.hajmousa.habits.domain.task.TodayProgressCalculator
import ir.hajmousa.habits.data.repo.toModel
import ir.hajmousa.habits.core.date.PersianDigits
import kotlinx.coroutines.flow.first

class TodaySummaryWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val container = (context.applicationContext as HabitsApp).container
        container.dayClock.setDayStartHour(container.settings.dayStartHour.first())
        val today = container.dayClock.today().toEpochDay()
        val versions = container.database.habitDao().allVersions().groupBy { it.habitId }
        val logs = container.database.habitDao().allLogs().groupBy { it.habitId }
        val habits = container.database.habitDao().allHabits().map { habit ->
            val schedule = versions[habit.id].orEmpty().map { it.toModel() }
            val log = logs[habit.id].orEmpty().firstOrNull { it.epochDay == today }?.toModel()
            val daySchedule = ScheduleResolver.versionOn(schedule, today)
            ProgressHabit(
                scheduled = daySchedule != null && ScheduleResolver.isScheduled(today, schedule),
                archived = habit.archived,
                target = daySchedule?.targetValue ?: 1,
                value = log?.value,
                skipped = log?.skipped == true
            )
        }
        val tasks = container.database.taskDao().allTasks().map { ProgressTask(it.dueEpochDay, it.isDone) }
        val progress = TodayProgressCalculator.calculate(today, habits, tasks)
        val label = context.getString(R.string.widget_progress)
        val count = context.getString(R.string.widget_progress_count, PersianDigits.convert(progress.done.toString()), PersianDigits.convert(progress.total.toString()))
        provideContent {
            Column(
                modifier = GlanceModifier.fillMaxSize().background(ColorProvider(androidx.compose.ui.graphics.Color(0xFF6750A4)))
                    .clickable(actionStartActivity<MainActivity>()).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(label, style = TextStyle(fontSize = 13.sp, color = ColorProvider(androidx.compose.ui.graphics.Color.White)))
                Text(count, style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = ColorProvider(androidx.compose.ui.graphics.Color.White)))
            }
        }
    }
}

class TodaySummaryWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TodaySummaryWidget()
}

object WidgetUpdater {
    suspend fun update(context: Context) = TodaySummaryWidget().updateAll(context)
}
