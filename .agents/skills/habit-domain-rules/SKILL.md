---
name: "habit-domain-rules"
description: "Use when implementing or testing date, Jalali calendar, habit schedule, streak, or task-cleanup logic in the Kar-ha Android app."
---
 
# Habit domain rules (app: "Kar-ha", package ir.hajmousa.habits)
 
Source of truth is PLAN.md sections 5 and 6. If this skill and PLAN.md disagree, ask the project owner.
 
## Hard constraints
- `core/date` and `domain/*` are pure Kotlin. No `android.*` imports, so everything is unit-testable with plain JUnit.
- Dates are stored as Gregorian `epochDay: Long`. Jalali is converted ONLY at display time.
- Times of day are `minuteOfDay: Int` (0..1439). Instants are `epochMillis: Long`.
- Never call `LocalDate.now()` in logic. Always go through an injectable `DayClock`.
- No third-party Jalali or calendar library. Write the converter and the date picker by hand.
## DayClock
- Setting `dayStartHour` (0..6, default 0).
- `effectiveToday = (now - dayStartHour hours).toLocalDate()`.
- Used by: Today screen, streaks, edit window, task cleanup, widget, notification checks.
- Tests must use a fake clock and cover the boundary around the start hour.
## Jalali converter
- Algorithm: well-known 33-year-cycle method (as in jalaali-js), valid for roughly years 1300..1500.
- Month lengths: months 1-6 have 31 days, months 7-11 have 30, month 12 has 29 (30 in a leap year).
- Week starts on Saturday (Saturday = index 0).
- Required tests (verify the reference values against an independent source before trusting them):
  - 1 Farvardin 1400 = 2021-03-21
  - 1 Farvardin 1402 = 2023-03-21
  - 1 Farvardin 1403 = 2024-03-20
  - 1 Farvardin 1404 = 2025-03-21
  - 30 Esfand 1403 is valid (leap year), 30 Esfand 1404 is invalid
  - 18 Aban 1405 = 2026-11-09
  - round-trip Gregorian -> Jalali -> Gregorian over several years
  - every Jalali year has 365 or 366 days and month lengths sum to that
- UI digits are Persian. Storage and logic use standard digits.
## Schedule versions
- Every change to schedule, target, step, or anchor creates a new `HabitScheduleVersion` with `effectiveFrom = effectiveToday`. If a version with the same `effectiveFrom` exists, replace it (never create duplicates).
- The version valid for date `d` is the one with the greatest `effectiveFrom <= d`. No version means the day is NOT scheduled (before habit creation).
- `isScheduled(habit, d)`:
  - DAILY: always
  - WEEKDAYS: bit for d's weekday is set (Saturday = bit 0 ... Friday = bit 6)
  - EVERY_N: `d >= anchor && (d - anchor) % N == 0` (fixed from anchor, never floating)
## Completion
- `completed(d) = !skipped && value >= target(version valid on d)`. Boolean habits have target 1.
- A partial numeric day counts as failed. Over-target values still count as complete (progress bar caps at 100%).
- Skipped days neither complete nor break a streak.
- Editable window: only dates `>= effectiveToday - 7`. Older is read-only. Logging on an unscheduled day is not allowed.
## Streak algorithm
```
cursor = effectiveToday
if scheduled(cursor) && !completed(cursor) && !skipped(cursor): cursor -= 1   # today still pending
n = 0
while cursor >= habit.createdEpochDay:
    if !scheduled(cursor): cursor -= 1; continue
    if skipped(cursor):    cursor -= 1; continue
    if completed(cursor):  n += 1; cursor -= 1; continue
    break
return n
```
- Best streak: one forward pass from creation to today with the same rules.
- Success percentage over a range = completed / scheduled, excluding skipped days, future days, and today if still pending. If the denominator is 0, show a dash.
## Tasks
- Overdue: `!isDone && dueEpochDay < effectiveToday`.
- Done tasks are hard-deleted when `doneEpochDay < effectiveToday - 7`. Only tasks, never habit history.
- Un-ticking a done task is allowed until it is deleted.
- Today progress = done/total over habits scheduled today (not archived, not skipped) plus tasks due today.
## Test checklist for the domain layer
ScheduleResolver (all three types, multiple versions, before creation), completion rules, streak (pending today, skip, unscheduled gap, partial numeric break, schedule change mid-history), best streak, percentages, task cleanup boundary, DayClock boundary.

