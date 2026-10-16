package com.example.spandsqlitedemo.ui

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.spandsqlitedemo.R
import com.example.spandsqlitedemo.data.TaskItem
import com.example.spandsqlitedemo.databinding.ItemTaskBinding
import com.example.spandsqlitedemo.databinding.ItemTaskCompactBinding

class TaskAdapter(
    private val context: Context,
    var isCompactMode: Boolean = false,
    private val onToggleCompleted: (TaskItem, Boolean) -> Unit,
    private val onEditClick: (TaskItem) -> Unit,
    private val onDeleteClick: (TaskItem) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items = listOf<TaskItem>()

    companion object {
        private const val VIEW_TYPE_STANDARD = 0
        private const val VIEW_TYPE_COMPACT = 1
    }

    fun submitList(newList: List<TaskItem>) {
        items = newList
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (isCompactMode) VIEW_TYPE_COMPACT else VIEW_TYPE_STANDARD
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_COMPACT) {
            val binding = ItemTaskCompactBinding.inflate(inflater, parent, false)
            CompactViewHolder(binding)
        } else {
            val binding = ItemTaskBinding.inflate(inflater, parent, false)
            StandardViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        if (holder is StandardViewHolder) {
            holder.bind(item)
        } else if (holder is CompactViewHolder) {
            holder.bind(item)
        }
    }

    override fun getItemCount(): Int = items.size

    inner class StandardViewHolder(private val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TaskItem) {
            binding.tvTitle.text = item.title
            binding.tvDescription.text = item.description
            binding.tvDescription.visibility = if (item.description.isBlank()) View.GONE else View.VISIBLE
            binding.tvCategory.text = item.category
            binding.tvPriority.text = item.priority

            // Set Checkbox state without triggering listener loop
            binding.cbCompleted.setOnCheckedChangeListener(null)
            binding.cbCompleted.isChecked = item.isCompleted

            // Strikethrough text if completed
            if (item.isCompleted) {
                binding.tvTitle.paintFlags = binding.tvTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                binding.tvTitle.alpha = 0.5f
                binding.tvDescription.alpha = 0.5f
            } else {
                binding.tvTitle.paintFlags = binding.tvTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                binding.tvTitle.alpha = 1.0f
                binding.tvDescription.alpha = 1.0f
            }

            // Priority Badge Colors
            when (item.priority) {
                "High" -> {
                    binding.tvPriority.setBackgroundResource(R.drawable.bg_pill_high)
                    binding.tvPriority.setTextColor(ContextCompat.getColor(context, R.color.priority_high_text))
                }
                "Medium" -> {
                    binding.tvPriority.setBackgroundResource(R.drawable.bg_pill_medium)
                    binding.tvPriority.setTextColor(ContextCompat.getColor(context, R.color.priority_medium_text))
                }
                else -> {
                    binding.tvPriority.setBackgroundResource(R.drawable.bg_pill_low)
                    binding.tvPriority.setTextColor(ContextCompat.getColor(context, R.color.priority_low_text))
                }
            }

            // Category Colors
            when (item.category) {
                "Work" -> {
                    binding.tvCategory.setBackgroundColor(ContextCompat.getColor(context, R.color.cat_work_bg))
                    binding.tvCategory.setTextColor(ContextCompat.getColor(context, R.color.cat_work_text))
                }
                "Personal" -> {
                    binding.tvCategory.setBackgroundColor(ContextCompat.getColor(context, R.color.cat_personal_bg))
                    binding.tvCategory.setTextColor(ContextCompat.getColor(context, R.color.cat_personal_text))
                }
                "Fitness" -> {
                    binding.tvCategory.setBackgroundColor(ContextCompat.getColor(context, R.color.cat_fitness_bg))
                    binding.tvCategory.setTextColor(ContextCompat.getColor(context, R.color.cat_fitness_text))
                }
                else -> {
                    binding.tvCategory.setBackgroundColor(ContextCompat.getColor(context, R.color.cat_ideas_bg))
                    binding.tvCategory.setTextColor(ContextCompat.getColor(context, R.color.cat_ideas_text))
                }
            }

            binding.cbCompleted.setOnCheckedChangeListener { _, isChecked ->
                onToggleCompleted(item, isChecked)
            }

            binding.btnEdit.setOnClickListener { onEditClick(item) }
            binding.btnDelete.setOnClickListener { onDeleteClick(item) }
        }
    }

    inner class CompactViewHolder(private val binding: ItemTaskCompactBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TaskItem) {
            binding.tvTitleCompact.text = item.title
            binding.tvCategoryCompact.text = item.category

            binding.cbCompletedCompact.setOnCheckedChangeListener(null)
            binding.cbCompletedCompact.isChecked = item.isCompleted

            if (item.isCompleted) {
                binding.tvTitleCompact.paintFlags = binding.tvTitleCompact.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                binding.tvTitleCompact.alpha = 0.5f
            } else {
                binding.tvTitleCompact.paintFlags = binding.tvTitleCompact.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                binding.tvTitleCompact.alpha = 1.0f
            }

            binding.cbCompletedCompact.setOnCheckedChangeListener { _, isChecked ->
                onToggleCompleted(item, isChecked)
            }

            binding.btnDeleteCompact.setOnClickListener { onDeleteClick(item) }
        }
    }
}
