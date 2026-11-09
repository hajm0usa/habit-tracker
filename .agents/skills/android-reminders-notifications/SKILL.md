---
name: "android-reminders-notifications"
description: "Use when implementing or debugging habit/task reminders, exact alarms, notification actions, boot rescheduling, or the weekly backup reminder in the Kar-ha Android app."
---
 
# Reminders and notifications (app: "Kar-ha", package ir.hajmousa.habits)
 
Target device: Android 13. minSdk 26. Source of truth is PLAN.md section 7.
 
## Permissions (manifest + runtime)
- `POST_NOTIFICATIONS`: request at first launch (Android 13+). Handle denial gracefully with an explanation and a link to app notification settings.
- Exact alarms: declare `USE_EXACT_ALARM` (sideloaded app, no Play policy issue). Before scheduling, check `AlarmManager.canScheduleExactAlarms()` and fall back to an inexact alarm if it fails. Verify current behavior in official docs for the chosen targetSdk.
- `RECEIVE_BOOT_COMPLETED`.
## Scheduling design
- Schedule ONLY the next occurrence of each reminder. When it fires, compute and schedule the following one. Never pre-schedule weeks of alarms.
- Use `setExactAndAllowWhileIdle` with a unique `PendingIntent` per reminder (key = type + id + minuteOfDay). Use `FLAG_IMMUTABLE`.
- Reminders: multiple times per day per habit (`HabitReminder` rows), at most one per task (at its due time, only if `reminderEnabled` and a due time exists), plus an optional weekly "make a backup" reminder.
- "Today" is decided via `DayClock` (effective day). The alarm clock time itself is plain wall-clock time.
- Reschedule everything after: device boot, time/timezone change, app update, backup restore, and any edit of a habit, task, or reminder.
## At fire time (AlarmReceiver)
Before posting, skip the notification if the habit is not scheduled that day, is archived, is already completed, or is skipped. Then schedule the next occurrence.
 
## Missed reminders
After boot, post a missed reminder once only if less than 3 hours have passed since its due time. Otherwise drop it.
 
## Notification channels
Separate channels: habits, tasks, backup. Create them on app start (API 26+).
 
## Actions (handled by a BroadcastReceiver, app does not open)
- Done: task -> mark done. Boolean habit -> log value 1. Numeric habit -> set value to the target.
- Snooze 10 minutes: schedule a one-shot alarm. It must not alter the regular recurring schedule.
- After an action, cancel the notification and update the widget.
## Settings screen
- Toggle for the weekly backup reminder.
- Short guide plus a shortcut intent to the app's battery settings, because some vendors (Xiaomi, Samsung, Huawei, Honor) kill background apps and silently drop reminders.
## Testing
- Pure logic (next-occurrence computation, skip conditions, missed-window rule) belongs in the domain layer with plain unit tests.
- Android pieces (alarms, receivers, channels) need manual verification on a real device: fire a reminder, tap both actions, reboot the phone and confirm reminders are restored.

