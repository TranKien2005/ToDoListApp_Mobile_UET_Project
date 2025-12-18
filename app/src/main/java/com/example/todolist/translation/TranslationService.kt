package com.example.todolist.translation

/**
 * Interface defining the translation service contract.
 * Provides abstraction for translation operations, allowing different
 * implementations (PyTorch, cloud-based, etc.) to be swapped easily.
 */
interface TranslationService {

    /**
     * Translates the given text from source language to target language.
     *
     * @param text The text to translate
     * @param sourceLang Source language code (e.g., "vi", "en")
     * @param targetLang Target language code (e.g., "en", "vi")
     * @return Translated text or error wrapped in Result
     */
    suspend fun translate(
        text: String,
        sourceLang: String,
        targetLang: String
    ): Result<String>

    /**
     * Checks if the translation model is loaded and ready for inference.
     *
     * @return true if model is loaded, false otherwise
     */
    fun isModelLoaded(): Boolean

    /**
     * Loads the translation model into memory.
     * Should be called before attempting translation.
     *
     * @return Result indicating success or failure with error details
     */
    suspend fun loadModel(): Result<Unit>

    /**
     * Releases the model from memory.
     * Call this when translation is no longer needed to free resources.
     */
    fun releaseModel()

    /**
     * Gets the list of supported language codes.
     *
     * @return List of language codes (e.g., ["vi", "en"])
     */
    fun getSupportedLanguages(): List<String>

    /**
     * Gets the model information including parameter count.
     *
     * @return ModelInfo containing details about the loaded model
     */
    fun getModelInfo(): ModelInfo?
}

/**
 * Data class containing information about the translation model.
 */
data class ModelInfo(
    val name: String,
    val parameterCount: Long,
    val supportedLanguages: List<String>,
    val version: String
)
