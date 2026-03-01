# 图片加载库

## 功能简介
- 支持普通图片、圆形、圆角、模糊、灰度、色彩滤镜等多种图片加载场景
- 支持占位图、错误图（资源ID或Drawable）
- 支持圆形裁剪、圆角（px/dp）、模糊、灰度、色彩滤镜
- 支持尺寸覆盖、缓存策略、加载回调、生命周期控制、列表复用防错位、自定义变换
- API 简洁，易于集成，支持 DSL
- 依赖 Glide 和 glide-transformations（普通版本）

## 快速开始

### 基础功能
```kotlin
imageView.load(url, placeholder = R.drawable.placeholder, error = R.drawable.error)
imageView.loadCircle(url, placeholder = R.drawable.placeholder, error = R.drawable.error)
imageView.loadRounded(url, radius = 16f, placeholder = R.drawable.placeholder, error = R.drawable.error)
```

### 变换效果
```kotlin
imageView.loadBlur(url, blurRadius = 18, blurSampling = 2, placeholder = R.drawable.placeholder, error = R.drawable.error)
imageView.loadGray(url, placeholder = R.drawable.placeholder, error = R.drawable.error)
imageView.loadColorFilter(url, color = 0x80FF4081.toInt(), placeholder = R.drawable.placeholder, error = R.drawable.error)
imageView.load(url, transformations = listOf(MyCustomTransformation()))
```

### 高级功能
```kotlin
// 过渡动画
imageView.load(url, transitionDuration = 500) // 自定义动画时长
imageView.load(url, disableTransition = true) // 禁用动画
// 加载优先级
imageView.load(url, priority = Priority.HIGH) // 高优先级加载
// 缩略图加载
imageView.load(url, thumbnailUrl = thumbUrl)
imageView.load(url, thumbnailSize = 0.1f) // 加载10%大小的缩略图
// 列表防错位/生命周期
imageView.load(url, cancelOnDetach = true)
```

> 图片显示方式由 ImageView 的 scaleType 决定（如 centerCrop、fitCenter、centerInside 等），库不会强制应用 CenterCrop。如需自定义变换可通过 transformations 参数传递。

### 工具方法
```kotlin
ImageLoaderUtils.preloadImage(context, url)
ImageLoaderUtils.preloadImage(context, url, width, height)
ImageLoaderUtils.downloadImage(context, url, onSuccess = { file -> /* ... */ }, onFailed = { e -> /* ... */ })
ImageLoaderUtils.loadAsBitmap(context, url, onSuccess = { bitmap -> /* ... */ }, onFailed = { e -> /* ... */ })
ImageLoaderUtils.pauseRequests(context)
ImageLoaderUtils.resumeRequests(context)
ImageLoaderUtils.shutdownExecutor()
ImageLoaderUtils.clearImageTask(imageView) // 清除单个任务
ImageLoaderUtils.clearMemoryCache(context) // 清除内存缓存
ImageLoaderUtils.clearDiskCache(context)   // 清除磁盘缓存
ImageLoaderUtils.clearAllCache(context)    // 清除全部缓存
```

### 占位图和错误图优先级说明
- 如果同时设置了 placeholder 和 placeholderDrawable，优先使用 Drawable。
- 如果同时设置了 error 和 errorDrawable，优先使用 Drawable。

## 参数说明
- url：图片来源（String/File/Uri/Bitmap等）
- placeholder/error：占位/错误图资源ID
- placeholderDrawable/errorDrawable：占位/错误Drawable
- isCircle：是否圆形裁剪
- radius：圆角半径（float，单位由 radiusInDp 控制）
- radiusInDp：true=dp，false=px
- isBlur/blurRadius/blurSampling：模糊相关
- isGray：是否灰度
- colorFilter：色彩滤镜色值（Int，ARGB）
- overrideWidth/overrideHeight：尺寸覆盖
- cacheStrategy/skipMemoryCache/skipDiskCache：缓存策略
- cancelOnDetach：View脱离窗口时取消加载（生命周期与列表复用）
- callback：加载回调
- transformations：自定义变换（List<Transformation<Bitmap>>）
- transitionDuration：过渡动画时长（毫秒，默认300）
- disableTransition：是否禁用过渡动画（默认false）
- priority：加载优先级（Priority.LOW/NORMAL/HIGH/IMMEDIATE）
- decodeFormat：图片解码格式（DecodeFormat.DEFAULT/PREFER_ARGB_8888/PREFER_RGB_565）
- thumbnailUrl：缩略图URL（可选）
- thumbnailSize：缩略图比例（0.0-1.0，默认0.1）

## DSL 用法（支持所有参数）
```kotlin
imageView.load {
    url = "https://example.com/a.png"
    placeholder = R.drawable.placeholder
    radius = 8f
    isBlur = true
    priority = Priority.HIGH
    transitionDuration = 500
    thumbnailUrl = "https://example.com/thumb.png"
    decodeFormat = DecodeFormat.PREFER_ARGB_8888
    disableTransition = false
}
```

## 依赖说明
- Glide
- glide-transformations（普通版本）

## 许可证
MIT
