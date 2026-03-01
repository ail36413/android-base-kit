package com.ail.lib_image

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import jp.wasabeef.glide.transformations.BlurTransformation
import jp.wasabeef.glide.transformations.ColorFilterTransformation
import jp.wasabeef.glide.transformations.GrayscaleTransformation

private const val TAG_DETACH_LISTENER_KEY = -10002


/**
 * 图片加载参数配置
 * @property transformations 支持 DSL 赋值和追加
 */
data class ImageLoadOptions(
    val url: Any? = null,
    val placeholder: Int? = null,
    val error: Int? = null,
    val placeholderDrawable: Drawable? = null,
    val errorDrawable: Drawable? = null,
    val isCircle: Boolean = false,
    val radius: Float? = null,
    val radiusInDp: Boolean = true,
    val isBlur: Boolean = false,
    val blurRadius: Int = 25,
    val blurSampling: Int = 1,
    val isGray: Boolean = false,
    val colorFilter: Int? = null,
    val overrideWidth: Int? = null,
    val overrideHeight: Int? = null,
    val cacheStrategy: DiskCacheStrategy = DiskCacheStrategy.AUTOMATIC,
    val skipMemoryCache: Boolean = false,
    val skipDiskCache: Boolean = false,
    val cancelOnDetach: Boolean = false,
    val callback: ImageLoadCallback? = null,
    var transformations: List<Transformation<android.graphics.Bitmap>> = emptyList(),
    // 新增：动画/过渡效果配置
    val transitionDuration: Int = 300, // 过渡动画时长（毫秒）
    val disableTransition: Boolean = false, // 是否禁用过渡动画
    // 新增：加载优先级
    val priority: com.bumptech.glide.Priority = com.bumptech.glide.Priority.NORMAL, // 加载优先级
    // 新增：图片解码格式
    val decodeFormat: com.bumptech.glide.load.DecodeFormat = com.bumptech.glide.load.DecodeFormat.DEFAULT, // 解码格式
    // 新增：缩略图相关
    val thumbnailUrl: Any? = null, // 缩略图URL
    val thumbnailSize: Float = 0.1f // 缩略图比例（0.0-1.0）
) {
    /**
     * 追加自定义变换（DSL友好）
     */
    fun addTransformation(transformation: Transformation<android.graphics.Bitmap>) {
        transformations = transformations + transformation
    }
}

/**
 * 图片加载回调接口
 * 可用于监听加载开始、成功、失败事件。
 *
 * onStart() 加载开始
 * onSuccess(drawable) 加载成功，返回Drawable
 * onFailed(throwable) 加载失败，返回异常
 */
interface ImageLoadCallback {
    fun onStart() {} // 加载开始
    fun onSuccess(drawable: Drawable) {} // 加载成功
    fun onFailed(throwable: Throwable?) {} // 加载失败
}

/**
 * ImageView扩展函数，支持所有参数和DSL方式。
 * 推荐直接调用 imageView.load(...)，或使用DSL imageView.load { ... }
 * 支持圆形、圆角、模糊、占位/错误图、缓存策略、尺寸覆盖、回调等。
 *
 * @param url 图片来源（String、Uri、File、Bitmap等）
 * @param placeholder 占位图资源ID（Int）
 * @param error 错误图资源ID（Int）
 * @param placeholderDrawable 占位图Drawable（Drawable）
 * @param errorDrawable 错误图Drawable（Drawable）
 * @param isCircle 是否圆形裁剪（Boolean）
 * @param radius 圆角半径（Float）
 * @param radiusInDp 圆角单位是否为dp（Boolean）
 * @param isBlur 是否高斯模糊（Boolean）
 * @param blurRadius 模糊半径（Int）
 * @param blurSampling 模糊采样率（Int）
 * @param isGray 是否灰度（Boolean）
 * @param colorFilter 色彩滤镜（Int，ARGB）
 * @param overrideWidth 指定宽度（Int）
 * @param overrideHeight 指定高度（Int）
 * @param cacheStrategy 磁盘缓存策略（DiskCacheStrategy）
 * @param skipMemoryCache 跳过内存缓存（Boolean）
 * @param skipDiskCache 跳过磁盘缓存（Boolean）
 * @param cancelOnDetach View脱离窗口时取消加载（Boolean）
 * @param callback 加载回调（ImageLoadCallback）
 * @param transformations 自定义变换（List<Transformation<Bitmap>>），可用于扩展更多效果
 * @param options DSL配置（ImageLoadOptions.() -> Unit），支持 transformations = listOf(...) 或 addTransformation(...)
 */
