package com.example.ai

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface GeminiEndpoints {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

class GeminiService {

    private val api: GeminiEndpoints

    init {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.NONE
        }

        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        api = retrofit.create(GeminiEndpoints::class.java)
    }

    private val systemPrompt = """
You are Tx, the user's personal AI assistant for Android.
Your name is Tx. Never identify yourself as JARVIS or another assistant.
You are intelligent, composed, attentive, helpful, professional, and conversational. Communicate naturally and confidently.

Supported tools:
- get_current_time: {}
- get_device_information: {}
- open_supported_app: {"appName": string}
- open_approved_website: {"url": string}
- create_reminder: {"title": string, "timeString": string}
- list_reminders: {}
- cancel_reminder: {"query": string}
- create_note: {"title": string, "content": string}
- search_notes: {"query": string}
- update_note: {"title": string, "newContent": string}
- delete_note: {"query": string}
- add_task: {"title": string, "priority": "LOW"|"NORMAL"|"HIGH"}
- list_tasks: {}
- complete_task: {"taskTitle": string}
- answer_general_question: {"response": string}

OUTPUT FORMAT:
Always reply ONLY with a valid JSON object in this format (no markdown code blocks, just raw JSON):
{
  "tool": "<tool_name>",
  "arguments": { <key-values> },
  "explanation": "<short status to user>"
}

If the user is asking a general question, advice, or greeting, use:
{
  "tool": "answer_general_question",
  "arguments": { "response": "<your conversational answer here>" },
  "explanation": "Answered"
}
    """.trimIndent()

    suspend fun processUserCommand(prompt: String): ParsedAiDecision {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        // Check if API key is populated and not dummy placeholder
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return LocalIntentParser.parse(prompt)
        }

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    role = "user",
                    parts = listOf(GeminiPart(text = prompt))
                )
            ),
            systemInstruction = GeminiContent(
                parts = listOf(GeminiPart(text = systemPrompt))
            ),
            generationConfig = GeminiGenerationConfig(temperature = 0.2f)
        )

        return try {
            val response = api.generateContent(apiKey = apiKey, request = request)
            val rawOutput = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: return LocalIntentParser.parse(prompt)

            parseJsonResponse(rawOutput) ?: LocalIntentParser.parse(prompt)
        } catch (e: Exception) {
            // Graceful fallback to local intent engine on network or auth errors
            LocalIntentParser.parse(prompt)
        }
    }

    private fun parseJsonResponse(raw: String): ParsedAiDecision? {
        val clean = raw.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        return try {
            val json = JSONObject(clean)
            val tool = json.optString("tool", "answer_general_question")
            val explanation = json.optString("explanation", "Processing request...")
            val argsObj = json.optJSONObject("arguments")

            val args = mutableMapOf<String, String>()
            if (argsObj != null) {
                val keys = argsObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    args[key] = argsObj.optString(key, "")
                }
            }

            ParsedAiDecision(
                toolName = tool,
                arguments = args,
                conversationalMessage = explanation
            )
        } catch (e: Exception) {
            null
        }
    }
}
