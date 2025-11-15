# TradO Android  
Dense README — quick reference for building, running, and understanding this repository.

## Summary  
TradO is an Android application inside the `app` module.  
Source code lives in `app/src/main/java/com/example/trado/` and resources in `app/src/main/res/`.

## Requirements  
- JDK 11+  
- Android Studio  
- Gradle Wrapper included  
- Android device or emulator matching the project `compileSdk`

## How to Run This Project in Android Studio  
1. Open **Android Studio**  
2. Go to **File > Open**  
3. Select the **root folder** of this repository  
4. Android Studio will automatically start **Gradle Sync**  
5. Install any missing SDK components  
6. Wait for indexing to finish  
7. Select an emulator or connect a device  
8. Click the **Run** (green triangle) button  
9. The app starts from **MainActivity**

If Gradle sync errors appear:  
- Click **Try Again** or **Install Missing Components**  
- Ensure `app/google-services.json` exists

## Quick Build & Run (Windows PowerShell)  
```sh
.\gradlew.bat clean assembleDebug
.\gradlew.bat installDebug
```


## Firebase and Configuration
- Keep `app/google-services.json`
- `MyFcmService` handles Firebase Cloud Messaging
- Internet permission and `usesCleartextTraffic="true"` enabled in the manifest

## Key Files and Layout
- `app/src/main/AndroidManifest.xml` — Permissions, Activities, Services
- `MainActivity.java` — Launcher Activity
- **Fragments:** HomeFragment, ChatsFragment, MyAdsFragment, AccountFragment
- **Activities:** AddAdActivity, AdDetailsActivity, ChatActivity, ProfileEditActivity, LoginEmailActivity, RegisterEmailActivity, SellerProfileActivity
- **Utils:** Utils.java, VolleySingleton.java
- **Packages:** models/, adapters/, filters/

## Common Issues & Quick Fixes
- Missing package attribute → Add `package="..."` manually if tools require it
- Runtime permissions → Request READ_EXTERNAL_STORAGE at runtime for modern SDKs
- FCM not receiving messages → Validate App ID and service registration

## Tests

```sh
.\gradlew.bat testDebug
.\gradlew.bat connectedAndroidTest
```


## Troubleshooting
- If SDK not found → open in Android Studio and install required components
- If dependencies fail → review both root and module `build.gradle` files

## Recommended Next Steps
- Backup `app/src/main` before major edits
- Add explicit manifest package attribute if needed
- Optionally add a CONTRIBUTING.md file
- Conservative, automatic rewrites available if requested


## Release Builds
```sh
.\gradlew.bat bundleRelease
.\gradlew.bat assembleRelease
```
