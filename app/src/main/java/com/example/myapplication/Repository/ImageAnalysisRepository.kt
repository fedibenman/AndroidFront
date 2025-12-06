package com.example.myapplication.Repository

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * Repository for handling image analysis operations
 * This class handles sending images to the backend for level detection
 */
class ImageAnalysisRepository {
    
<<<<<<< HEAD
    private val baseUrl = "http://192.168.238.182:3001/analyze"
=======
    private val baseUrl = "http://10.0.2.2:3001/analyze"
>>>>>>> d8587fa0d8c2db899b8a6ef793e73a5a7e4d3b1b
    
    /**
     * Analyzes an image and returns the detected level
     * @param bitmap The image to analyze
     * @return AnalysisResult containing the detected level and other information
     */
    suspend fun analyzeImage(bitmap: Bitmap): Result<ImageAnalysisResponseDto> {
        return withContext(Dispatchers.IO) {
            try {
                // Convert bitmap to base64
                val base64String = convertBitmapToBase64(bitmap)

                // Create the request body matching the Nest DTO
                val requestBody = """
                    {
                        "image": "$base64String",
                        "imageType": "image/jpeg"
                    }
                """.trimIndent()
                val jsonBody = JSONObject().apply {
                    put("image", base64String)
                    put("imageType", "image/jpeg")
                }.toString()

                // Log the request
                Log.d("ImageAnalysis", "Sending request to: $baseUrl")
                Log.d("ImageAnalysis", "Request body: $requestBody")

                // Make the API call
                val response = makeApiCall(jsonBody)


                // Log the response
                Log.d("ImageAnalysis", "Response: $response")

                // Parse the response
                val result = parseResponse(response)

                Result.success(result)
            } catch (e: Exception) {
                Log.e("ImageAnalysis", "Error during analysis: ${e.message}", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Converts a Bitmap to Base64 encoded string
     */
    private fun convertBitmapToBase64(bitmap: Bitmap): String {
        val output = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output)
        val byteArray = output.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)  // FIXED
    }

    
    /**
     * Makes the HTTP API call to the backend
     */
    private fun makeApiCall(requestBody: String): String {
        val url = URL(baseUrl)
        val connection = url.openConnection() as HttpURLConnection
        
        try {
            // Configure the connection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Accept", "application/json")
            
            // Write the request body
            connection.outputStream.use { outputStream ->
                outputStream.write(requestBody.toByteArray(Charsets.UTF_8))
                outputStream.flush()
            }
            
            // Read the response
            val responseCode = connection.responseCode
            Log.d("ImageAnalysis", "HTTP Response Code: $responseCode")
            
            val response = if (responseCode in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "HTTP Error: $responseCode"
            }
            
            Log.d("ImageAnalysis", "Raw response: $response")
            
            if (responseCode !in 200..299) {
                throw IOException("HTTP Error: $responseCode - $response")
            }
            
            return response
        } catch (e: Exception) {
            Log.e("ImageAnalysis", "Network error: ${e.message}", e)
            throw e
        } finally {
            connection.disconnect()
        }
    }
    
    /**
     * Parses the JSON response from the backend
     */
    private fun parseResponse(response: String): ImageAnalysisResponseDto {
        try {
            val jsonObject = JSONObject(response)
            
            // Extract data from JSON response matching the Nest DTO
            val success = jsonObject.optBoolean("success", false)
            val message = jsonObject.optString("message", "")
            
            return ImageAnalysisResponseDto(
                success = success,
                message = message
            )
        } catch (e: Exception) {
            throw IOException("Failed to parse response: ${e.message}")
        }
    }
}

/**
 * Data class representing the image analysis request DTO
 */
data class ImageAnalysisRequestDto(
    val image: String,
    val imageType: String = "image/jpeg"
)


/**
 * Data class representing the image analysis response DTO
 */
data class ImageAnalysisResponseDto(
    val success: Boolean,
    val message: String?
)

