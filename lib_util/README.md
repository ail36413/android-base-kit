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

> 说明：`NetStateListenerUtil` 依赖 `ACCESS_NETWORK_STATE`，已在 `lib_util` 清单声明，默认会通过 manifest merge 注入到宿主应用。

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
| Storage | `KvUtil`, `FileUtil`, `FilePathUtil`, `UriFileUtil`, `CacheUtil` |
| Log/Click/Thread | `LogUtil`, `CrashUtil`, `ClickUtil`, `ThreadUtil`, `RetryUtil` |
| Device | `AppInfoUtil`, `ClipboardUtil`, `IntentUtil`, `NetworkUtil`, `NetStateListenerUtil`, `PermissionUtil` |
| UI | `ToastUtil`, `DisplayUtil`, `ScreenUtil`, `KeyboardUtil`, `ResourceUtil`, `ViewUtil` |
| Text/Format | `StringUtil`, `NumberUtil`, `FormatUtil`, `ValidateUtil`, `RegexUtil`, `CollectionUtil`, `MapUtil`, `BooleanUtil`, `TemplateUtil`, `UrlParamUtil`, `VersionUtil`, `MaskUtil`, `CaseUtil`, `DecimalUtil` |
| Encode/Security | `EncodeUtil`, `EncryptUtil`, `HexUtil`, `ChecksumUtil` |
| JSON/ID | `JsonUtil`, `IdUtil` |
| Time/Math/Random | `DateTimeUtil`, `DateRangeUtil`, `BenchmarkUtil`, `MathUtil`, `RandomUtil` |

> 逐方法用途请看：[`lib_util/docs/api-reference.md`](docs/api-reference.md)

---

## Demo 覆盖

`app` 模块 `UtilDemoActivity` 已覆盖：
- 存储：`KvUtil`（命名空间、批量删除、键统计）、`FileUtil`、`FilePathUtil`、`UriFileUtil`、`CacheUtil`
- 线程与点击：`ThreadUtil`、`RetryUtil`、`ClickUtil`
- 设备与系统：`NetworkUtil`、`NetStateListenerUtil`、`AppInfoUtil`、`IntentUtil`、`ClipboardUtil`、`PermissionUtil`
- 文本与编码：`StringUtil`、`NumberUtil`、`DecimalUtil`、`RegexUtil`、`CollectionUtil`、`MapUtil`、`BooleanUtil`、`TemplateUtil`、`UrlParamUtil`、`VersionUtil`、`MaskUtil`、`CaseUtil`、`JsonUtil`、`EncodeUtil`、`EncryptUtil`、`HexUtil`、`ChecksumUtil`
- 时间与数学：`DateTimeUtil`、`DateRangeUtil`、`BenchmarkUtil`、`MathUtil`、`RandomUtil`、`IdUtil`
- UI：`ToastUtil`、`DisplayUtil`、`ScreenUtil`、`KeyboardUtil`、`ResourceUtil`、`ViewUtil`
- 缓存演示：`CacheUtil` 写入/读取/清空（`btn_cache_util_demo` + `btn_cache_clear_demo`）

---

## 参考 AndroidUtilCode 的差距与规划

参考：<https://github.com/Blankj/AndroidUtilCode/blob/master/lib/utilcode/README-CN.md>

### 当前建议优先补充（P0）

1. `SpanUtil`：常用富文本构建（颜色、点击、前景/背景样式）
2. `ScreenUtil`：状态栏/导航栏高度、全面屏与横竖屏辅助
3. `AppTaskUtil`：前后台状态、任务栈与 Activity 栈顶判断

### 次优先（P1）

1. `ZipUtil`：zip/unzip 与目录压缩
2. `ShellUtil`：受限命令执行封装（仅演示安全场景）
3. `InstallUtil`：APK 安装意图辅助（含 FileProvider 场景）

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

## 最近优化（2026-03）

- `RetryUtil`
  - `Config` 新增 `shouldRetry`，可按异常类型与 attempt 精细控制是否继续重试。
  - 遇到 `InterruptedException` 会立即中断并透传，不再吞掉中断信号。
- `DateTimeUtil`
  - 新增 `parseStrict`，用于严格日期解析（非法日期直接返回 `null`）。
- `UrlParamUtil`
  - `build` 支持 `#fragment` 安全拼接，避免 query 被追加到 fragment 后。
- `ScreenUtil`
  - 新增 `systemBarInsets(view)` 与 `availableContentWidthPx/availableContentHeightPx`。
  - Demo 新增“可用内容区”按钮，便于快速验证沉浸式布局下的可用尺寸。
