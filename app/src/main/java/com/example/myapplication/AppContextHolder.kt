package com.example.myapplication

import android.app.Application
import android.content.Context
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy

class MyApp : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        AppContextHolder.init(this)
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(100 * 1024 * 1024)
                    .build()
            }
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .crossfade(true)
            .respectCacheHeaders(false)
            .build()
    }
}

object AppContextHolder {
    private lateinit var appContextInternal: Context

    fun init(context: Context) {
        appContextInternal = context.applicationContext
    }

    val appContext: Context
        get() = appContextInternal
}


