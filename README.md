# TaskPulse Hub

**Experiment 9: SharedPreferences and SQLite Persistence**

**Student Name:** Harshil Sheth
**USN:** 25MCAR0225

---

## Table of Contents
1. [Experiment Overview](#experiment-overview)
2. [Concept and Technology](#concept-and-technology)
3. [Scenario](#scenario)
4. [Features](#features)
5. [Project Structure](#project-structure)
6. [Getting Started](#getting-started)
7. [Output](#output)
8. [Test Cases](#test-cases)
9. [Known Limitations](#known-limitations)
10. [Learning Outcomes](#learning-outcomes)

---

## Experiment Overview

This experiment shows two ways Android apps keep data after they close. Task data is stored in a SQLite database, and user settings are stored in SharedPreferences. TaskPulse Hub is a task manager with two tabs. The **SQLite Tasks** tab lets the user create, edit, complete, search, filter, and delete tasks, and it shows live statistics from the database. The **SharedPreferences Studio** tab lets the user change their display name, theme accent, and list layout, and it shows the raw SharedPreferences XML so the stored values can be inspected.

## Concept and Technology

- **SQLite** is a lightweight relational database built into Android. `DatabaseHelper` extends `SQLiteOpenHelper`, which creates the `tasks` table on first run and handles version upgrades. Each task row has an auto-incrementing ID, a title, a description, a category, a priority, a completion flag, and a creation timestamp.
- **CRUD operations** are implemented with `ContentValues` and the `insert`, `update`, and `delete` methods. Reads use `query()` with dynamic `WHERE` clauses built from the search text and category filter.
- **Search and filtering** use SQL `LIKE` on the title and description, and an equality match on the category. Results are sorted by completion status and creation time when that option is enabled.
- **SharedPreferences** stores key-value settings in an XML file (`task_pulse_prefs.xml`). `PreferenceManager` wraps the file with typed properties, so the rest of the app reads and writes settings without handling raw keys.
- **RecyclerView with two view types** renders the task list. `TaskAdapter` switches between a standard card and a compact row based on the saved layout preference.
- **Fragments and ViewPager** host the two tabs, with `TabLayout` for navigation. `MainActivity` also applies the saved accent color to the header and tab indicator when the app starts.

## Scenario

On first launch, the app seeds four sample tasks into the database. The user can:

- See the database status card with the total, completed, and high-priority counts and a progress bar
- Search tasks by text or filter them by category (All, Work, Personal, Fitness, Ideas)
- Tap **New Task** to add a task, tap the edit icon to change one, check it off, or delete it after confirming
- Tap **DB Info** to see the database name, version, size, and the first few rows
- Open the SharedPreferences Studio tab to change the greeting name, pick a theme accent, and toggle compact layout, auto-sorting, and notifications
- Tap **Reset** to restore the default preference values

## Features

- SQLite table with create, read, update, delete, and search
- Live search as the user types
- Category filter chips
- Database status card with a progress bar and counts
- Add and edit task dialog with category and priority selection
- Confirmation before deleting a task
- Database inspector showing metadata and raw row data
- Standard and compact list layouts
- Settings saved to SharedPreferences and restored on restart
- Live SharedPreferences XML inspector
- Theme accent (violet, coral, emerald, amber) applied to the header and tab indicator

## Project Structure

```
android_apps_experiment9/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/spandsqlitedemo/
│   │   │   │   ├── MainActivity.kt              # Tabs, theme accent, app header
│   │   │   │   ├── data/
│   │   │   │   │   ├── DatabaseHelper.kt        # SQLite schema, CRUD, search, stats
│   │   │   │   │   ├── PreferenceManager.kt     # SharedPreferences wrapper
│   │   │   │   │   └── TaskItem.kt              # Task data model
│   │   │   │   └── ui/
│   │   │   │       ├── TasksFragment.kt         # SQLite tab: search, filters, dialogs
│   │   │   │       ├── TaskAdapter.kt           # RecyclerView adapter (standard and compact)
│   │   │   │       ├── SettingsFragment.kt      # SharedPreferences tab
│   │   │   │       └── MainPagerAdapter.kt      # ViewPager adapter for the two tabs
│   │   │   ├── res/
│   │   │   │   ├── layout/
│   │   │   │   │   ├── activity_main.xml        # Header, tab layout, ViewPager
│   │   │   │   │   ├── fragment_tasks.xml       # Stats card, search, chips, task list, FAB
│   │   │   │   │   ├── fragment_settings.xml    # Preferences UI and XML inspector
│   │   │   │   │   ├── item_task.xml            # Standard task card
│   │   │   │   │   ├── item_task_compact.xml    # Compact task row
│   │   │   │   │   ├── dialog_add_edit_task.xml # Add and edit dialog
│   │   │   │   │   └── dialog_sqlite_inspector.xml # Database inspector dialog
│   │   │   │   ├── drawable/                    # Header gradients, pills, icons
│   │   │   │   ├── values/                      # strings.xml, colors.xml, themes.xml
│   │   │   │   └── values-night/themes.xml      # Dark theme
│   │   │   └── AndroidManifest.xml
│   │   ├── test/                # Unit test template
│   │   └── androidTest/         # Instrumented test template
│   └── build.gradle.kts
├── gradle/libs.versions.toml    # Dependency versions
├── build.gradle.kts
├── settings.gradle.kts
└── screenshots/                 # Output and test case images
```

## Getting Started

**Requirements:** Android Studio (latest stable), Android SDK, and an emulator or device running Android 7.0 (API 24) or later.

1. Clone the repository:
```bash
   git clone https://github.com/HarshilSheth888/Android_apps_experiment9.git
```
2. Open the project folder in Android Studio.
3. Let Gradle sync.
4. Press **Run** on an emulator or connected device.

## Output

<img width="299" height="568" alt="preview" src="https://github.com/user-attachments/assets/a5f5146d-95c1-4008-bd27-09032614f7a0" />
   




The app opens on the SQLite Tasks tab. The status card shows 4 total tasks, 2 done, 2 high priority, and 50% progress, followed by the search bar, category chips, and task cards.

## Test Cases

### Test Case 1: Database statistics reflect stored data
- **Input:** Launch the app with the four seeded tasks.
- **Expected:** The status card shows Total 4, Done 2, High Priority 2, and "50% completed (2 of 4 tasks)."
- **Result:** Pass

### Test Case 2: Add a task and confirm it persists
- **Input:** Tap **New Task**, enter a title, choose a category and priority, and tap Save. Close the app completely and reopen it.
- **Expected:** The toast "New task inserted into SQLite DB" appears, the new task appears in the list, the Total count increases by one, and the task is still there after restart.
- **Result:** ____________

### Test Case 3: Theme accent persists through SharedPreferences
- **Input:** Open the SharedPreferences Studio tab, tap the **Coral** accent button, then close and reopen the app.
- **Expected:** The header and tab indicator change to coral immediately, and they stay coral after the app restarts. The XML inspector shows `theme_accent` set to `coral`.
- **Result:** ____________

## Known Limitations

- **Enable Sync Notifications** saves its value to SharedPreferences but does not send any notifications.
- **Database upgrades** drop and recreate the `tasks` table, so existing data is lost if the database version is increased.
- **Last sync time** is updated every time a preference is changed. It does not reflect a real sync with a server.

## Learning Outcomes

- Creating and upgrading a SQLite database with `SQLiteOpenHelper`
- Performing create, read, update, delete, and search operations with `ContentValues`, `query()`, and parameterized `WHERE` clauses
- Storing and reading typed settings with SharedPreferences
- Displaying database records in a `RecyclerView` with multiple view types
- Organizing screens into tabs with fragments and `ViewPager`
- Applying saved preferences when the app starts

## Author

**Harshil Sheth**
USN: 25MCAR0225
GitHub: https://github.com/HarshilSheth888
