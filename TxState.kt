package com.example.agent

enum class TxState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING,
    EXECUTING,
    ERROR
}

data class UiTranscriptItem(
    val id: Long,
    val sender: String, // "USER" or "TX"
    val text: String,
    val toolName: String? = null,
    val isSuccess: Boolean? = null,
    val timestamp: Long = System.currentTimeMillis()
)
