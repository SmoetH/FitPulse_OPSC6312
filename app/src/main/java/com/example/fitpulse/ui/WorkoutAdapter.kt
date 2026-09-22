package com.example.fitpulse.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.fitpulse.R
import com.example.fitpulse.data.local.WorkoutEntity

class WorkoutAdapter(
    private val onItemClick: (WorkoutEntity) -> Unit
) : ListAdapter<WorkoutEntity, WorkoutAdapter.WorkoutViewHolder>(WorkoutDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_workout, parent, false)
        return WorkoutViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    class WorkoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvDuration: TextView = itemView.findViewById(R.id.tvDuration)
        private val tvCalories: TextView = itemView.findViewById(R.id.tvCalories)

        fun bind(workout: WorkoutEntity) {
            val context = itemView.context
            tvTitle.text = workout.title
            tvDuration.text = context.getString(R.string.duration_format, workout.durationMinutes)
            tvCalories.text = context.getString(R.string.calories_format, workout.caloriesBurned)
        }
    }

    class WorkoutDiffCallback : DiffUtil.ItemCallback<WorkoutEntity>() {
        override fun areItemsTheSame(oldItem: WorkoutEntity, newItem: WorkoutEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: WorkoutEntity, newItem: WorkoutEntity): Boolean {
            return oldItem == newItem
        }
    }
}