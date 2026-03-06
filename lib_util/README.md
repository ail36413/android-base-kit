# lib_util

Android 工具库（Kotlin + XML），主打开箱即用、低耦合。

## 文档导航

- 仓库总览：[`README.md`](../README.md)
- 图片库文档：[`lib_image/README.md`](../lib_image/README.md)
- 网络库文档：[`lib_network/README.md`](../lib_network/README.md)

## 依赖说明（放在最前）

- 版本来源：`gradle/libs.versions.toml`
- 本库核心依赖：
  - MMKV: `2.3.0`
  - Timber: `5.0.1`

Gradle（本仓库本地模块）示例：

```kotlin
dependencies {
    implementation(project(":lib_util"))
}
```

Gradle（直接写坐标，后续发布后使用）示例：

```kotlin
dependencies {
    implementation("com.tencent:mmkv:2.3.0")
    implementation("com.jakewharton.timber:timber:5.0.1")
    implementation("com.your.group:lib_util:<version>")
}
```

## 快速开始

在 `Application` 中初始化一次：

```kotlin
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        UtilKit.init(this)
    }
}
```

---

## 工具类说明（按类介绍）

### 1) `UtilKit`

用途：库入口与全局初始化。

```kotlin
UtilKit.init(
    context = this,
    config = UtilConfig(
        enableTimber = true,
        debugLog = BuildConfig.DEBUG,
        tagPrefix = "BaseKitUtil",
        mmkvRootDir = null
    )
)
```

说明：
- 默认会初始化 MMKV
- 默认启用 Timber（release 下仅输出 error）
- `init` 为幂等调用，重复调用会直接返回

### 2) `KvUtil`

用途：MMKV 键值存储封装，支持默认空间和 `mmapId` 命名空间。

```kotlin
KvUtil.putString("token", "abc")
val token = KvUtil.getString("token")

KvUtil.putInt("age", 18)
val age = KvUtil.getInt("age")

KvUtil.containsKey("token")
KvUtil.count()
KvUtil.allKeys()
KvUtil.removeKeys("token", "age")
KvUtil.clearAll()
```

命名空间示例：

```kotlin
KvUtil.putString("token", "user_token", mmapId = "user_space")
val token = KvUtil.getString("token", mmapId = "user_space")
val keyCount = KvUtil.count("user_space")
val keys = KvUtil.allKeys("user_space")
```

### 3) `LogUtil`

用途：统一日志入口（基于 Timber）。

```kotlin
LogUtil.d("debug message")
LogUtil.i("info message")
LogUtil.w("warn message")
LogUtil.e("error message")
```

说明：
- `UtilKit` 初始化后：走 Timber（支持你配置的日志策略）
- 未初始化时：自动回退到 `android.util.Log`，可直接使用

### 4) `ClickUtil`

用途：防止重复快速点击。

```kotlin
ClickUtil.setDebouncedClickListener(button, intervalMs = 1200L) {
    // 1.2s 内重复点击会被拦截
}
```

说明：
- 不传 `key` 时，默认按“View 实例”隔离，不会与其他按钮互相干扰
- 支持 `clear(key)` / `clearAll()` 清理防抖记录

### 5) `ThreadUtil`

用途：主线程 / IO 线程快速切换。

```kotlin
ThreadUtil.runOnIo {
    // do work
    ThreadUtil.runOnMain {
        // update ui
    }
}

val task = ThreadUtil.runOnMainDelay(1000) { /* delayed */ }
ThreadUtil.cancelMainTask(task)
```

说明：
- `shutdown()` 后再次调用 `runOnIo` 会自动恢复线程池
- 提供 `isMainThread()` 快速判断线程

### 6) `ToastUtil`

用途：Toast 统一弹出（主线程安全）。

```kotlin
ToastUtil.showShort("操作成功")
ToastUtil.showLong("网络请求进行中")
```

### 7) `ClipboardUtil`

用途：剪贴板读写。

```kotlin
ClipboardUtil.copyText("hello")
val text = ClipboardUtil.getText()
val hasText = ClipboardUtil.hasText()
```

