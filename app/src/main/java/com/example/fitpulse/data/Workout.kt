package com.example.fitpulse.data

data class Workout(
    val id: String = "",
    val title: String = "",
    val category: String = "Strength", // Cardio, Strength, Flexibility, HIIT
    val sets: Int = 0,
    val reps: Int = 0,
    val weight: Double = 0.0,
    val durationMinutes: Int = 0,
    val caloriesBurned: Int = 0,
    val dateTimeStamp: Long = System.currentTimeMillis()
) {
    // Calculates total weight moved for Volume Metrics requirements
    val totalVolume: Double
        get() = sets * reps * weight
}