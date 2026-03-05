# android-base-kit

Android 基础能力库集合（Kotlin + XML），当前包含独立可用的图片库与网络库，`app` 模块用于功能演示。

## 模块导航

- [`lib_image`](lib_image)：图片加载与常见变换能力
  - 文档：[`lib_image/README.md`](lib_image/README.md)
- [`lib_network`](lib_network)：HTTP + WebSocket 网络能力
  - 文档：[`lib_network/README.md`](lib_network/README.md)
- [`app`](app)：演示入口（图片与网络能力）

## 依赖入口索引

- 图片库依赖与版本：[`lib_image/README.md` - 依赖说明（放在最前）](lib_image/README.md#依赖说明放在最前)
- 网络库依赖与版本：[`lib_network/README.md` - 依赖说明（放在最前）](lib_network/README.md#依赖说明放在最前)
- 统一版本目录：[`gradle/libs.versions.toml`](gradle/libs.versions.toml)

## 快速开始

### 1) 接入图片库

```kotlin
dependencies {
    implementation(project(":lib_image"))
}
```

### 2) 接入网络库

```kotlin
dependencies {
    implementation(project(":lib_network"))
}
```

> 网络库依赖 Hilt，接入前请先阅读 [`lib_network/README.md`](lib_network/README.md) 的接入前置。

## 演示入口

- 主页面：[`app/src/main/java/com/ail/android_base_kit/MainActivity.kt`](app/src/main/java/com/ail/android_base_kit/MainActivity.kt)
- 图片 Demo：[`app/src/main/java/com/ail/android_base_kit/image/ImageDemoActivity.kt`](app/src/main/java/com/ail/android_base_kit/image/ImageDemoActivity.kt)
- 网络 Demo：[`app/src/main/java/com/ail/android_base_kit/network/http/http/NetActivity.kt`](app/src/main/java/com/ail/android_base_kit/network/http/http/NetActivity.kt)
- WebSocket Demo：[`app/src/main/java/com/ail/android_base_kit/network/websocket/WebSocketDemoActivity.kt`](app/src/main/java/com/ail/android_base_kit/network/websocket/WebSocketDemoActivity.kt)
