# lib_network

Android 网络基础库，支持 HTTP（OkHttp + Retrofit）与 WebSocket（多连接、心跳、重连、离线补发等）。

## 目录（从简单到复杂）
1. 5 分钟接入清单（先跑通）
2. 最低配置：只配 baseUrl 即可用
3. 常用增强：日志、公共 Header、超时
4. 稳定性增强：重试、下载上传、轮询
5. 鉴权增强：Token 刷新与 401 处理
6. 响应结构增强：全局字段映射（code/msg/data、status/message/data）
7. 项目扩展：自定义拦截器、多 Retrofit
8. WebSocket 快速接入
9. 错误码与异常处理速查
10. 生产环境建议（推荐）
11. 常见问题（FAQ）
12. 发布前检查清单（Release Checklist）
13. Demo 功能对照表

### 学习路径建议
- `只想先跑通`: 看 `0 -> 1 -> 2.1/2.4`
- `要接生产`: 再看 `4 -> 6 -> 9 -> 11`
- `要做实时通信`: 在 HTTP 跑通后看 `8`

---

## 0. 接入前置（必须）
`lib_network` 使用 Hilt 提供核心对象（`NetworkExecutor`、`Retrofit`、`IWebSocketManager` 等），接入方必须启用 Hilt。

### 根工程 `build.gradle.kts`
```kotlin
plugins {
    id("com.google.dagger.hilt.android") version "2.51" apply false
    id("com.google.devtools.ksp") version "2.0.21-1.0.28" apply false
}
```

### app 模块 `build.gradle.kts`
```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

dependencies {
    implementation("com.google.dagger:hilt-android:2.51")
    ksp("com.google.dagger:hilt-android-compiler:2.51")

    // 当前仓库本地模块
    implementation(project(":lib_network"))

    // 发布后改为：
    // implementation("com.your.group:lib_network:<version>")
}
```

### Application 标注
```kotlin
@HiltAndroidApp
class App : Application()
```

### 使用注入的页面标注
```kotlin
@AndroidEntryPoint
class NetActivity : AppCompatActivity()
```

---

## 1. 5 分钟接入清单（先跑通）

适用场景：`新项目首接入`、`先验证链路可用`

按这个顺序做，最快能在项目中发起第一个请求：

- [ ] 在工程启用 Hilt（插件 + 依赖 + `@HiltAndroidApp`）
- [ ] 在 app 的 Hilt Module 提供最小 `NetworkConfig(baseUrl = "...")`
- [ ] 定义一个 Retrofit API，返回 `GlobalResponse<T>`
- [ ] 注入 `NetworkExecutor` + API，并调用 `executeRequest { ... }`
- [ ] 先只验证成功链路，确认能拿到 `onSuccess`

最小可复制版本：

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppNetworkModule {

    @Provides
    @Singleton
    fun provideNetworkConfig(): NetworkConfig {
        return NetworkConfig(baseUrl = "https://httpbin.org/")
    }
}
```

```kotlin
interface DemoApi {
    @POST("anything/user/create")
    suspend fun createUser(@Body req: CreateUserRequest): GlobalResponse<User>
}
```

```kotlin
@Inject lateinit var networkExecutor: NetworkExecutor
@Inject lateinit var demoApi: DemoApi

val result = networkExecutor.executeRequest {
    demoApi.createUser(CreateUserRequest("demo", 18))
}
```

### 1.1 快速排错第一步（建议按顺序）
- [ ] `baseUrl` 是否以 `/` 结尾
- [ ] `Application` 是否加了 `@HiltAndroidApp`
- [ ] 调用页面是否加了 `@AndroidEntryPoint`
- [ ] API 返回结构是否与 `GlobalResponse` 默认 `code/msg/data` 一致
- [ ] 若不一致，是否已配置 `responseFieldMapping`

### 1.2 完整最小工程片段（可直接改包名使用）
```kotlin
// App.kt
@HiltAndroidApp
class App : Application()

// AppNetworkModule.kt
@Module
@InstallIn(SingletonComponent::class)
object AppNetworkModule {
    @Provides
    @Singleton
    fun provideNetworkConfig(): NetworkConfig {
        return NetworkConfig(baseUrl = "https://httpbin.org/")
    }

    @Provides
    @Singleton
    fun provideDemoApi(retrofit: Retrofit): DemoApi = retrofit.create(DemoApi::class.java)
}

// DemoApi.kt
interface DemoApi {
    @POST("anything/user/create")
    suspend fun createUser(@Body req: CreateUserRequest): GlobalResponse<User>
}

// DemoActivity.kt
@AndroidEntryPoint
class DemoActivity : AppCompatActivity() {

    @Inject lateinit var networkExecutor: NetworkExecutor
    @Inject lateinit var demoApi: DemoApi

