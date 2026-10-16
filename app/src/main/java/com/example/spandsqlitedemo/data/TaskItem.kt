package com.example.spandsqlitedemo.data

data class TaskItem(
    val id: Long = 0,
    val title: String,
    val description: String,
    val category: String, // "Work", "Personal", "Fitness", "Ideas"
    val priority: String, // "High", "Medium", "Low"
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
