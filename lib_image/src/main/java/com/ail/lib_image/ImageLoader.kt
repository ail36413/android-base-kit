package com.ail.lib_image

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException

import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import jp.wasabeef.glide.transformations.BlurTransformation
import jp.wasabeef.glide.transformations.ColorFilterTransformation
import jp.wasabeef.glide.transformations.GrayscaleTransformation
import java.util.WeakHashMap
import androidx.annotation.MainThread

// 通过弱引用映射保存每个 ImageView 的 detach listener，避免使用 setTag(key, value) 的 key 限制
private val detachListenerMap = WeakHashMap<ImageView, View.OnAttachStateChangeListener>()

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
    // 仅在 cancelOnDetach=true 时生效：View 重新 attach 后是否自动恢复请求
    val resumeOnReattach: Boolean = true,
    val callback: ImageLoadCallback? = null,
    var transformations: List<Transformation<android.graphics.Bitmap>> = emptyList(),
    // 动画过渡配置
    val transitionDuration: Int = 300,
    val disableTransition: Boolean = false,
    // 加载优先级
    val priority: com.bumptech.glide.Priority = com.bumptech.glide.Priority.NORMAL,
    // 图片解码格式
    val decodeFormat: com.bumptech.glide.load.DecodeFormat = com.bumptech.glide.load.DecodeFormat.DEFAULT,
    // 缩略图配置
    val thumbnailUrl: Any? = null,
    val thumbnailSize: Float = 0f
) {
    /**
     * 追加自定义变换（DSL 友好）
     */
    fun addTransformation(transformation: Transformation<android.graphics.Bitmap>) {
        transformations = transformations + transformation
    }
}

/**
 * 图片加载回调接口
 * 用于监听加载开始、成功、失败事件
 */
interface ImageLoadCallback {
    fun onStart() {}
    fun onSuccess(drawable: Drawable) {}
    fun onFailed(throwable: Throwable?) {}
}

