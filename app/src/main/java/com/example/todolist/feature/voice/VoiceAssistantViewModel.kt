package com.example.todolist.feature.voice

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.core.model.AppLanguage
import com.example.todolist.core.model.ChatMessage
import com.example.todolist.core.model.ChatRole
import com.example.todolist.core.model.Mission
import com.example.todolist.core.model.PendingCommand
import com.example.todolist.core.model.Task
import com.example.todolist.core.model.User
import com.example.todolist.core.model.UserContext
import com.example.todolist.domain.usecase.AIUseCases
import com.example.todolist.domain.usecase.MissionUseCases
import com.example.todolist.domain.usecase.SettingsUseCases
import com.example.todolist.domain.usecase.TaskUseCases
import com.example.todolist.domain.usecase.UserUseCases
import com.example.todolist.util.AudioRecorder
import com.example.todolist.util.TextToSpeechHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * UI State cho Voice Assistant
 */
data class VoiceAssistantUiState(
    val isListening: Boolean = false,
    val isSpeaking: Boolean = false,
    val isProcessing: Boolean = false,
    val conversationHistory: List<ChatMessage> = emptyList(),
    val pendingCommand: PendingCommand? = null,  // Command đang chờ confirm
    val error: String? = null
)

/**
 * ViewModel cho Voice Assistant
 * Hỗ trợ cả text chat và voice input trong cùng một cuộc hội thoại
 */
