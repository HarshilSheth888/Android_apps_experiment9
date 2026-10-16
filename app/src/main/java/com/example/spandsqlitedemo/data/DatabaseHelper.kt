package com.example.spandsqlitedemo.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.File

data class DatabaseStats(
    val dbName: String,
    val dbVersion: Int,
    val totalTasks: Int,
    val completedTasks: Int,
    val highPriorityTasks: Int,
    val dbSizeBytes: Long
)

class DatabaseHelper(private val context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "task_pulse_sqlite.db"
        const val DATABASE_VERSION = 1

        const val TABLE_TASKS = "tasks"
        const val COLUMN_ID = "_id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_DESC = "description"
        const val COLUMN_CATEGORY = "category"
        const val COLUMN_PRIORITY = "priority"
        const val COLUMN_IS_COMPLETED = "is_completed"
        const val COLUMN_CREATED_AT = "created_at"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_TASKS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TITLE TEXT NOT NULL,
                $COLUMN_DESC TEXT,
                $COLUMN_CATEGORY TEXT,
                $COLUMN_PRIORITY TEXT,
                $COLUMN_IS_COMPLETED INTEGER DEFAULT 0,
                $COLUMN_CREATED_AT INTEGER
            )
        """.trimIndent()
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TASKS")
        onCreate(db)
    }

    fun insertTask(task: TaskItem): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, task.title)
            put(COLUMN_DESC, task.description)
            put(COLUMN_CATEGORY, task.category)
            put(COLUMN_PRIORITY, task.priority)
            put(COLUMN_IS_COMPLETED, if (task.isCompleted) 1 else 0)
            put(COLUMN_CREATED_AT, task.createdAt)
        }
        return db.insert(TABLE_TASKS, null, values)
    }

    fun updateTask(task: TaskItem): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, task.title)
            put(COLUMN_DESC, task.description)
            put(COLUMN_CATEGORY, task.category)
            put(COLUMN_PRIORITY, task.priority)
            put(COLUMN_IS_COMPLETED, if (task.isCompleted) 1 else 0)
        }
        return db.update(TABLE_TASKS, values, "$COLUMN_ID = ?", arrayOf(task.id.toString()))
    }

    fun toggleTaskCompletion(id: Long, isCompleted: Boolean): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_IS_COMPLETED, if (isCompleted) 1 else 0)
        }
        return db.update(TABLE_TASKS, values, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }

    fun deleteTask(id: Long): Int {
        val db = writableDatabase
        return db.delete(TABLE_TASKS, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }

    fun deleteAllTasks(): Int {
        val db = writableDatabase
        return db.delete(TABLE_TASKS, null, null)
    }

    fun getAllTasks(searchQuery: String? = null, categoryFilter: String? = null, autoSortCompletedBottom: Boolean = true): List<TaskItem> {
        val taskList = mutableListOf<TaskItem>()
        val db = readableDatabase

        val selectionList = mutableListOf<String>()
        val selectionArgsList = mutableListOf<String>()

        if (!searchQuery.isNull_or_blank()) {
            selectionList.add("($COLUMN_TITLE LIKE ? OR $COLUMN_DESC LIKE ?)")
            selectionArgsList.add("%$searchQuery%")
            selectionArgsList.add("%$searchQuery%")
        }

        if (!categoryFilter.isNullOrBlank() && categoryFilter != "All") {
            selectionList.add("$COLUMN_CATEGORY = ?")
            selectionArgsList.add(categoryFilter)
        }

        val selection = if (selectionList.isNotEmpty()) selectionList.joinToString(" AND ") else null
        val selectionArgs = if (selectionArgsList.isNotEmpty()) selectionArgsList.toTypedArray() else null

        val orderBy = if (autoSortCompletedBottom) {
            "$COLUMN_IS_COMPLETED ASC, $COLUMN_CREATED_AT DESC"
        } else {
            "$COLUMN_CREATED_AT DESC"
        }

        val cursor = db.query(
            TABLE_TASKS,
            null,
            selection,
            selectionArgs,
            null,
            null,
            orderBy
        )

        cursor.use {
            if (it.moveToFirst()) {
                val idIndex = it.getColumnIndexOrThrow(COLUMN_ID)
                val titleIndex = it.getColumnIndexOrThrow(COLUMN_TITLE)
                val descIndex = it.getColumnIndexOrThrow(COLUMN_DESC)
                val catIndex = it.getColumnIndexOrThrow(COLUMN_CATEGORY)
                val prioIndex = it.getColumnIndexOrThrow(COLUMN_PRIORITY)
                val compIndex = it.getColumnIndexOrThrow(COLUMN_IS_COMPLETED)
                val createdIndex = it.getColumnIndexOrThrow(COLUMN_CREATED_AT)

                do {
                    val item = TaskItem(
                        id = it.getLong(idIndex),
                        title = it.getString(titleIndex),
                        description = it.getString(descIndex) ?: "",
                        category = it.getString(catIndex) ?: "Work",
                        priority = it.getString(prioIndex) ?: "Medium",
                        isCompleted = it.getInt(compIndex) == 1,
                        createdAt = it.getLong(createdIndex)
                    )
                    taskList.add(item)
                } while (it.moveToNext())
            }
        }
        return taskList
    }

    fun getStats(): DatabaseStats {
        val db = readableDatabase
        var total = 0
        var completed = 0
        var highPrio = 0

        val totalCursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_TASKS", null)
        totalCursor.use { if (it.moveToFirst()) total = it.getInt(0) }

        val completedCursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_TASKS WHERE $COLUMN_IS_COMPLETED = 1", null)
        completedCursor.use { if (it.moveToFirst()) completed = it.getInt(0) }

        val highPrioCursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_TASKS WHERE $COLUMN_PRIORITY = 'High'", null)
        highPrioCursor.use { if (it.moveToFirst()) highPrio = it.getInt(0) }

        val dbFile: File = context.getDatabasePath(DATABASE_NAME)
        val size = if (dbFile.exists()) dbFile.length() else 0L

        return DatabaseStats(
            dbName = DATABASE_NAME,
            dbVersion = DATABASE_VERSION,
            totalTasks = total,
            completedTasks = completed,
            highPriorityTasks = highPrio,
            dbSizeBytes = size
        )
    }

    fun seedDefaultDataIfEmpty() {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_TASKS", null)
        var count = 0
        cursor.use { if (it.moveToFirst()) count = it.getInt(0) }

        if (count == 0) {
            insertTask(
                TaskItem(
                    title = "Explore SharedPreferences Studio",
                    description = "Change theme accents & toggle compact view in SharedPreferences tab",
                    category = "Ideas",
                    priority = "High",
                    isCompleted = false
                )
            )
            insertTask(
                TaskItem(
                    title = "Test SQLite CRUD Operations",
                    description = "Add, update, search, and delete tasks from SQLite database",
                    category = "Work",
                    priority = "High",
                    isCompleted = false
                )
            )
            insertTask(
                TaskItem(
                    title = "Review Fluid UI Animations",
                    description = "Verify aesthetic cards, gradients and dynamic progress bar",
                    category = "Personal",
                    priority = "Medium",
                    isCompleted = true
                )
            )
            insertTask(
                TaskItem(
                    title = "Morning Workout & Hydration",
                    description = "30 mins cardinal run + 2L water goal",
                    category = "Fitness",
                    priority = "Low",
                    isCompleted = true
                )
            )
        }
    }

    private fun String?.isNull_or_blank(): Boolean {
        return this == null || this.trim().isEmpty()
    }
}
