package com.example.todolist.data.remote.ai

import android.util.Log
import com.example.todolist.BuildConfig
import com.example.todolist.core.model.AiChatResponse
import com.example.todolist.core.model.ChatMessage
import com.example.todolist.core.model.ChatRole
import com.example.todolist.core.model.Gender
import com.example.todolist.core.model.MissionStatus
import com.example.todolist.core.model.UserContext
import com.example.todolist.domain.repository.AiRepository
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Implementation của AiRepository sử dụng Gemini AI
 * - Chat tự nhiên với AI
 * - Command optional với confirmation flow
 * - Tự động detect ngôn ngữ người dùng
 */
class GeminiAiRepositoryImpl : AiRepository {

    companion object {
        private const val TAG = "GeminiAiRepository"
        private const val MODEL_NAME = "gemini-1.5-flash"
    }

    private val json = Json { 
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val generativeModel: GenerativeModel? by lazy {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank()) {
            Log.e(TAG, "Gemini API Key is not configured")
            null
        } else {
            GenerativeModel(
                modelName = MODEL_NAME,
                apiKey = apiKey,
                generationConfig = generationConfig {
                    temperature = 0.7f
                    topK = 40
                    topP = 0.95f
                    maxOutputTokens = 2048
                }
            )
        }
    }

    override suspend fun chat(
        userMessage: String,
        conversationHistory: List<ChatMessage>,
        userContext: UserContext
    ): Result<AiChatResponse> = withContext(Dispatchers.IO) {
        try {
            if (generativeModel == null) {
                return@withContext Result.failure(Exception("Gemini API Key not configured"))
            }

            val prompt = buildChatPrompt(userMessage, conversationHistory, userContext)
            Log.d(TAG, "Sending chat to Gemini")

            val response = generativeModel!!.generateContent(prompt)
            val responseText = response.text ?: ""

            Log.d(TAG, "Gemini raw response: $responseText")

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
            if (generativeModel == null) {
                return@withContext Result.failure(Exception("Gemini API Key not configured"))
            }

            Log.d(TAG, "Sending audio to Gemini (${audioBytes.size} bytes)")

            val prompt = buildAudioPrompt(conversationHistory, userContext)

            val content = content {
                text(prompt)
                blob(mimeType, audioBytes)
            }

            val response = generativeModel!!.generateContent(content)
            val responseText = response.text ?: ""

            Log.d(TAG, "Gemini audio response: $responseText")

            val aiResponse = parseAiResponse(responseText)
            Result.success(aiResponse)
        } catch (e: Exception) {
            Log.e(TAG, "Error processing audio", e)
            Result.failure(e)
        }
    }

