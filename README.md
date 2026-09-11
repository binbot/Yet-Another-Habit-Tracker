<p align="center">
  <img src="fastlane/metadata/android/en-US/images/icon.png" alt="YA Habit Tracker" width="120">
</p>

# YA Habit Tracker — fork by binbot

[![Codeberg](https://img.shields.io/badge/Codeberg-binbot%2FYet--Another--Habit--Tracker-blue?logo=codeberg)](https://codeberg.org/binbot/Yet-Another-Habit-Tracker)
[![GitHub mirror](https://img.shields.io/badge/GitHub-mirror-black?logo=github)](https://github.com/binbot/Yet-Another-Habit-Tracker)
[![License Apache-2.0](https://img.shields.io/badge/License-Apache--2.0-green)](LICENSE)
[![minSdk 26+](https://img.shields.io/badge/minSdk-26%2B-orange)](app/build.gradle.kts)
[![version 0.4.1](https://img.shields.io/badge/version-0.4.1-blue)](app/build.gradle.kts)

A modern, simple habit tracker for Android. Build streaks, track measurable goals, keep routines in sync — no ads, offline, backup-safe.

## Credits

Original by [@zaved707](https://github.com/zaved707/Yet-Another-Habit-Tracker) ©2025 Zaved Ahmad (Apache-2.0) — see [LICENSE](LICENSE).
Fork maintained by [binbot @ Codeberg](https://codeberg.org/binbot/Yet-Another-Habit-Tracker) · [GitHub mirror](https://github.com/binbot/Yet-Another-Habit-Tracker).

## Features

- **Tracking:** measurable habits (`repetitionPerDay` + `measurementUnit` + `isNegative` limit), flexible `Daily / Weekly / Custom` frequency/cycle, `skip` / `partial` / `NotNeeded` states, `yes/no` tap `Incomplete → Completed → Skipped` and measurable `Partial → Absolute → AbsoluteMore → Skipped`.
- **Visuals:** per-habit colors (shared `HabitColors` palette for habits/routines/notifications), heatmap + full grid dialog, `FrequencyChart`/`PieChart`/`StreakChart` with habit-color theming and legends.
- **Routines:** stacked habits (`RoutineTable` + `RoutineItem` + `sourceRoutineId`), unified `Habits & Routines` list with sticky `Routines`/`Habits` headers (`TopAppBar` small), `FAB → Habit / Routine`, dropdown tick + skip per step and routine header `Skip all` (`Dropdown` checklist, no timer v1).
- **Reminders:** per-habit time + global toggle + default (`Settings` `Notifications`), opportunistic (only if still due), quiet hours `22:00–07:00`, `>3` group summary, habit-color notification, `SCHEDULE_EXACT_ALARM` + `POST_NOTIFICATIONS`.
- **System:** reorderable `ReorderableLazyList` + archiving/filters (`ShowArchive`/`ShowActive`), `Glance` widget (tap to complete/skip), `Light / Dark / System` + `Amoled` + `Dynamic Color` (S+), top bar `Habits & Routines`.
- **Data:** `Room v6` `AutoMigration 5→6` (habit `reminderEnabled` + `Routine` + `sourceRoutineId`), no `fallbackToDestructiveMigration`, WAL checkpoint raw SQLite export/import.

## Installation

- **Codeberg Releases APK (primary):** https://codeberg.org/binbot/Yet-Another-Habit-Tracker/releases
- **GitHub Releases APK (mirror):** https://github.com/binbot/Yet-Another-Habit-Tracker/releases

No Obtainium / F-Droid / IzzyOnDroid listing for this fork — install the APK from the releases above.

## Technologies Used

- [Jetpack Compose](https://developer.android.com/compose) + `Compose BOM 2025.06.00` + `Material3 1.4.0-alpha15` + `Navigation 3 1.0.0-alpha05` + `Reorderable 2.5.1`
- [Koin 4.0.3](https://github.com/InsertKoinIO/koin) for DI, [Room 2.7.1](https://developer.android.com/jetpack/androidx/releases/room) for DB
- [MaterialKolor 3.0.0-beta07](https://github.com/jordond/material-kolor) for per-habit dynamic schemes, [Kizitonwose Calendar 2.7.0](https://github.com/kizitonwose/Calendar) for week/grid calendars
- Kotlin `2.2.0-RC2`, AGP `8.13.2`, `targetSdk 36` `minSdk 26`, JDK 17, Gradle 8.13

## Roadmap

- [x] Custom starting day of week
- [x] Measurable habits (`>=0.1.0`)
- [x] Data backup and restore (`>=0.2.0`)
- [x] HomeScreen widgets (`>=0.4.0`)
- [x] Routines — stacked habits (`>=0.4.x`)
- [x] Reminders + quiet hours (`>=0.4.x`)
- [x] Heatmap legends + habit-color charts
- [ ] Routine stats page (heatmaps + attribution `routine vs habit`)
- [ ] Table form habits view (deferred)
- [ ] Widget partial / long-press improvements

## Privacy & Data Safety

No internet; permissions only `POST_NOTIFICATIONS`, `SCHEDULE_EXACT_ALARM`, `RECEIVE_BOOT_COMPLETED`. Fully offline, no trackers. Export/import is a raw SQLite file copy with `pragma wal_checkpoint(full)`; `Room` throws on version mismatch rather than wiping (`No fallbackToDestructiveMigration`). DB version `6`.

## Build

```sh
# JDK 17, Gradle 8.13 wrapper, SDK 35/36 at /home/b/Android/Sdk
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
# if fresh checkout fails: sdkmanager --licenses
```

See [AGENTS.md](AGENTS.md) for branch-per-step workflow (push to both remotes only after device smoke test).

## License

Apache-2.0 — see [LICENSE](LICENSE). Original ©2025 Zaved Ahmad.
