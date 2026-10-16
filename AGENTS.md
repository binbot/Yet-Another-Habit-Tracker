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

Each development step gets its own branch. The gate is strict: nothing is
pushed to a remote until the USER has live-tested the change on the device.
Pulling on another machine must always yield working, user-verified code.

```sh
# 1. Branch off clean master (Codeberg is truth)
git checkout master && git pull origin master
git checkout -b <step-branch>

# 2. Implement

# 3. Build locally
./gradlew :app:assembleDebug

# 4. Commit locally only after it builds (checkpoint for rollback)
git add -A
git commit -m "<message>"

# 5. Install on the physical device
adb install -r app/build/outputs/apk/debug/app-debug.apk

# 6. USER smoke-tests the affected screens. NO PUSH UNTIL USER APPROVES.

# 7. Push branch to BOTH remotes
git push origin <step-branch>
git push github <step-branch>

# 8. Merge to master and push master to both remotes
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
- This is NixOS. The prebuilt SDK binaries (aapt2, etc.) need nix-ld enabled.
  If `adb`/Gradle tooling misbehaves, confirm `programs.nix-ld.enable` and its
  `libraries` (`zlib`, `stdenv.cc.cc.lib`, `expat`) are set in
  `/etc/nixos/configuration.nix`.
- `local.properties` (gitignored) sets `sdk.dir=/home/b/Android/Sdk`.

### Fresh machine bootstrap (NixOS)

Bringing a new machine (or re-provisioning this one) up to dev-ready:

1. In `/etc/nixos/configuration.nix` add (then `sudo nixos-rebuild switch`):
   - `programs.nix-ld.enable = true;` and
     `programs.nix-ld.libraries = with pkgs; [ zlib stdenv.cc.cc.lib expat ];`
   - `android-tools` and `jdk17` in `environment.systemPackages`.
   - `ANDROID_HOME = "/home/b/Android/Sdk";` in `environment.sessionVariables`.
   - USB device access works automatically via systemd uaccess (no udev
     package needed on modern NixOS).
2. Install the SDK manually (no Android Studio), after
   `mkdir -p ~/Android/Sdk/cmdline-tools && cd ~/Android/Sdk/cmdline-tools`:
   ```sh
   curl -sS -o cmdline-tools.zip \
     "https://dl.google.com/android/repository/commandlinetools-linux-<latest>_latest.zip"
   unzip -q cmdline-tools.zip && mv cmdline-tools latest && rm cmdline-tools.zip
   yes | ~/Android/Sdk/cmdline-tools/latest/bin/sdkmanager --sdk_root=$HOME/Android/Sdk --licenses
   ~/Android/Sdk/cmdline-tools/latest/bin/sdkmanager --sdk_root=$HOME/Android/Sdk \
     "platform-tools" "platforms;android-36" "platforms;android-35" "build-tools;35.0.0"
   ```
3. Create the gitignored `local.properties` with `sdk.dir=/home/b/Android/Sdk`.
4. Verify: `./gradlew :app:assembleDebug` then install on the Pixel via adb.

Note: a hard machine restart mid-build can corrupt the Gradle transform cache
(`~/.gradle/caches/<ver>/transforms/.../metadata.bin`), producing obscure
`NoClassDefFoundError`/`output.bin` errors. Fix: stop the Gradle daemon
(`./gradlew --stop`) and delete `~/.gradle/caches/<ver>/transforms`.

Memory: the first (cold) build is the heavy one; incremental builds are light.
But the Gradle + Kotlin daemons stay resident and hold ~4GB idle. If the machine
(no swap) feels taxed after a session, reclaim RAM with `./gradlew --stop`.

## Data safety

- Schema changes are never done casually. DB version is 4 with AutoMigrations.
  No `fallbackToDestructiveMigration` — Room throws on version mismatch rather
  than wiping data.
- Export/import in the app is a raw SQLite file copy (with WAL checkpoint).
- Export via the app before any schema-adjacent work.
- NEVER uninstall, clear app data, or reinstall the dev app to resolve an
  install/signature conflict without (a) confirming there is a recent device
  export/backup and (b) the USER's explicit approval. The dev app on the
  physical Pixel holds real user data.
- `adb install -r` failing with `INSTALL_FAILED_UPDATE_INCOMPATIBLE` means the
  installed app was signed with a different key than the new build — this is
  not a signal to uninstall. Stop and ask the USER how they want to proceed.
- If a smoke test depends on data being present, never substitute an empty
  install; restore from an existing export via Settings → Import Database.

## Testing approach

- Unit/instrumented tests exist only as boilerplate (`ExampleUnitTest`,
  `ExampleInstrumentedTest`) — manual device smoke testing is the real gate.
- Smoke-test the affected screen after each change:
  - Main list scrolling
  - Habit day-cell tap/skip/partial interactions
  - Habit details charts
  - Home screen widget states