    fun load() {
        lifecycleScope.launch {
            val result = networkExecutor.executeRequest {
                demoApi.createUser(CreateUserRequest("demo", 18))
            }
            result
                .onSuccess { }
                .onBusinessFailure { _, _ -> }
                .onTechnicalFailure { }
        }
    }
}
```

---

## 2. 最低配置：只配 baseUrl 即可用（推荐先跑通）

适用场景：`Demo`、`内部工具`、`早期验证`

### 2.1 提供最小 `NetworkConfig`
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppNetworkModule {

    @Provides
    @Singleton
    fun provideNetworkConfig(): NetworkConfig {
        return NetworkConfig(
            // 必填：必须 http/https，且必须以 / 结尾
            baseUrl = "https://httpbin.org/"
        )
    }
}
```

### 2.2 定义接口（推荐统一返回 `GlobalResponse<T>`）
```kotlin
interface DemoApi {
    @POST("anything/user/create")
    suspend fun createUser(@Body req: CreateUserRequest): GlobalResponse<User>
}
```

### 2.3 发起请求
```kotlin
@Inject lateinit var networkExecutor: NetworkExecutor
@Inject lateinit var demoApi: DemoApi

val result = networkExecutor.executeRequest {
    demoApi.createUser(req)
}

result
    .onSuccess { }
    .onTechnicalFailure { }
    .onBusinessFailure { code, msg -> }
```

---

## 3. 常用增强：日志、公共 Header、超时

适用场景：`联调阶段`、`多环境排查`

### 3.1 开启日志（简单方式）
```kotlin
NetworkConfig(
    baseUrl = "https://httpbin.org/",
    isLogEnabled = true
)
```

### 3.2 自定义日志输出（可选）
```kotlin
@Provides
@Singleton
fun provideNetLogger(): INetLogger = object : INetLogger {
    override fun d(tag: String, msg: String) = Log.d(tag, msg)
    override fun e(tag: String, msg: String, throwable: Throwable?) = Log.e(tag, msg, throwable)
}
```

### 3.3 全局公共 Header
```kotlin
NetworkConfig(
    baseUrl = "https://httpbin.org/",
    extraHeaders = mapOf(
        "X-App-Version" to "1.0.0",
        "X-Version-Code" to "100"
    )
)
```

### 3.4 全局超时 + 单接口超时
```kotlin
NetworkConfig(
    baseUrl = "https://httpbin.org/",
    connectTimeout = 15,
    readTimeout = 15,
    writeTimeout = 15
)
```

```kotlin
@Timeout(connect = 30, read = 30, write = 30)
@POST("anything/user/create")
suspend fun createUserTimeout(@Body req: CreateUserRequest): GlobalResponse<User>
```

---

## 4. 稳定性增强：重试、下载上传、轮询

适用场景：`弱网场景`、`任务型请求`

### 4.1 开启重试拦截器
```kotlin
NetworkConfig(
    baseUrl = "https://httpbin.org/",
    enableRetryInterceptor = true,
    retryMaxAttempts = 2,
    retryInitialBackoffMs = 300
)
```

### 4.2 下载、上传、轮询
- 下载：`NetworkExecutor.downloadFile(...)`
- 上传：`NetworkExecutor.uploadFile(...)` / `uploadParts(...)`
- 轮询：`pollingFlow(...)`

> `-999` 表示请求被取消（如主动取消下载）。

---

## 5. 鉴权增强：Token 刷新与 401

适用场景：`登录态系统`、`有过期刷新要求`

### 5.1 提供 `TokenProvider`（启用自动刷新）
```kotlin
@Provides
@Singleton
fun provideTokenProvider(provider: AppTokenProvider): TokenProvider = provider
```

### 5.2 提供 `UnauthorizedHandler`（401 回调）
```kotlin
@Provides
@Singleton
fun provideUnauthorizedHandler(handler: AppUnauthorizedHandler): UnauthorizedHandler = handler
```

说明：
- 提供 `TokenProvider` 后，库会注册 `TokenAuthenticator`。
- 请求出现 401 时会按库内流程尝试刷新并重试；失败后回调 `UnauthorizedHandler`。

---

## 6. 响应结构增强：全局字段映射

适用场景：`后端返回结构不统一`、`多项目复用同一网络层`

默认无需配置，按 `code/msg/data` 解析。

### 6.1 标准结构（默认）
```kotlin
NetworkConfig(
    baseUrl = "https://api.example.com/"
)
```