class VoiceAssistantViewModel(
    private val taskUseCases: TaskUseCases,
    private val missionUseCases: MissionUseCases,
    private val userUseCases: UserUseCases,
    private val settingsUseCases: SettingsUseCases,
    private val aiUseCases: AIUseCases,
    context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(VoiceAssistantUiState())
    val uiState: StateFlow<VoiceAssistantUiState> = _uiState.asStateFlow()

    private val audioRecorder = AudioRecorder(context)
    private val ttsHelper = TextToSpeechHelper(context)

    // Cache user context
    private var cachedUser: User? = null
    private var cachedTasks: List<Task> = emptyList()
    private var cachedMissions: List<Mission> = emptyList()
    private var cachedLanguage: AppLanguage = AppLanguage.VIETNAMESE

    init {
        // Load initial context
        loadUserContext()
    }

    /**
     * Load user context (user info + tasks + missions + settings)
     */
    private fun loadUserContext() {
        viewModelScope.launch {
            try {
                cachedUser = userUseCases.getUser().first()
                cachedTasks = taskUseCases.getTasks().first()
                cachedMissions = missionUseCases.getMissions().first()
                cachedLanguage = settingsUseCases.getSettings().first().language
            } catch (e: Exception) {
                // Ignore errors, will use defaults
            }
        }
    }

    /**
     * Get current user context
     */
    private suspend fun getUserContext(): UserContext {
        // Refresh context
        val user = cachedUser ?: userUseCases.getUser().first()
        val tasks = taskUseCases.getTasks().first()
        val missions = missionUseCases.getMissions().first()
        val language = try {
            settingsUseCases.getSettings().first().language
        } catch (e: Exception) {
            cachedLanguage
        }
        
        cachedUser = user
        cachedTasks = tasks
        cachedMissions = missions
        cachedLanguage = language
        
        // Ensure user is not null, use default if needed
        val safeUser = user ?: User(
            fullName = "User",
            age = 25,
            gender = com.example.todolist.core.model.Gender.OTHER
        )
        return UserContext(safeUser, tasks, missions, language)
    }

    /**
     * Xử lý text input từ người dùng
     */
    fun processTextInput(text: String) {
        if (text.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isProcessing = true, 
                error = null,
                pendingCommand = null  // Clear any pending command
            )

            // Thêm user message vào conversation
            val userMessage = ChatMessage(
                role = ChatRole.USER,
                content = text
            )
            addToConversation(userMessage)

            // Lấy context và conversation history
            val userContext = getUserContext()
            val history = _uiState.value.conversationHistory

            // Gửi đến AI
            val result = aiUseCases.chatWithAI(text, history, userContext)

            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    error = "Lỗi: ${result.exceptionOrNull()?.message}"
                )
                addErrorMessage("Xin lỗi, đã có lỗi xảy ra. Vui lòng thử lại.")
                return@launch
            }

            val aiResponse = result.getOrNull()!!

            // Thêm AI response vào conversation
            val assistantMessage = ChatMessage(
                role = ChatRole.ASSISTANT,
                content = aiResponse.message,
                pendingCommand = aiResponse.pending_command
            )
            addToConversation(assistantMessage)

            // Cập nhật pending command nếu có
            _uiState.value = _uiState.value.copy(
                isProcessing = false,
                pendingCommand = aiResponse.pending_command
            )
        }
    }

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
                error = "Không thể bắt đầu ghi âm"
            )
        }
    }

    /**
     * Dừng recording và gửi audio đến AI
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

        // Gửi audio trực tiếp đến AI
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
     * Xử lý audio với AI
     */
    private fun processAudioInput(audioBytes: ByteArray) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isProcessing = true,
                error = null,
                pendingCommand = null
            )

            // Thêm placeholder user message
            val userMessage = ChatMessage(
                role = ChatRole.USER,
                content = "🎤 Voice message..."
            )
            addToConversation(userMessage)

            // Lấy context và history
            val userContext = getUserContext()
            val history = _uiState.value.conversationHistory

            // Gửi audio đến AI
            val result = aiUseCases.chatWithAudio(audioBytes, "audio/mp4", history, userContext)

            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    error = "Lỗi xử lý audio: ${result.exceptionOrNull()?.message}"
                )
                addErrorMessage("Xin lỗi, không thể xử lý audio. Vui lòng thử lại.")
                return@launch
            }

            val aiResponse = result.getOrNull()!!

            // Thêm AI response vào conversation
            val assistantMessage = ChatMessage(
                role = ChatRole.ASSISTANT,
                content = aiResponse.message,
                pendingCommand = aiResponse.pending_command
            )
            addToConversation(assistantMessage)

            // Đọc phản hồi bằng TTS nếu có
            speakResponse(aiResponse.message)

            // Cập nhật pending command
            _uiState.value = _uiState.value.copy(
                isProcessing = false,
                pendingCommand = aiResponse.pending_command
            )
        }
    }

    /**
     * Xác nhận thực hiện pending command
     */
    fun confirmPendingCommand() {
        val command = _uiState.value.pendingCommand ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isProcessing = true,
                pendingCommand = null  // Clear pending command
            )

            // Thêm confirm message từ user
            val confirmMessage = ChatMessage(
                role = ChatRole.USER,
                content = "✓ Xác nhận"
            )
            addToConversation(confirmMessage)

            // Thực thi command
            val result = aiUseCases.executeCommand(command)

            val responseMessage = if (result.isSuccess) {
                result.getOrNull() ?: "Đã thực hiện thành công!"
            } else {
                "Lỗi: ${result.exceptionOrNull()?.message}"
            }

            // Thêm response
            val assistantMessage = ChatMessage(
                role = ChatRole.ASSISTANT,
                content = responseMessage
            )
            addToConversation(assistantMessage)

            // Refresh context sau khi thực thi command
            loadUserContext()

            _uiState.value = _uiState.value.copy(
                isProcessing = false,
                error = if (result.isFailure) result.exceptionOrNull()?.message else null
            )
        }
    }

    /**
     * Hủy pending command
     */
    fun cancelPendingCommand() {
        val command = _uiState.value.pendingCommand ?: return

        // Thêm cancel message
        val cancelMessage = ChatMessage(
            role = ChatRole.USER,
            content = "✗ Hủy"
        )
        addToConversation(cancelMessage)

        val assistantMessage = ChatMessage(
            role = ChatRole.ASSISTANT,
            content = "Đã hủy. Bạn cần gì khác không?"
        )
        addToConversation(assistantMessage)

        _uiState.value = _uiState.value.copy(pendingCommand = null)
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
     * Thêm tin nhắn vào lịch sử
     */
    private fun addToConversation(message: ChatMessage) {
        _uiState.value = _uiState.value.copy(
            conversationHistory = _uiState.value.conversationHistory + message
        )
    }

    /**
     * Thêm error message từ AI
     */
    private fun addErrorMessage(text: String) {
        addToConversation(
            ChatMessage(
                role = ChatRole.ASSISTANT,
                content = text
            )
        )
    }

    /**
     * Xóa lịch sử hội thoại
     */
    fun clearConversation() {
        _uiState.value = _uiState.value.copy(
            conversationHistory = emptyList(),
            pendingCommand = null,
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
