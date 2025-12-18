package com.example.todolist.data.remote.ai

import android.util.Log
import com.example.todolist.BuildConfig
import com.example.todolist.core.model.AiChatResponse
import com.example.todolist.core.model.AppLanguage
import com.example.todolist.core.model.ChatMessage
import com.example.todolist.core.model.ChatRole
import com.example.todolist.core.model.Gender
import com.example.todolist.core.model.MissionStatus
import com.example.todolist.core.model.UserContext
import com.example.todolist.domain.repository.AiRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

/**
 * Implementation của AiRepository sử dụng Groq API
 * - Whisper để Speech-to-Text
 * - LLaMA/Mixtral để Chat
 */
class GroqAiRepositoryImpl : AiRepository {

    companion object {
        private const val TAG = "GroqAiRepository"
        private const val GROQ_BASE_URL = "https://api.groq.com/openai/v1"
        private const val CHAT_MODEL = "llama-3.1-8b-instant"
        private const val WHISPER_MODEL = "whisper-large-v3"
    }

    private val json = Json { 
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val apiKey: String by lazy {
        BuildConfig.GROQ_API_KEY
    }

    override suspend fun chat(
        userMessage: String,
        conversationHistory: List<ChatMessage>,
        userContext: UserContext
    ): Result<AiChatResponse> = withContext(Dispatchers.IO) {
        try {
            if (apiKey.isBlank()) {
                return@withContext Result.failure(Exception("Groq API Key not configured"))
            }

            Log.d(TAG, "Sending chat to Groq LLaMA")

            val messages = buildChatMessages(userMessage, conversationHistory, userContext)
            val responseText = callGroqChat(messages)

            Log.d(TAG, "Groq raw response: $responseText")

            val aiResponse = parseAiResponse(responseText)
            Result.success(aiResponse)
        } catch (e: Exception) {
            Log.e(TAG, "Error processing chat", e)
            Result.failure(e)
        }
    }

    override suspend fun chatWithAudio(
        audioBytes: ByteArray,
        mimeType: String,
        conversationHistory: List<ChatMessage>,
        userContext: UserContext
    ): Result<AiChatResponse> = withContext(Dispatchers.IO) {
        try {
            if (apiKey.isBlank()) {
                return@withContext Result.failure(Exception("Groq API Key not configured"))
            }

            Log.d(TAG, "Sending audio to Groq Whisper (${audioBytes.size} bytes)")

            // Step 1: Transcribe audio using Whisper
            val transcribedText = transcribeAudio(audioBytes, mimeType)
            Log.d(TAG, "Whisper transcription: $transcribedText")

            if (transcribedText.isBlank()) {
                return@withContext Result.failure(Exception("Không thể nhận dạng giọng nói"))
            }

            // Step 2: Send transcribed text to LLaMA
            val messages = buildChatMessages(transcribedText, conversationHistory, userContext)
            val responseText = callGroqChat(messages)

            Log.d(TAG, "Groq LLaMA response: $responseText")

            val aiResponse = parseAiResponse(responseText)
            Result.success(aiResponse)
        } catch (e: Exception) {
            Log.e(TAG, "Error processing audio", e)
            Result.failure(e)
        }
    }

    /**
     * Transcribe audio using Groq Whisper
     */
    private fun transcribeAudio(audioBytes: ByteArray, mimeType: String): String {
        // Map MIME type to supported extension
        val extension = when {
            mimeType.contains("mp4") -> "m4a"
            mimeType.contains("m4a") -> "m4a"
            mimeType.contains("wav") -> "wav"
            mimeType.contains("mp3") -> "mp3"
            mimeType.contains("mpeg") -> "mp3"
            mimeType.contains("webm") -> "webm"
            mimeType.contains("ogg") -> "ogg"
            mimeType.contains("opus") -> "opus"
            mimeType.contains("flac") -> "flac"
            else -> "m4a" // Default to m4a for Android MPEG_4 output
        }
        
        // Use correct MIME type for the file
        val actualMimeType = when (extension) {
            "m4a" -> "audio/m4a"
            "mp3" -> "audio/mpeg"
            "wav" -> "audio/wav"
            "webm" -> "audio/webm"
            "ogg" -> "audio/ogg"
            "opus" -> "audio/opus"
            "flac" -> "audio/flac"
            else -> "audio/m4a"
        }
        
        Log.d(TAG, "Transcribing audio: extension=$extension, mimeType=$actualMimeType, size=${audioBytes.size}")

        // Check minimum audio size to avoid hallucination on empty/short audio
        if (audioBytes.size < 5000) {
            Log.w(TAG, "Audio too short (${audioBytes.size} bytes), may cause hallucination")
        }

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file",
                "audio.$extension",
                audioBytes.toRequestBody(actualMimeType.toMediaType())
            )
            .addFormDataPart("model", WHISPER_MODEL)
            // Prompt helps Whisper understand context and reduces hallucination
            .addFormDataPart("prompt", "Đây là tin nhắn giọng nói trong ứng dụng quản lý công việc. Người dùng có thể nói về: tạo task, tạo mission, hỏi lịch trình, hoặc trò chuyện thông thường.")
            // temperature=0 reduces creativity/hallucination
            .addFormDataPart("temperature", "0")
            .build()

        val request = Request.Builder()
            .url("$GROQ_BASE_URL/audio/transcriptions")
            .header("Authorization", "Bearer $apiKey")
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            Log.e(TAG, "Whisper API error: $responseBody")
            throw Exception("Whisper API error: ${response.code}")
        }

