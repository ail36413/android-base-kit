package com.bohai.android_base_kit.http

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.ail.android_base_kit.R
import com.bohai.UserService
import com.bohai.apis.DemoApi
import com.bohai.apis.PayApi
import com.bohai.apis.UploadApi
import com.bohai.model.CreateUserRequest
import com.bohai.android_base_kit.model.StatusResponse
import okhttp3.ResponseBody
import com.ail.lib_network.http.model.NetworkResult
import com.ail.lib_network.http.model.ProgressInfo
import com.ail.lib_network.http.model.onBusinessFailure
import com.ail.lib_network.http.model.onTechnicalFailure
import com.ail.lib_network.http.model.onSuccess
import com.ail.lib_network.http.util.HashVerificationStrategy
import com.ail.lib_network.http.util.NetworkExecutor
import com.ail.lib_network.http.util.pollingFlow
import com.ail.lib_network.http.util.retryWithBackoff
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

// 新增 imports for token demo
import com.ail.lib_network.http.auth.TokenProvider
import java.util.Optional
import com.bohai.util.TokenRefreshHelper
import com.bohai.auth.AppTokenProvider
import com.bohai.model.User
import kotlinx.coroutines.Job
import java.io.IOException

@AndroidEntryPoint
class NetActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    @Inject
    lateinit var networkExecutor: NetworkExecutor

    @Inject
    lateinit var userService: UserService

    @Inject
    lateinit var demoApi: DemoApi

    @Inject
    lateinit var uploadApi: UploadApi

    @Inject
    lateinit var payApi: PayApi

    @Inject
    lateinit var tokenProviderOptional: Optional<TokenProvider>

    private lateinit var tvStatus: TextView
    private lateinit var btnGet: Button
    private lateinit var btnPost: Button
    private lateinit var btnUploadSingle: Button
    private lateinit var btnUploadMulti: Button
    private lateinit var btnCustomHeader: Button
    private lateinit var btnTimeout: Button
    private lateinit var btnRaw: Button
    private lateinit var btnDownload: Button
    private lateinit var btnCancelDownload: Button
    private lateinit var btnHashDownload: Button
    private lateinit var btnRetry: Button
    private lateinit var btnPoll: Button
    private lateinit var btnPay: Button
    private lateinit var btnTokenDemo: Button
    private lateinit var btnStatusModel: Button
    private lateinit var ivPreview: ImageView

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        // permissions result ignored for demo
    }

    // holder for cancel job
    private var currentCancelJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_net)

        tvStatus = findViewById(R.id.tv_status)
        btnGet = findViewById(R.id.btn_get)
        btnPost = findViewById(R.id.btn_post)
        btnUploadSingle = findViewById(R.id.btn_upload_single)
        btnUploadMulti = findViewById(R.id.btn_upload_multi)
        btnCustomHeader = findViewById(R.id.btn_custom_header)
        btnTimeout = findViewById(R.id.btn_timeout)
        btnRaw = findViewById(R.id.btn_raw)
        btnDownload = findViewById(R.id.btn_download)
        btnCancelDownload = findViewById(R.id.btn_cancel_download)
        btnHashDownload = findViewById(R.id.btn_hash_download)
        btnRetry = findViewById(R.id.btn_retry)
        btnPoll = findViewById(R.id.btn_poll)
        btnPay = findViewById(R.id.btn_pay)
        btnTokenDemo = findViewById(R.id.btn_token_demo)
        btnStatusModel = findViewById(R.id.btn_status_model)
        ivPreview = findViewById(R.id.iv_preview)

        btnGet.setOnClickListener { doGet() }
        btnPost.setOnClickListener { doPost() }
        btnUploadSingle.setOnClickListener { doUploadSingle() }
        btnUploadMulti.setOnClickListener { doUploadMulti() }
        btnCustomHeader.setOnClickListener { doCustomHeader() }
        btnTimeout.setOnClickListener { doTimeoutCall() }
        btnRaw.setOnClickListener { doRawRequest() }
        btnDownload.setOnClickListener {
            requestPermissionsIfNeeded()
            doDownload()
        }
        btnCancelDownload.setOnClickListener { cancelDownload() }
        btnHashDownload.setOnClickListener {
            requestPermissionsIfNeeded()
            doHashDownload()
        }
        btnRetry.setOnClickListener { doRetryDemo() }
        btnPoll.setOnClickListener { doPollingDemo() }
        btnPay.setOnClickListener { doPayExample() }
        btnTokenDemo.setOnClickListener { doTokenRefreshDemo() }
        btnStatusModel.setOnClickListener { doStatusModelDemo() }
    }

    private fun requestPermissionsIfNeeded() {
        val perms = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
        permissionLauncher.launch(perms)
    }

    private fun doGet() {
        tvStatus.text = "状态"
        launch(Dispatchers.IO) {
            val result = networkExecutor.executeRequest {
                userService.checkStatus(1)
            }
            launch(Dispatchers.Main) {
                result
                    .onSuccess { data ->
                        tvStatus.text = "GET 成功: ${data?.size ?: 0} 条"
                    }
                    .onTechnicalFailure { e ->
                        tvStatus.text = "GET 技术失败: ${e.message}"
                    }
                    .onBusinessFailure { code, msg ->
                        tvStatus.text = "GET 业务失败: $code, $msg"
                    }
            }
        }
    }

    private fun doPost() {
        tvStatus.text = "POST 请求中..."
        launch(Dispatchers.IO) {
            val result = networkExecutor.executeRequest<User> {
                demoApi.createUser(CreateUserRequest("demo", 18))
            }
            launch(Dispatchers.Main) {
                result
                    .onSuccess { tvStatus.text = "POST 成功: ${it?.toString() ?: ""}" }
                    .onTechnicalFailure { tvStatus.text = "POST 错误: ${it.message}" }
                    .onBusinessFailure { code, msg -> tvStatus.text = "POST 业务失败: $code, $msg" }
            }
        }
    }

    private fun doUploadSingle() {
        tvStatus.text = "单文件上传中..."
        val dir = filesDir
        val demoFile = File(dir, "upload_demo.txt")
        if (!demoFile.exists()) demoFile.writeText("demo")

        val progressFlow = MutableSharedFlow<ProgressInfo>(replay = 1, extraBufferCapacity = 64)
        launch(Dispatchers.IO) {
            val result = networkExecutor.uploadFile(
                file = demoFile,
                partName = "file",
                progressFlow = progressFlow
            ) { part -> uploadApi.uploadFile(part) }

            launch(Dispatchers.Main) {
                result.onSuccess { tvStatus.text = "上传成功" }
                    .onTechnicalFailure { tvStatus.text = "上传失败: ${it.message}" }
            }
        }
    }

    private fun doUploadMulti() {
        tvStatus.text = "多文件上传中..."
        val dir = filesDir
        val f1 = File(dir, "upload1.txt").apply { if (!exists()) writeText("1") }
        val f2 = File(dir, "upload2.txt").apply { if (!exists()) writeText("2") }
        val progressFlow1 = MutableSharedFlow<ProgressInfo>(replay = 1, extraBufferCapacity = 64)
        val progressFlow2 = MutableSharedFlow<ProgressInfo>(replay = 1, extraBufferCapacity = 64)

        val p1 = networkExecutor.createProgressPart("files", f1, progressFlow1)
        val p2 = networkExecutor.createProgressPart("files", f2, progressFlow2)
        val fields = mapOf("userId" to "123".toRequestBody())

        launch(Dispatchers.IO) {
            val result = networkExecutor.uploadParts(listOf(p1, p2), fields) { parts, fs ->
                uploadApi.uploadMultiple(parts, fs)
            }
            launch(Dispatchers.Main) {
                result.onSuccess { tvStatus.text = "多文件上传成功" }
                    .onTechnicalFailure { tvStatus.text = "多文件上传失败: ${it.message}" }
            }
        }
    }

    private fun doCustomHeader() {
        tvStatus.text = "自定义 Header 请求中..."
        launch(Dispatchers.IO) {
            val result = networkExecutor.executeRequest<User> {
                demoApi.createUserWithHeader("key--123", CreateUserRequest("withHeader", 1))
            }
            launch(Dispatchers.Main) {
                result
                    .onSuccess { tvStatus.text = "自定义 Header 请求成功: ${it?.toString() ?: ""}" }
                    .onTechnicalFailure { tvStatus.text = "自定义 Header 技术失败: ${it.message}" }
                    .onBusinessFailure { code, msg -> tvStatus.text = "自定义 Header 业务失败: $code, $msg" }
            }
        }
    }

    private fun doTimeoutCall() {
        tvStatus.text = "方法级超时调用中..."
        launch(Dispatchers.IO) {
            val result = networkExecutor.executeRequest {
                demoApi.createUserTimeout(CreateUserRequest("timeout", 2))
            }
            launch(Dispatchers.Main) {
                result
                    .onSuccess { tvStatus.text = "超时调用返回: ${it?.toString() ?: ""}" }
                    .onTechnicalFailure { tvStatus.text = "超时调用技术失败: ${it.message}" }
                    .onBusinessFailure { code, msg -> tvStatus.text = "超时调用业务失败: $code, $msg" }
            }
        }
    }

    private fun doPayExample() {
        tvStatus.text = "Pay 接口请求中..."
        launch(Dispatchers.IO) {
            val result = networkExecutor.executeRequest<String> { payApi.getPayStatus() }
            launch(Dispatchers.Main) {
                result
                    .onSuccess { tvStatus.text = "Pay 成功: ${it ?: ""}" }
                    .onTechnicalFailure { tvStatus.text = "Pay 技术失败: ${it.message}" }
                    .onBusinessFailure { code, msg -> tvStatus.text = "Pay 业务失败: $code, $msg" }
            }
        }
    }

    private fun doRawRequest() {
        tvStatus.text = "Raw 请求中..."
        launch(Dispatchers.IO) {
            val result = networkExecutor.executeRawRequest<ResponseBody> {
                userService.downloadFile("https://i2.hdslb.com/bfs/archive/88e096dbe81976fdeca5ec86962a232a71500520.jpg")
            }
            launch(Dispatchers.Main) {
                when (result) {
                    is NetworkResult.Success -> {
                        val size = result.data?.contentLength() ?: -1
                        tvStatus.text = "Raw 成功: size=$size"
                        result.data?.close()
                    }
                    is NetworkResult.TechnicalFailure -> tvStatus.text = "Raw 技术失败: ${result.exception.message}"
                    is NetworkResult.BusinessFailure -> tvStatus.text = "Raw 业务失败: ${result.code}, ${result.msg}"
                }
            }
        }
    }

    private fun doDownload() {
        tvStatus.text = "状态"
        val dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: filesDir
        val targetFile = File(dir, "demo_net.jpg")
        val progressFlow = MutableSharedFlow<ProgressInfo>(replay = 1, extraBufferCapacity = 64)

        val job = launch {
            progressFlow.collect { info ->
                tvStatus.text = "下载进度: ${info.progress}%"
            }
        }

        launch(Dispatchers.IO) {
            currentCancelJob = Job()
            val result = networkExecutor.downloadFile(
                targetFile = targetFile,
                progressFlow = progressFlow,
                cancelJob = currentCancelJob
            ) {
                userService.downloadFile("https://i2.hdslb.com/bfs/archive/88e096dbe81976fdeca5ec86962a232a71500520.jpg")
            }

            launch(Dispatchers.Main) {
                job.cancel()
                currentCancelJob = null
                result
                    .onSuccess { file ->
                        tvStatus.text = "下载成功: ${file?.absolutePath ?: ""}"
                        file?.let { ivPreview.setImageURI(Uri.fromFile(it)) }
                    }
                    .onTechnicalFailure { e ->
                        tvStatus.text = "下载失败: ${e.message}"
                    }
                    .onBusinessFailure { code, msg ->
                        tvStatus.text = "下载业务失败: $code, $msg"
                    }
            }
        }
    }

    private fun cancelDownload() {
        currentCancelJob?.cancel()
        tvStatus.text = "取消下载请求已发"
    }

    private fun doHashDownload() {
        tvStatus.text = "哈希校验下载中..."
        val dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: filesDir
        val targetFile = File(dir, "demo_hash.jpg")
        val progressFlow = MutableSharedFlow<ProgressInfo>(replay = 1, extraBufferCapacity = 64)

        launch(Dispatchers.IO) {
            val result = networkExecutor.downloadFile(
                targetFile = targetFile,
                progressFlow = progressFlow,
                expectedHash = "deadbeef",
                hashStrategy = HashVerificationStrategy.KEEP_ON_MISMATCH
            ) {
                userService.downloadFile("https://i2.hdslb.com/bfs/archive/88e096dbe81976fdeca5ec86962a232a71500520.jpg")
            }

            launch(Dispatchers.Main) {
                result
                    .onSuccess { file -> tvStatus.text = "哈希下载成功: ${file?.absolutePath ?: ""}" }
                    .onTechnicalFailure { e -> tvStatus.text = "哈希下载失败: ${e.message ?: ""}" }
            }
        }
    }

    private fun doRetryDemo() {
        tvStatus.text = "重试示例中..."
        launch(Dispatchers.IO) {
            try {
                val response = retryWithBackoff(
                    maxAttempts = 4,
                    shouldRetry = { t -> t is IOException }
                ) {
                    networkExecutor.executeRequest<User> { demoApi.createUser(CreateUserRequest("retry", 1)) }
                }
                launch(Dispatchers.Main) {
                    response
                        .onSuccess { tvStatus.text = "Retry 成功: ${it?.toString() ?: ""}" }
                        .onTechnicalFailure { tvStatus.text = "Retry 技术失败: ${it.message}" }
                        .onBusinessFailure { code, msg -> tvStatus.text = "Retry 业务失败: $code, $msg" }
                }
            } catch (t: Throwable) {
                launch(Dispatchers.Main) { tvStatus.text = "Retry 抛出异常: ${t.message}" }
            }
        }
    }

    private fun doPollingDemo() {
        tvStatus.text = "轮询示例中..."
        val flow = pollingFlow(
            periodMillis = 2000,
            maxAttempts = 5,
            stopWhen = { _ -> false
            }
        ) {
            networkExecutor.executeRequest<User> { demoApi.createUser(CreateUserRequest("poll", 2)) }
        }

        launch(Dispatchers.IO) {
            flow.collect { item ->
                launch(Dispatchers.Main) {
                    when (item) {
                        is NetworkResult.Success<*> -> tvStatus.text = "轮询成功: ${item.data?.toString() ?: ""}"
                        is NetworkResult.TechnicalFailure -> tvStatus.text = "轮询技术失败: ${item.exception.message}"
                        is NetworkResult.BusinessFailure -> tvStatus.text = "轮询业务失败: ${item.code}, ${item.msg}"
                    }
                }
            }
        }
    }

    // 新增方法：Token 刷新示例（使用注入的 TokenProvider，如果没有则提示）
    private fun doTokenRefreshDemo() {
        if (!tokenProviderOptional.isPresent) {
            tvStatus.text = "未配置 TokenProvider，无法演示刷新"
            return
        }

        val provider = tokenProviderOptional.get()
        tvStatus.text = "当前 token=${provider.getAccessToken() ?: "(null)"}，开始演示..."

        // 如果是 AppTokenProvider，可以给 demo 一个初始 token（仅演示用途）
        if (provider is AppTokenProvider) {
            provider.setAccessToken("initial_demo_token")
        }

        launch(Dispatchers.IO) {
            // 1) 演示业务级过期（200 + code=401）处理：使用 withAppLevelRefresh 包装网络调用
            val result = try {
                TokenRefreshHelper.withAppLevelRefresh(
                    expiredBusinessCode = 401,
                    tokenProvider = provider
                ) {
                    // 这里用 networkExecutor.executeRequest 去调用可能返回 NetworkResult 的接口
                    networkExecutor.executeRequest<String> { payApi.getPayStatus() }
                }
            } catch (t: Throwable) {
                null
            }

            launch(Dispatchers.Main) {
                when (result) {
                    is NetworkResult.Success -> tvStatus.text = "请求成功，data=${result.data}，当前 token=${provider.getAccessToken()}"
                    is NetworkResult.BusinessFailure -> tvStatus.text = "业务失败: ${result.code}, ${result.msg}，当前 token=${provider.getAccessToken()}"
                    is NetworkResult.TechnicalFailure -> tvStatus.text = "技术失败: ${result.exception.message}，当前 token=${provider.getAccessToken()}"
                    else -> tvStatus.text = "调用异常或被中断，当前 token=${provider.getAccessToken()}"
                }
            }
        }
    }

    private fun doStatusModelDemo() {
        tvStatus.text = "status 返回模型示例中..."
        launch(Dispatchers.IO) {
            val result = networkExecutor.executeRequest {
                // 这里用本地模拟的 StatusResponse，真实项目中由 Retrofit 返回
                StatusResponse(
                    status = true,
                    rawCode = 0,
                    msg = "ok",
                    data = User(id = "1", name = "demo", age = 18),
                    successCode = 0
                )
            }
            launch(Dispatchers.Main) {
                result
                    .onSuccess { tvStatus.text = "status 模型成功: ${it?.toString() ?: ""}" }
                    .onTechnicalFailure { tvStatus.text = "status 模型技术失败: ${it.message}" }
                    .onBusinessFailure { code, msg -> tvStatus.text = "status 模型业务失败: $code, $msg" }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel() // cancel CoroutineScope
    }
}