### 6.2 自定义为 `status/message/data`
```kotlin
NetworkConfig(
    baseUrl = "https://api.example.com/",
    responseFieldMapping = ResponseFieldMapping(
        codeKey = "status",
        msgKey = "message",
        dataKey = "data",
        successCode = 0,
        failureCode = -1,
        codeValueConverter = { rawCode, mapping ->
            when (rawCode) {
                is Boolean -> if (rawCode) mapping.successCode else mapping.failureCode
                is Number -> rawCode.toInt()
                is String -> rawCode.toIntOrNull() ?: mapping.failureCode
                else -> mapping.failureCode
            }
        }
    )
)
```

### 6.3 字段偶发变化时的 fallback
```kotlin
responseFieldMapping = ResponseFieldMapping(
    codeKey = "code",
    msgKey = "msg",
    dataKey = "data",
    codeFallbackKeys = listOf("status", "rawCode"),
    msgFallbackKeys = listOf("message"),
    dataFallbackKeys = listOf("payload", "result", "json")
)
```

---

## 7. 项目扩展：自定义拦截器、多 Retrofit

适用场景：`中大型项目`、`多业务域`

### 7.1 注入项目拦截器（带顺序）
```kotlin
@Provides
@AppInterceptor
fun provideAppInterceptors(): Map<Int, Interceptor> {
    return mapOf(
        100 to AuthTokenInterceptor(...),
        200 to DeviceIdInterceptor(...)
    )
}
```

### 7.2 多 Retrofit（多业务域）
实现 `NetworkClientFactory`，按业务域创建不同 `Retrofit`。

---

## 8. WebSocket 快速接入

适用场景：`实时消息`、`状态推送`、`长连接`

### 8.1 直接注入
```kotlin
@AndroidEntryPoint
class WebSocketDemoActivity : AppCompatActivity() {
    @Inject lateinit var wsManager: IWebSocketManager
}
```

### 8.2 可选：自定义 WebSocket `OkHttpClient`
```kotlin
@Provides
@Singleton
@WebSocketClient
fun provideWebSocketOkHttpClient(): OkHttpClient {
    return OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .pingInterval(30, TimeUnit.SECONDS)
        .build()
}
```

---

## 配置说明速查

### HTTP
- `baseUrl`：必填，必须 `http/https` 且以 `/` 结尾
- `connectTimeout/readTimeout/writeTimeout`：默认 15 秒
- `defaultSuccessCode`：默认 0
- `isLogEnabled`：默认 false
- `extraHeaders`：默认空
- `cacheDir/cacheSize`：同时提供时启用 OkHttp Cache
- `enableRetryInterceptor/retryMaxAttempts/retryInitialBackoffMs`：重试相关
- `responseFieldMapping`：全局响应字段映射

### WebSocket（默认值）
| 参数 | 默认值 | 说明 |
|------|--------|------|
| connectTimeout | 10s | 连接超时 |
| readTimeout | 60s | 读取超时 |
| writeTimeout | 60s | 写入超时 |
| enableHeartbeat | true | 应用层心跳 |
| heartbeatIntervalMs | 30000 | 心跳间隔 |
| reconnectBaseDelayMs | 2000 | 重连基础延迟 |
| reconnectMaxDelayMs | 30000 | 重连最大延迟 |
| enableMessageReplay | false | 离线补发 |

---

## 9. 错误码与异常处理速查

适用场景：`统一错误处理`、`线上问题定位`

### 9.1 `NetworkResult` 三种结果
- `Success<T>`：请求成功，数据在 `data`
- `BusinessFailure(code, msg)`：HTTP 正常返回，但业务失败
- `TechnicalFailure(exception)`：网络/解析/超时等技术异常

### 9.2 常见错误码
| code | 场景 | 说明 |
|------|------|------|
| 0（默认） | 业务成功 | 由 `defaultSuccessCode` 或 `@SuccessCode` 决定 |
| 401 | 鉴权失效 | 可触发 token 刷新，失败后回调 `UnauthorizedHandler` |
| -999 | 请求取消 | 主动取消下载或任务取消 |

### 9.3 推荐处理方式
```kotlin
result
    .onSuccess { data ->
        // 正常展示
    }
    .onBusinessFailure { code, msg ->
        // 业务弹窗/提示
    }
    .onTechnicalFailure { e ->
        // 网络、解析、超时等统一处理
    }
```

---

## 10. 生产环境建议（推荐）

适用场景：`上线前检查`、`性能稳定性优化`

- 关闭详细日志：`isLogEnabled = false`（release 环境）
- 控制重试次数：建议 `retryMaxAttempts` 在 `1~2`，避免雪崩放大
- 合理超时：弱网场景可适当调大 `readTimeout`，避免误判失败
- 保持 `baseUrl` 稳定：环境切换建议通过 `NetworkConfigProvider.update` 统一修改
- 公共 Header 固定化：版本号、设备标识等通过 `extraHeaders` 统一注入
- 响应映射单点配置：统一在 `responseFieldMapping` 调整，避免接口层分散适配

