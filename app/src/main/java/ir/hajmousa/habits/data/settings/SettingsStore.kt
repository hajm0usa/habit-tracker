package ir.hajmousa.habits.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

class SettingsStore(context: Context) {
    private val store = context.applicationContext.settingsDataStore

    val weeklyBackupEnabled: Flow<Boolean> = store.data.map { it[WEEKLY_BACKUP_ENABLED] ?: true }
    val weeklyBackupMinute: Flow<Int> = store.data.map { it[WEEKLY_BACKUP_MINUTE] ?: (17 * 60) }
    val dayStartHour: Flow<Int> = store.data.map { it[DAY_START_HOUR] ?: 0 }

    suspend fun isWeeklyBackupEnabled(): Boolean = weeklyBackupEnabled.first()
    suspend fun getWeeklyBackupMinute(): Int = weeklyBackupMinute.first()
    suspend fun setDayStartHour(hour: Int) {
        require(hour in 0..6)
        store.edit { it[DAY_START_HOUR] = hour }
    }
    suspend fun setWeeklyBackupEnabled(enabled: Boolean) = store.edit { it[WEEKLY_BACKUP_ENABLED] = enabled }
    suspend fun setWeeklyBackupMinute(minuteOfDay: Int) {
        require(minuteOfDay in 0..1439)
        store.edit { it[WEEKLY_BACKUP_MINUTE] = minuteOfDay }
    }

    suspend fun notificationPermissionAsked(): Boolean = store.data.first()[NOTIFICATION_PERMISSION_ASKED] ?: false
    suspend fun setNotificationPermissionAsked() = store.edit { it[NOTIFICATION_PERMISSION_ASKED] = true }

    suspend fun claimDelivery(reminderKey: String, occurrenceToken: Long): Boolean {
        val key = longPreferencesKey("reminder_last_$reminderKey")
        var claimed = false
        store.edit { preferences ->
            if (preferences[key] != occurrenceToken) {
                preferences[key] = occurrenceToken
                claimed = true
            }
        }
        return claimed
    }

    suspend fun releaseDelivery(reminderKey: String, occurrenceToken: Long) {
        val key = longPreferencesKey("reminder_last_$reminderKey")
        store.edit { preferences -> if (preferences[key] == occurrenceToken) preferences.remove(key) }
    }

    private companion object {
        val WEEKLY_BACKUP_ENABLED = booleanPreferencesKey("weekly_backup_enabled")
        val WEEKLY_BACKUP_MINUTE = intPreferencesKey("weekly_backup_minute")
        val DAY_START_HOUR = intPreferencesKey("day_start_hour")
        val NOTIFICATION_PERMISSION_ASKED = booleanPreferencesKey("notification_permission_asked")
    }
}
