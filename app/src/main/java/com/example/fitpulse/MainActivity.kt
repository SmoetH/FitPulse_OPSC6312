package com.example.fitpulse

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.fitpulse.data.local.AppDatabase
import com.example.fitpulse.data.local.WorkoutEntity
import com.example.fitpulse.data.repository.WorkoutRepository
import com.example.fitpulse.ui.LoginActivity
import com.example.fitpulse.ui.WorkoutAdapter
import com.example.fitpulse.ui.WorkoutViewModel
import com.example.fitpulse.ui.WorkoutViewModelFactory
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private val viewModel: WorkoutViewModel by viewModels {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = WorkoutRepository(database.workoutDao())
        WorkoutViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Setup Toolbar as Action Bar
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Initialize Firebase Auth
        auth = Firebase.auth

        val tvTotalMinutes = findViewById<TextView>(R.id.tvTotalMinutes)
        val tvTotalCalories = findViewById<TextView>(R.id.tvTotalCalories)
        val tvEmptyState = findViewById<TextView>(R.id.tvEmptyState)
        val recyclerView = findViewById<RecyclerView>(R.id.rvWorkouts)

        // Setup Adapter with item click listener to show details
        val adapter = WorkoutAdapter { workout ->
            showWorkoutDetailDialog(workout)
        }
        recyclerView.adapter = adapter

        // Setup Swipe-to-Delete functionality with bounds safety
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                if (position != RecyclerView.NO_POSITION && position < adapter.currentList.size) {
                    val workoutToDelete = adapter.currentList[position]
                    viewModel.deleteWorkout(workoutToDelete.id)
                }
            }
        })
        itemTouchHelper.attachToRecyclerView(recyclerView)

        val fabAddWorkout = findViewById<FloatingActionButton>(R.id.fabAddWorkout)
        fabAddWorkout.setOnClickListener {
            showAddWorkoutDialog()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.allWorkouts.collect { workouts ->
                    adapter.submitList(workouts)

                    // Update empty state view visibility
                    if (workouts.isEmpty()) {
                        tvEmptyState.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    } else {
                        tvEmptyState.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                    }

                    // Update summary dashboard card
                    val totalMins = workouts.sumOf { it.durationMinutes }
                    val totalCals = workouts.sumOf { it.caloriesBurned }
                    tvTotalMinutes.text = getString(R.string.duration_format, totalMins)
                    tvTotalCalories.text = getString(R.string.calories_format, totalCals)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                performLogout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun performLogout() {
        auth.signOut()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showWorkoutDetailDialog(workout: WorkoutEntity) {
        AlertDialog.Builder(this)
            .setTitle(workout.title)
            .setMessage(
                "Category: ${workout.category}\n" +
                        "Duration: ${workout.durationMinutes} mins\n" +
                        "Calories: ${workout.caloriesBurned} kcal\n\n" +
                        "Sets: ${workout.sets}\n" +
                        "Reps: ${workout.reps}\n" +
                        "Weight: ${workout.weight} kg"
            )
            .setPositiveButton("Close", null)
            .show()
    }

    private fun showAddWorkoutDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_workout, null)

        val spCategory = dialogView.findViewById<Spinner>(R.id.spCategory)
        val etTitle = dialogView.findViewById<EditText>(R.id.etTitle)
        val etSets = dialogView.findViewById<EditText>(R.id.etSets)
        val etReps = dialogView.findViewById<EditText>(R.id.etReps)
        val etWeight = dialogView.findViewById<EditText>(R.id.etWeight)
        val etDuration = dialogView.findViewById<EditText>(R.id.etDuration)
        val etCalories = dialogView.findViewById<EditText>(R.id.etCalories)

        val categories = arrayOf("Strength", "Cardio", "Flexibility", "HIIT")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
        spCategory.adapter = spinnerAdapter

        val dialog = AlertDialog.Builder(this)
            .setTitle("Add New Workout")
            .setView(dialogView)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val title = etTitle.text.toString().trim()
            val durationText = etDuration.text.toString().trim()
            val caloriesText = etCalories.text.toString().trim()

            if (title.isEmpty()) {
                etTitle.error = "Title is required"
                etTitle.requestFocus()
                return@setOnClickListener
            }

            if (durationText.isEmpty()) {
                etDuration.error = "Duration is required"
                etDuration.requestFocus()
                return@setOnClickListener
            }

            val category = spCategory.selectedItem.toString()
            val sets = etSets.text.toString().toIntOrNull() ?: 0
            val reps = etReps.text.toString().toIntOrNull() ?: 0
            val weight = etWeight.text.toString().toDoubleOrNull() ?: 0.0
            val duration = durationText.toIntOrNull() ?: 0
            val calories = caloriesText.toIntOrNull() ?: 0

            viewModel.addWorkout(
                title = title,
                durationMinutes = duration,
                caloriesBurned = calories,
                category = category,
                sets = sets,
                reps = reps,
                weight = weight
            )

            dialog.dismiss()
        }
    }
}