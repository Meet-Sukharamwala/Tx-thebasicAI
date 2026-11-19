package com.example.agent

import com.example.ai.GeminiService
import com.example.data.repositories.TxRepository
import com.example.tools.ToolRegistry
import com.example.voice.SpeechRecognitionManager
import com.example.voice.TextToSpeechManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TxAgentCoordinator(
    private val toolRegistry: ToolRegistry,
    private val geminiService: GeminiService,
    private val repository: TxRepository,
    private val ttsManager: TextToSpeechManager,
    private val coroutineScope: CoroutineScope
) {
    private val _currentState = MutableStateFlow(TxState.IDLE)
    val currentState: StateFlow<TxState> = _currentState.asStateFlow()

    private val _lastRecognizedText = MutableStateFlow("")
    val lastRecognizedText: StateFlow<String> = _lastRecognizedText.asStateFlow()

    private val _statusDetail = MutableStateFlow("Tx System Online")
    val statusDetail: StateFlow<String> = _statusDetail.asStateFlow()

    private var speechRecognitionManager: SpeechRecognitionManager? = null

    fun setSpeechRecognitionManager(manager: SpeechRecognitionManager) {
        this.speechRecognitionManager = manager
    }

    fun startListening() {
        if (_currentState.value == TxState.LISTENING) {
            stopListening()
            return
        }
        ttsManager.stop()
        _currentState.value = TxState.LISTENING
        _statusDetail.value = "Listening to your voice..."
        speechRecognitionManager?.startListening()
    }

    fun stopListening() {
        speechRecognitionManager?.stopListening()
        if (_currentState.value == TxState.LISTENING) {
            _currentState.value = TxState.IDLE
            _statusDetail.value = "Voice interaction stopped"
        }
    }

    fun onSpeechRecognized(text: String) {
        _lastRecognizedText.value = text
        processCommand(text)
    }

    fun onSpeechError(errorMessage: String) {
        _currentState.value = TxState.ERROR
        _statusDetail.value = errorMessage
        coroutineScope.launch {
            kotlinx.coroutines.delay(3500)
            if (_currentState.value == TxState.ERROR) {
                _currentState.value = TxState.IDLE
                _statusDetail.value = "Tx Ready"
            }
        }
    }

    fun processCommand(command: String) {
        if (command.isBlank()) return
        ttsManager.stop()

        coroutineScope.launch {
            // 1. Log user message to Room
            repository.addMessage(sender = "USER", message = command)

            // 2. Set THINKING state
            _currentState.value = TxState.THINKING
            _statusDetail.value = "Tx is analyzing intent..."

            // 3. Gemini / Intent determination
            val decision = withContext(Dispatchers.IO) {
                geminiService.processUserCommand(command)
            }

            // 4. Set EXECUTING state
            _currentState.value = TxState.EXECUTING
            _statusDetail.value = "Executing: ${decision.toolName}..."

            // 5. Execute action via Tool Registry
            val result = withContext(Dispatchers.IO) {
                toolRegistry.executeTool(decision.toolName, decision.arguments)
            }

            // 6. Persist response to database
            repository.addMessage(
                sender = "TX",
                message = result.resultText,
                toolName = result.toolName,
                isExecutionSuccess = result.isSuccess
            )

            // 7. Spoken playback & SPEAKING state
            _currentState.value = TxState.SPEAKING
            _statusDetail.value = if (result.isSuccess) "Speaking response..." else "Reporting issue..."

            ttsManager.speak(result.spokenText)

            // Monitor when speech finishes or times out to return to IDLE
            launch {
                // Wait for speech to finish
                while (ttsManager.isSpeaking.value) {
                    kotlinx.coroutines.delay(200)
                }
                if (_currentState.value == TxState.SPEAKING) {
                    _currentState.value = TxState.IDLE
                    _statusDetail.value = "Tx Ready"
                }
            }
        }
    }

    fun stopSpeaking() {
        ttsManager.stop()
        if (_currentState.value == TxState.SPEAKING) {
            _currentState.value = TxState.IDLE
            _statusDetail.value = "Tx Ready"
        }
    }
}