### 8) `DisplayUtil`

用途：dp/sp/px 尺寸转换。

```kotlin
val px = DisplayUtil.dpToPx(16f)
val dp = DisplayUtil.pxToDp(100f)
val textPx = DisplayUtil.spToPx(14f)
val textSp = DisplayUtil.pxToSp(42f)
```

说明：
- 支持 `context` 重载，优先使用应用实际 `displayMetrics`
- 保留 `dp2px/px2dp/sp2px/px2sp` 兼容旧调用

### 9) `KeyboardUtil`

用途：软键盘显示与隐藏。

```kotlin
KeyboardUtil.show(editText)
KeyboardUtil.hide(activity)
```

说明：
- `show` 在 view ready 后触发，减少首次调用无效概率
- `hide` 在无 `windowToken` 时安全返回

### 10) `NetworkUtil`

用途：网络连接状态与类型判断。

```kotlin
val connected = NetworkUtil.isConnected()
val isWifi = NetworkUtil.isWifi()
val isVpn = NetworkUtil.isVpn()
val isMetered = NetworkUtil.isMetered()
val type = NetworkUtil.networkType()
```

说明：
- 需要在宿主 `AndroidManifest.xml` 中声明：`android.permission.ACCESS_NETWORK_STATE`
- 调用前需完成 `UtilKit.init(...)`

### 11) `AppInfoUtil`

用途：读取应用信息（包名、版本号、是否调试、是否主进程）。

```kotlin
val pkg = AppInfoUtil.packageName()
val versionName = AppInfoUtil.versionName()
val versionCode = AppInfoUtil.versionCode()
val debuggable = AppInfoUtil.isDebuggable()
val mainProcess = AppInfoUtil.isMainProcess()
```

### 12) `IntentUtil`

用途：常用系统 Intent 跳转封装。

```kotlin
IntentUtil.openBrowser("github.com") // 自动补全为 https://
IntentUtil.shareText("hello", "Share via")
IntentUtil.openAppSettings()

val canOpen = IntentUtil.canHandle(Intent(Intent.ACTION_DIAL))
val started = IntentUtil.safeStart(Intent(Intent.ACTION_DIAL))
```

说明：
- `safeStart` 会先检查是否可处理，再启动，失败时返回 `false`

### 13) `ViewUtil`

用途：View 显隐与状态批量控制。

```kotlin
ViewUtil.visible(view)
ViewUtil.gone(view)
ViewUtil.setVisible(view, visible = true)
ViewUtil.setEnabled(false, button1, button2)
```

### 14) `ResourceUtil`

用途：统一读取字符串/颜色/图片/尺寸资源。

```kotlin
val title = ResourceUtil.string(R.string.app_name)
val color = ResourceUtil.color(R.color.text_primary)
val drawable = ResourceUtil.drawable(R.drawable.ic_launcher_foreground)
val marginPx = ResourceUtil.dimenPx(R.dimen.your_dimen)
```

说明：
- 调用前需完成 `UtilKit.init(...)`

### 15) `ValidateUtil`

用途：常见文本校验（邮箱/链接/手机号/IP/密码强度）。

```kotlin
ValidateUtil.isEmail("demo@example.com")
ValidateUtil.isUrl("https://github.com")
ValidateUtil.isMobileCN("13812345678")
ValidateUtil.isIpV4("192.168.1.1")
ValidateUtil.isStrongPassword("Abc12345")
```

### 16) `DateTimeUtil`

用途：时间格式化与解析。

```kotlin
val now = DateTimeUtil.nowMillis()
val text = DateTimeUtil.format(now, "yyyy-MM-dd HH:mm:ss")
val millis = DateTimeUtil.parse("2026-03-05 10:00:00")
val today = DateTimeUtil.isToday(now)
```

### 17) `FormatUtil`

用途：常用格式化能力（时长、文件大小、手机号脱敏）。

```kotlin
FormatUtil.formatDuration(125000)      // 02:05
FormatUtil.formatFileSize(5 * 1024L)   // 5.00 KB
FormatUtil.maskPhone("13812345678")   // 138****5678
```

