package ir.hajmousa.habits.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ir.hajmousa.habits.R
import ir.hajmousa.habits.core.date.PersianDigits
import ir.hajmousa.habits.core.date.PersianFormat

@Composable
fun SettingsScreen(
    weeklyBackupEnabled: Boolean,
    weeklyBackupMinute: Int,
    notificationPermissionGranted: Boolean,
    onWeeklyBackupEnabled: (Boolean) -> Unit,
    onSaveBackupMinute: (Int) -> Unit,
    onRequestNotifications: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    onOpenBatterySettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var timeText by remember(weeklyBackupMinute) {
        mutableStateOf(PersianDigits.convert("%02d:%02d".format(java.util.Locale.ROOT, weeklyBackupMinute / 60, weeklyBackupMinute % 60)))
    }
    val minute = PersianFormat.parseTime(timeText)
    Column(modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.weekly_backup_reminder), Modifier.weight(1f))
            Switch(weeklyBackupEnabled, onWeeklyBackupEnabled)
        }
        OutlinedTextField(
            timeText,
            { timeText = PersianDigits.convert(PersianDigits.toAscii(it)) },
            label = { Text(stringResource(R.string.backup_reminder_time)) },
            isError = minute == null,
            enabled = weeklyBackupEnabled,
            singleLine = true
        )
        Button(onClick = { minute?.let(onSaveBackupMinute) }, enabled = weeklyBackupEnabled && minute != null) {
            Text(stringResource(R.string.save_settings))
        }
        if (!notificationPermissionGranted) {
            Text(stringResource(R.string.notification_permission_message))
            TextButton(onClick = onRequestNotifications) { Text(stringResource(R.string.enable_notifications)) }
            TextButton(onClick = onOpenNotificationSettings) { Text(stringResource(R.string.open_notification_settings)) }
        }
        Text(stringResource(R.string.battery_guide))
        TextButton(onClick = onOpenBatterySettings) { Text(stringResource(R.string.battery_settings)) }
    }
}
