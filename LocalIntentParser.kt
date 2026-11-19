package com.example.ai

object LocalIntentParser {

    fun parse(rawPrompt: String): ParsedAiDecision {
        val trimmed = rawPrompt.trim()
        val prefixRegex = Regex("^(?:hey\\s+tx|tx)[,\\s:]*", RegexOption.IGNORE_CASE)
        val clean = trimmed.replace(prefixRegex, "").trim()
        val lower = clean.lowercase()

        // 1. Time query
        if (lower.contains("time") && (lower.contains("what") || lower.contains("current") || lower.contains("tell me"))) {
            return ParsedAiDecision(
                toolName = "get_current_time",
                arguments = emptyMap(),
                conversationalMessage = "Checking current time..."
            )
        }

        // 2. Device info
        if (lower.contains("device") || lower.contains("battery") || lower.contains("phone status") || lower.contains("system status")) {
            return ParsedAiDecision(
                toolName = "get_device_information",
                arguments = emptyMap(),
                conversationalMessage = "Retrieving device status..."
            )
        }

        // 3. Open apps or websites
        if (lower.startsWith("open ") || lower.startsWith("launch ") || lower.startsWith("start ")) {
            val appRegex = Regex("^(?:open|launch|start)[\\s]+(?:my\\s+|the\\s+)?", RegexOption.IGNORE_CASE)
            val appQuery = clean.replace(appRegex, "").trim()

            if (appQuery.contains(".com") || appQuery.contains(".org") || appQuery.contains(".net") || appQuery.startsWith("http")) {
                return ParsedAiDecision(
                    toolName = "open_approved_website",
                    arguments = mapOf("url" to appQuery),
                    conversationalMessage = "Opening web destination..."
                )
            }

            return ParsedAiDecision(
                toolName = "open_supported_app",
                arguments = mapOf("appName" to appQuery.lowercase()),
                conversationalMessage = "Launching $appQuery..."
            )
        }

        // 4. Reminders
        if (lower.startsWith("remind me") || lower.contains("set a reminder") || lower.contains("create a reminder")) {
            val reminderPrefix = Regex("^(?:remind\\s+me|set\\s+a\\s+reminder|create\\s+a\\s+reminder)[\\s:]*(?:to\\s+)?", RegexOption.IGNORE_CASE)
            val reminderBody = clean.replace(reminderPrefix, "").trim()

            var titlePart = reminderBody
            var timePart = ""

            val atMatch = Regex("\\s+at\\s+", RegexOption.IGNORE_CASE).find(reminderBody)
            val inMatch = Regex("\\s+in\\s+", RegexOption.IGNORE_CASE).find(reminderBody)
            val tomorrowMatch = Regex("\\s+tomorrow\\s*", RegexOption.IGNORE_CASE).find(reminderBody)

            if (atMatch != null) {
                titlePart = reminderBody.substring(0, atMatch.range.first).trim()
                timePart = reminderBody.substring(atMatch.range.first).trim()
            } else if (inMatch != null) {
                titlePart = reminderBody.substring(0, inMatch.range.first).trim()
                timePart = reminderBody.substring(inMatch.range.first).trim()
            } else if (tomorrowMatch != null) {
                titlePart = reminderBody.substring(0, tomorrowMatch.range.first).trim()
                timePart = reminderBody.substring(tomorrowMatch.range.first).trim()
            }

            return ParsedAiDecision(
                toolName = "create_reminder",
                arguments = mapOf(
                    "title" to titlePart.ifBlank { "Reminder" },
                    "timeString" to timePart
                ),
                conversationalMessage = "Setting reminder for $titlePart..."
            )
        }

        if (lower.contains("reminders") && (lower.contains("show") || lower.contains("list") || lower.contains("what"))) {
            return ParsedAiDecision(
                toolName = "list_reminders",
                arguments = emptyMap(),
                conversationalMessage = "Listing pending reminders..."
            )
        }

        if (lower.startsWith("cancel reminder") || lower.startsWith("cancel my reminder")) {
            val cancelRegex = Regex("^cancel(?:\\s+my)?\\s+reminder[\\s:]*(?:for\\s+)?", RegexOption.IGNORE_CASE)
            val query = clean.replace(cancelRegex, "").trim()
            return ParsedAiDecision(
                toolName = "cancel_reminder",
                arguments = mapOf("query" to query),
                conversationalMessage = "Cancelling reminder..."
            )
        }

        // 5. Notes
        if (lower.startsWith("create a note") || lower.startsWith("create note") || lower.startsWith("take a note") || lower.startsWith("save this:")) {
            val notePrefix = Regex("^(?:create\\s+(?:a\\s+)?note|take\\s+a\\s+note|save\\s+this:?)[\\s:]*", RegexOption.IGNORE_CASE)
            val noteBody = clean.replace(notePrefix, "").trim()

            var title = "Quick Note"
            var content = noteBody

            val titledMatch = Regex("\\s+titled\\s+", RegexOption.IGNORE_CASE).find(noteBody)
            val contentMatch = Regex("\\s+content\\s+", RegexOption.IGNORE_CASE).find(noteBody)

            if (titledMatch != null && contentMatch != null && contentMatch.range.first > titledMatch.range.last) {
                title = noteBody.substring(titledMatch.range.last, contentMatch.range.first).trim()
                content = noteBody.substring(contentMatch.range.last).trim()
            } else if (noteBody.startsWith("titled ", ignoreCase = true)) {
                val afterTitled = noteBody.substring(7).trim()
                if (contentMatch != null) {
                    title = noteBody.substring(7, contentMatch.range.first).trim()
                    content = noteBody.substring(contentMatch.range.last).trim()
                } else {
                    title = afterTitled
                    content = afterTitled
                }
            } else if (titledMatch != null) {
                content = noteBody.substring(0, titledMatch.range.first).trim()
                title = noteBody.substring(titledMatch.range.last).trim()
            } else if (noteBody.length > 25) {
                title = noteBody.take(25).trim()
            }

            return ParsedAiDecision(
                toolName = "create_note",
                arguments = mapOf(
                    "title" to title.ifBlank { "Quick Note" },
                    "content" to content.ifBlank { title }
                ),
                conversationalMessage = "Saving note..."
            )
        }

        if (lower.contains("notes") && (lower.contains("search") || lower.contains("find"))) {
            val query = lower.replace(Regex(".*(?:search|find)\\s+(?:my\\s+)?notes?\\s+(?:for\\s+)?"), "").trim()
            return ParsedAiDecision(
                toolName = "search_notes",
                arguments = mapOf("query" to query),
                conversationalMessage = "Searching notes for \"$query\"..."
            )
        }

        if (lower.contains("notes") && (lower.contains("show") || lower.contains("list") || lower.contains("recent"))) {
            return ParsedAiDecision(
                toolName = "search_notes",
                arguments = mapOf("query" to ""),
                conversationalMessage = "Retrieving notes..."
            )
        }

        if (lower.startsWith("delete note") || lower.startsWith("delete my note")) {
            val deleteRegex = Regex("^delete(?:\\s+my)?\\s+note[\\s:]*", RegexOption.IGNORE_CASE)
            val query = clean.replace(deleteRegex, "").trim()
            return ParsedAiDecision(
                toolName = "delete_note",
                arguments = mapOf("query" to query),
                conversationalMessage = "Deleting note..."
            )
        }

        // 6. Tasks
        if (lower.startsWith("add task") || lower.startsWith("add a task") || lower.startsWith("create task")) {
            val taskPrefix = Regex("^(?:add\\s+(?:a\\s+)?task|create\\s+task)[\\s:]*(?:to\\s+)?", RegexOption.IGNORE_CASE)
            val title = clean.replace(taskPrefix, "").trim()
            return ParsedAiDecision(
                toolName = "add_task",
                arguments = mapOf("title" to title.ifBlank { "New Task" }, "priority" to "NORMAL"),
                conversationalMessage = "Adding task..."
            )
        }

        if (lower.contains("tasks") && (lower.contains("show") || lower.contains("list") || lower.contains("pending"))) {
            return ParsedAiDecision(
                toolName = "list_tasks",
                arguments = emptyMap(),
                conversationalMessage = "Fetching task list..."
            )
        }

        if (lower.startsWith("complete task") || lower.startsWith("mark task") || lower.contains("as completed") || lower.contains("as done")) {
            val title = clean.replace(Regex("^(?:complete\\s+task|mark\\s+(?:task\\s+)?)[\\s:]*", RegexOption.IGNORE_CASE), "")
                .replace(Regex("\\s+as\\s+(?:completed|done)", RegexOption.IGNORE_CASE), "")
                .trim()
            return ParsedAiDecision(
                toolName = "complete_task",
                arguments = mapOf("taskTitle" to title),
                conversationalMessage = "Marking task complete..."
            )
        }

        // 7. Identity / Greetings
        if (lower.startsWith("how are you") || lower.startsWith("hello") || lower.startsWith("hi")) {
            return ParsedAiDecision(
                toolName = "answer_general_question",
                arguments = mapOf("response" to "I am Tx, online and ready to assist you. What can I help you with?"),
                conversationalMessage = "Online and operational."
            )
        }

        if (lower.contains("who are you") || lower.contains("what are you") || lower.contains("your name")) {
            return ParsedAiDecision(
                toolName = "answer_general_question",
                arguments = mapOf("response" to "I am Tx, your intelligent personal AI assistant for Android. I manage your reminders, tasks, notes, app launches, and everyday questions."),
                conversationalMessage = "I am Tx."
            )
        }

        // Default conversational answer
        val general = "I received your request: \"$trimmed\". As your Tx assistant, I can launch apps, set reminders, take notes, manage tasks, or answer questions."
        return ParsedAiDecision(
            toolName = "answer_general_question",
            arguments = mapOf("response" to general),
            conversationalMessage = "Processing..."
        )
    }
}
