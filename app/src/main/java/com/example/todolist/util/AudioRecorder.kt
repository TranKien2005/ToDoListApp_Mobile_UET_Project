package com.example.todolist.util

import android.content.Context
import android.media.MediaRecorder
import android.util.Log
import java.io.File
import java.io.IOException

/**
 * Helper class để record audio và gửi đến Gemini
 * Gemini sẽ xử lý transcribe tiếng Việt tốt hơn Android Speech Recognition
 */
class AudioRecorder(private val context: Context) {

    companion object {
        private const val TAG = "AudioRecorder"
    }

    private var recorder: MediaRecorder? = null
    private var audioFile: File? = null
    private var isRecording = false

    /**
     * Bắt đầu recording
     */
    fun startRecording(): File? {
        return try {
            // Tạo file tạm để lưu audio
            // Dùng M4A format vì Groq Whisper hỗ trợ: flac, mp3, mp4, mpeg, mpga, m4a, ogg, opus, wav, webm
            audioFile = File(context.cacheDir, "voice_${System.currentTimeMillis()}.m4a")

            recorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(audioFile?.absolutePath)

                try {
                    prepare()
                    start()
                    isRecording = true
                    Log.d(TAG, "Recording started: ${audioFile?.absolutePath}")
                } catch (e: IOException) {
                    Log.e(TAG, "Failed to start recording", e)
                    return null
                }
            }

            audioFile
        } catch (e: Exception) {
            Log.e(TAG, "Error starting recording", e)
            null
        }
    }

    /**
     * Dừng recording và trả về audio data
     */
    fun stopRecording(): ByteArray? {
        return try {
            if (!isRecording) {
                Log.w(TAG, "Not recording")
                return null
            }

            recorder?.apply {
                stop()
                release()
            }
            recorder = null
            isRecording = false

            val bytes = audioFile?.readBytes()
            Log.d(TAG, "Recording stopped: ${bytes?.size ?: 0} bytes")

            // Xóa file tạm sau khi đọc
            audioFile?.delete()
            audioFile = null

            bytes
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping recording", e)
            null
        }
    }

    /**
     * Hủy recording
     */
    fun cancelRecording() {
        try {
            if (isRecording) {
                recorder?.apply {
                    stop()
                    release()
                }
                recorder = null
                isRecording = false
            }

            audioFile?.delete()
            audioFile = null

            Log.d(TAG, "Recording cancelled")
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling recording", e)
        }
    }

    /**
     * Check nếu đang recording
     */
    fun isRecording(): Boolean = isRecording

    /**
     * Cleanup
     */
    fun release() {
        cancelRecording()
    }
}

