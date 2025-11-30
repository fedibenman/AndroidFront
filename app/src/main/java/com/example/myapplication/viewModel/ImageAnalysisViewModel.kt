package com.example.myapplication.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.myapplication.DTOs.Profile
import com.example.myapplication.Repository.ImageAnalysisRepository
import com.example.myapplication.Repository.AnalysisResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * ViewModel for handling image analysis operations
 */
class ImageAnalysisViewModel : ViewModel() {
    
    private val repository = ImageAnalysisRepository()
    
    // UI State
    var isLoading = mutableStateOf(false)
    var analysisResult = mutableStateOf<String?>(null)
    var error = mutableStateOf<String?>(null)
    
    /**
     * Analyzes an image and updates the UI state
     * @param bitmap The image to analyze
     */
    suspend fun analyzeImage(bitmap: android.graphics.Bitmap) {
        withContext(Dispatchers.IO) {
            isLoading.value = true
            error.value = null
            analysisResult.value = null
            
            try {
                val result = repository.analyzeImage(bitmap)
                
                if (result.isSuccess) {
                    val analysis = result.getOrNull()
                    val formattedResult = """
                        Analysis Results:
                        - Detected Level: ${analysis?.detectedLevel ?: "Unknown"}
                        - Confidence: ${analysis?.confidence?.toInt() ?: 0}%
                        - Recommendations: 
                          ${analysis?.recommendations?.joinToString("\n                          ") { "* $it" } ?: "-"}
                        - Message: ${analysis?.message ?: ""}
                    """.trimIndent()
                    
                    analysisResult.value = formattedResult
                } else {
                    error.value = result.exceptionOrNull()?.message ?: "Analysis failed"
                }
            } catch (e: Exception) {
                error.value = "Analysis failed: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }
    
    /**
     * Clears the current analysis result
     */
    fun clearResult() {
        analysisResult.value = null
        error.value = null
    }
}
