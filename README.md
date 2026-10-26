# JobAutoApply 🚀
### Smart Autonomous Job Application Android App for Instahyre & LinkedIn Easy Apply

A native Android application built with **Kotlin** and **Jetpack Compose (Material 3)** featuring an in-app WebView automation engine. Designed to autonomously browse, filter, and apply to job opportunities on **Instahyre** and **LinkedIn Easy Apply** with human-like delays, anti-bot safety pacing, and configurable profile answers.

---

## 📱 Key Features

- **Multi-Platform Support**:
  - **Instahyre**: Automatically navigates candidate feed, checks match score, handles "Apply", applies to bulk recommended similar jobs, and skips external career portals.
  - **LinkedIn Easy Apply**: Finds Easy Apply opportunities, fills phone number, auto-answers work authorization & visa sponsorship questions, enters experience years and notice period, reviews and submits applications.
- **In-App WebView Automation Engine**:
  - Secure and anti-bot resistant: runs directly inside an Android WebView maintaining user cookies and session tokens.
  - Bi-directional `@JavascriptInterface` bridge between Kotlin and injected JS scripts (`instahyre_engine.js`, `linkedin_engine.js`).
- **Human Jitter & Safety Pacing**:
  - Configurable minimum/maximum delay timers (e.g., 2.5s to 5.5s) to avoid rate limits and simulate natural human interaction.
  - Max session limits (e.g., stop after 30 applications).
- **Candidate Answer Vault**:
  - Save standard answers for screening questions: Full Name, Phone, Total Experience, Notice Period, Current & Expected CTC, Work Authorization.
- **Real-Time Event Stream**:
  - Live console logs streaming every action, button click, skip reason, and status update.
- **Persistent Application History**:
  - Records every applied, skipped, or failed job with timestamps, company, and role.
- **Sleek Modern UI**:
  - Dark mode aesthetic built with Jetpack Compose, Material 3, and edge-to-edge support.

---

## 🛠️ Tech Stack & Architecture

- **Language**: Kotlin 1.9.24
- **UI Toolkit**: Jetpack Compose & Material 3
- **Navigation**: Jetpack Compose Navigation
- **Automation Engine**: Android WebKit + Custom Injected JavaScript Engine
- **JSON Serialization**: Google Gson
- **Storage**: SharedPreferences & Kotlin Coroutines StateFlow
- **Minimum SDK**: Android 8.0 (API 26)
- **Target SDK**: Android 14 (API 34)

---

## 📂 Project Structure

```
JobAutoApply/
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── assets/
│       │   ├── instahyre_engine.js     # Instahyre automation logic
│       │   └── linkedin_engine.js      # LinkedIn Easy Apply automation logic
│       ├── java/com/jobautoapply/app/
│       │   ├── MainActivity.kt         # Jetpack Compose Navigation & Scaffold
│       │   ├── data/
│       │   │   ├── model/              # JobApplication, UserProfile, AppSettings
│       │   │   └── repository/         # AppPreferences & history storage
│       │   ├── automation/
│       │   │   ├── AutomationManager.kt # WebView lifecycle & script injector
│       │   │   ├── AutomationState.kt   # State models & status enums
│       │   │   └── JobApplyBridge.kt    # @JavascriptInterface bridge
│       │   └── ui/
│       │       ├── theme/               # Dark Color, Typography & Theme
│       │       ├── components/          # StatCard, LogListView
│       │       └── screens/             # Dashboard, WebView, Profile, History, Settings
│       └── res/                         # Strings, colors, adaptive icons
├── gradle/wrapper/
├── build.gradle.kts
├── settings.gradle.kts
└── local.properties
```

---

## 🚀 How to Run

### Option 1: Run Directly on Windows Desktop (Standalone App)
Double-click `JobAutoApply.bat` in the project root:
```powershell
c:\Users\sahyadri\.gemini\antigravity-ide\scratch\JobAutoApply\JobAutoApply.bat
```
Or open [desktop/index.html](file:///c:/Users/sahyadri/.gemini/antigravity-ide/scratch/JobAutoApply/desktop/index.html) in your browser.
- **1-Click Bookmarklet**: Drag the `⚡ AutoApply Bot` button to your Chrome/Edge bookmarks bar to automate active LinkedIn or Instahyre tabs with a floating live HUD.
- **Candidate Answers Vault**: Edit and persist all screening answers (phone, experience, notice period, CTC, work authorization).
- **History Tracker**: Export applied jobs to CSV.

---

### Option 2: Run the Android App (Phone or Studio)
1. Open **Android Studio** and select `c:\Users\sahyadri\.gemini\antigravity-ide\scratch\JobAutoApply`.
2. Connect your Android phone with USB Debugging enabled (or launch an AVD).
3. Click **Run 'app'** (`Shift + F10`) or install the built APK directly:
   ```powershell
   & "E:\Android\platform-tools\adb.exe" install -r "c:\Users\sahyadri\.gemini\antigravity-ide\scratch\JobAutoApply\app\build\outputs\apk\debug\app-debug.apk"
   ```

