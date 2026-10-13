# 🛡️ SafeGuard Parental Control for Android

An all-in-one, privacy-focused, offline-first Parental Control application for Android built with **Kotlin**, **Jetpack Compose**, **Material 3**, and **Room Database**.

SafeGuard empowers parents to supervise and protect their child's digital wellbeing by blocking harmful websites in **Google Chrome**, enforcing daily application time allowances, locking devices during bedtime curfews, and preventing unauthorized app uninstallation.

---

## ✨ Key Features

### 🌐 1. Chrome & Browser Web Filtering
- **Real-Time URL Detection:** Inspects active URLs within **Google Chrome**, Microsoft Edge, Firefox, Brave, and Samsung Internet via Accessibility Service.
- **Custom Website Blacklist:** Add any custom domain or keyword (e.g. `reddit.com`, `tiktok.com`, `twitter.com`, `instagram.com`).
- **Preconfigured Safety Categories:** Built-in protection against Adult (18+), Online Gambling, and Unsafe Social platforms.
- **Instant Overlay Blocker:** Immediately navigates away and presents a non-dismissible parental warning overlay whenever restricted domains are accessed.

### ⏳ 2. Per-App Time Limits & Quotas
- Detects all user-installed applications.
- Set granular daily time allowances (e.g., 30 mins for games, 45 mins for video apps) or lock specific apps entirely.
- Tracks real-time daily usage via Android's `UsageStatsManager`.
- Once the time limit expires, the app is automatically locked.

### 🌙 3. Bedtime & Curfew Downtime
- Configure nightly curfews (e.g., 9:30 PM to 6:30 AM).
- During downtime, all non-essential applications are locked to encourage healthy sleep.
- Emergency dialer and parent contact are permanently whitelisted for child safety.

### 🔒 4. Anti-Tamper & Device Admin Protection
- **Device Administrator API:** Registers SafeGuard as an active device admin to prevent children from simply dragging the app to the trash or uninstalling via launcher.
- **Settings Guard:** Monitors access to Android System Settings and blocks attempts to disable permissions or force-stop the guardian service.
- **PIN Gatekeeper:** All parental settings, rule modifications, and mode toggles require a master 4-to-6 digit Parent PIN.

### 📱 5. Standalone All-In-One Architecture
- Completely offline: No external servers, no subscriptions, and zero tracking or cloud data leaks.
- Battery-efficient event-driven architecture using Android `AccessibilityService` and `JobScheduler` / `WorkManager`.

---

## 🚀 Downloading & Installing the APK

### Direct APK Download
1. Head to the **[Releases](../../releases)** section of this GitHub repository.
2. Download `safeguard-parental-control-debug.apk` or `safeguard-parental-control-release.apk`.
3. Transfer or open the `.apk` file on the Android device and select **Install**.

---

## 🛠️ First-Time Android Setup Guide

When launching SafeGuard for the first time on your child's phone:

1. **Set Parent PIN:** Create a 4-to-6 digit PIN that only you know.
2. **Grant Required Permissions:**
   - **Accessibility Service:** Select *SafeGuard Parental Control* in Accessibility settings and toggle **ON**. This allows Chrome URL detection and foreground app locking.
   - **Usage Access:** Allow SafeGuard in *Usage access* to calculate screen time.
   - **Display Over Other Apps (Overlay):** Allow permission to show the full-screen blocker when limits are reached.
   - **Device Administrator:** Activate device admin to prevent child uninstallation.
3. **Configure Rules:**
   - Tap **Chrome & Browser Web Blocker** to add websites you wish to block.
   - Tap **App Limits & Quotas** to set daily time limits for apps.
   - Tap **Bedtime & Curfews** to set sleep hours.
4. **Switch to Child Mode:** Ensure Child Mode is toggled **ON** on the main dashboard before handing the device to your child.

---

## 🏗️ Architecture & Tech Stack

```
com.safeguard.parentalcontrol/
├── data/
│   ├── local/              # Room Database (App rules, blocked sites, logs, schedules)
│   ├── preferences/        # PIN hashing & master toggles
│   └── repository/         # Single source of truth for business logic
├── service/
│   ├── AppMonitorAccessibilityService.kt  # Intercepts Chrome URLs & foreground apps
│   ├── GuardianForegroundService.kt       # Persistent background guardian service
│   ├── SafeGuardDeviceAdminReceiver.kt     # Anti-uninstall policy handler
│   └── BootCompletedReceiver.kt           # Restarts protection on device reboot
├── ui/
│   ├── dashboard/          # Parent Center with stats & activity feed
│   ├── webfilter/          # Chrome web blocker management
│   ├── applimits/          # Time limits & app toggles
│   ├── schedule/           # Curfew & downtime scheduler
│   ├── overlay/            # Fullscreen interactive blocker
│   └── security/           # Master PIN dialog & verification
└── util/
    ├── BrowserUrlDetector.kt  # Chrome URL bar node inspector
    ├── WebsiteFilterEngine.kt # Domain & keyword evaluation
    ├── PermissionHelper.kt    # Android permission status & intents
    └── UsageStatsHelper.kt    # OS screen time computations
```

- **Kotlin:** 2.0.20
- **Jetpack Compose:** Material 3, Navigation Compose
- **Room Database:** 2.6.1 with KSP
- **Target SDK:** 35 (Android 15)
- **Min SDK:** 26 (Android 8.0 Oreo)

---

## 🤖 Automated CI/CD Pipeline (GitHub Actions)

This repository includes `.github/workflows/build-and-release.yml`:
- Compiles the application automatically on Ubuntu GitHub Actions runners with Temurin JDK 17.
- Generates signed/debug `.apk` assets.
- Automatically publishes downloadable APKs under GitHub Releases whenever code is tagged or manually triggered.

---

## 📄 License
Licensed under the [Apache License, Version 2.0](LICENSE).
