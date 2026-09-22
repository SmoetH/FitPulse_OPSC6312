package com.example.fitpulse

import android.app.Application
import com.example.fitpulse.data.local.AppDatabase
import com.example.fitpulse.data.repository.WorkoutRepository

class FitPulseApp : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { WorkoutRepository(database.workoutDao()) }
}