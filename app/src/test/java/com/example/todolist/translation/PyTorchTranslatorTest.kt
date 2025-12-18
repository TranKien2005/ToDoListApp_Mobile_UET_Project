package com.example.todolist.translation

import android.content.Context
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.pytorch.Tensor
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [28])
class PyTorchTranslatorTest {

    private lateinit var translator: PyTorchTranslator
    private lateinit var context: Context

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        // Initialize in test mode to use default vocabularies without assets
        translator = PyTorchTranslator(context, testMode = true)
    }

    @Test
    fun `test preprocessText converts text to token indices`() {
        // Arrange
        val text = "abc"
        val language = "en"
        
        // Act
        val tokens = translator.preprocessText(text, language)
        
        // Assert
        // Expected: [SOS, 'a', 'b', 'c', EOS, PAD, ..., PAD]
        assertTrue("Sequence should start with SOS", tokens[0] == 1L)
        assertTrue("Sequence should contain 'a'", tokens[1] > 3L)
        assertTrue("Sequence should contain 'b'", tokens[2] > 3L)
        assertTrue("Sequence should contain 'c'", tokens[3] > 3L)
        // Find first EOS (2L)
        val eosIndex = tokens.indexOf(2L)
        assertTrue("Sequence should contain EOS", eosIndex != -1)
        assertEquals("Sequence length should be MAX_SEQUENCE_LENGTH (512)", 512, tokens.size)
    }

    @Test
    fun `test createInputTensor creates tensor with correct shape`() {
        // Arrange
        val tokens = longArrayOf(1, 4, 5, 2)
        
        // Act
        val tensor = translator.createInputTensor(tokens)
        
        // Assert
        val shape = tensor.shape()
        assertEquals("Rank should be 2", 2, shape.size)
        assertEquals("Batch size should be 1", 1L, shape[0])
        assertEquals("Sequence length should match input", tokens.size.toLong(), shape[1])
    }

    @Test
    fun `test postprocessOutput decodes tokens back to text`() {
        // Arrange
        // Using default vocabulary indices from initializeDefaultVocabularies
        // 'a' starts at 4, 'b' is 5, 'c' is 6
        val tokens = LongArray(512) { 0L }
        tokens[0] = 1L // SOS
        tokens[1] = 4L // 'a'
        tokens[2] = 5L // 'b'
        tokens[3] = 6L // 'c'
        tokens[4] = 2L // EOS
        
        val outputTensor = Tensor.fromBlob(tokens, longArrayOf(1, 512))
        
        // Act
        val decodedText = translator.postprocessOutput(outputTensor, "en")
        
        // Assert
        assertEquals("abc", decodedText)
    }

    @Test
    fun `test fallback tokenization handles unknown characters`() {
        // Arrange
        val text = "🚀" // Emoji likely not in default vocab
        
        // Act
        val tokens = translator.preprocessText(text, "en")
        
        // Assert
        // Should contain UNK token (3L)
        assertTrue("Should contain UNK token for unknown emoji", tokens.contains(3L))
    }
}
