package com.example.myapplication.cache

import android.content.Context
import android.graphics.Bitmap
import android.util.LruCache
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import java.lang.ref.WeakReference

object ImageCacheManager {
    private const val MEMORY_CACHE_SIZE = 10 * 1024 * 1024 // 10MB
    private const val PRELOAD_COUNT = 5 // 预加载数量

    private lateinit var contextRef: WeakReference<Context>
    private val memoryCache = object : LruCache<String, Bitmap>(MEMORY_CACHE_SIZE) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return bitmap.byteCount / 1024
        }
    }

    /**
     * 初始化缓存管理器
     */
    fun initialize(context: Context) {
        contextRef = WeakReference(context)
    }

    /**
     * 预加载图片到内存缓存
     */
    fun preloadImages(imageUrls: List<String>) {
        val context = contextRef.get() ?: return

        // 只预加载前 PRELOAD_COUNT 张图片，避免过度预加载
        imageUrls.take(PRELOAD_COUNT).forEach { url ->
            try {
                Glide.with(context)
                    .asBitmap()
                    .load(url)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .submit()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 从内存缓存获取图片
     */
    fun getFromMemoryCache(key: String): Bitmap? {
        return try {
            memoryCache.get(key)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 添加图片到内存缓存
     */
    fun putToMemoryCache(key: String, bitmap: Bitmap) {
        try {
            if (getFromMemoryCache(key) == null) {
                memoryCache.put(key, bitmap)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 清理所有缓存
     */
    fun clearCache() {
        try {
            // 清理内存缓存
            memoryCache.evictAll()

            // 清理Glide内存缓存
            val context = contextRef.get() ?: return
            Glide.get(context).clearMemory()

            // 在后台线程清理磁盘缓存
            Thread {
                try {
                    Glide.get(context).clearDiskCache()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 获取缓存统计信息
     */
    fun getCacheStats(): String {
        return "内存缓存: ${memoryCache.size()}KB / ${MEMORY_CACHE_SIZE}KB"
    }
}