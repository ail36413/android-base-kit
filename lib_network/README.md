# lib_network

Android 网络基础库，支持 HTTP（OkHttp+Retrofit）与 WebSocket（多连接、心跳、重连、离线补发等），Kotlin/传统 XML 开发，独立运行与发布。

## 功能简介
- HTTP：统一结果包装、执行入口、异常处理、上传/下载、重试、轮询、动态 BaseUrl/Timeout、公共 Header、Token 刷新、进度上报、事件追踪
- WebSocket：多连接管理、心跳与超时、断线重连、离线消息补发、主线程/当前线程回调、连接状态回调、调试日志、OkHttpClient 可自定义
- Hilt 注入，模块独立、可单独集成

## 快速开始

### HTTP 用法

#### 1. 提供全局 NetworkConfig
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppNetworkModule {
    @Provides
    @Singleton
    fun provideNetworkConfig(): NetworkConfig {
        return NetworkConfig(
            baseUrl = "https://example.com/",
            isLogEnabled = true,
            extraHeaders = mapOf(
                "X-App-Version" to "1.0.0",
                "X-Version-Code" to "100"
            )
        )
    }
}
```

#### 2. 可选组件
- 日志输出、TokenProvider、拦截器均可选注入

#### 3. Retrofit 接口
- 业务实体实现 `IBaseResponse`
- 支持全局模型别名、动态 BaseUrl、Timeout 注解

#### 4. 统一执行入口
```kotlin
@Inject lateinit var networkExecutor: NetworkExecutor
val result = networkExecutor.executeRequest { demoApi.createUser(req) }
result.onSuccess { }.onTechnicalFailure { }.onBusinessFailure { code, msg -> }
```

#### 5. 上传/下载/轮询/重试/事件追踪
- 详见上方示例代码

### WebSocket 用法

#### 1. 默认 OkHttpClient
- 未提供 `@WebSocketClient` 时自动使用默认 OkHttpClient

#### 2. 自定义 OkHttpClient（可选）
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppNetworkModule {
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
}
```

#### 3. 注入并使用
```kotlin
@AndroidEntryPoint
class WebSocketDemoActivity : AppCompatActivity() {
    @Inject lateinit var wsManager: IWebSocketManager
    private val config = WebSocketManager.Config(
        enableHeartbeat = true,
        enableMessageReplay = true,
        callbackOnMainThread = true
    )
    private fun connect() {
        wsManager.connect(
            connectionId = "demo_ws",
            url = "wss://echo.websocket.events",
            config = config,
            listener = object : WebSocketManager.WebSocketListener {
                override fun onOpen(connectionId: String) {}
                override fun onMessage(connectionId: String, text: String) {}
                override fun onFailure(connectionId: String, throwable: Throwable) {}
            }
        )
    }
}
```

#### 4. 发送/重连/断开
```kotlin
wsManager.sendMessage("demo_ws", "hello")
wsManager.reconnect("demo_ws")
wsManager.disconnect("demo_ws", permanent = true)
```

#### 5. 回调说明
```kotlin
override fun onStateChanged(
    connectionId: String,
    oldState: WebSocketManager.State,
    newState: WebSocketManager.State
) { /* DISCONNECTED -> CONNECTING -> CONNECTED */ }
```

## 配置说明
- HTTP 配置项：baseUrl、extraHeaders、isLogEnabled、TokenProvider、拦截器等
- WebSocket 配置项：心跳、重连、离线补发、队列容量、主线程回调、日志开关等

## 参数说明
- 详见各类 Config、接口注释与源码

## 依赖说明
- OkHttp、Retrofit、Gson、Hilt

## 许可证
MIT
