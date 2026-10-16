package com.example.spandsqlitedemo.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spandsqlitedemo.R
import com.example.spandsqlitedemo.data.DatabaseHelper
import com.example.spandsqlitedemo.data.PreferenceManager
import com.example.spandsqlitedemo.data.TaskItem
import com.example.spandsqlitedemo.databinding.DialogAddEditTaskBinding
import com.example.spandsqlitedemo.databinding.DialogSqliteInspectorBinding
import com.example.spandsqlitedemo.databinding.FragmentTasksBinding
import com.google.android.material.chip.Chip

class TasksFragment : Fragment() {

    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var prefManager: PreferenceManager
    private lateinit var taskAdapter: TaskAdapter

    private var currentCategoryFilter: String = "All"
    private var currentSearchQuery: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTasksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbHelper = DatabaseHelper(requireContext())
        prefManager = PreferenceManager(requireContext())

        // Seed default demo tasks into SQLite if empty
        dbHelper.seedDefaultDataIfEmpty()

        setupRecyclerView()
        setupSearchAndFilters()
        setupListeners()
        refreshTasksFromDatabase()
    }

    override fun onResume() {
        super.onResume()
        // Check if layout mode (compact vs standard) changed in SharedPreferences
        if (::taskAdapter.isInitialized && taskAdapter.isCompactMode != prefManager.isCompactView) {
            taskAdapter.isCompactMode = prefManager.isCompactView
            refreshTasksFromDatabase()
        }
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(
            context = requireContext(),
            isCompactMode = prefManager.isCompactView,
            onToggleCompleted = { task, isCompleted ->
                dbHelper.toggleTaskCompletion(task.id, isCompleted)
                refreshTasksFromDatabase()
            },
            onEditClick = { task ->
                showAddEditTaskDialog(task)
            },
            onDeleteClick = { task ->
                showDeleteConfirmationDialog(task)
            }
        )

        binding.rvTasks.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = taskAdapter
        }
    }

    private fun setupSearchAndFilters() {
        // Live Search Input Listener
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearchQuery = s?.toString()?.trim() ?: ""
                refreshTasksFromDatabase()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Category Chip Group Selection Listener
        binding.chipGroupCategory.setOnCheckedStateChangeListener { group, checkedIds ->
            currentCategoryFilter = if (checkedIds.isNotEmpty()) {
                val chip = group.findViewById<Chip>(checkedIds[0])
                chip?.text?.toString() ?: "All"
            } else {
                "All"
            }
            refreshTasksFromDatabase()
        }
    }

    private fun setupListeners() {
        binding.fabAddTask.setOnClickListener {
            showAddEditTaskDialog(null)
        }

        binding.btnSqliteInspector.setOnClickListener {
            showSqliteInspectorDialog()
        }
    }

    fun refreshTasksFromDatabase() {
        val tasks = dbHelper.getAllTasks(
            searchQuery = currentSearchQuery,
            categoryFilter = currentCategoryFilter,
            autoSortCompletedBottom = prefManager.isAutoSortCompleted
        )

        taskAdapter.submitList(tasks)

        // Show/Hide Empty State
        if (tasks.isEmpty()) {
            binding.layoutEmpty.visibility = View.VISIBLE
            binding.rvTasks.visibility = View.GONE
        } else {
            binding.layoutEmpty.visibility = View.GONE
            binding.rvTasks.visibility = View.VISIBLE
        }

        // Update Overview Stats Card
        val stats = dbHelper.getStats()
        binding.tvTotalCount.text = stats.totalTasks.toString()
        binding.tvCompletedCount.text = stats.completedTasks.toString()
        binding.tvHighPriorityCount.text = stats.highPriorityTasks.toString()

        val progressPercent = if (stats.totalTasks > 0) {
            (stats.completedTasks * 100) / stats.totalTasks
        } else {
            0
        }
        binding.progressTasks.progress = progressPercent
        binding.tvProgressSubtitle.text = "$progressPercent% completed (${stats.completedTasks} of ${stats.totalTasks} tasks)"
    }

    private fun showAddEditTaskDialog(existingTask: TaskItem?) {
        val dialogBinding = DialogAddEditTaskBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        if (existingTask != null) {
            dialogBinding.tvDialogTitle.text = "Edit Task in SQLite"
            dialogBinding.etTaskTitle.setText(existingTask.title)
            dialogBinding.etTaskDesc.setText(existingTask.description)

            // Prefill category chip
            when (existingTask.category) {
                "Personal" -> dialogBinding.chipDialogPersonal.isChecked = true
                "Fitness" -> dialogBinding.chipDialogFitness.isChecked = true
                "Ideas" -> dialogBinding.chipDialogIdeas.isChecked = true
                else -> dialogBinding.chipDialogWork.isChecked = true
            }

            // Prefill priority chip
            when (existingTask.priority) {
                "High" -> dialogBinding.chipPriorityHigh.isChecked = true
                "Low" -> dialogBinding.chipPriorityLow.isChecked = true
                else -> dialogBinding.chipPriorityMedium.isChecked = true
            }
        } else {
            dialogBinding.tvDialogTitle.text = "New Task (Insert to SQLite)"
            // Pre-select default category from SharedPreferences!
            when (prefManager.defaultCategory) {
                "Personal" -> dialogBinding.chipDialogPersonal.isChecked = true
                "Fitness" -> dialogBinding.chipDialogFitness.isChecked = true
                "Ideas" -> dialogBinding.chipDialogIdeas.isChecked = true
                else -> dialogBinding.chipDialogWork.isChecked = true
            }
        }

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnSave.setOnClickListener {
            val title = dialogBinding.etTaskTitle.text?.toString()?.trim() ?: ""
            val desc = dialogBinding.etTaskDesc.text?.toString()?.trim() ?: ""

            if (title.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a task title", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val category = when (dialogBinding.cgDialogCategory.checkedChipId) {
                R.id.chipDialogPersonal -> "Personal"
                R.id.chipDialogFitness -> "Fitness"
                R.id.chipDialogIdeas -> "Ideas"
                else -> "Work"
            }

            val priority = when (dialogBinding.cgDialogPriority.checkedChipId) {
                R.id.chipPriorityHigh -> "High"
                R.id.chipPriorityLow -> "Low"
                else -> "Medium"
            }

            if (existingTask != null) {
                val updatedTask = existingTask.copy(
                    title = title,
                    description = desc,
                    category = category,
                    priority = priority
                )
                dbHelper.updateTask(updatedTask)
                Toast.makeText(requireContext(), "Task updated in SQLite DB", Toast.LENGTH_SHORT).show()
            } else {
                val newTask = TaskItem(
                    title = title,
                    description = desc,
                    category = category,
                    priority = priority,
                    isCompleted = false
                )
                dbHelper.insertTask(newTask)
                Toast.makeText(requireContext(), "New task inserted into SQLite DB", Toast.LENGTH_SHORT).show()
            }

            dialog.dismiss()
            refreshTasksFromDatabase()
        }

        dialog.show()
    }

    private fun showDeleteConfirmationDialog(task: TaskItem) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Task")
            .setMessage("Are you sure you want to delete '${task.title}' from SQLite database?")
            .setPositiveButton("Delete") { _, _ ->
                dbHelper.deleteTask(task.id)
                Toast.makeText(requireContext(), "Deleted task row from SQLite", Toast.LENGTH_SHORT).show()
                refreshTasksFromDatabase()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showSqliteInspectorDialog() {
        val inspectorBinding = DialogSqliteInspectorBinding.inflate(layoutInflater)
        val stats = dbHelper.getStats()
        val allTasks = dbHelper.getAllTasks()

        val infoBuilder = StringBuilder()
        infoBuilder.append("=== DATABASE METADATA ===\n")
        infoBuilder.append("Database File: ${stats.dbName}\n")
        infoBuilder.append("Version: ${stats.dbVersion}\n")
        infoBuilder.append("Size: ${stats.dbSizeBytes} Bytes\n\n")

        infoBuilder.append("=== TABLE: tasks ===\n")
        infoBuilder.append("Total Rows: ${stats.totalTasks}\n")
        infoBuilder.append("Completed: ${stats.completedTasks}\n")
        infoBuilder.append("High Priority: ${stats.highPriorityTasks}\n\n")

        infoBuilder.append("=== RECENT ROWS RAW DATA ===\n")
        if (allTasks.isEmpty()) {
            infoBuilder.append("(No rows in table)\n")
        } else {
            allTasks.take(5).forEach { task ->
                infoBuilder.append("ID:${task.id} | [${task.category}] \"${task.title}\" | Prio:${task.priority} | Done:${task.isCompleted}\n")
            }
            if (allTasks.size > 5) {
                infoBuilder.append("... and ${allTasks.size - 5} more rows")
            }
        }

        inspectorBinding.tvSqliteDetails.text = infoBuilder.toString()

        val dialog = AlertDialog.Builder(requireContext())
            .setView(inspectorBinding.root)
            .create()

        inspectorBinding.btnCloseInspector.setOnClickListener {
            dialog.dismiss()
        }

        inspectorBinding.btnClearSqlite.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Clear SQLite Table")
                .setMessage("Delete all rows from SQLite tasks table?")
                .setPositiveButton("Clear All") { _, _ ->
                    dbHelper.deleteAllTasks()
                    Toast.makeText(requireContext(), "SQLite table cleared", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    refreshTasksFromDatabase()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
