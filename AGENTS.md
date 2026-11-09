# Agent rules

Follow `PLAN.md` phases in order; do not start a later phase until the previous phase's CI build is green.
Verify compatible stable dependency versions from official sources; do not guess versions.
Add dependencies only with a clear reason. Implement Jalali calendar and charts by hand.
Keep `core/date` and `domain` free of `android.*` imports.
Use `DayClock` for all date-sensitive logic; do not call `LocalDate.now()` in domain logic.
All UI strings must be Persian and live in `strings.xml`.
Ask the owner before making decisions not specified in `PLAN.md` or that conflict with it.
Use small commits with clear messages and check CI after each push.
Fix root causes of CI failures; do not use temporary workarounds.
Never change package or application ID `ir.hajmousa.habits`.