/**
 * ImageView 扩展函数，支持参数方式和 DSL 方式。
 * 推荐直接调用 imageView.load(...)，或使用DSL imageView.load { ... }
 * 支持圆形、圆角、模糊、占位图、错误图、缓存策略、尺寸覆盖、回调等配置
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
@MainThread
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
    resumeOnReattach: Boolean = true,
    callback: ImageLoadCallback? = null,
    transformations: List<Transformation<android.graphics.Bitmap>> = emptyList(),
    transitionDuration: Int = 300,
    disableTransition: Boolean = false,
    priority: com.bumptech.glide.Priority = com.bumptech.glide.Priority.NORMAL,
    decodeFormat: com.bumptech.glide.load.DecodeFormat = com.bumptech.glide.load.DecodeFormat.DEFAULT,
    thumbnailUrl: Any? = null,
    thumbnailSize: Float = 0f,
    options: (ImageLoadOptions.() -> Unit)? = null
) {
    // 构建参数对象，支持DSL
    val opts = ImageLoadOptions(
        url, placeholder, error, placeholderDrawable, errorDrawable, isCircle, radius, radiusInDp,
        isBlur, blurRadius, blurSampling, isGray, colorFilter, overrideWidth, overrideHeight, cacheStrategy,
        skipMemoryCache, skipDiskCache, cancelOnDetach, resumeOnReattach, callback, transformations,
        transitionDuration, disableTransition, priority, decodeFormat, thumbnailUrl, thumbnailSize
    ).apply { options?.invoke(this) }

    // 参数兜底，避免非法值导致行为异常
    val safeTransitionDuration = opts.transitionDuration.coerceAtLeast(0)
    val safeThumbnailSize = opts.thumbnailSize.coerceIn(0f, 1f)
    val safeBlurRadius = opts.blurRadius.coerceIn(1, 25)
    val safeBlurSampling = opts.blurSampling.coerceAtLeast(1)

    val context = this.context
    val glide = Glide.with(context)
    var restartOnAttach: (() -> Unit)? = null

    // cancelOnDetach: 监听View detach自动clear，或清理旧监听器
    val oldListener = detachListenerMap.remove(this)
    if (oldListener != null) {
        this.removeOnAttachStateChangeListener(oldListener)
    }
    if (opts.cancelOnDetach) {
        val detachListener = object : View.OnAttachStateChangeListener {
            private var clearedByDetach = false

            override fun onViewAttachedToWindow(v: View) {
                // RecyclerView 快速抖动时，item 可能 detach/attach 但不会触发 onBind，
                // 这里可选自动恢复最近一次请求，避免长期停留在 placeholder。
                if (clearedByDetach && opts.resumeOnReattach) {
                    clearedByDetach = false
                    try {
                        restartOnAttach?.invoke()
                    } catch (_: Exception) {
                        // View/Activity 状态异常时忽略
                    }
                }
            }

            override fun onViewDetachedFromWindow(v: View) {
                try {
                    Glide.with(v).clear(this@load)
                    clearedByDetach = true
                } catch (_: Exception) {
                    // Activity 已销毁，忽略
                }
            }
        }
        this.addOnAttachStateChangeListener(detachListener)
        detachListenerMap[this] = detachListener
    }

    // 不再无条件 clear：首次加载时会增加额外状态切换与重绘开销
    opts.callback?.onStart()

    // 构建 Glide 请求
    var request = glide.load(opts.url)

    // 仅在非默认值时设置，减少每次请求额外对象与配置开销
    if (opts.cacheStrategy != DiskCacheStrategy.AUTOMATIC) {
        request = request.diskCacheStrategy(opts.cacheStrategy)
    }
    if (opts.skipMemoryCache) {
        request = request.skipMemoryCache(true)
    }
    if (opts.priority != com.bumptech.glide.Priority.NORMAL ||
        opts.decodeFormat != com.bumptech.glide.load.DecodeFormat.DEFAULT
    ) {
        request = request.apply(RequestOptions().priority(opts.priority).format(opts.decodeFormat))
    }

    // 过渡动画配置
    if (!opts.disableTransition) {
        // 开启 crossFade，避免圆形/圆角透明区域透出 placeholder 造成“占位图残留”
        val crossFadeFactory = DrawableCrossFadeFactory.Builder(safeTransitionDuration)
            .setCrossFadeEnabled(true)
            .build()
        request = request.transition(DrawableTransitionOptions.with(crossFadeFactory))
    }
    // 缩略图配置
    if (opts.thumbnailUrl != null) {
        request = request.thumbnail(glide.load(opts.thumbnailUrl))
    } else if (safeThumbnailSize > 0f) {
        // 避免使用已弃用的 thumbnail(Float)
        request = request.thumbnail(glide.load(opts.url).sizeMultiplier(safeThumbnailSize))
    }
    if (opts.skipDiskCache) {
        // skipDiskCache 的优先级高于 cacheStrategy
        request = request.diskCacheStrategy(DiskCacheStrategy.NONE)
    }
    // 尺寸覆盖
    if ((opts.overrideWidth ?: 0) > 0 && (opts.overrideHeight ?: 0) > 0) {
        request = request.override(opts.overrideWidth!!, opts.overrideHeight!!)
    }

    // 变换列表
    val transforms = mutableListOf<Transformation<android.graphics.Bitmap>>()

    // 纯圆形场景优先走 Glide 原生快路径，减少首次冷启动时的额外开销
    val hasExtraBitmapTransform = opts.radius != null || opts.isBlur || opts.isGray || opts.colorFilter != null || opts.transformations.isNotEmpty()
    if (opts.isCircle && !hasExtraBitmapTransform) {
        request = request.circleCrop()
    } else {
        if (opts.isCircle) transforms.add(CircleCrop()) // 圆形裁剪
        else if (opts.radius != null && opts.radius > 0) {
            // 圆角裁剪，支持dp/px
            val px = if (opts.radiusInDp) (opts.radius * context.resources.displayMetrics.density).toInt() else opts.radius.toInt()
            if (px > 0) {
                transforms.add(RoundedCorners(px))
            }
        }
        if (opts.isBlur) transforms.add(BlurTransformation(safeBlurRadius, safeBlurSampling)) // 高斯模糊
        if (opts.isGray) transforms.add(GrayscaleTransformation()) // 灰度
        opts.colorFilter?.let { transforms.add(ColorFilterTransformation(it)) } // 色彩滤镜
        if (opts.transformations.isNotEmpty()) transforms.addAll(opts.transformations) // 自定义变换
    }

    // 构建 RequestOptions，同时设置占位图、错误图和变换，确保主图加载参数统一
    var requestOptions: RequestOptions? = null

    fun ensureOptions(): RequestOptions {
        if (requestOptions == null) requestOptions = RequestOptions()
        return requestOptions!!
    }

    // 占位/错误图优先使用Drawable
    if (opts.placeholderDrawable != null) {
        requestOptions = ensureOptions().placeholder(opts.placeholderDrawable)
    } else if (opts.placeholder != null) {
        requestOptions = ensureOptions().placeholder(opts.placeholder)
    }

    if (opts.errorDrawable != null) {
        requestOptions = ensureOptions().error(opts.errorDrawable)
    } else if (opts.error != null) {
        requestOptions = ensureOptions().error(opts.error)
    }

    // 应用所有变换
    if (transforms.isNotEmpty()) {
        requestOptions = ensureOptions().transform(*transforms.toTypedArray())
    }

    if (requestOptions != null) {
        request = request.apply(requestOptions)
    }

    // 仅在外部传入回调时注册 listener，减少默认路径开销
    if (opts.callback != null) {
        request = request.listener(object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean
            ): Boolean {
                opts.callback.onFailed(e)
                return false
            }

            override fun onResourceReady(
                resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: com.bumptech.glide.load.DataSource?, isFirstResource: Boolean
            ): Boolean {
                resource?.let { opts.callback.onSuccess(it) }
                return false
            }
        })
    }

    // 保存一份最终请求用于 re-attach 恢复
    val finalRequest = request
    if (opts.cancelOnDetach) {
        restartOnAttach = {
            finalRequest.clone().into(this)
        }
    }
    finalRequest.into(this)
}

/**
 * 加载圆形图片，等价于 load(url, isCircle = true)
 */
