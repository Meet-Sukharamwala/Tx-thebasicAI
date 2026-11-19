package com.example.tools

data class ToolDefinition(
    val name: String,
    val description: String,
    val parameters: Map<String, ToolParamDoc>
)

data class ToolParamDoc(
    val type: String,
    val description: String,
    val required: Boolean = true
)

data class ToolExecutionResult(
    val toolName: String,
    val isSuccess: Boolean,
    val resultText: String,
    val spokenText: String,
    val data: Any? = null
)
