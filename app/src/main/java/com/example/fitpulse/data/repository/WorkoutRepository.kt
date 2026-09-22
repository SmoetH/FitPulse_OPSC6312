package com.example.fitpulse.data.repository

import com.example.fitpulse.data.Workout
import com.example.fitpulse.data.local.WorkoutDao
import com.example.fitpulse.data.local.WorkoutEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow

class WorkoutRepository(private val workoutDao: WorkoutDao) {

    // --- 1. Local Room Operations ---
    val allWorkouts: Flow<List<WorkoutEntity>> = workoutDao.getAllWorkouts()

    suspend fun insert(workout: WorkoutEntity) {
        workoutDao.insertWorkout(workout)
    }

    suspend fun delete(workoutId: Int) {
        workoutDao.deleteWorkoutById(workoutId)
    }

    // --- 2. Cloud Firestore Operations ---
    private val db = FirebaseFirestore.getInstance()
    private val userId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

    fun syncWorkoutToCloud(workout: Workout, onComplete: (Boolean, String?) -> Unit) {
        if (userId.isEmpty()) {
            onComplete(false, "User not logged in.")
            return
        }

        db.collection("users")
            .document(userId)
            .collection("workouts")
            .add(workout)
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { e -> onComplete(false, e.localizedMessage) }
    }

    fun observeCloudWorkouts(
        onData: (List<Workout>) -> Unit,
        onError: (String) -> Unit
    ): ListenerRegistration? {
        if (userId.isEmpty()) {
            onError("User not logged in.")
            return null
        }

        return db.collection("users")
            .document(userId)
            .collection("workouts")
            .orderBy("dateTimeStamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error.localizedMessage ?: "Firestore error")
                    return@addSnapshotListener
                }

                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Workout::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                onData(list)
            }
    }
}