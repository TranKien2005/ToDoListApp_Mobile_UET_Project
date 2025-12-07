package com.example.todolist.domain.ai

import android.content.Context
import android.util.Log
import com.example.todolist.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Service để giao tiếp với Gemini AI
 * CHỈ LO GIAO TIẾP - không parse, không validate
 */
class GeminiService(private val context: Context) {

    companion object {
        private const val TAG = "GeminiService"
        // Gemini 1.5 Flash - Fast and efficient for most tasks
        private const val MODEL_NAME = "gemini-2.5-flash"
        // Alternative: "gemini-1.5-pro" for more complex tasks
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
                    maxOutputTokens = 1024
                }
            )
        }
    }

    /**
     * Gửi prompt đến Gemini và nhận RAW response (JSON string)
     * Không parse, không validate - chỉ trả về response thô
     */
    suspend fun sendPrompt(prompt: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (generativeModel == null) {
                return@withContext Result.failure(Exception("Gemini API Key not configured. Please add your API key to local.properties"))
            }

            Log.d(TAG, "Sending prompt to Gemini")

            val response = generativeModel!!.generateContent(prompt)
            val responseText = response.text ?: ""

            Log.d(TAG, "Gemini raw response: $responseText")
            Result.success(responseText)
        } catch (e: Exception) {
            Log.e(TAG, "Error calling Gemini API", e)
            Result.failure(e)
        }
    }

    /**
     * Build prompt cho voice command
     * Public để UseCase có thể tùy chỉnh nếu cần
     */
    fun buildVoiceCommandPrompt(userInput: String): String {
        val currentDate = java.time.LocalDate.now()
        val dateFormatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val todayStr = currentDate.format(dateFormatter)
        val tomorrowStr = currentDate.plusDays(1).format(dateFormatter)

        return """
You are a TODO list voice assistant for a Vietnamese/English bilingual app. Parse user voice commands and return ONLY valid JSON.

**Current Date**: $todayStr

**Available Actions**:
- CREATE_TASK: Create a new task (requires: title, time, duration)
- CREATE_MISSION: Create a new mission (requires: title, deadline)
- LIST_TASKS: List tasks (optional: filter by date)
- LIST_MISSIONS: List missions (optional: filter)
- COMPLETE_TASK: Mark task as completed
- COMPLETE_MISSION: Mark mission as completed
- DELETE_TASK: Delete a task
- DELETE_MISSION: Delete a mission

**JSON Response Format**:
{
  "action": "ACTION_NAME",
  "params": {
    "title": "task/mission title",
    "description": "optional description",
    "date": "dd/MM/yyyy (optional, default today)",
    "time": "HH:mm (optional, default 09:00)",
    "duration": "minutes (integer, for tasks only)",
    "filter": "today|week|month|all (for list operations)"
  },
  "response_text": "Vietnamese response to user"
}

**Date Parsing Rules**:
- "hôm nay" / "today" → $todayStr
- "ngày mai" / "tomorrow" → $tomorrowStr
- "tuần này" / "this week" → use filter "week"
- "tháng này" / "this month" → use filter "month"
- Specific date: convert to dd/MM/yyyy format

**Time Parsing Rules**:
- "2 giờ chiều" / "2pm" → 14:00
- "9 giờ sáng" / "9am" → 09:00
- "buổi sáng" → 09:00 (default morning)
- "buổi chiều" → 14:00 (default afternoon)

**Examples**:

User: "Tạo task họp với team lúc 2 giờ chiều mai"
Response:
{
  "action": "CREATE_TASK",
  "params": {
    "title": "Họp với team",
    "date": "$tomorrowStr",
    "time": "14:00",
    "duration": 60
  },
  "response_text": "Đã tạo task 'Họp với team' vào lúc 14:00 ngày mai. Task kéo dài 60 phút."
}

User: "Tạo mission nộp báo cáo deadline 10/12/2025"
Response:
{
  "action": "CREATE_MISSION",
  "params": {
    "title": "Nộp báo cáo",
    "date": "10/12/2025",
    "time": "23:59"
  },
  "response_text": "Đã tạo mission 'Nộp báo cáo' với deadline 10/12/2025 23:59."
}

User: "Có task nào hôm nay không"
Response:
{
  "action": "LIST_TASKS",
  "params": {
    "filter": "today"
  },
  "response_text": "Đây là danh sách các task hôm nay."
}

User: "Xem mission tuần này"
Response:
{
  "action": "LIST_MISSIONS",
  "params": {
    "filter": "week"
  },
  "response_text": "Đây là danh sách các mission trong tuần này."
}

User: "Hoàn thành task họp team"
Response:
{
  "action": "COMPLETE_TASK",
  "params": {
    "title": "Họp team"
  },
  "response_text": "Đã đánh dấu task 'Họp team' là hoàn thành."
}

**Important**: 
- Return ONLY the JSON object, no additional text
- Use Vietnamese for response_text
- If command is unclear, use action "UNKNOWN" and ask for clarification
- For tasks, default duration is 60 minutes if not specified
- For missions, default time is 23:59 if not specified

**User Command**: "$userInput"

**Your JSON Response**:
""".trimIndent()
    }

    /**
     * Gửi audio trực tiếp đến Gemini (hỗ trợ tiếng Việt tốt hơn)
     * @param audioFile File audio (WAV, MP3, etc.)
     * @return Text được transcribe từ audio
     */
    suspend fun processAudioFile(audioFile: File): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (generativeModel == null) {
                return@withContext Result.failure(Exception("Gemini API Key not configured"))
            }

            Log.d(TAG, "Sending audio to Gemini: ${audioFile.name}")

            val prompt = """
Bạn là trợ lý ứng dụng TODO list. Hãy transcribe audio này sang text (tiếng Việt hoặc tiếng Anh), 
sau đó phân tích lệnh và trả về JSON theo format sau:

{
  "action": "CREATE_TASK|CREATE_MISSION|LIST_TASKS|LIST_MISSIONS|COMPLETE_TASK|COMPLETE_MISSION|DELETE_TASK|DELETE_MISSION|UNKNOWN",
  "params": {
    "title": "tiêu đề task/mission",
    "description": "mô tả (optional)",
    "date": "dd/MM/yyyy",
    "time": "HH:mm",
    "duration": số phút (cho task),
    "filter": "today|week|month|all (cho list)"
  },
  "response_text": "Phản hồi bằng tiếng Việt"
}

Chỉ trả về JSON, không thêm text khác.
""".trimIndent()

            val content = content {
                text(prompt)
                // Fix: Thứ tự đúng là blob(mimeType, data)
                blob("audio/wav", audioFile.readBytes())
            }

            val response = generativeModel!!.generateContent(content)
            val responseText = response.text ?: ""

            Log.d(TAG, "Gemini audio response: $responseText")
            Result.success(responseText)
        } catch (e: Exception) {
            Log.e(TAG, "Error processing audio with Gemini", e)
            Result.failure(e)
        }
    }

    /**
     * Gửi audio bytes trực tiếp (không cần file)
     */
    suspend fun processAudioBytes(audioBytes: ByteArray, mimeType: String = "audio/wav"): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (generativeModel == null) {
                return@withContext Result.failure(Exception("Gemini API Key not configured"))
            }

            Log.d(TAG, "Sending audio bytes to Gemini (${audioBytes.size} bytes)")

            // Lấy ngày giờ hiện tại chính xác
            val now = java.time.LocalDateTime.now()
            val currentDate = now.toLocalDate()
            val currentTime = now.toLocalTime()
            val dateFormatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val timeFormatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm")
            val dateTimeFormatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

            val todayStr = currentDate.format(dateFormatter)
            val tomorrowStr = currentDate.plusDays(1).format(dateFormatter)
            val currentTimeStr = currentTime.format(timeFormatter)
            val currentDateTimeStr = now.format(dateTimeFormatter)
            val dayOfWeek = when (currentDate.dayOfWeek.value) {
                1 -> "Thứ Hai"
                2 -> "Thứ Ba"
                3 -> "Thứ Tư"
                4 -> "Thứ Năm"
                5 -> "Thứ Sáu"
                6 -> "Thứ Bảy"
                7 -> "Chủ Nhật"
                else -> ""
            }

            val prompt = """
# Bạn là trợ lý TODO list tiếng Việt

## THÔNG TIN HIỆN TẠI (QUAN TRỌNG!)
- **Ngày giờ hiện tại**: $currentDateTimeStr ($dayOfWeek)
- **Hôm nay**: $todayStr
- **Ngày mai**: $tomorrowStr
- **Giờ hiện tại**: $currentTimeStr

## NHIỆM VỤ
Transcribe audio tiếng Việt và phân tích thành lệnh TODO, trả về JSON format:

```json
{
  "action": "CREATE_TASK|CREATE_MISSION|LIST_TASKS|LIST_MISSIONS|COMPLETE_TASK|COMPLETE_MISSION|DELETE_TASK|DELETE_MISSION|UNKNOWN",
  "params": {
    "title": "tiêu đề",
    "description": "mô tả (optional)",
    "date": "dd/MM/yyyy",
    "time": "HH:mm",
    "duration": số phút (integer)
  },
  "response_text": "Phản hồi tiếng Việt"
}
```

## QUY TẮC QUAN TRỌNG

### 1. Xử lý ngày tháng (SỬ DỤNG NGÀY HIỆN TẠI!)
- "hôm nay" / "today" → $todayStr
- "ngày mai" / "tomorrow" → $tomorrowStr  
- "hôm qua" → ${currentDate.minusDays(1).format(dateFormatter)}
- "tuần này" → sử dụng filter "week"
- "tháng này" → sử dụng filter "month"
- "ngày kia" → ${currentDate.plusDays(2).format(dateFormatter)}
- Nếu user nói "7 giờ sáng mai" → date: "$tomorrowStr", time: "07:00"
- Nếu user nói "2 giờ chiều hôm nay" → date: "$todayStr", time: "14:00"

### 2. Xử lý thời gian
- "7 giờ sáng" / "7am" → "07:00"
- "2 giờ chiều" / "2pm" → "14:00"
- "8 giờ tối" → "20:00"
- "9h30 sáng" → "09:30"
- "buổi sáng" → "09:00"
- "buổi chiều" → "14:00"
- "buổi tối" → "19:00"

### 3. Duration mặc định
- Nếu không đề cập: 60 phút
- "1 tiếng" → 60
- "30 phút" → 30
- "2 tiếng" → 120

## VÍ DỤ

**User nói:** "Tạo task học toán 7 giờ sáng ngày mai"
**Bạn trả về:**
```json
{
  "action": "CREATE_TASK",
  "params": {
    "title": "học toán",
    "date": "$tomorrowStr",
    "time": "07:00",
    "duration": 60
  },
  "response_text": "Đã tạo task 'học toán' vào ngày mai lúc 7 giờ sáng ($tomorrowStr 07:00)."
}
```

**User nói:** "Có task nào hôm nay"
**Bạn trả về:**
```json
{
  "action": "LIST_TASKS",
  "params": {
    "filter": "today"
  },
  "response_text": "Đây là danh sách tasks hôm nay ($todayStr)."
}
```

**QUAN TRỌNG:**
- CHỈ trả về JSON, KHÔNG thêm text hay giải thích
- Sử dụng ĐÚNG ngày hiện tại ($todayStr)
- Luôn bao gồm "response_text" bằng tiếng Việt
- Nếu không rõ lệnh → action: "UNKNOWN"

**Bây giờ hãy transcribe audio và trả về JSON:**
""".trimIndent()

            val content = content {
                text(prompt)
                blob(mimeType, audioBytes)
            }

            val response = generativeModel!!.generateContent(content)
            val responseText = response.text ?: ""

            Log.d(TAG, "Gemini audio response: $responseText")
            Result.success(responseText)
        } catch (e: Exception) {
            Log.e(TAG, "Error processing audio bytes with Gemini", e)
            Result.failure(e)
        }
    }
}
