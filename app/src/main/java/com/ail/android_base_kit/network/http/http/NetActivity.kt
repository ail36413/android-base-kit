package com.ail.android_base_kit.network.http.http

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.ail.android_base_kit.R
import com.ail.android_base_kit.network.http.UserService
import com.ail.android_base_kit.network.http.http.apis.DemoApi
import com.ail.android_base_kit.network.http.http.apis.PayApi
import com.ail.android_base_kit.network.http.http.apis.UploadApi
import com.ail.android_base_kit.network.http.model.CreateUserRequest
import com.ail.android_base_kit.network.http.model.StatusResponse
import com.ail.android_base_kit.network.http.model.ResponseMappingPresets
import com.ail.lib_network.http.annotations.NetworkConfigProvider
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
import com.ail.android_base_kit.network.http.util.TokenRefreshHelper
import com.ail.android_base_kit.network.http.http.auth.AppTokenProvider
import com.ail.android_base_kit.network.http.model.User
import kotlinx.coroutines.Job
import java.io.IOException
import androidx.annotation.StringRes

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

    @Inject
    lateinit var networkConfigProvider: NetworkConfigProvider

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
    private lateinit var switchResponseMapping: SwitchCompat

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        // permissions result ignored for demo
    }

    // holder for cancel job
    private var currentCancelJob: Job? = null

    private companion object {
        const val DEMO_IMAGE_URL = "https://picsum.photos/1200/800"
    }

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
        switchResponseMapping = findViewById(R.id.switch_response_mapping)

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

        setupResponseMappingSwitch()
    }

    private fun setStatus(@StringRes resId: Int, vararg args: Any) {
        tvStatus.text = getString(resId, *args)
    }

    private fun setStatusText(@StringRes resId: Int) {
        tvStatus.text = getString(resId)
    }

    private fun requestPermissionsIfNeeded() {
        val perms = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
        permissionLauncher.launch(perms)
    }

    private fun doGet() {
        setStatusText(R.string.status_default)
        launch(Dispatchers.IO) {
            val result = networkExecutor.executeRequest {
                userService.checkStatus(1)
            }
            launch(Dispatchers.Main) {
                result
                    .onSuccess { data ->
                        setStatus(R.string.get_success, data?.size ?: 0)
                    }
                    .onTechnicalFailure { e ->
                        setStatus(R.string.get_tech_fail, e.message ?: "")
                    }
                    .onBusinessFailure { code, msg ->
                        setStatus(R.string.get_business_fail, code, msg)
                    }
            }
        }
    }

    private fun doPost() {
        setStatusText(R.string.post_in_progress)
        launch(Dispatchers.IO) {
            val result = networkExecutor.executeRequest<User> {
                demoApi.createUser(CreateUserRequest("demo", 18))
            }
            launch(Dispatchers.Main) {
                result
                    .onSuccess { setStatus(R.string.post_success_fmt, it?.toString() ?: "") }
                    .onTechnicalFailure { setStatus(R.string.post_error_fmt, it.message ?: "") }
                    .onBusinessFailure { code, msg -> setStatus(R.string.post_business_fail_fmt, code, msg) }
            }
        }
    }

    private fun doUploadSingle() {
        setStatusText(R.string.upload_single_in_progress)
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
                result.onSuccess { setStatusText(R.string.upload_success) }
                    .onTechnicalFailure { setStatus(R.string.upload_error_fmt, it.message ?: "") }
            }
        }
    }

    private fun doUploadMulti() {
        setStatusText(R.string.upload_multi_in_progress)
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
                result.onSuccess { setStatusText(R.string.upload_multi_success) }
                    .onTechnicalFailure { setStatus(R.string.upload_multi_error_fmt, it.message ?: "") }
            }
        }
    }

    private fun doCustomHeader() {
        setStatusText(R.string.custom_header_in_progress)
        launch(Dispatchers.IO) {
            val result = networkExecutor.executeRequest<User> {
                demoApi.createUserWithHeader("key--123", CreateUserRequest("withHeader", 1))
            }
            launch(Dispatchers.Main) {
                result
                    .onSuccess { setStatus(R.string.custom_header_success_fmt, it?.toString() ?: "") }
                    .onTechnicalFailure { setStatus(R.string.custom_header_tech_fail_fmt, it.message ?: "") }
                    .onBusinessFailure { code, msg -> setStatus(R.string.custom_header_business_fail_fmt, code, msg) }
            }
        }
    }

    private fun doTimeoutCall() {
        setStatusText(R.string.timeout_in_progress)
        launch(Dispatchers.IO) {
            val result = networkExecutor.executeRequest {
                demoApi.createUserTimeout(CreateUserRequest("timeout", 2))
            }
            launch(Dispatchers.Main) {
                result
                    .onSuccess { setStatus(R.string.timeout_success_fmt, it?.toString() ?: "") }
                    .onTechnicalFailure { setStatus(R.string.timeout_tech_fail_fmt, it.message ?: "") }
                    .onBusinessFailure { code, msg -> setStatus(R.string.timeout_business_fail_fmt, code, msg) }
            }
        }
    }

    private fun doPayExample() {
        setStatusText(R.string.pay_in_progress)
        launch(Dispatchers.IO) {
            val result = networkExecutor.executeRequest<String> { payApi.getPayStatus() }
            launch(Dispatchers.Main) {
                result
                    .onSuccess { setStatus(R.string.pay_success_fmt, it ?: "") }
                    .onTechnicalFailure { setStatus(R.string.pay_tech_fail_fmt, it.message ?: "") }
                    .onBusinessFailure { code, msg -> setStatus(R.string.pay_business_fail_fmt, code, msg) }
            }
        }
    }

    private fun doRawRequest() {
        setStatusText(R.string.raw_in_progress)
        launch(Dispatchers.IO) {
            val result = networkExecutor.executeRawRequest<ResponseBody> {
                userService.downloadFile(DEMO_IMAGE_URL)
            }
            launch(Dispatchers.Main) {
                when (result) {
                    is NetworkResult.Success -> {
                        val size = result.data?.contentLength() ?: -1
                        setStatus(R.string.raw_success_size, size)
                        result.data?.close()
                    }
                    is NetworkResult.TechnicalFailure -> setStatus(R.string.raw_tech_fmt, result.exception.message ?: "")
                    is NetworkResult.BusinessFailure -> setStatus(R.string.raw_business_msg_fmt, result.code, result.msg)
                }
            }
        }
    }

    private fun doDownload() {
        setStatusText(R.string.status_default)
        val dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: filesDir
        val targetFile = File(dir, "demo_net.jpg")
        val progressFlow = MutableSharedFlow<ProgressInfo>(replay = 1, extraBufferCapacity = 64)

        val job = launch {
            progressFlow.collect { info ->
                setStatus(R.string.download_progress, info.progress)
            }
        }

        launch(Dispatchers.IO) {
            currentCancelJob = Job()
            val result = networkExecutor.downloadFile(
                targetFile = targetFile,
                progressFlow = progressFlow,
                cancelJob = currentCancelJob
            ) {
                userService.downloadFile(DEMO_IMAGE_URL)
            }

            launch(Dispatchers.Main) {
                job.cancel()
                currentCancelJob = null
                result
                    .onSuccess { file ->
                        setStatus(R.string.download_success, file?.absolutePath ?: "")
                        file?.let { ivPreview.setImageURI(Uri.fromFile(it)) }
                    }
                    .onTechnicalFailure { e ->
                        if (e.code == -999) {
                            setStatusText(R.string.download_cancelled)
                        } else {
                            setStatus(R.string.download_fail, e.message ?: "")
                        }
                    }
                    .onBusinessFailure { code, msg ->
                        setStatus(R.string.download_business_fail, code, msg)
                    }
            }
        }
    }

    private fun cancelDownload() {
        currentCancelJob?.cancel()
        setStatusText(R.string.cancel_download_sent)
    }

    private fun doHashDownload() {
        setStatusText(R.string.hash_download_in_progress)
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
                userService.downloadFile(DEMO_IMAGE_URL)
            }

            launch(Dispatchers.Main) {
                result
                    .onSuccess { file -> setStatus(R.string.hash_download_success, file?.absolutePath ?: "") }
                    .onTechnicalFailure { e -> setStatus(R.string.hash_download_fail_fmt, e.message ?: "") }
            }
        }
    }

    private fun doRetryDemo() {
        setStatusText(R.string.retry_in_progress)
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
                        .onSuccess { setStatus(R.string.retry_success_fmt, it?.toString() ?: "") }
                        .onTechnicalFailure { setStatus(R.string.retry_tech_fail_fmt, it.message ?: "") }
                        .onBusinessFailure { code, msg -> setStatus(R.string.retry_business_fail_fmt, code, msg) }
                }
            } catch (t: Throwable) {
                launch(Dispatchers.Main) { setStatus(R.string.retry_thrown_fmt, t.message ?: "") }
            }
        }
    }

    private fun doPollingDemo() {
        setStatusText(R.string.poll_in_progress)
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
                        is NetworkResult.Success<*> -> setStatus(R.string.poll_success, item.data?.toString() ?: "")
                        is NetworkResult.TechnicalFailure -> setStatus(R.string.poll_tech_fmt, item.exception.message ?: "")
                        is NetworkResult.BusinessFailure -> setStatus(R.string.poll_business_with_msg_fmt, item.code, item.msg)
                    }
                }
            }
        }
    }

    private fun doTokenRefreshDemo() {
        if (!tokenProviderOptional.isPresent) {
            setStatusText(R.string.token_demo_not_configured)
            return
        }

        val provider = tokenProviderOptional.get()
        setStatus(R.string.token_demo_current, provider.getAccessToken() ?: "(null)")

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
                    is NetworkResult.Success -> setStatus(R.string.token_demo_success, result.data ?: "", provider.getAccessToken() ?: "")
                    is NetworkResult.BusinessFailure -> setStatus(R.string.token_demo_business_fail, result.code, result.msg, provider.getAccessToken() ?: "")
                    is NetworkResult.TechnicalFailure -> setStatus(R.string.token_demo_tech_fail, result.exception.message ?: "", provider.getAccessToken() ?: "")
                    else -> setStatus(R.string.token_demo_error, provider.getAccessToken() ?: "")
                }
            }
        }
    }

    private fun doStatusModelDemo() {
        setStatusText(R.string.status_model_in_progress)
        launch(Dispatchers.IO) {
            val result = networkExecutor.executeRequest {
                // 这里用本地模拟的 StatusResponse，真实项目中由 Retrofit 返回
                StatusResponse(
                    status = true,
                    rawCode = 0,
                    msg = "ok",
                    localData = User(id = "1", name = "demo", age = 18),
                    successCode = 0
                )
            }
            launch(Dispatchers.Main) {
                result
                    .onSuccess { setStatus(R.string.status_model_success_fmt, it?.toString() ?: "") }
                    .onTechnicalFailure { setStatus(R.string.status_model_tech_fail_fmt, it.message ?: "") }
                    .onBusinessFailure { code, msg -> setStatus(R.string.status_model_business_fail_fmt, code, msg) }
            }
        }
    }

    private fun setupResponseMappingSwitch() {
        val current = networkConfigProvider.current.responseFieldMapping
        val isStatusMessageMode = current.codeKey == "status" && current.msgKey == "message"
        switchResponseMapping.setOnCheckedChangeListener(null)
        switchResponseMapping.isChecked = isStatusMessageMode
        switchResponseMapping.text = getString(
            if (isStatusMessageMode) R.string.response_mapping_switch_on else R.string.response_mapping_switch_off
        )
        switchResponseMapping.setOnCheckedChangeListener { _, isChecked ->
            applyResponseMappingPreset(isChecked)
        }
    }

    private fun applyResponseMappingPreset(useStatusMessagePreset: Boolean) {
        val targetMapping = if (useStatusMessagePreset) {
            ResponseMappingPresets.statusMessageData()
        } else {
            ResponseMappingPresets.standardCodeMsgData()
        }
        networkConfigProvider.update { old ->
            old.copy(responseFieldMapping = targetMapping)
        }
        switchResponseMapping.text = getString(
            if (useStatusMessagePreset) R.string.response_mapping_switch_on else R.string.response_mapping_switch_off
        )
        setStatusText(
            if (useStatusMessagePreset) {
                R.string.response_mapping_applied_status_message
            } else {
                R.string.response_mapping_applied_standard
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel() // cancel CoroutineScope
    }
}
