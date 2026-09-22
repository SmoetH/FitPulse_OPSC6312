# FitPulse - OPSC6312 Part 2

## Student Information
- **Student Name:** Tiisetso Tshefofatso Monaisa
- **Student ID:** [Insert Student Number Here]
- **Module:** OPSC6312 - Open Source Development (Part 2)
- **Institution:** Rosebank College

---

## YouTube Video Demonstration
- **Video Link:** https://youtube.com/@officiallegarcon?si=Vvb7AwHeGkRTYy6x

---

## Project Overview
**FitPulse** is a modern Android fitness and workout tracking application designed to help users log, monitor, and manage their daily exercise routines efficiently. Built using **Kotlin**, **Room Database**, and the **MVVM (Model-View-ViewModel)** architectural pattern, FitPulse provides local persistent storage, smooth UI reactive data flow via StateFlow/LiveData, and full CRUD operations for workout management.

---

## Key Features & Functionality
- **User Authentication:** Secure local login and session management interface.
- **Workout Logging System:** Interactive "Add Workout" modal dialog supporting detailed exercise entries including:
  - Exercise Title & Category (e.g., General, Cardio, Strength)
  - Duration (minutes) and Estimated Calories Burned
  - Sets, Reps, and Weight lifted
  - Automated timestamp logging
- **Real-Time Data Persistence:** Utilizes Android's **Room ORM Database** for offline-first local data storage.
- **Reactive UI Rendering:** Implements `ViewModel` and flow architecture to update exercise lists in real time upon entry saving.
- **Logging & Debugging:** Detailed application logging (`Log.d()` / `Log.e()`) integrated into repository and database layers to trace data flow during execution.

---

## Tech Stack & Architecture
- **Language:** Kotlin
- **IDE:** Android Studio
- **Architecture Pattern:** MVVM (Model-View-ViewModel)
- **Database / Storage:** Room ORM Database (SQLite)
- **Asynchronous Execution:** Kotlin Coroutines & Flow / LiveData
- **UI Components:** Android Jetpack XML Layouts, Material Design, RecyclerView & Adapters

---

## Project Structure
```text
com.example.fitpulse/
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt       # Room Database configuration
│   │   ├── WorkoutDao.kt        # Data Access Object for CRUD queries
│   │   └── WorkoutEntity.kt     # Database entity representing workout items
│   └── repository/
│       └── WorkoutRepository.kt # Data management abstraction layer
├── ui/
│   ├── MainActivity.kt          # Primary dashboard & RecyclerView handler
│   ├── LoginActivity.kt         # User authentication view
│   └── WorkoutViewModel.kt      # ViewModel handling UI state & database tasks
└── adapter/
    └── WorkoutAdapter.kt        # RecyclerView adapter for displaying workout items
