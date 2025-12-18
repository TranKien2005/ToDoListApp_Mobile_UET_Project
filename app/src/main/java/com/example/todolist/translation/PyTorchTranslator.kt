package com.example.todolist.translation

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.Tensor
import java.io.File
import java.io.FileOutputStream

/**
 * PyTorch-based translation implementation.
 * Loads and runs inference on a TorchScript model (best_model.pth) with ~11M parameters.
 *
 * @param context Android context for accessing assets
 * @param modelFileName Name of the model file in assets/models/ directory
 */
class PyTorchTranslator(
    private val context: Context,
    private val modelFileName: String = "best_model.pth"
) : TranslationService {

    /**
     * Secondary constructor for testing purposes only.
     * Allows initializing the translator without real Android assets.
     */
    internal constructor(context: Context, testMode: Boolean) : this(context, "test_model.pth") {
        if (testMode) {
            initializeDefaultVocabularies()
        }
    }

    companion object {
        private const val TAG = "PyTorchTranslator"
        private const val MODEL_PARAM_COUNT = 11_000_000L // ~11M parameters
        private const val MODEL_VERSION = "1.0.0"
        private const val ASSETS_MODEL_PATH = "models"
        
        // Supported language codes
        private val SUPPORTED_LANGUAGES = listOf("vi", "en")
        
        // Special tokens
        private const val PAD_TOKEN = 0
        private const val SOS_TOKEN = 1
        private const val EOS_TOKEN = 2
        private const val UNK_TOKEN = 3
        
        // Model constraints
        private const val MAX_SEQUENCE_LENGTH = 512
    }

    private var model: Module? = null
    private var vocabSrcToIdx: Map<String, Int> = emptyMap()
    private var vocabIdxToTgt: Map<Int, String> = emptyMap()

    override suspend fun translate(
        text: String,
        sourceLang: String,
        targetLang: String
    ): Result<String> = withContext(Dispatchers.Default) {
        try {
            if (!isModelLoaded()) {
                return@withContext Result.failure(
                    IllegalStateException("Model not loaded. Call loadModel() first.")
                )
            }

            if (sourceLang !in SUPPORTED_LANGUAGES || targetLang !in SUPPORTED_LANGUAGES) {
                return@withContext Result.failure(
                    IllegalArgumentException("Unsupported language pair: $sourceLang -> $targetLang")
                )
            }

            // Preprocess: tokenize input text
            val inputTokens = preprocessText(text, sourceLang)
            
            // Create input tensor
            val inputTensor = createInputTensor(inputTokens)
            
            // Run inference
            val outputTensor = runInference(inputTensor)
            
            // Postprocess: decode output tokens to text
            val translatedText = postprocessOutput(outputTensor, targetLang)
            
            Result.success(translatedText)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun isModelLoaded(): Boolean = model != null

    override suspend fun loadModel(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (isModelLoaded()) {
                return@withContext Result.success(Unit)
            }

            // Copy model from assets to cache directory for PyTorch to load
            val modelFile = copyAssetToCache("$ASSETS_MODEL_PATH/$modelFileName")
            
            // Load the TorchScript model
            model = Module.load(modelFile.absolutePath)
            
            // Load vocabularies
            loadVocabularies()
            
            android.util.Log.d(TAG, "Model loaded successfully: $modelFileName")
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Failed to load model", e)
            Result.failure(e)
        }
    }

    override fun releaseModel() {
        model?.destroy()
        model = null
        vocabSrcToIdx = emptyMap()
        vocabIdxToTgt = emptyMap()
        android.util.Log.d(TAG, "Model released")
    }

    override fun getSupportedLanguages(): List<String> = SUPPORTED_LANGUAGES

    override fun getModelInfo(): ModelInfo? {
        return if (isModelLoaded()) {
            ModelInfo(
                name = modelFileName,
                parameterCount = MODEL_PARAM_COUNT,
                supportedLanguages = SUPPORTED_LANGUAGES,
                version = MODEL_VERSION
            )
        } else {
            null
        }
    }

    // ==================== Public/Internal Helper Methods (Internal for testing) ====================

    /**
     * Copies an asset file to the app's cache directory.
     */
    private fun copyAssetToCache(assetPath: String): File {
        val cacheFile = File(context.cacheDir, assetPath.replace("/", "_"))
        
        if (!cacheFile.exists()) {
            context.assets.open(assetPath).use { inputStream ->
                FileOutputStream(cacheFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        }
        
        return cacheFile
    }

    /**
     * Loads source and target vocabularies from assets.
     * Expected files: vocab_src.txt, vocab_tgt.txt
     */
    private fun loadVocabularies() {
        try {
            // Load source vocabulary (word -> index)
            val srcVocabFile = "$ASSETS_MODEL_PATH/vocab_src.txt"
            vocabSrcToIdx = loadVocabToIndex(srcVocabFile)
            
            // Load target vocabulary (index -> word)
            val tgtVocabFile = "$ASSETS_MODEL_PATH/vocab_tgt.txt"
            vocabIdxToTgt = loadIndexToVocab(tgtVocabFile)
            
            android.util.Log.d(TAG, "Vocabularies loaded: src=${vocabSrcToIdx.size}, tgt=${vocabIdxToTgt.size}")
        } catch (e: Exception) {
            android.util.Log.w(TAG, "Could not load vocabulary files, using default tokenization", e)
            // Use simple character-level tokenization as fallback
            initializeDefaultVocabularies()
        }
    }

    private fun loadVocabToIndex(assetPath: String): Map<String, Int> {
        return context.assets.open(assetPath).bufferedReader().useLines { lines ->
            lines.mapIndexed { index, word -> word.trim() to index }.toMap()
        }
    }

    private fun loadIndexToVocab(assetPath: String): Map<Int, String> {
        return context.assets.open(assetPath).bufferedReader().useLines { lines ->
            lines.mapIndexed { index, word -> index to word.trim() }.toMap()
        }
    }

    internal fun initializeDefaultVocabularies() {
        // Fallback: simple character-level vocabulary
        val defaultVocab = mutableMapOf<String, Int>()
        defaultVocab["<pad>"] = PAD_TOKEN
        defaultVocab["<sos>"] = SOS_TOKEN
        defaultVocab["<eos>"] = EOS_TOKEN
        defaultVocab["<unk>"] = UNK_TOKEN
        
        // Add basic ASCII characters
        var idx = 4
        for (c in 'a'..'z') {
            defaultVocab[c.toString()] = idx++
        }
        for (c in 'A'..'Z') {
            defaultVocab[c.toString()] = idx++
        }
        for (c in '0'..'9') {
            defaultVocab[c.toString()] = idx++
        }
        defaultVocab[" "] = idx++
        defaultVocab["."] = idx++
        defaultVocab[","] = idx++
        defaultVocab["?"] = idx++
        defaultVocab["!"] = idx
        
        vocabSrcToIdx = defaultVocab
        vocabIdxToTgt = defaultVocab.entries.associate { it.value to it.key }
    }

    /**
     * Preprocesses input text into token indices.
     */
    internal fun preprocessText(text: String, language: String): LongArray {
        val tokens = mutableListOf<Long>()
        
        // Add start-of-sequence token
        tokens.add(SOS_TOKEN.toLong())
        
        // Tokenize based on vocabulary
        val words = text.lowercase().split(" ")
        for (word in words) {
            if (vocabSrcToIdx.containsKey(word)) {
                tokens.add(vocabSrcToIdx[word]!!.toLong())
            } else {
                // Character-level fallback for unknown words
                for (char in word) {
                    tokens.add(vocabSrcToIdx[char.toString()]?.toLong() ?: UNK_TOKEN.toLong())
                }
            }
            // Add space token between words
            tokens.add(vocabSrcToIdx[" "]?.toLong() ?: UNK_TOKEN.toLong())
        }
        
        // Remove last space and add end-of-sequence token
        if (tokens.lastOrNull() == vocabSrcToIdx[" "]?.toLong()) {
            tokens.removeAt(tokens.lastIndex)
        }
        tokens.add(EOS_TOKEN.toLong())
        
        // Pad or truncate to max sequence length
        return when {
            tokens.size >= MAX_SEQUENCE_LENGTH -> tokens.take(MAX_SEQUENCE_LENGTH).toLongArray()
            else -> {
                val padded = tokens.toLongArray().copyOf(MAX_SEQUENCE_LENGTH)
                for (i in tokens.size until MAX_SEQUENCE_LENGTH) {
                    padded[i] = PAD_TOKEN.toLong()
                }
                padded
            }
        }
    }

    /**
     * Creates a PyTorch tensor from token indices.
     */
    internal fun createInputTensor(tokens: LongArray): Tensor {
        // Shape: [1, sequence_length] for batch size of 1
        return Tensor.fromBlob(tokens, longArrayOf(1, tokens.size.toLong()))
    }

    /**
     * Runs model inference on input tensor.
     */
    private fun runInference(inputTensor: Tensor): Tensor {
        val output = model!!.forward(IValue.from(inputTensor))
        return output.toTensor()
    }

    /**
     * Postprocesses model output tensor to translated text.
     */
    internal fun postprocessOutput(outputTensor: Tensor, language: String): String {
        val outputData = outputTensor.dataAsLongArray
        
        val words = StringBuilder()
        for (idx in outputData) {
            when (idx.toInt()) {
                PAD_TOKEN, SOS_TOKEN -> continue
                EOS_TOKEN -> break
                else -> {
                    val word = vocabIdxToTgt[idx.toInt()]
                    if (word != null) {
                        words.append(word)
                    }
                }
            }
        }
        
        return words.toString().trim()
    }
}
