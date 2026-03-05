package com.ail.lib_image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import androidx.annotation.MainThread

/**
 * 图片加载相关工具方法
 * 提供预加载、下载、缓存管理、请求控制等功能
 */
interface ImageTaskHandle {
    fun cancel()
}

object ImageLoaderUtils {
    // 单例线程池，避免每次创建新线程池
    private val diskCacheExecutor: ExecutorService by lazy { Executors.newSingleThreadExecutor() }

    /**
     * 清除单个 ImageView 的加载任务（防止列表错位）
     * @param imageView 目标ImageView
     */
    @MainThread
    fun clearImageTask(imageView: ImageView) {
        Glide.with(imageView.context).clear(imageView)
    }

    /**
     * 清除 Glide 内存缓存（需在主线程调用）
     * @param context Context
     */
    @MainThread
    fun clearMemoryCache(context: Context) {
        Glide.get(context).clearMemory()
    }

    /**
     * 清除 Glide 磁盘缓存（自动在子线程执行）
     * @param context Context
     */
    fun clearDiskCache(context: Context) {
        diskCacheExecutor.execute {
            Glide.get(context).clearDiskCache()
        }
    }

    /**
     * 同时清除内存和磁盘缓存
     * 内存缓存在主线程清除，磁盘缓存在子线程清除
     * @param context Context
     */
    @MainThread
    fun clearAllCache(context: Context) {
        clearMemoryCache(context)
        clearDiskCache(context)
    }

    /**
     * 预加载图片到内存和磁盘缓存
     * @param context Context
     * @param url 图片URL（String/Uri/File等）
     */
    @MainThread
    fun preloadImage(context: Context, url: Any?) {
        Glide.with(context).load(url).preload()
    }

    /**
     * 预加载指定大小的图片到缓存
     * @param context Context
     * @param url 图片URL
     * @param width 指定宽度（像素）
     * @param height 指定高度（像素）
     */
    @MainThread
    fun preloadImage(context: Context, url: Any?, width: Int, height: Int) {
        Glide.with(context).load(url).override(width, height).preload()
    }

    /**
     * 下载图片到本地文件
     * 文件保存在Glide缓存目录中，可通过File获取路径
     * @param context Context
     * @param url 图片URL
     * @param onSuccess 下载成功回调，返回File对象
     * @param onFailed 下载失败回调，返回实际异常信息
     */
    @MainThread
    fun downloadImage(
        context: Context,
        url: Any?,
        onSuccess: (File) -> Unit,
        onFailed: (Throwable?) -> Unit = {}
    ): ImageTaskHandle {
        var exception: Throwable? = null
        val requestManager = Glide.with(context)
        val target = object : CustomTarget<File>() {
            override fun onResourceReady(resource: File, transition: Transition<in File>?) {
                onSuccess(resource)
            }

            override fun onLoadCleared(placeholder: Drawable?) {}

            override fun onLoadFailed(errorDrawable: Drawable?) {
                onFailed(exception)
            }
        }

        requestManager
            .asFile()
            .load(url)
            .listener(object : RequestListener<File> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<File>,
                    isFirstResource: Boolean
                ): Boolean {
                    exception = e
                    return false
                }

                override fun onResourceReady(
                    resource: File,
                    model: Any,
                    target: Target<File>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean = false
            })
            .into(target)

        return object : ImageTaskHandle {
            override fun cancel() {
                requestManager.clear(target)
            }
        }
    }

    /**
     * 加载图片为 Bitmap 对象
     * @param context Context
     * @param url 图片URL
     * @param onSuccess 加载成功回调，返回Bitmap
     * @param onFailed 加载失败回调，返回实际异常信息
     */
    @MainThread
    fun loadAsBitmap(
        context: Context,
        url: Any?,
        onSuccess: (Bitmap) -> Unit,
        onFailed: (Throwable?) -> Unit = {}
    ): ImageTaskHandle {
        var exception: Throwable? = null
        val requestManager = Glide.with(context)
        val target = object : CustomTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                onSuccess(resource)
            }

            override fun onLoadCleared(placeholder: Drawable?) {}

            override fun onLoadFailed(errorDrawable: Drawable?) {
                onFailed(exception)
            }
        }

        requestManager
            .asBitmap()
            .load(url)
            .listener(object : RequestListener<Bitmap> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Bitmap>,
                    isFirstResource: Boolean
                ): Boolean {
                    exception = e
                    return false
                }

                override fun onResourceReady(
                    resource: Bitmap,
                    model: Any,
                    target: Target<Bitmap>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean = false
            })
            .into(target)

        return object : ImageTaskHandle {
            override fun cancel() {
                requestManager.clear(target)
            }
        }
    }

    /**
     * 暂停所有图片加载请求
     * 适用于列表滑动时暂停加载以提升性能
     * @param context Context
     */
    @MainThread
    fun pauseRequests(context: Context) {
        Glide.with(context).pauseRequests()
    }

    /**
     * 恢复所有图片加载请求
     * @param context Context
     */
    @MainThread
    fun resumeRequests(context: Context) {
        Glide.with(context).resumeRequests()
    }

    /**
     * 关闭磁盘缓存线程池
     * 建议在应用退出时调用，释放资源
     */
    fun shutdownExecutor() {
        if (!diskCacheExecutor.isShutdown) {
            diskCacheExecutor.shutdown()
        }
    }
}
