---
name: "android-github-actions-apk"
description: "Use when setting up, fixing, or changing the Gradle build, signing, or GitHub Actions workflow that produces the installable debug APK for the Kar-ha Android app (no Android Studio available)."
---
 
# Building the APK without Android Studio (repo: habit-tracker)
 
The project owner has no Android Studio. The only way an APK exists is a GitHub Actions run on a public repo. Source of truth is PLAN.md section 11.
 
## Non-negotiables
1. **Stable debug signing.** A fresh debug keystore on every CI run gives every APK a different signature, so Android refuses to install an update over the old one and the owner must uninstall (losing all data). Fix: commit one `app/debug.keystore` and point `signingConfigs.debug` at it. It is a debug-only key in a public repo, so never reuse it for anything else.
2. **Gradle wrapper must be committed.** Include `gradlew`, `gradlew.bat`, and `gradle/wrapper/gradle-wrapper.jar` plus properties. Without them the build cannot start. If the wrapper cannot be generated locally, make the workflow install Gradle and run `gradle wrapper`, then commit the result.
3. **Increasing versionCode.** Derive it from `github.run_number` (via an env var read in `build.gradle.kts`, with a local fallback of 1).
4. **No hardcoded versions from memory.** Use `gradle/libs.versions.toml` and pick the latest stable, mutually compatible AGP, Gradle, Kotlin, Compose compiler plugin, KSP, Compose BOM, Room, and Glance. KSP must match the Kotlin version.
## Workflow outline (.github/workflows/build-apk.yml)
- Triggers: `push` and `workflow_dispatch`.
- Runner: `ubuntu-latest`. The Android SDK is preinstalled. If license errors appear, run `yes | sdkmanager --licenses`.
- Steps: `actions/checkout`, `actions/setup-java` (Temurin 17), `gradle/actions/setup-gradle`, then `./gradlew testDebugUnitTest assembleDebug`, then `actions/upload-artifact` for `app/build/outputs/apk/debug/*.apk`.
- Unit tests must run in CI. A failing test fails the build.
## Gradle settings
- `gradle.properties`: `android.useAndroidX=true`, `org.gradle.jvmargs=-Xmx3g` (adjust if the runner runs out of memory), `kotlin.code.style=official`.
- `minSdk 26`, JDK 17, `minifyEnabled false` for debug.
- Room: `exportSchema = true` with `room.schemaLocation` set, and commit the generated schema JSON files.
- applicationId and namespace are `ir.hajmousa.habits`. Never change them.
## Debugging build failures
- Read the first real error in the CI log, not the last line. Fix the root cause. Do not paper over with workarounds or by disabling checks.
- Typical culprits: Kotlin / KSP / Compose compiler version mismatch, missing wrapper jar, missing Vazirmatn font file in `res/font`, Room schema path, JDK mismatch.
- You cannot run the build locally, so push small commits and check the Actions result after each one.
## Delivery
After each phase in PLAN.md section 13, the latest green run must produce an APK the owner can download from the run's Artifacts and install (they must allow installs from unknown sources for the browser or file manager).

