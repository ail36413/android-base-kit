package com.ail.lib_image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * 图片加载相关工具方法
 */
object ImageLoaderUtils {
    // 单例线程池，避免每次创建新线程池
    private val diskCacheExecutor: ExecutorService by lazy { Executors.newSingleThreadExecutor() }

    /**
     * 清除单个 ImageView 的加载任务（防止列表错位）
     */
    fun clearImageTask(imageView: ImageView) {
        Glide.with(imageView.context).clear(imageView)
    }

    /**
     * 清除 Glide 内存缓存（主线程调用）
     */
    fun clearMemoryCache(context: Context) {
        Glide.get(context).clearMemory()
    }

    /**
     * 清除 Glide 磁盘缓存（需子线程调用）
     */
    fun clearDiskCache(context: Context) {
        diskCacheExecutor.execute {
            Glide.get(context).clearDiskCache()
        }
    }

    /**
     * 同时清除内存和磁盘缓存
     */
    fun clearAllCache(context: Context) {
        clearMemoryCache(context)
        clearDiskCache(context)
    }

    /**
     * 预加载图片
     */
    fun preloadImage(context: Context, url: Any?) {
        Glide.with(context).load(url).preload()
    }

    /**
     * 预加载指定大小的图片
     */
    fun preloadImage(context: Context, url: Any?, width: Int, height: Int) {
        Glide.with(context).load(url).override(width, height).preload()
    }

    /**
     * 下载图片
     */
    fun downloadImage(
        context: Context,
        url: String,
        onSuccess: (java.io.File) -> Unit,
        onFailed: (Throwable?) -> Unit = {}
    ) {
        Glide.with(context)
            .asFile()
            .load(url)
            .into(object : CustomTarget<java.io.File>() {
                override fun onResourceReady(resource: java.io.File, transition: Transition<in java.io.File>?) {
                    onSuccess(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {}

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    onFailed(null)
                }
            })
    }

    /**
     * 加载图片为 Bitmap
     */
    fun loadAsBitmap(
        context: Context,
        url: Any?,
        onSuccess: (Bitmap) -> Unit,
        onFailed: (Throwable?) -> Unit = {}
    ) {
        Glide.with(context)
            .asBitmap()
            .load(url)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    onSuccess(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {}

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    onFailed(null)
                }
            })
    }

    /**
     * 暂停请求
     */
    fun pauseRequests(context: Context) {
        Glide.with(context).pauseRequests()
    }

    /**
     * 恢复请求
     */
    fun resumeRequests(context: Context) {
        Glide.with(context).resumeRequests()
    }

    /**
     * 关闭线程池
     */
    fun shutdownExecutor() {
        if (!diskCacheExecutor.isShutdown) {
            diskCacheExecutor.shutdown()
        }
    }
}