### 18) `JsonUtil`

用途：轻量 JSON 构建、解析与美化（基于 `org.json`）。

```kotlin
val json = JsonUtil.toJson(mapOf("name" to "demo", "age" to 18, "enabled" to true))
val pretty = JsonUtil.pretty(json)
val name = JsonUtil.optString(json, "name")
val enabled = JsonUtil.optBoolean(json, "enabled")
val time = JsonUtil.optLong(json, "time")
val validObject = JsonUtil.isObject(json)
```

### 19) `EncodeUtil`

用途：常见编码能力（Base64、URL、摘要哈希）。

```kotlin
val raw = "hello base kit"

val base64 = EncodeUtil.base64Encode(raw)
val decoded = EncodeUtil.base64Decode(base64)
val bytes = EncodeUtil.base64DecodeToBytes(base64)

val urlEncoded = EncodeUtil.urlEncode(raw)
val urlDecoded = EncodeUtil.urlDecode(urlEncoded)
val urlDecodedOrNull = EncodeUtil.urlDecodeOrNull("%%%invalid%%")

val base64Url = EncodeUtil.base64UrlEncode(raw)
val base64UrlDecoded = EncodeUtil.base64UrlDecode(base64Url)
val base64UrlBytes = EncodeUtil.base64UrlDecodeToBytes(base64Url)

val md5 = EncodeUtil.md5(raw)
val sha256 = EncodeUtil.sha256(raw)
```

说明：
- `base64Decode/base64UrlDecode` 失败时返回空串（兼容旧行为）
- 需要明确区分失败场景时，优先使用 `base64DecodeToBytes` / `base64UrlDecodeToBytes` / `urlDecodeOrNull`
- `base64Encode` 同时支持 `String` 与 `ByteArray` 重载

### 20) `FileUtil`

用途：应用私有目录下文本文件读写。

```kotlin
FileUtil.writeText("demo.txt", "hello")
FileUtil.appendText("demo.txt", " | world")
val text = FileUtil.readText("demo.txt")
val size = FileUtil.size("demo.txt")
FileUtil.clearDir() // 清理 util_demo 目录
```

### 21) `StringUtil`

用途：常见字符串处理（空值、裁剪、省略）。

```kotlin
val trimmed = StringUtil.orEmptyTrim("  demo  ")
val short = StringUtil.ellipsize("android-base-kit", maxLength = 8)
val safe = StringUtil.nullIfBlank("   ") ?: "fallback"
val same = StringUtil.equalsIgnoreCase("Abc", "abc")
```

### 22) `NumberUtil`

用途：数字解析、范围约束、四舍五入与格式化。

```kotlin
val intVal = NumberUtil.parseInt("12", defaultValue = -1)
val doubleVal = NumberUtil.parseDouble("12.3456")
val clamped = NumberUtil.clamp(20, 0, 10) // 10
val rounded = NumberUtil.round(12.3456, 2) // 12.35
val text = NumberUtil.formatDecimal(12.3000, 2) // 12.3
```

### 23) `CollectionUtil`

用途：集合空安全处理与常见列表操作。

```kotlin
val list = listOf("a", "b", "a")
val distinct = CollectionUtil.distinctStable(list) // [a, b]
val chunks = CollectionUtil.chunkedSafe(distinct, 2) // [[a, b]]
val joined = CollectionUtil.joinToStringSafe(distinct, "|") // a|b
```

### 24) `RegexUtil`

用途：正则匹配、提取、替换与分割。

```kotlin
val text = "A-12 B-99 C-7"
val first = RegexUtil.findFirst(text, "\\d+") // 12
val all = RegexUtil.findAll(text, "\\d+") // [12, 99, 7]
val replaced = RegexUtil.replace(text, "\\d+", "*")
val parts = RegexUtil.split("a,b,c", ",")
```

### 25) `IdUtil`

用途：生成 UUID、短 ID、时间戳 ID。

