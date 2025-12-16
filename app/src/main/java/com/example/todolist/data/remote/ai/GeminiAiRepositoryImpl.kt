package com.example.todolist.data.remote.ai

import android.content.Context
import android.util.Log
import com.example.todolist.BuildConfig
import com.example.todolist.core.model.VoiceCommand
import com.example.todolist.domain.repository.AiRepository
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Implementation của AiRepository sử dụng Gemini AI
 * - Gửi request đến Gemini
 * - Parse response thành VoiceCommand
 */
class GeminiAiRepositoryImpl(private val context: Context) : AiRepository {

    companion object {
        private const val TAG = "GeminiAiRepository"
        private const val MODEL_NAME = "gemini-2.5-flash"
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

    override suspend fun processTextCommand(userInput: String): Result<VoiceCommand> = withContext(Dispatchers.IO) {
        try {
            if (generativeModel == null) {
                return@withContext Result.failure(Exception("Gemini API Key not configured"))
            }

            val prompt = buildPrompt(userInput)
            Log.d(TAG, "Sending text command to Gemini")

            val response = generativeModel!!.generateContent(prompt)
            val responseText = response.text ?: ""

            Log.d(TAG, "Gemini raw response: $responseText")

            val command = VoiceCommandMapper.fromJsonResponse(responseText).getOrThrow()
            VoiceCommandMapper.validateCommand(command).getOrThrow()

            Result.success(command)
        } catch (e: Exception) {
            Log.e(TAG, "Error processing text command", e)
            Result.failure(e)
        }
    }

    override suspend fun processAudioCommand(audioBytes: ByteArray, mimeType: String): Result<VoiceCommand> = withContext(Dispatchers.IO) {
        try {
            if (generativeModel == null) {
                return@withContext Result.failure(Exception("Gemini API Key not configured"))
            }

            Log.d(TAG, "Sending audio to Gemini (${audioBytes.size} bytes)")

            val prompt = buildAudioPrompt()

            val content = content {
                text(prompt)
                blob(mimeType, audioBytes)
            }

            val response = generativeModel!!.generateContent(content)
            val responseText = response.text ?: ""

            Log.d(TAG, "Gemini audio response: $responseText")

            val command = VoiceCommandMapper.fromJsonResponse(responseText).getOrThrow()
            VoiceCommandMapper.validateCommand(command).getOrThrow()

            Result.success(command)
        } catch (e: Exception) {
            Log.e(TAG, "Error processing audio command", e)
            Result.failure(e)
        }
    }

    /**
     * Build prompt linh hoạt cho voice command
     */
    private fun buildPrompt(userInput: String): String {
        val now = java.time.LocalDateTime.now()
        val currentDate = now.toLocalDate()
        val dateFormatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val todayStr = currentDate.format(dateFormatter)
        val tomorrowStr = currentDate.plusDays(1).format(dateFormatter)

        return """
Bạn là trợ lý TODO list thông minh. Phân tích yêu cầu người dùng và trả về JSON.

## NGÀY HIỆN TẠI: $todayStr

## CÁC HÀNH ĐỘNG CHÍNH
| Action | Mô tả | Params cần thiết |
|--------|-------|------------------|
| CREATE_TASK | Tạo task mới | title, date, time, duration |
| CREATE_MISSION | Tạo mission mới | title, date (deadline) |
| DELETE_TASK | Xóa task | title hoặc taskId |
| DELETE_MISSION | Xóa mission | title hoặc missionId |
| COMPLETE_MISSION | Hoàn thành mission | title hoặc missionId |
| QUERY | Hỏi/xem thông tin (để app xử lý) | query |
| CHAT | Trò chuyện thông thường | (không cần) |

## QUY TẮC NGÀY GIỜ
- "hôm nay" → $todayStr
- "ngày mai" → $tomorrowStr
- "7 giờ sáng" → "07:00"
- "2 giờ chiều" → "14:00"
- "8 giờ tối" → "20:00"
- Duration mặc định: 60 phút

## ĐỊNH DẠNG JSON
```json
{
  "action": "ACTION_NAME",
  "params": {
    "title": "tiêu đề",
    "description": "mô tả (optional)",
    "date": "dd/MM/yyyy",
    "time": "HH:mm",
    "duration": 60,
    "query": "nội dung truy vấn (cho QUERY)"
  },
  "response_text": "Phản hồi tự nhiên cho người dùng"
}
```

## HƯỚNG DẪN
1. **response_text LUÔN phải có** - đây là câu trả lời cho người dùng
2. Với QUERY: chỉ trích xuất ý định, app sẽ lấy dữ liệu và trả về
3. Với CHAT: trả lời tự nhiên như trợ lý AI
4. Với CREATE/DELETE/COMPLETE: xác nhận hành động đã thực hiện
5. Nếu không hiểu yêu cầu → action: "CHAT" và hỏi lại

## VÍ DỤ

User: "Tạo task họp team lúc 2 giờ chiều mai"
```json
{
  "action": "CREATE_TASK",
  "params": {"title": "Họp team", "date": "$tomorrowStr", "time": "14:00", "duration": 60},
  "response_text": "Đã tạo task 'Họp team' vào lúc 14:00 ngày mai."
}
```

User: "Hôm nay tôi có task gì?"
```json
{
  "action": "QUERY",
  "params": {"query": "tasks_today"},
  "response_text": "Để tôi xem các task hôm nay của bạn..."
}
```

User: "Xin chào"
```json
{
  "action": "CHAT",
  "params": {},
  "response_text": "Xin chào! Tôi là trợ lý TODO list. Bạn muốn tạo task, xem lịch hay cần giúp gì?"
}
```

User: "Xóa task họp team"
```json
{
  "action": "DELETE_TASK",
  "params": {"title": "họp team"},
  "response_text": "Đã xóa task 'họp team'."
}
```

**CHỈ TRẢ VỀ JSON, KHÔNG THÊM TEXT KHÁC**

**User**: "$userInput"
""".trimIndent()
    }

    /**
     * Build prompt cho audio processing
     */
    private fun buildAudioPrompt(): String {
        val now = java.time.LocalDateTime.now()
        val currentDate = now.toLocalDate()
        val dateFormatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val todayStr = currentDate.format(dateFormatter)
        val tomorrowStr = currentDate.plusDays(1).format(dateFormatter)

        return """
Bạn là trợ lý TODO list. Transcribe audio tiếng Việt và trả về JSON.

**Ngày hiện tại**: $todayStr | **Ngày mai**: $tomorrowStr

**Actions**: CREATE_TASK, CREATE_MISSION, DELETE_TASK, DELETE_MISSION, COMPLETE_MISSION, QUERY, CHAT

**JSON Format**:
```json
{
  "action": "ACTION_NAME",
  "params": {"title": "...", "date": "dd/MM/yyyy", "time": "HH:mm", "duration": 60, "query": "..."},
  "response_text": "Phản hồi tiếng Việt"
}
```

**Quy tắc**:
- "hôm nay" → $todayStr, "ngày mai" → $tomorrowStr
- "7 giờ sáng" → "07:00", "2 giờ chiều" → "14:00"
- response_text LUÔN phải có
- CHỈ trả về JSON

**Transcribe audio và trả về JSON:**
""".trimIndent()
    }
}
