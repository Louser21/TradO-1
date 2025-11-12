# TradO (Android)

Dense README — quick reference for building, running and understanding this repository.

## Summary

TradO is an Android application located in the `app` module. The app code lives under
`app/src/main/java/com/example/trado/` and resources under `app/src/main/res/`.

This README is intentionally compact and focused: required tools, build and run commands,
key files, troubleshooting hints, and suggested next steps.

## Requirements

- JDK 11+ (match Android Gradle Plugin compatibility used by this project)
- Android Studio (recommended) or command-line Gradle
- Gradle wrapper is included (`gradlew` / `gradlew.bat`)
- An Android device or emulator with API level compatible with the module's compileSdk

## Quick build & run (Windows PowerShell)

Open PowerShell in the repository root and run:

```powershell
# Clean and assemble debug APK
.\gradlew.bat clean assembleDebug

# Install to a connected device (or emulator)
.\gradlew.bat installDebug

# Or launch from Android Studio Run configuration
```

If you want to build an APK bundle or release build, use `bundleRelease` / `assembleRelease`
and ensure signing configs are provided.

## Firebase and configuration

- `app/google-services.json` is present. Keep it in place — it's used by Firebase services.
- The app registers a `Service` named `MyFcmService` (see `app/src/main/java/.../MyFcmService.java`) for FCM.
- Internet permission is declared in `app/src/main/AndroidManifest.xml` and `usesCleartextTraffic` is enabled.

## Key files and layout

- `app/src/main/AndroidManifest.xml` — app permissions, activities and service declarations.
- `app/src/main/java/com/example/trado/MainActivity.java` — app entry activity (launcher).
- Fragments: `HomeFragment`, `ChatsFragment`, `MyAdsFragment`, `AccountFragment` — UI sections.
- Activities handling flows: `AddAdActivity`, `AdDetailsActivity`, `ChatActivity`, `ProfileEditActivity`, `LoginEmailActivity`, `RegisterEmailActivity`, `SellerProfileActivity`.
- `utils/Utils.java`, `VolleySingleton.java` — helpers and networking wiring.
- `models/`, `adapters/`, `filters/` — app domain objects and UI adapters.

If you need a quick map, the Java package is `com.example.trado` and follows a conventional Android structure.

## Common issues & quick fixes

- Missing package attribute in `AndroidManifest.xml`: modern Gradle can inject the applicationId at build time. If you rely on fully-qualified names in manifest entries or use tools that expect an explicit `package="..."`, add it explicitly.
- Runtime permissions: the app requests `READ_EXTERNAL_STORAGE` in the manifest, but depending on your target SDK you may need to request it at runtime.
- Firebase messages not received: confirm `google-services.json` matches the appId and that `MyFcmService` is declared correctly.

## Tests and lint

This repository does not include a dedicated test harness in the root; Android unit and instrumentation tests can be run with Gradle tasks:

```powershell
.\gradlew.bat testDebug
.\gradlew.bat connectedAndroidTest
```

## Troubleshooting build failures

- If Gradle fails with missing SDK or compile errors, open the project in Android Studio and let it sync SDK and recommended components.
- For dependency or plugin issues, check the `build.gradle` files in the root and `app/` module.

## Small, safe next steps (recommended)

1. Create a backup of `app/src/main` before applying automated edits.
2. Optionally: add an explicit `package` attribute to the manifest if you rely on fully-qualified component names.
3. Add a minimal CONTRIBUTING or developer notes file describing Java and Gradle versions used locally.
4. If you want, I can create conservative code tidy-ups (fix missing imports, standardize package declarations) — I will always create a backup first.

## Where I changed files

- This README was added at the repository root: `README.md`.

## Contact and follow-up

If you'd like me to proceed with automatic, conservative rewrites of files under `app/src/main/java/com/example/trado/` (backed up first), reply with `Backup + Rewrite` and I will:

- create a timestamped backup under `app/src/main/backups/` and
- apply minimal fixes to manifest and any Java files that have obvious structural issues.

Otherwise, tell me which specific file(s) you want created or rewritten and I'll act on them.

---
Generated on 2025-11-12.