fun ImageView.load(
    url: Any? = null,
    placeholder: Int? = null,
    error: Int? = null,
    placeholderDrawable: Drawable? = null,
    errorDrawable: Drawable? = null,
    isCircle: Boolean = false,
    radius: Float? = null,
    radiusInDp: Boolean = true,
    isBlur: Boolean = false,
    blurRadius: Int = 25,
    blurSampling: Int = 1,
    isGray: Boolean = false,
    colorFilter: Int? = null,
    overrideWidth: Int? = null,
    overrideHeight: Int? = null,
    cacheStrategy: DiskCacheStrategy = DiskCacheStrategy.AUTOMATIC,
    skipMemoryCache: Boolean = false,
    skipDiskCache: Boolean = false,
    cancelOnDetach: Boolean = false,
    callback: ImageLoadCallback? = null,
    transformations: List<Transformation<android.graphics.Bitmap>> = emptyList(),
    transitionDuration: Int = 300,
    disableTransition: Boolean = false,
    priority: com.bumptech.glide.Priority = com.bumptech.glide.Priority.NORMAL,
    decodeFormat: com.bumptech.glide.load.DecodeFormat = com.bumptech.glide.load.DecodeFormat.DEFAULT,
    thumbnailUrl: Any? = null,
    thumbnailSize: Float = 0.1f,
    options: (ImageLoadOptions.() -> Unit)? = null
) {
    // 构建参数对象，支持DSL
    val opts = ImageLoadOptions(
        url, placeholder, error, placeholderDrawable, errorDrawable, isCircle, radius, radiusInDp,
        isBlur, blurRadius, blurSampling, isGray, colorFilter, overrideWidth, overrideHeight, cacheStrategy,
        skipMemoryCache, skipDiskCache, cancelOnDetach, callback, transformations,
        transitionDuration, disableTransition, priority, decodeFormat, thumbnailUrl, thumbnailSize
    ).apply { options?.invoke(this) }
    val context = this.context
    val glide = Glide.with(context)
    // cancelOnDetach: 监听View detach自动clear，或清理旧监听器
    val oldListener = this.getTag(TAG_DETACH_LISTENER_KEY) as? View.OnAttachStateChangeListener
    if (oldListener != null) {
        this.removeOnAttachStateChangeListener(oldListener)
        this.setTag(TAG_DETACH_LISTENER_KEY, null)
    }
    if (opts.cancelOnDetach) {
        val detachListener = object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {}
            override fun onViewDetachedFromWindow(v: View) {
                Glide.with(context).clear(this@load)
            }
        }
        this.addOnAttachStateChangeListener(detachListener)
        this.setTag(TAG_DETACH_LISTENER_KEY, detachListener)
    }
    // 加载前清除上一次任务，减少列表错位
    Glide.with(context).clear(this)
    opts.callback?.onStart() // 回调提前
    // 构建 Glide 请求
    var request = glide.load(opts.url)
        .diskCacheStrategy(opts.cacheStrategy)
        .skipMemoryCache(opts.skipMemoryCache)
        .apply(RequestOptions().priority(opts.priority).format(opts.decodeFormat))
    // 过渡动画配置
    if (!opts.disableTransition) {
        request = request.transition(DrawableTransitionOptions.withCrossFade(opts.transitionDuration))
    }
    // 缩略图配置
    if (opts.thumbnailUrl != null) {
        request = request.thumbnail(glide.load(opts.thumbnailUrl))
    } else if (opts.thumbnailSize in 0.0f..1.0f && opts.thumbnailSize != 0f) {
        request = request.thumbnail(opts.thumbnailSize)
    }
    if (opts.skipDiskCache) request.diskCacheStrategy(DiskCacheStrategy.NONE)
    // 尺寸覆盖
    opts.overrideWidth?.let { w -> opts.overrideHeight?.let { h -> request.override(w, h) } }
    // 占位/错误图优先使用Drawable
    opts.placeholderDrawable?.let { request.placeholder(it) } ?: opts.placeholder?.let { request.placeholder(it) }
    opts.errorDrawable?.let { request.error(it) } ?: opts.error?.let { request.error(it) }
    // 变换列表
    val transforms = mutableListOf<Transformation<android.graphics.Bitmap>>()
    // 移除强制 CenterCrop，变换仅根据参数自动添加
    if (opts.isCircle) transforms.add(CircleCrop()) // 圆形裁剪
    else if (opts.radius != null && opts.radius > 0) {
        // 圆角裁剪，支持dp/px
        val px = if (opts.radiusInDp) (opts.radius * context.resources.displayMetrics.density).toInt() else opts.radius.toInt()
        transforms.add(RoundedCorners(px))
    }
    if (opts.isBlur && opts.blurRadius > 0) transforms.add(BlurTransformation(opts.blurRadius, opts.blurSampling)) // 高斯模糊
    if (opts.isGray) transforms.add(GrayscaleTransformation()) // 灰度
    opts.colorFilter?.let { transforms.add(ColorFilterTransformation(it)) } // 色彩滤镜
    if (opts.transformations.isNotEmpty()) transforms.addAll(opts.transformations) // 自定义变换
    // 应用所有变换
    if (transforms.isNotEmpty()) {
        request = request.apply(RequestOptions().transform(*transforms.toTypedArray()))
    }
    // 加载回调
    request = request.listener(object : RequestListener<Drawable> {
        override fun onLoadFailed(
            e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean
        ): Boolean {
            opts.callback?.onFailed(e)
            return false // 继续交给 CustomTarget 处理
        }
        override fun onResourceReady(
            resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: com.bumptech.glide.load.DataSource?, isFirstResource: Boolean
        ): Boolean {
            resource?.let { opts.callback?.onSuccess(it) }
            // 可选：可在此处回调数据来源 dataSource
            return false // 继续交给 CustomTarget 处理
        }
    })
    // 加载到 ImageView（CustomTarget 保证状态）
    request.into(object : com.bumptech.glide.request.target.CustomTarget<Drawable>() {
        override fun onResourceReady(resource: Drawable, transition: com.bumptech.glide.request.transition.Transition<in Drawable>?) {
            this@load.setImageDrawable(resource)
        }
        override fun onLoadCleared(placeholder: Drawable?) {
            this@load.setImageDrawable(placeholder)
        }
        override fun onLoadFailed(errorDrawable: Drawable?) {
            this@load.setImageDrawable(errorDrawable)
        }
    })
}

