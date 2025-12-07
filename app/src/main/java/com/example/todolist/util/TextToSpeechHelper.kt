package com.example.todolist.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

/**
 * Helper class cho Text-to-Speech
 */
class TextToSpeechHelper(private val context: Context) {

    companion object {
        private const val TAG = "TextToSpeechHelper"
    }

    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false

    init {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech?.setLanguage(Locale("vi", "VN"))
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "Vietnamese language not supported, falling back to default")
                    textToSpeech?.setLanguage(Locale.getDefault())
                }
                isInitialized = true
                Log.d(TAG, "TextToSpeech initialized successfully")
            } else {
                Log.e(TAG, "TextToSpeech initialization failed")
            }
        }
    }

    /**
     * Speak text
     */
    fun speak(
        text: String,
        onStart: (() -> Unit)? = null,
        onDone: (() -> Unit)? = null,
        onError: (() -> Unit)? = null
    ) {
        if (!isInitialized) {
            Log.e(TAG, "TextToSpeech not initialized")
            onError?.invoke()
            return
        }

        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                Log.d(TAG, "Started speaking: $utteranceId")
                onStart?.invoke()
            }

            override fun onDone(utteranceId: String?) {
                Log.d(TAG, "Finished speaking: $utteranceId")
                onDone?.invoke()
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                Log.e(TAG, "Error speaking: $utteranceId")
                onError?.invoke()
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                Log.e(TAG, "Error speaking: $utteranceId, code: $errorCode")
                onError?.invoke()
            }
        })

        val utteranceId = System.currentTimeMillis().toString()
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        Log.d(TAG, "Speaking: $text")
    }

    /**
     * Stop speaking
     */
    fun stop() {
        textToSpeech?.stop()
    }

    /**
     * Check if currently speaking
     */
    fun isSpeaking(): Boolean {
        return textToSpeech?.isSpeaking ?: false
    }

    /**
     * Shutdown and release resources
     */
    fun shutdown() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        isInitialized = false
    }
}

