# 图片加载库

## 文档导航

- 仓库总览：[`README.md`](../README.md)
- 网络库文档：[`lib_network/README.md`](../lib_network/README.md)

## 依赖说明（放在最前）

- 版本来源：`gradle/libs.versions.toml`
- 升级建议：以版本目录为准统一升级，避免 README 与实际依赖漂移

本库基于以下依赖构建（版本来自项目版本目录）：

| 依赖 | 版本 | 说明 |
|------|------|------|
| [Glide](https://github.com/bumptech/glide) | `5.0.5` | 图片加载核心库 |
| [glide-transformations](https://github.com/wasabeef/glide-transformations) | `4.3.0` | 图片变换效果库（模糊、灰度等） |

Gradle（使用版本目录）示例：

```kotlin
dependencies {
    implementation(libs.glide)
    implementation(libs.glide.transformations)
}
```

Gradle（直接写坐标）示例：

```kotlin
dependencies {
    implementation("com.github.bumptech.glide:glide:5.0.5")
    implementation("jp.wasabeef:glide-transformations:4.3.0")
}
```

## 初始化说明

**无需初始化**，直接使用即可。库内部基于 Glide，Glide 会自动完成初始化。

如需自定义 Glide 配置（如缓存大小、网络配置等），请参考 [Glide 官方文档](https://bumptech.github.io/glide/doc/configuration.html) 创建 `AppGlideModule`。

### 全局默认配置（可选）

可在应用启动时统一设置高频默认参数，减少每次调用重复传参：

```kotlin
ImageLoaderDefaults.update { old ->
    old.copy(
        placeholder = R.drawable.placeholder,
        error = R.drawable.error,
        cacheStrategy = DiskCacheStrategy.AUTOMATIC,
        disableTransition = false,
        cancelOnDetach = true,
        resumeOnReattach = true
    )
}
```

说明：
- 单次 `load(...)` 显式传参优先于全局默认
- 全局默认适合列表页和统一视觉规范场景

## 功能简介
- 支持普通图片、圆形、圆角、模糊、灰度、色彩滤镜等多种图片加载场景
- 支持占位图、错误图（资源ID或Drawable）
- 支持圆形裁剪、圆角（px/dp）、模糊、灰度、色彩滤镜
- 支持尺寸覆盖、缓存策略、加载回调、生命周期控制、列表复用防错位、自定义变换
- API 简洁，易于集成，支持 DSL

## 快速开始

### 基础功能
```kotlin
imageView.load(url, placeholder = R.drawable.placeholder, error = R.drawable.error)
imageView.loadCircle(url, placeholder = R.drawable.placeholder, error = R.drawable.error)
imageView.loadRounded(url, radius = 16f, placeholder = R.drawable.placeholder, error = R.drawable.error)
```

> 说明：`loadRounded()` 默认 `radiusInDp = true`，即半径按 dp 解释。

### 变换效果
```kotlin
imageView.loadBlur(url, blurRadius = 18, blurSampling = 2, placeholder = R.drawable.placeholder, error = R.drawable.error)
imageView.loadGray(url, placeholder = R.drawable.placeholder, error = R.drawable.error)
imageView.loadColorFilter(url, color = 0x80FF4081.toInt(), placeholder = R.drawable.placeholder, error = R.drawable.error)
imageView.load(url, transformations = listOf(MyCustomTransformation()))
```

### 高级功能
```kotlin
// 过渡动画（默认300ms）
imageView.load(url, transitionDuration = 500) // 自定义动画时长
imageView.load(url, disableTransition = true) // 禁用动画
```

> 说明：默认使用 crossFade 过渡以避免圆形/圆角图片透明区域透出占位图导致“占位图残留”视觉问题。

```kotlin
// 加载优先级（默认NORMAL）
imageView.load(url, priority = Priority.HIGH) // 高优先级加载

// 缩略图加载（默认比例0.0f=不使用）
imageView.load(url, thumbnailUrl = thumbUrl)  // 使用指定URL作为缩略图
imageView.load(url, thumbnailSize = 0.1f)     // 加载10%大小的缩略图

// 列表防错位/生命周期（默认false，建议在RecyclerView中使用）
imageView.load(url, cancelOnDetach = true, resumeOnReattach = true)

// 高质量图片（默认DEFAULT，可选PREFER_ARGB_8888获得更好色彩）
imageView.load(url, decodeFormat = DecodeFormat.PREFER_ARGB_8888)

// 指定尺寸加载（覆盖ImageView尺寸，节省内存）
imageView.load(url, overrideWidth = 200, overrideHeight = 200)

// 跳过缓存（强制不写磁盘缓存）
imageView.load(url, skipDiskCache = true)

// 缓存策略（默认AUTOMATIC）
imageView.load(url, cacheStrategy = DiskCacheStrategy.ALL)      // 缓存原始和转换后
imageView.load(url, cacheStrategy = DiskCacheStrategy.DATA)     // 仅缓存原始
imageView.load(url, cacheStrategy = DiskCacheStrategy.RESOURCE) // 仅缓存转换后
imageView.load(url, cacheStrategy = DiskCacheStrategy.NONE)     // 不缓存
```

> 图片显示方式由 ImageView 的 scaleType 决定（如 centerCrop、fitCenter、centerInside 等），库不会强制应用 CenterCrop。如需自定义变换可通过 transformations 参数传递。

### 工具方法
```kotlin
ImageLoaderUtils.preloadImage(context, url)
ImageLoaderUtils.preloadImage(context, url, width, height)
ImageLoaderUtils.downloadImage(context, url, onSuccess = { file -> /* ... */ }, onFailed = { e -> /* ... */ }) // url 支持 Any?
ImageLoaderUtils.loadAsBitmap(context, url, onSuccess = { bitmap -> /* ... */ }, onFailed = { e -> /* ... */ })
ImageLoaderUtils.pauseRequests(context)
ImageLoaderUtils.resumeRequests(context)
ImageLoaderUtils.shutdownExecutor()
ImageLoaderUtils.clearImageTask(imageView) // 清除单个任务
ImageLoaderUtils.clearMemoryCache(context) // 清除内存缓存
ImageLoaderUtils.clearDiskCache(context)   // 清除磁盘缓存
ImageLoaderUtils.clearAllCache(context)    // 清除全部缓存
```

### 可取消任务（工具方法）
```kotlin
val downloadHandle = ImageLoaderUtils.downloadImage(context, url, onSuccess = { }, onFailed = { })
val bitmapHandle = ImageLoaderUtils.loadAsBitmap(context, url, onSuccess = { }, onFailed = { })

// 需要时可主动取消
// downloadHandle.cancel()
// bitmapHandle.cancel()
```

## 参数说明

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| url | Any? | null | 图片来源（String/File/Uri/Bitmap/ResourceId等） |
| placeholder | Int? | null | 占位图资源ID |
| error | Int? | null | 错误图资源ID |
| placeholderDrawable | Drawable? | null | 占位图Drawable（优先级高于placeholder） |
| errorDrawable | Drawable? | null | 错误图Drawable（优先级高于error） |
| isCircle | Boolean | false | 是否圆形裁剪 |
| radius | Float? | null | 圆角半径，单位由radiusInDp控制 |
| radiusInDp | Boolean | true | true=dp单位，false=px单位 |
| isBlur | Boolean | false | 是否高斯模糊 |
| blurRadius | Int | 25 | 模糊半径，内部会限制到 1..25 |
| blurSampling | Int | 1 | 模糊采样率，最小值为 1 |
| isGray | Boolean | false | 是否灰度处理 |
| colorFilter | Int? | null | 色彩滤镜色值（ARGB） |
| overrideWidth | Int? | null | 指定加载宽度（像素，<=0 时忽略） |
| overrideHeight | Int? | null | 指定加载高度（像素，<=0 时忽略） |
| cacheStrategy | DiskCacheStrategy | AUTOMATIC | 磁盘缓存策略 |
| skipMemoryCache | Boolean | false | 是否跳过内存缓存 |
| skipDiskCache | Boolean | false | 是否跳过磁盘缓存 |
| cancelOnDetach | Boolean | false | View脱离窗口时自动取消加载 |
| resumeOnReattach | Boolean | true | cancelOnDetach=true 时，View 重新 attach 后是否自动恢复请求 |
| callback | ImageLoadCallback? | null | 加载状态回调 |
| transformations | List | emptyList | 自定义变换列表 |
| transitionDuration | Int | 300 | 过渡动画时长（毫秒，<0 时按 0 处理） |
| disableTransition | Boolean | false | 是否禁用过渡动画（冷启动追求首帧速度时建议设为 true） |
| priority | Priority | NORMAL | 加载优先级（LOW/NORMAL/HIGH/IMMEDIATE） |
| decodeFormat | DecodeFormat | DEFAULT | 图片解码格式 |
| thumbnailUrl | Any? | null | 缩略图URL（优先于thumbnailSize） |
| thumbnailSize | Float | 0f | 缩略图比例（0.0-1.0），超出范围会被限制；0表示不使用 |

### 占位图和错误图优先级
- 如果同时设置了 placeholder 和 placeholderDrawable，优先使用 Drawable
- 如果同时设置了 error 和 errorDrawable，优先使用 Drawable

### 参数优先级说明
- `skipDiskCache = true` 时，磁盘缓存策略强制为 `DiskCacheStrategy.NONE`（优先级高于 `cacheStrategy`）
- `thumbnailUrl` 与 `thumbnailSize` 同时存在时，优先使用 `thumbnailUrl`
- `cancelOnDetach = true` 且 `resumeOnReattach = true` 时，item 重新 attach 会自动恢复最近一次请求

### 失败场景建议
```kotlin
imageView.load(
    url = "https://example.invalid/not_found.jpg",
    placeholder = R.drawable.placeholder,
    error = R.drawable.error,
    callback = object : ImageLoadCallback {
        override fun onFailed(throwable: Throwable?) {
            // 上报或提示
        }
    }
)
```

### RecyclerView 复用建议
- 列表图片建议开启 `cancelOnDetach = true`，减少快速滑动时的无效请求
- `onBindViewHolder` 中建议同时配置 `placeholder/error`，提升复用体验
- 首屏追求更快出图可配置 `disableTransition = true`

### RecyclerView 抖动说明
- 开启 `cancelOnDetach = true` 时，库内会在 item re-attach 后自动恢复最近一次请求
- 这样可避免快速左右抖动时出现长期停留 placeholder 的情况

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

## 许可证
MIT

## 线程安全说明

- `load()` 等加载方法必须在**主线程**调用
- `ImageLoaderUtils` 中与请求生命周期相关的方法（`downloadImage`、`loadAsBitmap`、`pauseRequests`、`resumeRequests` 等）建议在**主线程**调用
- `ImageLoadCallback` 回调在**主线程**执行
- `clearMemoryCache()` 必须在**主线程**调用
- `clearDiskCache()` 内部自动在子线程执行

## ProGuard 混淆配置

如果开启了代码混淆，请添加以下规则：

```proguard
# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule { <init>(...); }
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-keep class com.bumptech.glide.load.data.ParcelFileDescriptorRewinder$InternalRewinder {
  *** rewind();
}

# glide-transformations
-keep class jp.wasabeef.glide.transformations.** { *; }
```

## 性能建议
- 冷启动首屏如果追求“尽快出图”，建议设置 `disableTransition = true`（避免过渡动画带来的视觉慢一拍）
- 对比原生 Glide 时，请保证 URL、尺寸、变换、placeholder、启动时机一致，否则体感会有偏差
- 第二次进入通常命中内存/磁盘缓存，差异会显著缩小，属于正常现象
