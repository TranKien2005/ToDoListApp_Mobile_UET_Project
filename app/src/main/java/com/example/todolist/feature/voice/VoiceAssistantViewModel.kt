package com.example.todolist.feature.voice

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.domain.usecase.AIUseCases
import com.example.todolist.domain.usecase.MissionUseCases
import com.example.todolist.domain.usecase.TaskUseCases
import com.example.todolist.util.AudioRecorder
import com.example.todolist.util.TextToSpeechHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI State cho Voice Assistant
 */
data class VoiceAssistantUiState(
    val isListening: Boolean = false,
    val isSpeaking: Boolean = false,
    val isProcessing: Boolean = false,
    val userInput: String = "",
    val aiResponse: String = "",
    val error: String? = null,
    val conversationHistory: List<ConversationItem> = emptyList()
)

/**
 * Conversation item
 */
data class ConversationItem(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * ViewModel cho Voice Assistant
 * Sử dụng AUDIO trực tiếp - không dùng text chat
 */
class VoiceAssistantViewModel(
    private val taskUseCases: TaskUseCases,
    private val missionUseCases: MissionUseCases,
    private val aiUseCases: AIUseCases,
    context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(VoiceAssistantUiState())
    val uiState: StateFlow<VoiceAssistantUiState> = _uiState.asStateFlow()

    private val audioRecorder = AudioRecorder(context)
    private val ttsHelper = TextToSpeechHelper(context)

    /**
     * Bắt đầu recording audio
     */
    fun startListening() {
        _uiState.value = _uiState.value.copy(
            isListening = true,
            error = null
        )

        val audioFile = audioRecorder.startRecording()
        if (audioFile == null) {
            _uiState.value = _uiState.value.copy(
                isListening = false,
                error = "Không thể bắt đầu recording audio"
            )
        }
    }

    /**
     * Dừng recording và gửi audio đến Gemini
     */
    fun stopListening() {
        _uiState.value = _uiState.value.copy(isListening = false)

        val audioBytes = audioRecorder.stopRecording()
        if (audioBytes == null) {
            _uiState.value = _uiState.value.copy(
                error = "Không thể lấy audio data"
            )
            return
        }

        // Gửi audio trực tiếp đến Gemini (hỗ trợ tiếng Việt tốt!)
        processAudioInput(audioBytes)
    }

    /**
     * Hủy recording
     */
    fun cancelListening() {
        audioRecorder.cancelRecording()
        _uiState.value = _uiState.value.copy(
            isListening = false,
            error = null
        )
    }

    /**
     * Xử lý audio với Gemini (transcribe + parse)
     */
    private fun processAudioInput(audioBytes: ByteArray) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true, error = null)

            // Step 1: Gửi audio đến Gemini (transcribe + parse)
            val commandResult = aiUseCases.processAudioCommand(audioBytes, "audio/3gpp")
            if (commandResult.isFailure) {
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    error = "Lỗi xử lý audio: ${commandResult.exceptionOrNull()?.message}"
                )
                return@launch
            }

            val command = commandResult.getOrNull()!!

            // Thêm vào conversation (user input từ AI transcribe)
            addToConversation(command.params.title ?: "Voice command", isUser = true)

            // Step 2: Thực thi command
            val executeResult = aiUseCases.executeVoiceCommand(command)
            if (executeResult.isFailure) {
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    error = "Lỗi thực thi: ${executeResult.exceptionOrNull()?.message}"
                )
                return@launch
            }

            // Step 3: Phản hồi bằng giọng nói
            val responseText = executeResult.getOrNull() ?: command.responseText
            speakResponse(responseText)
            addToConversation(responseText, isUser = false)

            _uiState.value = _uiState.value.copy(
                isProcessing = false,
                aiResponse = responseText
            )
        }
    }

    /**
     * Đọc phản hồi bằng TTS
     */
    private fun speakResponse(text: String) {
        _uiState.value = _uiState.value.copy(isSpeaking = true)

        ttsHelper.speak(
            text = text,
            onDone = {
                _uiState.value = _uiState.value.copy(isSpeaking = false)
            },
            onError = {
                _uiState.value = _uiState.value.copy(
                    isSpeaking = false,
                    error = "Lỗi text-to-speech"
                )
            }
        )
    }

    /**
     * Dừng đọc
     */
    fun stopSpeaking() {
        ttsHelper.stop()
        _uiState.value = _uiState.value.copy(isSpeaking = false)
    }

    /**
     * Thêm vào lịch sử hội thoại
     */
    private fun addToConversation(text: String, isUser: Boolean) {
        val newItem = ConversationItem(text, isUser)
        _uiState.value = _uiState.value.copy(
            conversationHistory = _uiState.value.conversationHistory + newItem
        )
    }

    /**
     * Xóa lịch sử
     */
    fun clearConversation() {
        _uiState.value = _uiState.value.copy(
            conversationHistory = emptyList(),
            userInput = "",
            aiResponse = "",
            error = null
        )
    }

    /**
     * Cleanup
     */
    override fun onCleared() {
        super.onCleared()
        audioRecorder.release()
        ttsHelper.shutdown()
    }
}
