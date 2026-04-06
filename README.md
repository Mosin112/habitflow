# HabitFlow — Android Habit Tracker

A production-ready Android habit tracker built with **Kotlin + Jetpack Compose + MVVM + Room + Hilt**.

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────┐
│                    Presentation Layer                    │
│  Screens (Compose)  ←→  ViewModels (StateFlow + Hilt)  │
├─────────────────────────────────────────────────────────┤
│                      Domain Layer                        │
│        Models  ·  Repository Interfaces  ·  UseCases   │
├─────────────────────────────────────────────────────────┤
│                       Data Layer                         │
│   Room (SQLite)  ·  DataStore  ·  Mock Auth Service     │
└─────────────────────────────────────────────────────────┘
```

**Tech Stack:**
| Layer | Technology |
|-------|-----------|
| UI | Jetpack Compose + Material3 |
| Architecture | MVVM + Clean Architecture |
| DI | Hilt (KSP) |
| Database | Room 2.6 |
| Preferences | DataStore |
| Navigation | Compose Navigation |
| State | StateFlow + collectAsState |
| Async | Kotlin Coroutines + Flow |

---

## App Structure

```
app/src/main/java/com/habitflow/app/
├── HabitFlowApp.kt               @HiltAndroidApp entry point
├── MainActivity.kt               Single activity, handles session check + splash
│
├── data/
│   ├── local/
│   │   ├── dao/                  HabitDao, HabitEntryDao (Room DAOs)
│   │   ├── database/             HabitDatabase.kt
│   │   ├── entity/               HabitEntity, HabitEntryEntity
│   │   └── mapper/               Entity ↔ Domain model converters
│   ├── mock/
│   │   └── MockOtpService.kt     Simulates email OTP delivery
│   ├── preferences/
│   │   └── SessionManager.kt     DataStore-backed auth session
│   └── repository/
│       ├── AuthRepositoryImpl.kt
│       └── HabitRepositoryImpl.kt (streak calculation logic)
│
├── domain/
│   ├── model/                    Habit, HabitEntry, HabitType, User, AnalyticsData
│   └── repository/               Pure Kotlin interfaces
│
├── di/
│   ├── DatabaseModule.kt         Provides Room DB + DAOs
│   └── RepositoryModule.kt       Binds interface → impl
│
├── presentation/
│   ├── theme/                    Color.kt, Theme.kt, Type.kt (dark-first design)
│   ├── navigation/               NavGraph.kt, Screen.kt
│   ├── components/               HabitCard, WeekCalendar, ProgressRing, StreakBadge, EmptyState
│   ├── auth/                     Login, SignUp, OTP screens + AuthViewModel
│   ├── home/                     HomeScreen + HomeViewModel
│   ├── habit/                    AddEditHabitScreen + ViewModel
│   ├── analytics/                AnalyticsScreen + ViewModel
│   └── profile/                  ProfileScreen + ViewModel
│
└── util/
    └── Result.kt                 Sealed class for Success/Error/Loading
```

---

## UX Flow

```
App Launch
    ├── Session exists → Home Screen
    └── No session    → Login Screen
                              │
                        ┌─────┴────┐
                     Login        Sign Up
                        │              │
                   [email+pw]    [name+email+pw]
                        │              │
                   Home Screen    OTP Screen
                                       │
                                  Home Screen

Home Screen (bottom nav: Today · Insights · Profile)
    ├── Week Calendar with color-coded completion dots
    ├── Daily progress ring (% complete today)
    ├── Habit list for selected day
    │     ├── Check-in habit: tap ✓ button to complete
    │     └── Numeric habit:  [−  5  +] stepper + progress bar
    └── Long press habit → Edit / Delete dialog

Add/Edit Habit Screen
    ├── Name, Type (Check-in | Numeric)
    ├── Goal + Unit (numeric only)
    ├── Days of week frequency picker
    ├── Icon selector (emoji grid)
    └── Color picker (9 preset colors)

Insights Screen
    ├── Today completion ring
    ├── Weekly bar chart (Mon–Sun)
    └── Per-habit breakdown: streak, 30-day rate, best streak

Profile Screen
    └── User info + logout
```

---

## Habit Types

| Type | UI | Completion |
|------|-----|-----------|
| **Check-in** | Checkbox button with bounce animation | Tap to toggle |
| **Numeric** | Stepper (−/+) with progress bar | Goal reached or any entry |

---

## How to Run

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 34
- Gradle 8.2+

### Steps

```bash
# 1. Open in Android Studio
File → Open → select HabitFlow/

# 2. Sync Gradle
Android Studio will auto-sync; or: ./gradlew --stop && ./gradlew build

# 3. Run
# Connect a device (API 26+) or create an emulator, then press ▶

# 4. Sign up flow
#    - Create account with any email + password (≥6 chars)
#    - On OTP screen, the 6-digit code is shown in an amber hint box
#    - Enter the code and tap Verify
```

### Command line build
```bash
cd HabitFlow
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

---

## Authentication (Demo Mode)

OTP verification is mocked locally — no real email is sent.
The generated OTP is displayed on-screen in an amber hint banner for demo convenience.

In production, replace `MockOtpService` with your preferred provider:
- **Firebase Auth** (recommended)
- **Twilio Verify**
- **AWS SNS**

---

## Key Design Decisions

### Dark-First Theme
Indigo (#6366F1) primary on near-black (#0A0A14) background — looks premium and reduces eye strain.

### Streak Algorithm
Streaks are calculated in `HabitRepositoryImpl.calculateStreak()`:
- If today is scheduled and NOT YET completed → doesn't break the streak (might complete later)
- If any past scheduled day was missed → streak resets to 0
- Efficient: only queries last 365 days

### One-handed UX
- All primary actions reachable with right thumb
- Bottom nav + FAB in top-right (easy reach zone)
- Large tap targets (min 40dp) on all interactive elements

### Offline-First
All data lives in Room. Zero network dependency except for auth (mocked locally).

---

## Extending the App

| Feature | Where to add |
|---------|-------------|
| Real email OTP | Replace `MockOtpService` |
| Cloud sync | Add a `RemoteDataSource` + sync in `HabitRepositoryImpl` |
| Notifications | `WorkManager` + `NotificationCompat` |
| Widgets | `GlanceAppWidget` reading from Room |
| Dark/light toggle | Add a preference in `SessionManager` + read in `Theme.kt` |