```kotlin
val uuid = IdUtil.uuid()              // 去掉 - 的 UUID
val uuidWithDash = IdUtil.uuid(true)  // 标准 UUID
val short = IdUtil.shortId(8)
val timeId = IdUtil.timeBasedId(prefix = "demo_")
```

### 26) `RetryUtil`

用途：轻量重试执行器（不依赖网络模块），支持退避间隔。

```kotlin
val result = RetryUtil.retryOrNull(
    RetryUtil.Config(
        maxAttempts = 3,
        initialDelayMs = 100,
        backoffFactor = 1.5,
        maxDelayMs = 300,
    ),
) { attempt ->
    if (attempt < 3) error("mock fail")
    "success@$attempt"
}
```

### 27) `MathUtil`

用途：常见数学能力（最大公约数、最小公倍数、百分比）。

```kotlin
val gcd = MathUtil.gcd(24, 36) // 12
val lcm = MathUtil.lcm(24, 36) // 72
val percent = MathUtil.percentage(24.0, 36.0) // 66.67
```

### 28) `RandomUtil`

用途：随机数、随机布尔值、随机字符串。

```kotlin
val intVal = RandomUtil.nextInt(10, 100)
val boolVal = RandomUtil.nextBoolean()
val random = RandomUtil.randomString(8)
```

### 29) `CacheUtil`

用途：轻量内存缓存（支持简单 TTL 过期策略）。

```kotlin
CacheUtil.put("token", "abc", ttlMs = 10_000)
val token: String? = CacheUtil.get("token")
val size = CacheUtil.size()
CacheUtil.remove("token")
```

### 30) `BenchmarkUtil`

用途：轻量耗时测量，便于 Demo/调试阶段做基准观察。

```kotlin
val result = BenchmarkUtil.measure {
    var sum = 0L
    repeat(20_000) { sum += it }
    sum
}
// result.value / result.costMs
```

### 31) `BooleanUtil`

用途：布尔值解析、翻转与整型映射。

```kotlin
val flag = BooleanUtil.parse("yes")
val toggled = BooleanUtil.toggle(flag)
val intValue = BooleanUtil.toInt(flag)
```

### 32) `MapUtil`

用途：Map 常用能力（合并、默认值读取、空值过滤）。

```kotlin
val merged = MapUtil.merge(mapOf("a" to 1), mapOf("b" to 2, "a" to 3))
val a = MapUtil.getOrDefault(merged, "a", 0)
val safe = MapUtil.filterNotNullValues(mapOf("x" to 1, "y" to null))
```

### 33) `UrlParamUtil`

用途：URL Query 参数构建与解析。

```kotlin
val url = UrlParamUtil.build("https://api.demo.com/path", mapOf("type" to 1, "q" to "android kit"))
val params = UrlParamUtil.parse(url)
val append = UrlParamUtil.append("https://api.demo.com/path", "page", 2)
```

### 34) `VersionUtil`

用途：语义版本号比较（如 `1.2.10` 与 `1.2.3`）。

```kotlin
val compare = VersionUtil.compare("1.2.10", "1.2.3") // > 0
val ok = VersionUtil.isAtLeast("1.2.10", "1.2.3") // true
```

### 35) `MaskUtil`

用途：常见脱敏处理（手机号、邮箱、证件号）。

```kotlin
val phone = MaskUtil.maskPhone("13812345678")
val email = MaskUtil.maskEmail("demo@example.com")
val idCard = MaskUtil.maskIdCard("123456199001011234")
```

### 36) `CaseUtil`

用途：命名风格转换与首字母大小写处理。

```kotlin
val snake = CaseUtil.camelToSnake("helloWorld")
val camel = CaseUtil.snakeToCamel("hello_world")
val cap = CaseUtil.capitalizeFirst("demo")
```

### 37) `HexUtil`

用途：Hex 与字节/字符串互转。

```kotlin
val hex = HexUtil.stringToHexUtf8("android-kit")
val raw = HexUtil.hexToStringUtf8(hex)
val bytes = HexUtil.hexToBytes(hex)
```

### 38) `ChecksumUtil`

