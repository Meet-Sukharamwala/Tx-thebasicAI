package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sender: String, // "USER", "TX", "SYSTEM"
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val toolName: String? = null,
    val isExecutionSuccess: Boolean? = null
)
