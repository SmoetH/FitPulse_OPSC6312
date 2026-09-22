package com.example.fitpulse.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fitpulse.data.Workout
import com.example.fitpulse.data.local.WorkoutEntity
import com.example.fitpulse.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WorkoutViewModel(private val repository: WorkoutRepository) : ViewModel() {

    val allWorkouts: StateFlow<List<WorkoutEntity>> = repository.allWorkouts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addWorkout(
        title: String,
        durationMinutes: Int,
        caloriesBurned: Int,
        category: String = "Strength",
        sets: Int = 0,
        reps: Int = 0,
        weight: Double = 0.0
    ) {
        viewModelScope.launch {
            // 1. Insert into local Room database
            val localWorkout = WorkoutEntity(
                title = title,
                category = category,
                durationMinutes = durationMinutes,
                caloriesBurned = caloriesBurned,
                sets = sets,
                reps = reps,
                weight = weight
            )
            repository.insert(localWorkout)

            // 2. Sync to Firebase Firestore cloud database
            val cloudWorkout = Workout(
                title = title,
                category = category,
                sets = sets,
                reps = reps,
                weight = weight,
                durationMinutes = durationMinutes,
                caloriesBurned = caloriesBurned
            )
            repository.syncWorkoutToCloud(cloudWorkout) { success, _ ->
                // Cloud sync complete callback
            }
        }
    }

    fun deleteWorkout(workoutId: Int) {
        viewModelScope.launch {
            repository.delete(workoutId)
        }
    }
}

class WorkoutViewModelFactory(private val repository: WorkoutRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkoutViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WorkoutViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}