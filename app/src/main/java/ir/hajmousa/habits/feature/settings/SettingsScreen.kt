package ir.hajmousa.habits.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import ir.hajmousa.habits.R
import ir.hajmousa.habits.core.date.PersianDigits
import ir.hajmousa.habits.core.date.PersianFormat
import ir.hajmousa.habits.core.ui.AppSpacing

@Composable
fun SettingsScreen(
    weeklyBackupEnabled: Boolean,
    weeklyBackupMinute: Int,
    dayStartHour: Int,
    notificationPermissionGranted: Boolean,
    onWeeklyBackupEnabled: (Boolean) -> Unit,
    onSaveBackupMinute: (Int) -> Unit,
    onSaveDayStartHour: (Int) -> Unit,
    onRequestNotifications: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    onOpenBatterySettings: () -> Unit,
    onExportBackup: () -> Unit,
    onImportBackup: () -> Unit,
    modifier: Modifier = Modifier
) {
    var timeText by rememberSaveable(weeklyBackupMinute) {
        mutableStateOf(PersianDigits.convert("%02d:%02d".format(java.util.Locale.ROOT, weeklyBackupMinute / 60, weeklyBackupMinute % 60)))
    }
    var dayStartText by rememberSaveable(dayStartHour) { mutableStateOf(PersianDigits.convert(dayStartHour.toString())) }
    val parsedDayStart = PersianDigits.toAscii(dayStartText).toIntOrNull()?.takeIf { it in 0..6 }
    val minute = PersianFormat.parseTime(timeText)
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(AppSpacing.medium), verticalArrangement = Arrangement.spacedBy(AppSpacing.large)) {
        Text(stringResource(R.string.backup_section), style = MaterialTheme.typography.titleLarge)
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)) {
            Column(Modifier.padding(AppSpacing.medium), verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    val reminderLabel = stringResource(R.string.weekly_backup_reminder)
                    Text(reminderLabel, Modifier.weight(1f))
                    Switch(weeklyBackupEnabled, onWeeklyBackupEnabled, modifier = Modifier.semantics { contentDescription = reminderLabel })
                }
                OutlinedTextField(
                    timeText,
                    { timeText = PersianDigits.convert(PersianDigits.toAscii(it)) },
                    label = { Text(stringResource(R.string.backup_reminder_time)) },
                    isError = minute == null,
                    enabled = weeklyBackupEnabled,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Button(onClick = { minute?.let(onSaveBackupMinute) }, enabled = weeklyBackupEnabled && minute != null) {
                    Text(stringResource(R.string.save_settings))
                }
                OutlinedButton(onClick = onExportBackup, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.export_backup)) }
                TextButton(onClick = onImportBackup, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.import_backup)) }
            }
        }
        Text(stringResource(R.string.day_section), style = MaterialTheme.typography.titleLarge)
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)) {
            Column(Modifier.padding(AppSpacing.medium), verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)) {
                OutlinedTextField(dayStartText, { dayStartText = PersianDigits.convert(PersianDigits.toAscii(it)) }, label = { Text(stringResource(R.string.day_start_hour)) }, isError = parsedDayStart == null, singleLine = true, modifier = Modifier.fillMaxWidth())
                Button(onClick = { parsedDayStart?.let(onSaveDayStartHour) }, enabled = parsedDayStart != null) { Text(stringResource(R.string.save_settings)) }
            }
        }
        Text(stringResource(R.string.reminder_section), style = MaterialTheme.typography.titleLarge)
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)) {
            Column(Modifier.padding(AppSpacing.medium), verticalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
                if (!notificationPermissionGranted) {
                    Text(stringResource(R.string.notification_permission_message))
                    TextButton(onClick = onRequestNotifications) { Text(stringResource(R.string.enable_notifications)) }
                    TextButton(onClick = onOpenNotificationSettings) { Text(stringResource(R.string.open_notification_settings)) }
                }
                Text(stringResource(R.string.battery_guide))
                TextButton(onClick = onOpenBatterySettings) { Text(stringResource(R.string.battery_settings)) }
            }
        }
    }
}
