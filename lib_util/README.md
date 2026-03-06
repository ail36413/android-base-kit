# lib_util

Android 工具库（Kotlin + XML，非 Compose），目标是 **开箱即用 + 低耦合 + 可按需引入**。

## 文档导航

- 仓库总览：[`README.md`](../README.md)
- 图片库文档：[`lib_image/README.md`](../lib_image/README.md)
- 网络库文档：[`lib_network/README.md`](../lib_network/README.md)
- API 逐方法说明：[`lib_util/docs/api-reference.md`](docs/api-reference.md)
- 注释覆盖清单：[`lib_util/docs/comment-coverage.md`](docs/comment-coverage.md)

## 依赖说明

- 版本来源：`gradle/libs.versions.toml`
- 核心依赖：
  - MMKV `2.3.0`
  - Timber `5.0.1`

```kotlin
dependencies {
    implementation(project(":lib_util"))
}
```

## 快速开始

```kotlin
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        UtilKit.init(this)
    }
}
```

可选自定义：

```kotlin
UtilKit.init(
    context = this,
    config = UtilConfig(
        enableTimber = true,
        debugLog = BuildConfig.DEBUG,
        tagPrefix = "BaseKitUtil",
        mmkvRootDir = null,
    ),
)
```

---

## 能力总览

| 分类 | 已有工具 |
|---|---|
| Core | `UtilKit`, `UtilConfig` |
| Storage | `KvUtil`, `FileUtil`, `FilePathUtil`, `CacheUtil` |
| Log/Click/Thread | `LogUtil`, `ClickUtil`, `ThreadUtil`, `RetryUtil` |
| Device | `AppInfoUtil`, `ClipboardUtil`, `IntentUtil`, `NetworkUtil`, `PermissionUtil` |
| UI | `ToastUtil`, `DisplayUtil`, `KeyboardUtil`, `ResourceUtil`, `ViewUtil` |
| Text/Format | `StringUtil`, `NumberUtil`, `FormatUtil`, `ValidateUtil`, `RegexUtil`, `CollectionUtil`, `MapUtil`, `BooleanUtil` |
| Encode/Security | `EncodeUtil`, `EncryptUtil`, `HexUtil`, `ChecksumUtil` |
| JSON/ID | `JsonUtil`, `IdUtil`, `TemplateUtil`, `UrlParamUtil`, `VersionUtil` |
| Time/Math/Random | `DateTimeUtil`, `DateRangeUtil`, `BenchmarkUtil`, `MathUtil`, `RandomUtil`, `DecimalUtil` |

> 逐方法用途请看：[`lib_util/docs/api-reference.md`](docs/api-reference.md)

---

## Demo 覆盖

`app` 模块 `UtilDemoActivity` 已覆盖：
- 存储：`KvUtil`（命名空间、批量删除、键统计）、`FileUtil`、`CacheUtil`
- 线程与点击：`ThreadUtil`、`RetryUtil`、`ClickUtil`
- 设备与系统：`NetworkUtil`、`AppInfoUtil`、`IntentUtil`、`ClipboardUtil`、`PermissionUtil`
- 文本与编码：`StringUtil`、`NumberUtil`、`RegexUtil`、`JsonUtil`、`EncodeUtil`、`EncryptUtil`
- 时间与数学：`DateTimeUtil`、`DateRangeUtil`、`BenchmarkUtil`、`MathUtil`、`RandomUtil`
- UI：`ToastUtil`、`DisplayUtil`、`KeyboardUtil`、`ResourceUtil`、`ViewUtil`

---

## 参考 AndroidUtilCode 的差距与规划

参考：<https://github.com/Blankj/AndroidUtilCode/blob/master/lib/utilcode/README-CN.md>

### 当前建议优先补充（P0）

1. `CrashUtil`：全局崩溃捕获、落盘与可选上报接口
2. `UriFileUtil`：`content://` 与文件路径互转、MIME/文件名提取增强
3. `NetStateListenerUtil`：网络变化监听（回调式）

> `PermissionUtil` 已完成并接入 Demo。

### 次优先（P1）

1. `SpanUtil`：常用富文本构建
2. `ScreenUtil`：状态栏/导航栏高度、全面屏辅助
3. `AppTaskUtil`：前后台状态与任务栈辅助

### 说明

- 会继续保持“互不依赖、默认可用、按类独立引入”的原则
- 每批次优先做高频、低侵入、可演示能力

---

## 注释规范

- 每个工具对象提供一句话用途说明（对象级 KDoc）
- 对外方法优先补齐参数/返回语义，明确失败回退策略
- 覆盖清单见：[`lib_util/docs/comment-coverage.md`](docs/comment-coverage.md)

## 许可证

MIT
