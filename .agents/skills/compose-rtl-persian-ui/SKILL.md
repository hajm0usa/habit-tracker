---
name: "compose-rtl-persian-ui"
description: "Use when building or changing Jetpack Compose UI, RTL layout, Persian fonts and digits, Material You theming, drag-and-drop lists, Canvas charts, or the Glance widget in the Kar-ha Android app."
---
 
# Compose UI for a Persian RTL app (package ir.hajmousa.habits)
 
Source of truth is PLAN.md sections 2, 8, and 10. If this skill and PLAN.md disagree, ask the project owner.
 
## Language, direction, digits
- The app is Persian only. No English user-facing strings. Every string lives in `strings.xml`.
- Set `android:supportsRtl="true"` and wrap the app content in `CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl)`.
- Use `start`/`end` (never `left`/`right`) in padding, alignment, and arrangement.
- Directional icons (back, previous/next month, chevrons) must mirror correctly. Use `Icons.AutoMirrored` variants.
- Show Persian digits in the UI through one helper (`PersianDigits.convert(text)`) used everywhere: counts, times, dates, progress ("4 of 6"), streaks, percentages. Storage, logic, and JSON always use standard digits.
- Date display goes through `PersianFormat` and the hand-written Jalali converter. Week starts on Saturday.
## Font
- Vazirmatn (OFL license) bundled in `res/font`, wired as a `FontFamily` into the Material 3 `Typography` for every text style.
- The TTF files must be committed to the repo. If you have no network access, ask the project owner to add them. A missing font file breaks the CI build.
## Theme
- Follow the system light/dark setting. No in-app theme switch.
- Dynamic color (Material You) on API 31+. On lower APIs use a fixed `ColorScheme`.
- The "done/success" color in the stats calendar is a fixed color, not derived from the wallpaper, so it stays readable.
- Adaptive launcher icon is vector only (a check mark on a colored background), with a `monochrome` layer for Android 13 themed icons.
## Screens and navigation
- Bottom navigation, 4 tabs: Today (start destination), Tasks, Habits, Stats. Settings, tag management, and the task/habit forms are secondary screens.
- Every list needs an empty state. Tag filtering uses AND logic. When nothing matches, show "no item has all the selected tags" plus a clear-filter button.
- Undo goes through a Snackbar for deleting tasks and habits. Hard-deleting a habit is delayed 5 seconds so Undo is real.
- Tasks screen sections: overdue, today, upcoming, no date, and a collapsible "done" section showing the last 7 days grouped by day.
- Today screen: overdue tasks first (warning color), then today's tasks and habits. Completed habits stay in place with a check mark. They do not move to the bottom.
## Habit input controls
- Boolean habit: a single check toggle.
- Numeric habit: "+" button adds the habit's `step`, "-" subtracts it, a progress bar caps at 100%, and tapping the number opens an exact-value dialog. Show the optional unit next to the number. Values above the target are allowed.
- Habit list reordering: long-press drag and drop. Prefer `detectDragGesturesAfterLongPress` or a small library only if it is actively maintained and compatible with the pinned Compose version. Persist `sortOrder`.
## Tags
- Name + color + icon, where the icon comes from a fixed set of about 30 to 40 Material icons (a `TagIcons` map from key to `ImageVector`). No free-form icon picker.
- Multi-select chips in forms and in the filter row.
## Jalali date picker (hand-built)
- Month grid, previous/next month buttons, year selection, RTL, Persian digits, Saturday-first columns.
- Reused for due dates, the every-N-days anchor, and the stats calendar.
## Charts (no chart library)
- Monthly calendar heatmap per habit: done / missed / skipped / not scheduled, drawn with Canvas or a grid of boxes. Tapping a day inside the 7-day edit window opens a value editor.
- 30-day trend line or bars for numeric habits drawn with `Canvas`.
- Never rely on color alone. Add a shape or label so states stay distinguishable.
## Glance widget
- One small widget showing "today's progress" (e.g. "4 of 6") in Persian digits. Tapping opens `MainActivity`. No interactive controls.
- Refresh after any habit log or task tick, after the effective day rolls over, and after a backup restore.
## Quality checks
- Check every screen in both light and dark mode and with a large font scale.
- No hardcoded dp magic numbers repeated across screens: use shared spacing values.
- Keep composables stateless where possible and hoist state into ViewModels. Business rules stay in `domain/*`, never in composables.

