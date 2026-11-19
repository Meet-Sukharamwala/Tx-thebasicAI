package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.agent.TxAgentCoordinator
import com.example.agent.TxState
import com.example.ai.GeminiService
import com.example.applications.AppLauncherManager
import com.example.data.database.AppDatabase
import com.example.data.database.ConversationEntity
import com.example.data.database.NoteEntity
import com.example.data.database.ReminderEntity
import com.example.data.database.TaskEntity
import com.example.data.repositories.TxRepository
import com.example.reminders.ReminderScheduler
import com.example.tools.ToolRegistry
import com.example.voice.SpeechRecognitionManager
import com.example.voice.TextToSpeechManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TxNavigationTab {
    DASHBOARD,
    TASKS_NOTES,
    REMINDERS,
    APP_HUB,
    SETTINGS
}

class TxViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    val repository = TxRepository(
        noteDao = database.noteDao(),
        taskDao = database.taskDao(),
        reminderDao = database.reminderDao(),
        conversationDao = database.conversationDao()
    )

    private val appLauncher = AppLauncherManager(application)
    private val toolRegistry = ToolRegistry(application, repository, appLauncher)
    private val geminiService = GeminiService()

    val ttsManager = TextToSpeechManager(application)
    val coordinator = TxAgentCoordinator(
        toolRegistry = toolRegistry,
        geminiService = geminiService,
        repository = repository,
        ttsManager = ttsManager,
        coroutineScope = viewModelScope
    )

    val speechManager = SpeechRecognitionManager(
        context = application,
        onResult = { text -> coordinator.onSpeechRecognized(text) },
        onError = { err -> coordinator.onSpeechError(err) },
        onStateChanged = { isListening ->
            // Audio state handled
        }
    )

    init {
        coordinator.setSpeechRecognitionManager(speechManager)
    }

    // Navigation state
    private val _currentTab = MutableStateFlow(TxNavigationTab.DASHBOARD)
    val currentTab: StateFlow<TxNavigationTab> = _currentTab.asStateFlow()

    fun selectTab(tab: TxNavigationTab) {
        _currentTab.value = tab
    }

    // Text command input in dashboard
    private val _typedCommand = MutableStateFlow("")
    val typedCommand: StateFlow<String> = _typedCommand.asStateFlow()

    fun setTypedCommand(text: String) {
        _typedCommand.value = text
    }

    fun submitTypedCommand() {
        val text = _typedCommand.value.trim()
        if (text.isNotBlank()) {
            _typedCommand.value = ""
            coordinator.processCommand(text)
        }
    }

    // Reactive streams from Room
    val conversationHistory: StateFlow<List<ConversationEntity>> = repository.conversationHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotes: StateFlow<List<NoteEntity>> = repository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeReminders: StateFlow<List<ReminderEntity>> = repository.activeReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Settings & state
    val isApiKeyConfigured: Boolean
        get() = try {
            val key = BuildConfig.GEMINI_API_KEY
            key.isNotBlank() && key != "MY_GEMINI_API_KEY"
        } catch (e: Throwable) {
            false
        }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearConversationHistory()
        }
    }

    fun toggleTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.setTaskCompleted(task.id, !task.isCompleted)
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            repository.deleteTaskById(taskId)
        }
    }

    fun createManualTask(title: String, priority: String) {
        viewModelScope.launch {
            repository.insertTask(TaskEntity(title = title, priority = priority))
        }
    }

    fun createManualNote(title: String, content: String) {
        viewModelScope.launch {
            repository.insertNote(NoteEntity(title = title, content = content))
        }
    }

    fun deleteNote(noteId: Long) {
        viewModelScope.launch {
            repository.deleteNoteById(noteId)
        }
    }

    fun cancelReminder(reminderId: Long) {
        viewModelScope.launch {
            repository.cancelReminder(reminderId)
            ReminderScheduler.cancel(getApplication(), reminderId)
        }
    }

    fun testReminderAlarm(title: String, delaySeconds: Int = 5) {
        viewModelScope.launch {
            val targetTime = System.currentTimeMillis() + (delaySeconds * 1000L)
            val id = repository.insertReminder(
                ReminderEntity(
                    title = title,
                    targetTimeMillis = targetTime
                )
            )
            ReminderScheduler.schedule(getApplication(), id, title, targetTime)
        }
    }

    fun launchAppDirect(appName: String) {
        appLauncher.openAppByName(appName)
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.destroy()
        ttsManager.shutdown()
    }
}
