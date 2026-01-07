package com.example.myapplication.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class ImageCacheManager private constructor(private val cacheDir: File) {
    
    // In-memory cache (20% of available memory)
    private val memoryCache: LruCache<String, Bitmap>
    
    // Disk cache directory
    private val diskCacheDir: File = File(cacheDir, "image_cache")
    
    init {
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        val cacheSize = maxMemory / 5
        
        memoryCache = object : LruCache<String, Bitmap>(cacheSize) {
            override fun sizeOf(key: String, bitmap: Bitmap): Int {
                return bitmap.byteCount / 1024
            }
        }
        
        // Create disk cache directory if it doesn't exist
        if (!diskCacheDir.exists()) {
            diskCacheDir.mkdirs()
        }
    }
    
    suspend fun loadImage(url: String): Bitmap? = withContext(Dispatchers.IO) {
        // Check memory cache first
        memoryCache.get(url)?.let { return@withContext it }
        
        // Check disk cache
        val diskFile = getDiskCacheFile(url)
        if (diskFile.exists()) {
            val bitmap = BitmapFactory.decodeFile(diskFile.absolutePath)
            bitmap?.let {
                memoryCache.put(url, it)
                return@withContext it
            }
        }
        
        // Download from network
        try {
            val connection = URL(url).openConnection()
            connection.connect()
            val inputStream = connection.getInputStream()
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            
            bitmap?.let {
                // Save to caches
                memoryCache.put(url, it)
                saveToDiskCache(url, it)
                return@withContext it
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        null
    }
    
    private fun getDiskCacheFile(url: String): File {
        val filename = url.hashCode().toString()
        return File(diskCacheDir, filename)
    }
    
    private fun saveToDiskCache(url: String, bitmap: Bitmap) {
        try {
            val file = getDiskCacheFile(url)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun clearCache() {
        memoryCache.evictAll()
        diskCacheDir.listFiles()?.forEach { it.delete() }
    }
    
    companion object {
        @Volatile
        private var INSTANCE: ImageCacheManager? = null
        
        fun getInstance(cacheDir: File): ImageCacheManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ImageCacheManager(cacheDir).also { INSTANCE = it }
            }
        }
    }
}