用途：轻量校验值（CRC32、Adler32、XOR）计算。

```kotlin
val crc = ChecksumUtil.crc32("android-kit")
val adler = ChecksumUtil.adler32("android-kit")
val xor = ChecksumUtil.xorChecksum("abc".toByteArray())
```

### 39) `DecimalUtil`

用途：基于 `BigDecimal` 的精确加减乘除与比较。

```kotlin
val add = DecimalUtil.add("12.5", "3")
val divide = DecimalUtil.divide("12.5", "3")
val compare = DecimalUtil.compare("1.20", "1.2") // 0
```

### 40) `TemplateUtil`

用途：模板占位替换（`%{key}` 语法）与占位键提取。

```kotlin
val tpl = "Hello %{name}, today is %{day}"
val text = TemplateUtil.render(tpl, mapOf("name" to "BaseKit", "day" to "03-06"))
val keys = TemplateUtil.keys(tpl) // [name, day]
```

### 41) `FilePathUtil`

用途：路径字符串处理（拼接、文件名、扩展名、父路径）。

```kotlin
val path = FilePathUtil.join("demo", "folder", "test.txt")
val name = FilePathUtil.fileName(path)
val ext = FilePathUtil.extension(path)
val parent = FilePathUtil.parent(path)
```

### 42) `DateRangeUtil`

用途：时间范围判断与当天起止时间计算。

```kotlin
val now = DateTimeUtil.nowMillis()
val start = DateRangeUtil.startOfDay(now)
val end = DateRangeUtil.endOfDay(now)
val inToday = DateRangeUtil.isInRange(now, start, end)
```

---

## Demo 覆盖

`app` 模块中的 `UtilDemoActivity` 已覆盖：
- `KvUtil` 的增删改查、命名空间、`contains/count/allKeys/removeKeys`
- `ClickUtil` 防抖行为
- `ThreadUtil` IO -> Main 切换
- `ToastUtil` / `ClipboardUtil` / `DisplayUtil` / `KeyboardUtil`
- `NetworkUtil` 类型与计费网络状态
- `AppInfoUtil` 信息读取
- `IntentUtil` 成功与失败分支
- `LogUtil` 四级日志
- `ViewUtil` / `ResourceUtil` / `ValidateUtil` / `DateTimeUtil` / `FormatUtil`
- `JsonUtil` / `EncodeUtil` / `FileUtil`（含文件目录清理演示）
- `StringUtil` / `NumberUtil` / `CollectionUtil`（文本、数字、集合常见处理）
- `RegexUtil` / `IdUtil` / `RetryUtil`（正则、ID、轻量重试）
- `MathUtil` / `RandomUtil` / `CacheUtil` / `BenchmarkUtil`（数学、随机、缓存、耗时测量）
- `BooleanUtil` / `MapUtil` / `UrlParamUtil` / `VersionUtil`（布尔、Map、URL 参数、版本比较）
- `MaskUtil` / `CaseUtil` / `HexUtil` / `ChecksumUtil`（脱敏、命名转换、Hex、校验值）
- `DecimalUtil` / `TemplateUtil` / `FilePathUtil` / `DateRangeUtil`（精确运算、模板、路径、时间范围）

## 设计约定

- 工具能力保持低耦合，不依赖 `lib_image` / `lib_network`
- 优先提供默认配置，减少接入方样板代码
- API 命名与仓库现有风格一致：`xxxUtil` + `init once`
- 当前仅覆盖高频工具，后续按模块迭代扩展

### API 注释规范

- 每个工具对象必须有一句话用途说明（对象级 KDoc）
- 对外公开函数优先补齐参数/返回说明，尤其是默认值与失败回退策略
- 对异常分支要写清楚返回约定（如返回空串、`null`、`false`）
- 注释简洁直达使用者，不写实现细节噪音

### 注释覆盖状态

- 当前 `lib_util` 已按统一风格补齐主要公共 API 注释
- 新增工具类时请同步补齐对象级说明与关键参数说明
- 覆盖清单见：`lib_util/docs/comment-coverage.md`

## 许可证

MIT
