package com.ail.android_base_kit.image

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ail.android_base_kit.R
import com.ail.lib_image.ImageLoadCallback
import com.ail.lib_image.ImageLoaderUtils
import com.ail.lib_image.load
import com.ail.lib_image.loadBlur
import com.ail.lib_image.loadCircle
import com.ail.lib_image.loadColorFilter
import com.ail.lib_image.loadGray
import com.ail.lib_image.loadRounded
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import jp.wasabeef.glide.transformations.GrayscaleTransformation


class ImageDemoActivity : AppCompatActivity() {
    private lateinit var imageNormal: ImageView
    private lateinit var imageCircle: ImageView
    private lateinit var imageRounded: ImageView
    private lateinit var imageBlur: ImageView
    private lateinit var imageGray: ImageView
    private lateinit var imageColor: ImageView
    private lateinit var imageCustom: ImageView
    private lateinit var imageDsl: ImageView
    private lateinit var imageCallback: ImageView
    private lateinit var textCallback: TextView
    private lateinit var imageErrorCase: ImageView
    private lateinit var textErrorCase: TextView
    private lateinit var recyclerReuse: RecyclerView
    private lateinit var btnClearCache: Button
    private lateinit var imageTransition: ImageView
    private lateinit var imagePriority: ImageView
    private lateinit var imageThumbnail: ImageView
    private lateinit var imageHighQuality: ImageView
    private lateinit var imageOverrideSize: ImageView
    private lateinit var imageSkipCache: ImageView
    private lateinit var imageCancelOnDetach: ImageView
    private lateinit var btnPreload: Button
    private lateinit var textPreload: TextView
    private lateinit var btnDownload: Button
    private lateinit var textDownload: TextView
    private lateinit var btnLoadBitmap: Button
    private lateinit var textBitmap: TextView
    private lateinit var btnPause: Button
    private lateinit var btnResume: Button
    private lateinit var textPauseResume: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_loader_demo)

        val url = "https://images.unsplash.com/photo-1506744038136-46273834b3fb"
        imageNormal = findViewById(R.id.imageNormal)
        imageCircle = findViewById(R.id.imageCircle)
        imageRounded = findViewById(R.id.imageRounded)
        imageBlur = findViewById(R.id.imageBlur)
        imageGray = findViewById(R.id.imageGray)
        imageColor = findViewById(R.id.imageColor)
        imageCustom = findViewById(R.id.imageCustom)
        imageDsl = findViewById(R.id.imageDsl)
        imageCallback = findViewById(R.id.imageCallback)
        textCallback = findViewById(R.id.textCallback)
        imageErrorCase = findViewById(R.id.imageErrorCase)
        textErrorCase = findViewById(R.id.textErrorCase)
        recyclerReuse = findViewById(R.id.recyclerReuse)
        btnClearCache = findViewById(R.id.btnClearCache)
        imageTransition = findViewById(R.id.imageTransition)
        imagePriority = findViewById(R.id.imagePriority)
        imageThumbnail = findViewById(R.id.imageThumbnail)
        imageHighQuality = findViewById(R.id.imageHighQuality)
        imageOverrideSize = findViewById(R.id.imageOverrideSize)
        imageSkipCache = findViewById(R.id.imageSkipCache)
        imageCancelOnDetach = findViewById(R.id.imageCancelOnDetach)
        btnPreload = findViewById(R.id.btnPreload)
        textPreload = findViewById(R.id.textPreload)
        btnDownload = findViewById(R.id.btnDownload)
        textDownload = findViewById(R.id.textDownload)
        btnLoadBitmap = findViewById(R.id.btnLoadBitmap)
        textBitmap = findViewById(R.id.textBitmap)
        btnPause = findViewById(R.id.btnPause)
        btnResume = findViewById(R.id.btnResume)
        textPauseResume = findViewById(R.id.textPauseResume)


        // 基础加载（对比原生 Glide 时关闭过渡动画，避免首帧视觉慢一拍）
        imageNormal.load(url, disableTransition = true)
        // 圆形加载（对比原生 Glide circleCrop 时关闭过渡动画）
        imageCircle.loadCircle(
            url,
            placeholder = R.drawable.placeholder,
            error = R.drawable.error,
            disableTransition = true
        )

        // 圆角加载
        imageRounded.loadRounded(
            url,
            radius = 56f,
            placeholder = R.drawable.placeholder,
            error = R.drawable.error
        )

        // 模糊加载
        imageBlur.loadBlur(
            url,
            blurRadius = 38,
            blurSampling = 2,
            placeholder = R.drawable.placeholder,
            error = R.drawable.error
        )
        // 灰度加载
        imageGray.loadGray(url, placeholder = R.drawable.placeholder, error = R.drawable.error)
        // 色彩滤镜加载
        imageColor.loadColorFilter(
            url,
            color = 0x80FF4081.toInt(),
            placeholder = R.drawable.placeholder,
            error = R.drawable.error
        )
        // 自定义变换（灰度+圆角）
        imageCustom.load(
            url, transformations = listOf(
                GrayscaleTransformation(),
                RoundedCorners(162)
            )
        )
        // DSL用法加载（lambda仅用于添加自定义变换）
        imageDsl.load(
            url = url,
            placeholder = R.drawable.placeholder,
            error = R.drawable.error,
            radius = 80f,
            isBlur = true,
            isGray = true,
            colorFilter = 0xFF4081,
            cacheStrategy = DiskCacheStrategy.AUTOMATIC,
            options = {
                addTransformation(GrayscaleTransformation())
            }
        )
        // 回调演示（loading/success/error）
        imageCallback.load(
            url,
            placeholder = R.drawable.placeholder,
            error = R.drawable.error,
            callback = object :
                ImageLoadCallback {
                override fun onStart() {
                    textCallback.text = getString(R.string.image_loading)
                }

                override fun onSuccess(drawable: Drawable) {
                    textCallback.text = getString(R.string.image_loaded)
                }

                override fun onFailed(throwable: Throwable?) {
                    textCallback.text = getString(R.string.image_failed)
                }
            })
        // 失败回调演示（错误 URL）
        val invalidUrl = "https://example.invalid/not_found.jpg"
        imageErrorCase.load(
            invalidUrl,
            placeholder = R.drawable.placeholder,
            error = R.drawable.error,
            callback = object : ImageLoadCallback {
                override fun onStart() {
                    textErrorCase.text = getString(R.string.image_error_loading)
                }

                override fun onSuccess(drawable: Drawable) {
                    textErrorCase.text = getString(R.string.image_error_unexpected_success)
                }

                override fun onFailed(throwable: Throwable?) {
                    textErrorCase.text = getString(R.string.image_error_failed_callback)
                }
            }
        )

        setupRecyclerReuseDemo()

        // 缓存清理演示
        btnClearCache.setOnClickListener {
            ImageLoaderUtils.clearAllCache(this)
            textCallback.text = getString(R.string.image_cache_cleared)
        }

        // 过渡动画演示（自定义时长）
        imageTransition.load(url, placeholder = R.drawable.placeholder, transitionDuration = 1000)
        // 加载优先级演示（HIGH）
        imagePriority.load(url, placeholder = R.drawable.placeholder, priority = Priority.HIGH)
        // 缩略图加载演示（先加载缩略图再加载原图）
        val thumbUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=50"
        imageThumbnail.load(
            url,
            placeholder = R.drawable.placeholder,
            thumbnailUrl = thumbUrl,
            thumbnailSize = 0.2f
        )

        // 高质量图片加载演示（ARGB_8888格式，色彩更丰富但占用更多内存）
        imageHighQuality.load(
            url,
            placeholder = R.drawable.placeholder,
            decodeFormat = DecodeFormat.PREFER_ARGB_8888
        )

        // 指定尺寸加载演示（覆盖ImageView尺寸，加载200x200像素）
        imageOverrideSize.load(
            url,
            placeholder = R.drawable.placeholder,
            overrideWidth = 200,
            overrideHeight = 200
        )

        // 跳过缓存加载演示（不从内存/磁盘缓存读取，强制从网络加载）
        imageSkipCache.load(
            url,
            placeholder = R.drawable.placeholder,
            skipMemoryCache = true,
            skipDiskCache = true
        )

        // 列表防错位演示（View脱离窗口时自动取消加载，适用于RecyclerView）
        imageCancelOnDetach.load(url, placeholder = R.drawable.placeholder, cancelOnDetach = true)

        // 预加载图片到缓存演示
        val preloadUrl = "https://images.unsplash.com/photo-1469474968028-56623f02e42e"
        btnPreload.setOnClickListener {
            textPreload.text = getString(R.string.image_preloading)
            ImageLoaderUtils.preloadImage(this, preloadUrl)
            // 预加载是异步的，这里简单延迟显示结果
            textPreload.postDelayed({ textPreload.text = getString(R.string.image_preloaded) }, 2000)
        }

        // 下载图片到本地演示
        val downloadUrl = "https://images.unsplash.com/photo-1501854140801-50d01698950b"
        btnDownload.setOnClickListener {
            textDownload.text = getString(R.string.image_downloading)
            ImageLoaderUtils.downloadImage(
                context = this,
                url = downloadUrl,
                onSuccess = { file ->
                    textDownload.text = getString(R.string.image_download_success, file.absolutePath)
                },
                onFailed = { e ->
                    textDownload.text = getString(R.string.image_download_failed, e?.message ?: "未知错误")
                }
            )
        }

        // 加载图片为 Bitmap 演示
        btnLoadBitmap.setOnClickListener {
            textBitmap.text = getString(R.string.image_bitmap_loading)
            ImageLoaderUtils.loadAsBitmap(
                context = this,
                url = url,
                onSuccess = { bitmap ->
                    textBitmap.text = getString(R.string.image_bitmap_success, bitmap.width, bitmap.height)
                },
                onFailed = { e ->
                    textBitmap.text = getString(R.string.image_bitmap_failed, e?.message ?: "未知错误")
                }
            )
        }

        // 暂停/恢复加载请求演示
        btnPause.setOnClickListener {
            ImageLoaderUtils.pauseRequests(this)
            textPauseResume.text = getString(R.string.image_pause_done)
        }
        btnResume.setOnClickListener {
            ImageLoaderUtils.resumeRequests(this)
            textPauseResume.text = getString(R.string.image_resume_done)
        }
    }

    private fun setupRecyclerReuseDemo() {
        val demoUrls = listOf(
            "https://images.unsplash.com/photo-1506744038136-46273834b3fb",
            "https://images.unsplash.com/photo-1469474968028-56623f02e42e",
            "https://images.unsplash.com/photo-1501854140801-50d01698950b",
            "https://images.unsplash.com/photo-1516117172878-fd2c41f4a759",
            "https://images.unsplash.com/photo-1521572267360-ee0c2909d518"
        )
        recyclerReuse.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        recyclerReuse.adapter = ReuseAdapter(demoUrls)
    }

    private class ReuseAdapter(private val urls: List<String>) : RecyclerView.Adapter<ReuseAdapter.VH>() {
        class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val image: ImageView = itemView.findViewById(R.id.itemImage)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_image_reuse_demo, parent, false)
            return VH(itemView)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val url = urls[position % urls.size]
            holder.image.load(
                url = url,
                placeholder = R.drawable.placeholder,
                error = R.drawable.error,
                cancelOnDetach = true,
                disableTransition = true
            )
        }

        override fun getItemCount(): Int = 30
    }
}