---

## 11. 常见问题（FAQ）

适用场景：`快速自助排障`

### Q1：`baseUrl` 为什么必须以 `/` 结尾？
Retrofit 要求 `baseUrl` 必须是目录语义，否则会在运行时报错。库里已做 fail-fast 校验。

### Q2：为什么我返回成功了但进入 `BusinessFailure`？
检查：
1. `defaultSuccessCode` 是否和后端成功码一致
2. 是否使用了 `@SuccessCode` 再覆盖
3. 全局 `responseFieldMapping` 的 `codeKey` / `codeValueConverter` 是否匹配

### Q3：GET/POST 某些接口偶发解析失败怎么办？
优先在 `responseFieldMapping` 配置 `dataFallbackKeys`（如 `payload`、`result`、`json`），避免因为后端字段不一致导致解析失败。

### Q4：切换了 `status/message/data` 后部分旧接口异常？
给映射增加 fallback：
```kotlin
responseFieldMapping = ResponseFieldMapping(
    codeKey = "status",
    msgKey = "message",
    dataKey = "data",
    codeFallbackKeys = listOf("code"),
    msgFallbackKeys = listOf("msg")
)
```

### Q5：401 没有自动刷新？
确认：
1. 已提供 `TokenProvider`
2. 刷新逻辑返回 `true`
3. 项目未被业务层拦截提前消费掉 401

---

## 12. 发布前检查清单（Release Checklist）

适用场景：`发布前自检`、`CI 发布门禁`

- [ ] `README` 中的最小接入示例可直接运行
- [ ] `NetworkConfig.baseUrl` fail-fast 校验行为符合预期（空值/无 `/` 能及时报错）
- [ ] release 构建关闭详细日志（`isLogEnabled = false`）
- [ ] `defaultSuccessCode` 与服务端约定一致
- [ ] 若后端结构非 `code/msg/data`，已配置 `responseFieldMapping`
- [ ] 鉴权项目已接入 `TokenProvider`，并验证 401 刷新链路
- [ ] 下载取消行为已验证（`-999`）
- [ ] 核心演示页可运行：`NetActivity`、`WebSocketDemoActivity`
- [ ] 依赖版本与 AGP/Kotlin 版本在项目内一致

建议在 CI 里至少执行：

```bash
./gradlew :lib_network:compileDebugKotlin
./gradlew :app:compileDebugKotlin
```

Windows 可使用：

```powershell
.\gradlew.bat :lib_network:compileDebugKotlin
.\gradlew.bat :app:compileDebugKotlin
```

---

## 13. Demo 功能对照表

适用场景：`快速定位示例入口`、`文档与演示互查`

### HTTP Demo（`app/src/main/java/com/ail/android_base_kit/network/http/http/NetActivity.kt`）
| 按钮/入口 | 能力点 | 对应章节 |
|---|---|---|
| `GET 请求示例` | 基础请求 + 统一结果处理 | `2`、`9` |
| `POST 请求示例` | 基础请求 + 业务返回解析 | `2`、`6` |
| `自定义 Header 示例` | 请求级 Header 示例 | `3`、`7` |
| `方法级超时 示例` | `@Timeout` 覆盖超时 | `3` |
| `单文件上传/多文件上传` | 上传与进度 | `4` |
| `下载/取消下载/哈希校验下载` | 下载、取消、校验 | `4`、`9` |
| `重试示例/轮询示例` | 重试与轮询 | `4` |
| `Token 刷新示例` | 鉴权与 401 处理 | `5`、`11` |
| `响应映射开关` | `code/msg/data` 与 `status/message/data` 切换 | `6` |

### WebSocket Demo（`app/src/main/java/com/ail/android_base_kit/network/websocket/WebSocketDemoActivity.kt`）
| 按钮/入口 | 能力点 | 对应章节 |
|---|---|---|
| `连接/断开/手动重连` | 连接生命周期与重连 | `8` |
| `发送文本/二进制` | 消息发送 | `8` |
| `默认连接快捷 API` | 单连接快捷调用 | `8` |
| `日志区域` | 状态回调与调试 | `8`、`10` |

---

## App 演示覆盖
- `app` 首页可进入 HTTP 与 WebSocket 演示
- HTTP 演示：`app/src/main/java/com/ail/android_base_kit/network/http/http/NetActivity.kt`
  - 支持运行时切换响应映射（`code/msg/data` 与 `status/message/data`）
  - GET/POST、上传下载、重试轮询、动态超时、Token 刷新等
- WebSocket 演示：`app/src/main/java/com/ail/android_base_kit/network/websocket/WebSocketDemoActivity.kt`

## 许可证
MIT