    /**
     * Parse AI response từ JSON
     */
    private fun parseAiResponse(responseText: String): AiChatResponse {
        return try {
            // Tìm JSON trong response (có thể có text thừa)
            val jsonStart = responseText.indexOf('{')
            val jsonEnd = responseText.lastIndexOf('}')
            
            if (jsonStart != -1 && jsonEnd != -1 && jsonEnd > jsonStart) {
                val jsonStr = responseText.substring(jsonStart, jsonEnd + 1)
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
     * Build prompt cho text chat
     */
    private fun buildChatPrompt(
        userMessage: String,
        conversationHistory: List<ChatMessage>,
        userContext: UserContext
    ): String {
        val systemPrompt = buildSystemPrompt(userContext)
        val historyPrompt = buildConversationHistory(conversationHistory)
        
        return """
$systemPrompt

$historyPrompt

User: "$userMessage"

Respond with valid JSON only:
""".trimIndent()
    }

    /**
     * Build prompt cho audio chat
     */
    private fun buildAudioPrompt(
        conversationHistory: List<ChatMessage>,
        userContext: UserContext
    ): String {
        val systemPrompt = buildSystemPrompt(userContext)
        val historyPrompt = buildConversationHistory(conversationHistory)
        
        return """
$systemPrompt

$historyPrompt

[User is speaking via voice - transcribe and respond]

Respond with valid JSON only:
""".trimIndent()
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
You are a friendly personal assistant for a to-do list app. You help users manage their tasks and missions naturally through conversation.

## CONTEXT
Current date: $currentDate
Current time: $currentTime
Tomorrow: $tomorrowDate

### User Profile
- Name: ${userContext.user.fullName}
- Age: ${userContext.user.age}
- Gender: $genderText

### User's Tasks (scheduled activities with specific time)
$tasksFormatted

### User's Missions (goals with deadlines)
$missionsFormatted

## INSTRUCTIONS
1. **Language**: Respond in the SAME language the user uses. If they write in English, respond in English. If Vietnamese, respond in Vietnamese. Match their language exactly.

2. **Natural Conversation**: Be friendly and helpful. You can discuss anything - not just tasks. Be conversational and personable.

3. **Commands are OPTIONAL**: Only include a `pending_command` when you clearly detect the user wants to:
   - CREATE a new task or mission
   - DELETE an existing task or mission
   - UPDATE an existing task or mission
   - COMPLETE a mission (mark as done)

4. **Always Ask for Confirmation**: When you detect a command intent:
   - Describe what you're about to do in your message
   - Ask the user to confirm
   - Include the command details in `pending_command`

5. **Context Awareness**: Use the user's data to give personalized responses. Reference their existing tasks/missions when relevant.

6. **Date/Time Parsing**:
   - "today" → $currentDate
   - "tomorrow" → $tomorrowDate
   - "at 2pm" → "14:00"
   - "at 9am" → "09:00"

## RESPONSE FORMAT
Always respond with valid JSON (no markdown code blocks, just raw JSON):
{
  "message": "Your natural language response to the user",
  "pending_command": null
}

OR when detecting a command:
{
  "message": "Your message explaining what you'll do and asking for confirmation",
  "pending_command": {
    "action": "CREATE_TASK|DELETE_TASK|UPDATE_TASK|CREATE_MISSION|DELETE_MISSION|UPDATE_MISSION|COMPLETE_MISSION",
    "params": {
      "title": "...",
      "description": "...",
      "date": "dd/MM/yyyy",
      "time": "HH:mm",
      "duration": 60,
      "taskId": 123,
      "missionId": 456
    },
    "confirmationMessage": "Short summary of what will happen"
  }
}

## EXAMPLES

User: "Hello!"
{"message": "Hello! How can I help you today? I can help you manage your tasks and missions, or just chat!", "pending_command": null}

User: "Thêm task họp team lúc 3 giờ chiều mai"
{"message": "Tôi sẽ tạo task 'Họp team' vào lúc 15:00 ngày $tomorrowDate. Bạn xác nhận nhé?", "pending_command": {"action": "CREATE_TASK", "params": {"title": "Họp team", "date": "$tomorrowDate", "time": "15:00", "duration": 60}, "confirmationMessage": "Tạo task 'Họp team' lúc 15:00 ngày $tomorrowDate"}}

User: "What do I have today?"
{"message": "Today you have:\n\n📋 Tasks:\n- [list their tasks]\n\n🎯 Missions:\n- [list their missions]\n\nNeed anything else?", "pending_command": null}
""".trimIndent()
    }

    /**
     * Build conversation history cho context
     */
    private fun buildConversationHistory(history: List<ChatMessage>): String {
        if (history.isEmpty()) return ""
        
        // Lấy tối đa 10 tin nhắn gần nhất
        val recentHistory = history.takeLast(10)
        
        val historyText = recentHistory.joinToString("\n") { msg ->
            val role = if (msg.role == ChatRole.USER) "User" else "Assistant"
            "$role: ${msg.content}"
        }
        
        return """
## CONVERSATION HISTORY
$historyText
""".trimIndent()
    }
}
