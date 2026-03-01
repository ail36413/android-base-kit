package com.ail.android_base_kit.image

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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
    private lateinit var btnClearCache: Button
    private lateinit var imageTransition: ImageView
    private lateinit var imagePriority: ImageView
    private lateinit var imageThumbnail: ImageView

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
        btnClearCache = findViewById(R.id.btnClearCache)
        imageTransition = findViewById(R.id.imageTransition)
        imagePriority = findViewById(R.id.imagePriority)
        imageThumbnail = findViewById(R.id.imageThumbnail)
        // 基础加载
        imageNormal.load(url)
        // 圆形加载
        imageCircle.loadCircle(url, placeholder = R.drawable.placeholder, error = R.drawable.error)
        // 圆角加载
        imageRounded.loadRounded(url, radius = 16f, placeholder = R.drawable.placeholder, error = R.drawable.error)
        // 模糊加载
        imageBlur.loadBlur(url, blurRadius = 18, blurSampling = 2, placeholder = R.drawable.placeholder, error = R.drawable.error)
        // 灰度加载
        imageGray.loadGray(url, placeholder = R.drawable.placeholder, error = R.drawable.error)
        // 色彩滤镜加载
        imageColor.loadColorFilter(url, color = 0x80FF4081.toInt(), placeholder = R.drawable.placeholder, error = R.drawable.error)
        // 自定义变换（灰度+圆角）
        imageCustom.load(url, transformations = listOf(
            GrayscaleTransformation(),
            RoundedCorners(32)
        ))
        // DSL用法加载（lambda仅用于添加自定义变换）
        imageDsl.load(
            url = url,
            placeholder = R.drawable.placeholder,
            error = R.drawable.error,
            radius = 8f,
            isBlur = true,
            isGray = true,
            colorFilter = 0xFF4081,
            cacheStrategy = DiskCacheStrategy.AUTOMATIC,
            options = {
                addTransformation(GrayscaleTransformation())
            }
        )
        // 回调演示（loading/success/error）
        imageCallback.load(url, placeholder = R.drawable.placeholder, error = R.drawable.error, callback = object :
            ImageLoadCallback {
            override fun onStart() {
                textCallback.text = "加载中..."
            }
            override fun onSuccess(drawable: Drawable) {
                textCallback.text = "加载成功"
            }
            override fun onFailed(throwable: Throwable?) {
                textCallback.text = "加载失败"
            }
        })
        // 缓存清理演示
        btnClearCache.setOnClickListener {
            ImageLoaderUtils.clearAllCache(this)
            textCallback.text = "已清除全部缓存"
        }

        // 过渡动画演示（自定义时长）
        imageTransition.load(url, placeholder = R.drawable.placeholder, transitionDuration = 1000)
        // 加载优先级演示（HIGH）
        imagePriority.load(url, placeholder = R.drawable.placeholder, priority = Priority.HIGH)
        // 缩略图加载演示（先加载缩略图再加载原图）
        val thumbUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=50"
        imageThumbnail.load(url, placeholder = R.drawable.placeholder, thumbnailUrl = thumbUrl, thumbnailSize = 0.2f)
    }
}