@MainThread
fun ImageView.loadCircle(
    url: Any?,
    placeholder: Int? = null,
    error: Int? = null,
    disableTransition: Boolean = false,
    callback: ImageLoadCallback? = null,
    transformations: List<Transformation<android.graphics.Bitmap>> = emptyList(),
    options: (ImageLoadOptions.() -> Unit)? = null
) = load(
    url = url,
    isCircle = true,
    placeholder = placeholder,
    error = error,
    disableTransition = disableTransition,
    callback = callback,
    transformations = transformations,
    options = options
)

/**
 * 加载圆角图片，等价于 load(url, radius = radius, radiusInDp = radiusInDp)
 */
@MainThread
fun ImageView.loadRounded(
    url: Any?,
    radius: Float,
    placeholder: Int? = null,
    error: Int? = null,
    radiusInDp: Boolean = true,
    disableTransition: Boolean = false,
    callback: ImageLoadCallback? = null,
    transformations: List<Transformation<android.graphics.Bitmap>> = emptyList(),
    options: (ImageLoadOptions.() -> Unit)? = null
) = load(
    url = url,
    radius = radius,
    radiusInDp = radiusInDp,
    placeholder = placeholder,
    error = error,
    disableTransition = disableTransition,
    callback = callback,
    transformations = transformations,
    options = options
)

/**
 * 加载模糊图片，等价于 load(url, isBlur = true, blurRadius = blurRadius, blurSampling = blurSampling)
 */
@MainThread
fun ImageView.loadBlur(
    url: Any?,
    blurRadius: Int = 25,
    blurSampling: Int = 1,
    placeholder: Int? = null,
    error: Int? = null,
    disableTransition: Boolean = false,
    callback: ImageLoadCallback? = null,
    transformations: List<Transformation<android.graphics.Bitmap>> = emptyList(),
    options: (ImageLoadOptions.() -> Unit)? = null
) = load(
    url = url,
    isBlur = true,
    blurRadius = blurRadius,
    blurSampling = blurSampling,
    placeholder = placeholder,
    error = error,
    disableTransition = disableTransition,
    callback = callback,
    transformations = transformations,
    options = options
)

/**
 * 加载灰度图片，等价于 load(url, isGray = true)
 */
@MainThread
fun ImageView.loadGray(
    url: Any?,
    placeholder: Int? = null,
    error: Int? = null,
    disableTransition: Boolean = false,
    callback: ImageLoadCallback? = null,
    transformations: List<Transformation<android.graphics.Bitmap>> = emptyList(),
    options: (ImageLoadOptions.() -> Unit)? = null
) = load(
    url = url,
    isGray = true,
    placeholder = placeholder,
    error = error,
    disableTransition = disableTransition,
    callback = callback,
    transformations = transformations,
    options = options
)

/**
 * 加载色彩滤镜图片，等价于 load(url, colorFilter = color)
 */
@MainThread
fun ImageView.loadColorFilter(
    url: Any?,
    color: Int,
    placeholder: Int? = null,
    error: Int? = null,
    disableTransition: Boolean = false,
    callback: ImageLoadCallback? = null,
    transformations: List<Transformation<android.graphics.Bitmap>> = emptyList(),
    options: (ImageLoadOptions.() -> Unit)? = null
) = load(
    url = url,
    colorFilter = color,
    placeholder = placeholder,
    error = error,
    disableTransition = disableTransition,
    callback = callback,
    transformations = transformations,
    options = options
)