/**
 * 加载圆形图片，等价于 load(url, isCircle = true)
 */
fun ImageView.loadCircle(
    url: Any?,
    placeholder: Int? = null,
    error: Int? = null,
    callback: ImageLoadCallback? = null,
    transformations: List<Transformation<android.graphics.Bitmap>> = emptyList(),
    options: (ImageLoadOptions.() -> Unit)? = null
) = load(url = url, isCircle = true, placeholder = placeholder, error = error, callback = callback, transformations = transformations, options = options)

/**
 * 加载圆角图片，等价于 load(url, radius = radius, radiusInDp = radiusInDp)
 */
fun ImageView.loadRounded(
    url: Any?,
    radius: Float,
    placeholder: Int? = null,
    error: Int? = null,
    radiusInDp: Boolean = true,
    callback: ImageLoadCallback? = null,
    transformations: List<Transformation<android.graphics.Bitmap>> = emptyList(),
    options: (ImageLoadOptions.() -> Unit)? = null
) = load(url = url, radius = radius, radiusInDp = radiusInDp, placeholder = placeholder, error = error, callback = callback, transformations = transformations, options = options)

/**
 * 加载模糊图片，等价于 load(url, isBlur = true, blurRadius = blurRadius, blurSampling = blurSampling)
 */
fun ImageView.loadBlur(
    url: Any?,
    blurRadius: Int = 25,
    blurSampling: Int = 1,
    placeholder: Int? = null,
    error: Int? = null,
    callback: ImageLoadCallback? = null,
    transformations: List<Transformation<android.graphics.Bitmap>> = emptyList(),
    options: (ImageLoadOptions.() -> Unit)? = null
) = load(url = url, isBlur = true, blurRadius = blurRadius, blurSampling = blurSampling, placeholder = placeholder, error = error, callback = callback, transformations = transformations, options = options)

/**
 * 加载灰度图片，等价于 load(url, isGray = true)
 */
fun ImageView.loadGray(
    url: Any?,
    placeholder: Int? = null,
    error: Int? = null,
    callback: ImageLoadCallback? = null,
    transformations: List<Transformation<android.graphics.Bitmap>> = emptyList(),
    options: (ImageLoadOptions.() -> Unit)? = null
) = load(url = url, isGray = true, placeholder = placeholder, error = error, callback = callback, transformations = transformations, options = options)

/**
 * 加载色彩滤镜图片，等价于 load(url, colorFilter = color)
 */
fun ImageView.loadColorFilter(
    url: Any?,
    color: Int,
    placeholder: Int? = null,
    error: Int? = null,
    callback: ImageLoadCallback? = null,
    transformations: List<Transformation<android.graphics.Bitmap>> = emptyList(),
    options: (ImageLoadOptions.() -> Unit)? = null
) = load(url = url, colorFilter = color, placeholder = placeholder, error = error, callback = callback, transformations = transformations, options = options)