        val jsonResponse = JSONObject(responseBody)
        val rawText = jsonResponse.optString("text", "").trim()
        
        // Filter out common Whisper hallucinations
        val hallucinations = listOf(
            "subscribe", "kênh", "video", "like", "comment", "share",
            "cảm ơn đã xem", "thanks for watching", "đăng ký", 
            "la la school", "nhạc", "music", "♪", "🎵"
        )
        
        val isHallucination = hallucinations.any { rawText.lowercase().contains(it.lowercase()) }
        
        if (isHallucination) {
            Log.w(TAG, "Detected Whisper hallucination: $rawText")
            return "" // Return empty to trigger error handling
        }
        
        return rawText
    }

    /**
     * Call Groq Chat API
     */
    private fun callGroqChat(messages: List<Map<String, String>>): String {
        val messagesArray = JSONArray()
        messages.forEach { msg ->
            val msgObj = JSONObject()
            msgObj.put("role", msg["role"])
            msgObj.put("content", msg["content"])
            messagesArray.put(msgObj)
        }

        val requestJson = JSONObject()
        requestJson.put("model", CHAT_MODEL)
        requestJson.put("messages", messagesArray)
        requestJson.put("temperature", 0.7)
        requestJson.put("max_tokens", 2048)

        val requestBody = requestJson.toString()
            .toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("$GROQ_BASE_URL/chat/completions")
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", "application/json")
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            Log.e(TAG, "Groq Chat API error: $responseBody")
            throw Exception("Groq Chat API error: ${response.code}")
        }

        val jsonResponse = JSONObject(responseBody)
        val choices = jsonResponse.getJSONArray("choices")
        if (choices.length() > 0) {
            val message = choices.getJSONObject(0).getJSONObject("message")
            return message.getString("content")
        }

        return ""
    }

    /**
     * Build chat messages array for Groq API
     */
    private fun buildChatMessages(
        userMessage: String,
        conversationHistory: List<ChatMessage>,
        userContext: UserContext
    ): List<Map<String, String>> {
        val messages = mutableListOf<Map<String, String>>()

        // System prompt
        messages.add(mapOf(
            "role" to "system",
            "content" to buildSystemPrompt(userContext)
        ))

        // Add conversation history (last 10 messages)
        conversationHistory.takeLast(10).forEach { msg ->
            val role = if (msg.role == ChatRole.USER) "user" else "assistant"
            messages.add(mapOf(
                "role" to role,
                "content" to msg.content
            ))
        }

        // Add new user message
        messages.add(mapOf(
            "role" to "user",
            "content" to userMessage
        ))

        return messages
    }

    /**
     * Parse AI response từ JSON
     */
    private fun parseAiResponse(responseText: String): AiChatResponse {
        return try {
            // Tìm JSON trong response (có thể có text thừa)
            val jsonStart = responseText.indexOf('{')
            var jsonEnd = responseText.lastIndexOf('}')
            
            if (jsonStart != -1 && jsonEnd != -1 && jsonEnd > jsonStart) {
                var jsonStr = responseText.substring(jsonStart, jsonEnd + 1)
                
                // Fix: Nếu JSON bị cắt (thiếu }), thử thêm } vào cuối
                val openBraces = jsonStr.count { it == '{' }
                val closeBraces = jsonStr.count { it == '}' }
                if (openBraces > closeBraces) {
                    jsonStr += "}".repeat(openBraces - closeBraces)
                    Log.d(TAG, "Fixed truncated JSON by adding ${openBraces - closeBraces} closing braces")
                }
                
                // Fix: AI có thể dùng "id" thay vì "taskId" hoặc "missionId"
                jsonStr = jsonStr
                    .replace("\"id\":", "\"taskId\":")
                    .replace("\"task_id\":", "\"taskId\":")
                    .replace("\"mission_id\":", "\"missionId\":")
                
                json.decodeFromString<AiChatResponse>(jsonStr)
            } else {
                // Không tìm thấy JSON, dùng response text làm message
                AiChatResponse(message = responseText, pending_command = null)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse JSON response, using as plain text", e)
            AiChatResponse(message = responseText, pending_command = null)
        }
    }

    /**
     * Build system prompt với user context
     */
    private fun buildSystemPrompt(userContext: UserContext): String {
        val now = LocalDateTime.now()
        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val currentDate = now.format(dateFormatter)
        val currentTime = now.format(timeFormatter)
        val tomorrowDate = now.plusDays(1).format(dateFormatter)

        val genderText = when (userContext.user.gender) {
            Gender.MALE -> "Male"
            Gender.FEMALE -> "Female"
            Gender.OTHER -> "Other"
        }
        
        // Get user's preferred language
        val preferredLanguage = when (userContext.preferredLanguage) {
            AppLanguage.VIETNAMESE -> "Vietnamese (Tiếng Việt)"
            AppLanguage.ENGLISH -> "English"
        }
        
        val languageInstruction = when (userContext.preferredLanguage) {
            AppLanguage.VIETNAMESE -> "Người dùng đã chọn Tiếng Việt. Hãy LUÔN trả lời bằng tiếng Việt."
            AppLanguage.ENGLISH -> "User has chosen English. ALWAYS respond in English."
        }

        val tasksFormatted = if (userContext.tasks.isEmpty()) {
            "No tasks scheduled."
        } else {
            userContext.tasks.take(20).joinToString("\n") { task ->
                val taskDate = task.startTime.format(dateFormatter)
                val taskTime = task.startTime.format(timeFormatter)
                val duration = task.durationMinutes?.let { "${it}min" } ?: "no duration"
                "- [ID:${task.id}] \"${task.title}\" on $taskDate at $taskTime ($duration)"
            }
        }

        val missionsFormatted = if (userContext.missions.isEmpty()) {
            "No missions."
        } else {
            userContext.missions.take(20).joinToString("\n") { mission ->
                val deadline = mission.deadline.format(dateFormatter)
                val status = when (mission.status) {
                    MissionStatus.ACTIVE -> "active"
                    MissionStatus.COMPLETED -> "completed"
                    MissionStatus.MISSED -> "missed"
                }
                "- [ID:${mission.id}] \"${mission.title}\" deadline: $deadline ($status)"
            }
        }

        return """
You are a friendly TodoList AI assistant named "TodoBot". $languageInstruction

## USER INFO (Remember this!)
- Name: ${userContext.user.fullName}
- Age: ${userContext.user.age}
- Gender: $genderText

## DATA TYPES
- TASK: Scheduled activity with date+time (Meeting at 14:00). Has: title, date, time, duration, repeatType.
- MISSION: Goal with deadline (Finish report by Friday). Has: title, deadline, status.

## CONTEXT
Today: $currentDate $currentTime | Tomorrow: $tomorrowDate

### User's Tasks
$tasksFormatted

### User's Missions
$missionsFormatted

## IMPORTANT RULES
1. **ALWAYS respond in $preferredLanguage**
2. **Be friendly** - Use the user's name when greeting
3. **Stay on topic** - Only discuss tasks, missions, schedules, productivity
4. **For vague dates** (birthday, Tết, weekend) - ASK for exact date
5. **Always confirm** before creating/updating/deleting anything
6. **Ignore unrelated questions** - Politely redirect to task management

## RESPONSE FORMAT
You MUST respond with ONLY valid JSON (no markdown, no backticks, no explanation):

For chat (no action needed):
{"message": "Your friendly response here", "pending_command": null}

For commands (IMPORTANT: use "taskId" for tasks, "missionId" for missions):
{"message": "Describe what you'll do and ask for confirmation", "pending_command": {"action": "CREATE_TASK|UPDATE_TASK|DELETE_TASK|CREATE_MISSION|UPDATE_MISSION|DELETE_MISSION|COMPLETE_MISSION", "params": {"title": "...", "date": "dd/MM/yyyy", "time": "HH:mm", "duration": 60, "taskId": 123, "missionId": 456}, "confirmationMessage": "..."}}

CRITICAL: For UPDATE_TASK or DELETE_TASK, you MUST include "taskId" (not "id"). For UPDATE_MISSION, DELETE_MISSION, COMPLETE_MISSION, you MUST include "missionId" (not "id").

## EXAMPLES

User: "Xin chào"
{"message": "Xin chào ${userContext.user.fullName}! 👋 Tôi là TodoBot, trợ lý quản lý công việc của bạn. Hôm nay ($currentDate) bạn cần tôi giúp gì? Tạo lịch trình mới, thêm nhiệm vụ, hay xem lịch hôm nay?", "pending_command": null}

User: "Hello"
{"message": "Hello ${userContext.user.fullName}! 👋 I'm TodoBot, your task management assistant. How can I help you today? Create a new schedule, add a mission, or check your tasks?", "pending_command": null}

User: "Hôm nay tôi có gì?"
{"message": "Xin chào ${userContext.user.fullName}! Đây là lịch của bạn hôm nay ($currentDate):\n\n📅 LỊCH TRÌNH: [list tasks or 'Không có']\n🎯 NHIỆM VỤ: [list active missions or 'Không có']\n\nBạn muốn thêm gì không?", "pending_command": null}

User: "Tạo lịch họp lúc 2 giờ chiều mai"
{"message": "Tôi sẽ tạo lịch:\n📅 Họp\n⏰ 14:00 ngày $tomorrowDate\n⏱️ 60 phút\n\nXác nhận tạo nhé?", "pending_command": {"action": "CREATE_TASK", "params": {"title": "Họp", "date": "$tomorrowDate", "time": "14:00", "duration": 60}, "confirmationMessage": "Tạo lịch Họp lúc 14:00"}}

User: "Thời tiết hôm nay thế nào?"
{"message": "Xin lỗi ${userContext.user.fullName}, tôi chỉ có thể giúp bạn quản lý lịch trình và nhiệm vụ. Bạn có muốn tạo task hay mission gì không?", "pending_command": null}

User: "Kể cho tôi một câu chuyện"
{"message": "Tôi là trợ lý quản lý công việc, không phải người kể chuyện 😊 Nhưng tôi có thể giúp ${userContext.user.fullName} tạo lịch trình hoặc theo dõi nhiệm vụ. Bạn cần gì nào?", "pending_command": null}

REMEMBER: Output ONLY the JSON object, nothing else!
""".trimIndent()
    }
}
