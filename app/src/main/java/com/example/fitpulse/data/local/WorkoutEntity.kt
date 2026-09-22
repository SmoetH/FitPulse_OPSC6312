package com.example.fitpulse.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,sm
    val title: String,
    val category: String = "General",
    val durationMinutes: Int,
    val caloriesBurned: Int,
    val sets: Int = 0,
    val reps: Int = 0,
    val weight: Double = 0.0,
    val dateTimestamp: Long = System.currentTimeMillis()
)