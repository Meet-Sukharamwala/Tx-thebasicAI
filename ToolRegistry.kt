package com.example.tools

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import com.example.applications.AppLauncherManager
import com.example.data.database.NoteEntity
import com.example.data.database.ReminderEntity
import com.example.data.database.TaskEntity
import com.example.data.repositories.TxRepository
import com.example.reminders.ReminderScheduler
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class ToolRegistry(
    private val context: Context,
    private val repository: TxRepository,
    private val appLauncher: AppLauncherManager
) {

    val toolDefinitions = listOf(
        ToolDefinition(
            name = "get_current_time",
            description = "Get the current system time, date, day of week, and device time zone.",
            parameters = emptyMap()
        ),
        ToolDefinition(
            name = "get_device_information",
            description = "Get system device details such as manufacturer, model, Android OS version, and battery percentage.",
            parameters = emptyMap()
        ),
        ToolDefinition(
            name = "open_supported_app",
            description = "Launch an authorized application installed on the device (e.g., YouTube, Chrome, Google Maps, Calculator, Camera, Settings, Clock).",
            parameters = mapOf(
                "appName" to ToolParamDoc("string", "Name of the application to open, e.g. 'YouTube' or 'Calculator'")
            )
        ),
        ToolDefinition(
            name = "open_approved_website",
            description = "Open a valid website URL in the device browser.",
            parameters = mapOf(
                "url" to ToolParamDoc("string", "The destination website URL (e.g. 'https://en.wikipedia.org' or 'google.com')")
            )
        ),
        ToolDefinition(
            name = "create_reminder",
            description = "Schedule a reminder notification for a specific future date and time.",
            parameters = mapOf(
                "title" to ToolParamDoc("string", "What to remind the user about"),
                "timeString" to ToolParamDoc("string", "Target time description, e.g. 'in 15 minutes', 'tomorrow 9:00 AM', '19:00'"),
                "delayMinutes" to ToolParamDoc("integer", "Optional explicit delay in minutes from now", required = false)
            )
        ),
        ToolDefinition(
            name = "list_reminders",
            description = "Retrieve all upcoming active scheduled reminders.",
            parameters = emptyMap()
        ),
        ToolDefinition(
            name = "cancel_reminder",
            description = "Cancel an active reminder by title or query.",
            parameters = mapOf(
                "query" to ToolParamDoc("string", "Title or keywords of the reminder to cancel")
            )
        ),
        ToolDefinition(
            name = "create_note",
            description = "Save a new persistent note with a title and content to local storage.",
            parameters = mapOf(
                "title" to ToolParamDoc("string", "Title of the note"),
                "content" to ToolParamDoc("string", "Content or body of the note")
            )
        ),
        ToolDefinition(
            name = "search_notes",
            description = "Search existing persistent notes by keyword or title.",
            parameters = mapOf(
                "query" to ToolParamDoc("string", "Search query keyword")
            )
        ),
        ToolDefinition(
            name = "update_note",
            description = "Update the content of an existing note.",
            parameters = mapOf(
                "title" to ToolParamDoc("string", "Title or keyword of note to update"),
                "newContent" to ToolParamDoc("string", "New text content for the note")
            )
        ),
        ToolDefinition(
            name = "delete_note",
            description = "Delete a persistent note by title or keyword.",
            parameters = mapOf(
                "query" to ToolParamDoc("string", "Title of the note to remove")
            )
        ),
        ToolDefinition(
            name = "add_task",
            description = "Add a new todo task to the persistent task list.",
            parameters = mapOf(
                "title" to ToolParamDoc("string", "Description of the task to complete"),
                "priority" to ToolParamDoc("string", "Priority: LOW, NORMAL, or HIGH", required = false)
            )
        ),
        ToolDefinition(
            name = "list_tasks",
            description = "List all pending or active tasks.",
            parameters = emptyMap()
        ),
        ToolDefinition(
            name = "complete_task",
            description = "Mark a task as completed.",
            parameters = mapOf(
                "taskTitle" to ToolParamDoc("string", "Title or keyword of the task to complete")
            )
        ),
        ToolDefinition(
            name = "answer_general_question",
            description = "Answer a general question or conversational inquiry.",
            parameters = mapOf(
                "response" to ToolParamDoc("string", "The conversational response to speak and display")
            )
        )
    )

    suspend fun executeTool(toolName: String, args: Map<String, String>): ToolExecutionResult {
        return when (toolName) {
            "get_current_time" -> handleGetCurrentTime()
            "get_device_information" -> handleGetDeviceInfo()
            "open_supported_app" -> handleOpenApp(args["appName"] ?: "")
            "open_approved_website" -> handleOpenWebsite(args["url"] ?: "")
            "create_reminder" -> handleCreateReminder(
                title = args["title"] ?: "Reminder",
                timeString = args["timeString"] ?: "",
                delayMinutes = args["delayMinutes"]?.toIntOrNull()
            )
            "list_reminders" -> handleListReminders()
            "cancel_reminder" -> handleCancelReminder(args["query"] ?: "")
            "create_note" -> handleCreateNote(
                title = args["title"] ?: "Untitled Note",
                content = args["content"] ?: ""
            )
            "search_notes" -> handleSearchNotes(args["query"] ?: "")
            "update_note" -> handleUpdateNote(args["title"] ?: "", args["newContent"] ?: "")
            "delete_note" -> handleDeleteNote(args["query"] ?: "")
            "add_task" -> handleAddTask(
                title = args["title"] ?: "New Task",
                priority = args["priority"] ?: "NORMAL"
            )
            "list_tasks" -> handleListTasks()
            "complete_task" -> handleCompleteTask(args["taskTitle"] ?: "")
            "answer_general_question" -> {
                val ans = args["response"] ?: "I am Tx, ready to assist."
                ToolExecutionResult("answer_general_question", true, ans, ans)
            }
            else -> ToolExecutionResult(
                toolName = toolName,
                isSuccess = false,
                resultText = "Unknown tool requested: $toolName",
                spokenText = "I encountered an unsupported action request."
            )
        }
    }

    private fun handleGetCurrentTime(): ToolExecutionResult {
        val now = Date()
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
        val tzFormat = SimpleDateFormat("z", Locale.getDefault())

        val timeStr = timeFormat.format(now)
        val dateStr = dateFormat.format(now)
        val tzStr = tzFormat.format(now)

        val fullText = "The current time is $timeStr ($tzStr) on $dateStr."
        val spoken = "It is currently $timeStr on $dateStr."
        return ToolExecutionResult("get_current_time", true, fullText, spoken)
    }

    private fun handleGetDeviceInfo(): ToolExecutionResult {
        val manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
        val model = Build.MODEL
        val androidRelease = Build.VERSION.RELEASE
        val sdkInt = Build.VERSION.SDK_INT

        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = if (level >= 0 && scale > 0) (level * 100 / scale) else -1

        val text = buildString {
            append("Device: $manufacturer $model\n")
            append("OS: Android $androidRelease (API $sdkInt)\n")
            if (batteryPct >= 0) append("Battery: $batteryPct%\n")
            append("Tx System Core: Operational")
        }
        val spoken = "You are running on a $manufacturer $model with Android $androidRelease." +
                if (batteryPct >= 0) " Battery is at $batteryPct percent." else ""

        return ToolExecutionResult("get_device_information", true, text, spoken)
    }

    private fun handleOpenApp(appName: String): ToolExecutionResult {
        if (appName.isBlank()) {
            return ToolExecutionResult(
                "open_supported_app",
                false,
                "Please specify which application to open.",
                "Please tell me which app you would like to open."
            )
        }
        val result = appLauncher.openAppByName(appName)
        return ToolExecutionResult(
            toolName = "open_supported_app",
            isSuccess = result.isSuccess,
            resultText = result.message,
            spokenText = result.message
        )
    }

    private fun handleOpenWebsite(url: String): ToolExecutionResult {
        if (url.isBlank()) {
            return ToolExecutionResult(
                "open_approved_website",
                false,
                "No URL provided.",
                "Please provide a website URL to open."
            )
        }
        val result = appLauncher.openWebsite(url)
        return ToolExecutionResult(
            toolName = "open_approved_website",
            isSuccess = result.isSuccess,
            resultText = result.message,
            spokenText = result.message
        )
    }

    private suspend fun handleCreateReminder(
        title: String,
        timeString: String,
        delayMinutes: Int?
    ): ToolExecutionResult {
        val now = System.currentTimeMillis()
        var targetTime = now

        if (delayMinutes != null && delayMinutes > 0) {
            targetTime = now + (delayMinutes * 60 * 1000L)
        } else {
            // Parse common relative phrases
            targetTime = parseTimeOffset(timeString, now)
        }

        if (targetTime <= now) {
            // Default fallback: 10 minutes from now if ambiguous
            targetTime = now + 10 * 60 * 1000L
        }

        val reminder = ReminderEntity(
            title = title.ifBlank { "Reminder" },
            targetTimeMillis = targetTime
        )
        val id = repository.insertReminder(reminder)
        val scheduled = ReminderScheduler.schedule(context, id, reminder.title, targetTime)

        val timeFmt = SimpleDateFormat("h:mm a, MMM d", Locale.getDefault()).format(Date(targetTime))
        val resultMsg = if (scheduled) {
            "Reminder scheduled for $timeFmt: \"${reminder.title}\""
        } else {
            "Reminder saved in Tx schedule for $timeFmt."
        }
        val spoken = "Reminder set for ${reminder.title} at $timeFmt."

        return ToolExecutionResult(
            toolName = "create_reminder",
            isSuccess = true,
            resultText = resultMsg,
            spokenText = spoken,
            data = id
        )
    }

    private suspend fun handleListReminders(): ToolExecutionResult {
        val reminders = repository.activeReminders.first()
        if (reminders.isEmpty()) {
            return ToolExecutionResult(
                "list_reminders",
                true,
                "You have no pending reminders.",
                "You have no pending reminders."
            )
        }
        val fmt = SimpleDateFormat("h:mm a (MMM d)", Locale.getDefault())
        val text = buildString {
            append("Upcoming Reminders (${reminders.size}):\n")
            reminders.forEachIndexed { i, r ->
                val time = fmt.format(Date(r.targetTimeMillis))
                append("${i + 1}. ${r.title} — $time\n")
            }
        }
        val spoken = "You have ${reminders.size} active reminders. First is ${reminders.first().title}."
        return ToolExecutionResult("list_reminders", true, text.trimEnd(), spoken)
    }

    private suspend fun handleCancelReminder(query: String): ToolExecutionResult {
        val reminder = repository.findFirstActiveReminderByTitle(query)
        return if (reminder != null) {
            repository.cancelReminder(reminder.id)
            ReminderScheduler.cancel(context, reminder.id)
            ToolExecutionResult(
                "cancel_reminder",
                true,
                "Cancelled reminder: \"${reminder.title}\"",
                "I have cancelled the reminder for ${reminder.title}."
            )
        } else {
            ToolExecutionResult(
                "cancel_reminder",
                false,
                "No active reminder matching \"$query\" found.",
                "I could not find an active reminder matching $query."
            )
        }
    }

    private suspend fun handleCreateNote(title: String, content: String): ToolExecutionResult {
        val note = NoteEntity(
            title = title.ifBlank { "Quick Note" },
            content = content.ifBlank { title },
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val id = repository.insertNote(note)
        val text = "Note saved: \"${note.title}\"\n${note.content}"
        val spoken = "I've saved your note titled ${note.title}."
        return ToolExecutionResult("create_note", true, text, spoken, id)
    }

    private suspend fun handleSearchNotes(query: String): ToolExecutionResult {
        val notes = repository.searchNotes(query).first()
        return if (notes.isEmpty()) {
            ToolExecutionResult(
                "search_notes",
                true,
                "No notes found matching \"$query\".",
                "I found no notes matching $query."
            )
        } else {
            val text = buildString {
                append("Found ${notes.size} note(s) matching \"$query\":\n")
                notes.forEach { n ->
                    append("• ${n.title}: ${n.content.take(60)}...\n")
                }
            }
            ToolExecutionResult(
                "search_notes",
                true,
                text.trimEnd(),
                "Found ${notes.size} notes matching your search."
            )
        }
    }

    private suspend fun handleUpdateNote(title: String, newContent: String): ToolExecutionResult {
        val existing = repository.findFirstNoteByTitle(title)
        return if (existing != null) {
            val updated = existing.copy(
                content = newContent,
                updatedAt = System.currentTimeMillis()
            )
            repository.updateNote(updated)
            ToolExecutionResult(
                "update_note",
                true,
                "Updated note: \"${updated.title}\"",
                "I have updated your note ${updated.title}."
            )
        } else {
            ToolExecutionResult(
                "update_note",
                false,
                "Note matching \"$title\" not found to update.",
                "Could not find a note matching $title to update."
            )
        }
    }

    private suspend fun handleDeleteNote(query: String): ToolExecutionResult {
        val existing = repository.findFirstNoteByTitle(query)
        return if (existing != null) {
            repository.deleteNoteById(existing.id)
            ToolExecutionResult(
                "delete_note",
                true,
                "Deleted note: \"${existing.title}\"",
                "Deleted your note titled ${existing.title}."
            )
        } else {
            ToolExecutionResult(
                "delete_note",
                false,
                "Note matching \"$query\" not found.",
                "Could not find a note matching $query to delete."
            )
        }
    }

    private suspend fun handleAddTask(title: String, priority: String): ToolExecutionResult {
        val task = TaskEntity(
            title = title.ifBlank { "Untitled Task" },
            priority = priority.uppercase().takeIf { it in listOf("LOW", "NORMAL", "HIGH") } ?: "NORMAL"
        )
        val id = repository.insertTask(task)
        val text = "Task added: \"${task.title}\" [Priority: ${task.priority}]"
        val spoken = "Added task: ${task.title}."
        return ToolExecutionResult("add_task", true, text, spoken, id)
    }

    private suspend fun handleListTasks(): ToolExecutionResult {
        val tasks = repository.pendingTasks.first()
        return if (tasks.isEmpty()) {
            ToolExecutionResult(
                "list_tasks",
                true,
                "All tasks completed! Your task list is clear.",
                "You have no pending tasks."
            )
        } else {
            val text = buildString {
                append("Pending Tasks (${tasks.size}):\n")
                tasks.forEachIndexed { i, t ->
                    append("${i + 1}. [ ] ${t.title} (${t.priority})\n")
                }
            }
            ToolExecutionResult(
                "list_tasks",
                true,
                text.trimEnd(),
                "You have ${tasks.size} pending tasks. Top task is ${tasks.first().title}."
            )
        }
    }

    private suspend fun handleCompleteTask(taskTitle: String): ToolExecutionResult {
        val task = repository.findFirstTaskByTitle(taskTitle)
        return if (task != null) {
            repository.setTaskCompleted(task.id, true)
            ToolExecutionResult(
                "complete_task",
                true,
                "Marked task completed: \"${task.title}\"",
                "Marked task ${task.title} as completed."
            )
        } else {
            ToolExecutionResult(
                "complete_task",
                false,
                "Could not find a pending task matching \"$taskTitle\".",
                "I couldn't find a task matching $taskTitle."
            )
        }
    }

    private fun parseTimeOffset(query: String, now: Long): Long {
        val lower = query.lowercase()
        val cal = Calendar.getInstance().apply { timeInMillis = now }

        // "in X minutes"
        val minRegex = Regex("in (\\d+) (?:min|minute|minutes)")
        minRegex.find(lower)?.let { match ->
            val mins = match.groupValues[1].toIntOrNull() ?: 10
            return now + mins * 60 * 1000L
        }

        // "in X hours"
        val hourRegex = Regex("in (\\d+) (?:hour|hours|hr|hrs)")
        hourRegex.find(lower)?.let { match ->
            val hrs = match.groupValues[1].toIntOrNull() ?: 1
            return now + hrs * 3600 * 1000L
        }

        // "tomorrow at X"
        if (lower.contains("tomorrow")) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
            // check for hour
            val timeMatch = Regex("(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)?").find(lower)
            if (timeMatch != null) {
                var hour = timeMatch.groupValues[1].toIntOrNull() ?: 9
                val min = timeMatch.groupValues[2].toIntOrNull() ?: 0
                val ampm = timeMatch.groupValues[3]
                if (ampm == "pm" && hour < 12) hour += 12
                if (ampm == "am" && hour == 12) hour = 0
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, min)
                cal.set(Calendar.SECOND, 0)
                return cal.timeInMillis
            }
            cal.set(Calendar.HOUR_OF_DAY, 9)
            cal.set(Calendar.MINUTE, 0)
            return cal.timeInMillis
        }

        // "at 7 PM" or "at 7:30 AM"
        val atRegex = Regex("at (\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)?")
        atRegex.find(lower)?.let { match ->
            var hour = match.groupValues[1].toIntOrNull() ?: 12
            val min = match.groupValues[2].toIntOrNull() ?: 0
            val ampm = match.groupValues[3]
            if (ampm == "pm" && hour < 12) hour += 12
            if (ampm == "am" && hour == 12) hour = 0
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, min)
            cal.set(Calendar.SECOND, 0)
            if (cal.timeInMillis <= now) {
                // If already passed today, schedule for tomorrow
                cal.add(Calendar.DAY_OF_YEAR, 1)
            }
            return cal.timeInMillis
        }

        // Default 15 minutes
        return now + 15 * 60 * 1000L
    }
}
