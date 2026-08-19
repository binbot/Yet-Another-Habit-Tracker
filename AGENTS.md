# AGENTS.md

Development workflow for Yet-Another-Habit-Tracker.

## Remotes

- `origin` = Codeberg (source of truth, SSH)
- `github` = GitHub (backup, HTTPS)

Pushing a branch to a remote:

```sh
git push origin <branch>
git push github <branch>
```

Pushing master to both remotes after a merge:

```sh
git push origin master
git push github master
```

## Branch-per-step workflow

Each development step gets its own branch. Only push code that builds and
passes a device smoke test, so pulling on another machine always yields
working code.

```sh
# 1. Branch off clean master (Codeberg is truth)
git checkout master && git pull origin master
git checkout -b <step-branch>

# 2. Implement

# 3. Build locally
./gradlew :app:assembleDebug

# 4. Install and smoke-test on the physical device
adb install -r app/build/outputs/apk/debug/app-debug.apk

# 5. Commit only when it builds
git add -A
git commit -m "<message>"

# 6. Push branch to BOTH remotes
git push origin <step-branch>
git push github <step-branch>

# 7. Merge to master and push master to both remotes
git checkout master && git pull origin master
git merge <step-branch>
git push origin master
git push github master
```

## Build / test environment

- JDK 17, Gradle 8.13 wrapper, SDK at `/home/b/Android/Sdk` (platforms
  android-35/36, build-tools 35.0.0, platform-tools).
- No Android Studio; everything runs from the CLI.
- Test target: physical Pixel 9 Pro over USB adb.
- The project uses pre-release toolchains (Kotlin 2.2.0-RC2, Navigation 3,
  Compose alpha). If a fresh checkout fails to build, verify Gradle
  distribution + SDK licenses first (`sdkmanager --licenses`).

## Data safety

- Schema changes are never done casually. DB version is 4 with AutoMigrations.
  No `fallbackToDestructiveMigration` — Room throws on version mismatch rather
  than wiping data.
- Export/import in the app is a raw SQLite file copy (with WAL checkpoint).
- Export via the app before any schema-adjacent work.

## Testing approach

- Unit/instrumented tests exist only as boilerplate (`ExampleUnitTest`,
  `ExampleInstrumentedTest`) — manual device smoke testing is the real gate.
- Smoke-test the affected screen after each change:
  - Main list scrolling
  - Habit day-cell tap/skip/partial interactions
  - Habit details charts
  - Home screen widget states