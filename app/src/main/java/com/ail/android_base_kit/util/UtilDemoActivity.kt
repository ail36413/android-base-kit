package com.ail.android_base_kit.util

import android.Manifest
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ail.android_base_kit.R
import com.ail.android_base_kit.ui.bindToolbar
import com.ail.lib_util.click.ClickUtil
import com.ail.lib_util.device.AppInfoUtil
import com.ail.lib_util.device.ClipboardUtil
import com.ail.lib_util.device.IntentUtil
import com.ail.lib_util.device.NetworkUtil
import com.ail.lib_util.device.NetStateListenerUtil
import com.ail.lib_util.device.PermissionUtil
import com.ail.lib_util.log.CrashUtil
import com.ail.lib_util.log.LogUtil
import com.ail.lib_util.storage.CacheUtil
import com.ail.lib_util.storage.FilePathUtil
import com.ail.lib_util.storage.FileUtil
import com.ail.lib_util.storage.KvUtil
import com.ail.lib_util.storage.UriFileUtil
import com.ail.lib_util.thread.RetryUtil
import com.ail.lib_util.thread.ThreadUtil
import com.ail.lib_util.text.BooleanUtil
import com.ail.lib_util.text.CaseUtil
import com.ail.lib_util.text.ChecksumUtil
import com.ail.lib_util.text.CollectionUtil
import com.ail.lib_util.text.DecimalUtil
import com.ail.lib_util.text.EncodeUtil
import com.ail.lib_util.text.EncryptUtil
import com.ail.lib_util.text.FormatUtil
import com.ail.lib_util.text.HexUtil
import com.ail.lib_util.text.IdUtil
import com.ail.lib_util.text.JsonUtil
import com.ail.lib_util.text.MapUtil
import com.ail.lib_util.text.MaskUtil
import com.ail.lib_util.text.MathUtil
import com.ail.lib_util.text.NumberUtil
import com.ail.lib_util.text.RandomUtil
import com.ail.lib_util.text.RegexUtil
import com.ail.lib_util.text.StringUtil
import com.ail.lib_util.text.TemplateUtil
import com.ail.lib_util.text.UrlParamUtil
import com.ail.lib_util.text.ValidateUtil
import com.ail.lib_util.text.VersionUtil
import com.ail.lib_util.time.BenchmarkUtil
import com.ail.lib_util.time.DateRangeUtil
import com.ail.lib_util.time.DateTimeUtil
import com.ail.lib_util.ui.DisplayUtil
import com.ail.lib_util.ui.KeyboardUtil
import com.ail.lib_util.ui.ResourceUtil
import com.ail.lib_util.ui.ToastUtil
import com.ail.lib_util.ui.ViewUtil
import com.ail.lib_util.ui.ScreenUtil

class UtilDemoActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_MODE = "util_mode"
        const val MODE_ALL = "all"
        const val MODE_BASIC = "basic"
        const val MODE_TEXT = "text"
        const val MODE_STORAGE_PERF = "storage_perf"

        const val PREF_UTIL_DEMO = "util_demo_prefs"
        const val KEY_LAST_MODE = "last_mode"
    }

    private lateinit var tvStatus: TextView
    private lateinit var etKey: EditText
    private lateinit var etValue: EditText
    private lateinit var etNamespace: EditText

    private var netStateListenerToken: String? = null
    private var pendingPermissionRequest: Array<String> = emptyArray()

    private val permissionLauncher =
        registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val denied = pendingPermissionRequest.filter { permission -> result[permission] != true }
            val allGranted = denied.isEmpty()
            setStatus(
                "PermissionUtil",
                getString(R.string.util_status_permission_result, allGranted.toString(), denied.joinToString()),
            )
            pendingPermissionRequest = emptyArray()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_util_demo)

        bindToolbar(R.id.toolbar_util)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.util_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tvStatus = findViewById(R.id.tv_util_status)
        etKey = findViewById(R.id.et_key)
        etValue = findViewById(R.id.et_value)
        etNamespace = findViewById(R.id.et_namespace)

        val mode = normalizeMode(intent?.getStringExtra(EXTRA_MODE))
        supportActionBar?.title = getString(modeTitleRes(mode))
        applyDisplayMode(mode)
        setStatus("Mode", getString(modeHintRes(mode)))
        persistLastMode(mode)

        findViewById<Button>(R.id.btn_status_copy).setOnClickListener {
            val statusText = tvStatus.text?.toString().orEmpty()
            ClipboardUtil.copyText(statusText)
            setStatus("Status", getString(R.string.util_status_copied))
        }
        findViewById<Button>(R.id.btn_status_clear).setOnClickListener {
            clearStatus()
        }

        findViewById<Button>(R.id.btn_put_string).setOnClickListener {
            val key = readKey() ?: return@setOnClickListener
            val value = etValue.text.toString()
            KvUtil.putString(key, value)
            setStatus("KvUtil", getString(R.string.util_status_saved, key, value))
        }

        findViewById<Button>(R.id.btn_get_string).setOnClickListener {
            val key = readKey() ?: return@setOnClickListener
            val value = KvUtil.getString(key, "") ?: ""
            setStatus("KvUtil", getString(R.string.util_status_read, key, value))
        }

        findViewById<Button>(R.id.btn_put_int).setOnClickListener {
            val key = readKey() ?: return@setOnClickListener
            val raw = etValue.text.toString().trim()
            val value = raw.toIntOrNull()
            if (value == null) {
                setStatus("KvUtil", getString(R.string.util_status_value_invalid_int))
                return@setOnClickListener
            }
            KvUtil.putInt(key, value)
            setStatus("KvUtil", getString(R.string.util_status_saved_int, key, value))
        }

        findViewById<Button>(R.id.btn_get_int).setOnClickListener {
            val key = readKey() ?: return@setOnClickListener
            val value = KvUtil.getInt(key, 0)
            setStatus("KvUtil", getString(R.string.util_status_read_int, key, value))
        }

        findViewById<Button>(R.id.btn_namespace_put).setOnClickListener {
            val key = readKey() ?: return@setOnClickListener
            val mmapId = readNamespace() ?: return@setOnClickListener
            val value = etValue.text.toString()
            KvUtil.putString(key, value, mmapId)
            setStatus("KvUtil", getString(R.string.util_status_namespace_saved, mmapId, key, value))
        }

        findViewById<Button>(R.id.btn_namespace_get).setOnClickListener {
            val key = readKey() ?: return@setOnClickListener
            val mmapId = readNamespace() ?: return@setOnClickListener
            val value = KvUtil.getString(key, "", mmapId) ?: ""
            setStatus("KvUtil", getString(R.string.util_status_namespace_read, mmapId, key, value))
        }

        findViewById<Button>(R.id.btn_remove_key).setOnClickListener {
            val key = readKey() ?: return@setOnClickListener
            KvUtil.remove(key)
            setStatus("KvUtil", getString(R.string.util_status_removed, key))
        }

        findViewById<Button>(R.id.btn_remove_multi).setOnClickListener {
            val key = readKey() ?: return@setOnClickListener
            val valueKey = etValue.text.toString().trim().ifBlank { "value" }
            KvUtil.removeKeys(key, valueKey)
            setStatus("KvUtil", getString(R.string.util_status_removed_multi, "$key,$valueKey"))
        }

        findViewById<Button>(R.id.btn_contains_key).setOnClickListener {
            val key = readKey() ?: return@setOnClickListener
            val contains = KvUtil.containsKey(key)
            setStatus("KvUtil", getString(R.string.util_status_contains, key, contains.toString()))
        }

        findViewById<Button>(R.id.btn_count_keys).setOnClickListener {
            val count = KvUtil.count().toInt()
            setStatus("KvUtil", getString(R.string.util_status_count, count))
        }

        findViewById<Button>(R.id.btn_all_keys).setOnClickListener {
            val keys = KvUtil.allKeys().joinToString(prefix = "[", postfix = "]")
            setStatus("KvUtil", getString(R.string.util_status_all_keys, keys))
        }

        findViewById<Button>(R.id.btn_clear_all).setOnClickListener {
            KvUtil.clearAll()
            setStatus("KvUtil", getString(R.string.util_status_cleared))
        }

        val debounceButton = findViewById<Button>(R.id.btn_debounce_demo)
        ClickUtil.setDebouncedClickListener(debounceButton, intervalMs = 1200L) {
            setStatus("ClickUtil", getString(R.string.util_status_debounce_pass))
            LogUtil.i("Debounce click accepted")
        }

        findViewById<Button>(R.id.btn_thread_demo).setOnClickListener {
            setStatus("ThreadUtil", getString(R.string.util_status_thread_running))
            ThreadUtil.runOnIo {
                Thread.sleep(400)
                val message = "io done @${System.currentTimeMillis()}"
                LogUtil.d(message)
                ThreadUtil.runOnMain {
                    setStatus("ThreadUtil", getString(R.string.util_status_thread_done, message))
                }
            }
        }

        findViewById<Button>(R.id.btn_toast_demo).setOnClickListener {
            val message = etValue.text.toString().ifBlank { "Hello from ToastUtil" }
            ToastUtil.showShort(message)
            setStatus("ToastUtil", getString(R.string.util_status_toast_done))
        }

        findViewById<Button>(R.id.btn_clipboard_copy).setOnClickListener {
            val text = etValue.text.toString().ifBlank { "clipboard_demo_text" }
            ClipboardUtil.copyText(text)
            setStatus("ClipboardUtil", getString(R.string.util_status_clipboard_copied, text))
        }

        findViewById<Button>(R.id.btn_clipboard_read).setOnClickListener {
            val text = ClipboardUtil.getText().ifBlank { "<empty>" }
            setStatus("ClipboardUtil", getString(R.string.util_status_clipboard_read, text))
        }

        findViewById<Button>(R.id.btn_display_demo).setOnClickListener {
            val dpPx = DisplayUtil.dpToPx(16f, this)
            val spPx = DisplayUtil.spToPx(14f, this)
            val pxDp = DisplayUtil.pxToDp(100f, this)
            setStatus("DisplayUtil", getString(R.string.util_status_display_result, dpPx, spPx, pxDp))
        }

        findViewById<Button>(R.id.btn_screen_util_demo).setOnClickListener {
            val width = ScreenUtil.screenWidthPx(this)
            val height = ScreenUtil.screenHeightPx(this)
            val statusBar = ScreenUtil.statusBarHeight(this)
            val navBar = ScreenUtil.navigationBarHeight(this)
            val orientation = if (ScreenUtil.isLandscape(this)) "landscape" else "portrait"
            val insets = ScreenUtil.systemBarInsets(findViewById(R.id.util_root))
            val insetsText = if (insets == null) {
                "insets=unavailable"
            } else {
                "insets(top=${insets.statusBarTop}, bottom=${insets.navigationBarBottom}, left=${insets.left}, right=${insets.right})"
            }
            setStatus(
                "ScreenUtil",
                getString(R.string.util_status_screen_demo, width, height, statusBar, navBar, "$orientation, $insetsText"),
            )
        }

        findViewById<Button>(R.id.btn_screen_available_demo).setOnClickListener {
            val root = findViewById<View>(R.id.util_root)
            val screenWidth = ScreenUtil.screenWidthPx(this)
            val screenHeight = ScreenUtil.screenHeightPx(this)
            val availableWidth = ScreenUtil.availableContentWidthPx(root, this)
            val availableHeight = ScreenUtil.availableContentHeightPx(root, this)
            setStatus(
                "ScreenUtil",
                getString(
                    R.string.util_status_screen_available,
                    screenWidth,
                    screenHeight,
                    availableWidth,
                    availableHeight,
                ),
            )
        }

        findViewById<Button>(R.id.btn_keyboard_show).setOnClickListener {
            KeyboardUtil.show(etValue)
            setStatus("KeyboardUtil", getString(R.string.util_status_keyboard_show))
        }

        findViewById<Button>(R.id.btn_keyboard_hide).setOnClickListener {
            KeyboardUtil.hide(this)
            setStatus("KeyboardUtil", getString(R.string.util_status_keyboard_hide))
        }

        findViewById<Button>(R.id.btn_network_demo).setOnClickListener {
            val type = NetworkUtil.networkType().name
            val connected = NetworkUtil.isConnected()
            val metered = NetworkUtil.isMetered()
            setStatus("NetworkUtil", getString(R.string.util_status_network, "$connected, metered=$metered", type))
        }

        findViewById<Button>(R.id.btn_net_state_listener_demo).setOnClickListener {
            val token = netStateListenerToken
            if (token.isNullOrBlank()) {
                val newToken = NetStateListenerUtil.register { connected, type ->
                    runOnUiThread {
                        setStatus(
                            "NetStateListenerUtil",
                            getString(R.string.util_status_net_state_changed, connected.toString(), type.name),
                        )
                    }
                }
                if (newToken.isBlank()) {
                    setStatus("NetStateListenerUtil", getString(R.string.util_status_net_state_listener_fail))
                } else {
                    netStateListenerToken = newToken
                    setStatus("NetStateListenerUtil", getString(R.string.util_status_net_state_listener_started))
                }
            } else {
                NetStateListenerUtil.unregister(token)
                netStateListenerToken = null
                setStatus("NetStateListenerUtil", getString(R.string.util_status_net_state_listener_stopped))
            }
        }

        findViewById<Button>(R.id.btn_app_info_demo).setOnClickListener {
            val pkg = AppInfoUtil.packageName()
            val verName = AppInfoUtil.versionName()
            val verCode = AppInfoUtil.versionCode()
            setStatus("AppInfoUtil", getString(R.string.util_status_app_info, pkg, verName, verCode.toString()))
        }

        findViewById<Button>(R.id.btn_permission_util_demo).setOnClickListener {
            val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.POST_NOTIFICATIONS)
            } else {
                arrayOf(Manifest.permission.CAMERA)
            }
            val denied = PermissionUtil.deniedPermissions(this, *permissions)
            if (denied.isEmpty()) {
                setStatus("PermissionUtil", getString(R.string.util_status_permission_granted))
            } else {
                pendingPermissionRequest = denied.toTypedArray()
                permissionLauncher.launch(pendingPermissionRequest)
                setStatus("PermissionUtil", getString(R.string.util_status_permission_requesting, denied.joinToString()))
            }
        }

        findViewById<Button>(R.id.btn_open_browser).setOnClickListener {
            val ok = IntentUtil.openBrowser("https://github.com/Blankj/AndroidUtilCode")
            setStatus("IntentUtil", getString(if (ok) R.string.util_status_intent_success else R.string.util_status_intent_failed, "openBrowser"))
        }

        findViewById<Button>(R.id.btn_share_text).setOnClickListener {
            val content = etValue.text.toString().ifBlank { "share from util demo" }
            val ok = IntentUtil.shareText(content, "Share via")
            setStatus("IntentUtil", getString(if (ok) R.string.util_status_intent_success else R.string.util_status_intent_failed, "shareText"))
        }

        findViewById<Button>(R.id.btn_open_settings).setOnClickListener {
            val ok = IntentUtil.openAppSettings()
            setStatus("IntentUtil", getString(if (ok) R.string.util_status_intent_success else R.string.util_status_intent_failed, "openAppSettings"))
        }

        findViewById<Button>(R.id.btn_open_invalid).setOnClickListener {
            val ok = IntentUtil.openBrowser("bad://invalid-url")
            setStatus("IntentUtil", getString(if (ok) R.string.util_status_intent_success else R.string.util_status_intent_failed, "invalidUrl"))
        }

        findViewById<Button>(R.id.btn_view_util_demo).setOnClickListener {
            val target = debounceButton
            val nextVisible = target.visibility != View.VISIBLE
            ViewUtil.setVisible(target, nextVisible)
            setStatus("ViewUtil", getString(if (nextVisible) R.string.util_status_view_visible else R.string.util_status_view_gone))
        }

        findViewById<Button>(R.id.btn_resource_util_demo).setOnClickListener {
            val appName = ResourceUtil.string(R.string.app_name)
            val textColor = ResourceUtil.color(R.color.text_primary)
            setStatus("ResourceUtil", getString(R.string.util_status_resource_demo, appName, textColor.toString()))
        }

        findViewById<Button>(R.id.btn_validate_util_demo).setOnClickListener {
            val input = etValue.text.toString().ifBlank { "demo@example.com" }
            val email = ValidateUtil.isEmail(input)
            val url = ValidateUtil.isUrl(input)
            val phone = ValidateUtil.isMobileCN(input)
            setStatus("ValidateUtil", getString(R.string.util_status_validate_demo, email.toString(), url.toString(), phone.toString()))
        }

        findViewById<Button>(R.id.btn_datetime_util_demo).setOnClickListener {
            val now = DateTimeUtil.nowMillis()
            val text = DateTimeUtil.format(now)
            val isToday = DateTimeUtil.isToday(now)
            setStatus("DateTimeUtil", getString(R.string.util_status_datetime_demo, text, isToday.toString()))
        }

        findViewById<Button>(R.id.btn_format_util_demo).setOnClickListener {
            val phone = etValue.text.toString().ifBlank { "13812345678" }
            val duration = FormatUtil.formatDuration(125000)
            val size = FormatUtil.formatFileSize(5L * 1024L * 1024L + 200L)
            val masked = FormatUtil.maskPhone(phone)
            setStatus("FormatUtil", getString(R.string.util_status_format_demo, duration, size, masked))
        }

        findViewById<Button>(R.id.btn_json_util_demo).setOnClickListener {
            val key = etKey.text.toString().trim().ifBlank { "name" }
            val value = etValue.text.toString().ifBlank { "demo" }
            val json = JsonUtil.toJson(mapOf(key to value, "enabled" to true, "time" to DateTimeUtil.nowMillis()))
            val valid = JsonUtil.isObject(json)
            val enabled = JsonUtil.optBoolean(json, "enabled", false)
            setStatus("JsonUtil", getString(R.string.util_status_json_demo, "valid=$valid, enabled=$enabled, body=${JsonUtil.pretty(json).replace("\n", " ")}"))
        }

        findViewById<Button>(R.id.btn_encode_util_demo).setOnClickListener {
            val raw = etValue.text.toString().ifBlank { "hello base kit" }
            val b64 = EncodeUtil.base64Encode(raw)
            val decoded = EncodeUtil.base64Decode(b64)
            val urlEncoded = EncodeUtil.urlEncode(raw)
            val urlDecoded = EncodeUtil.urlDecode(urlEncoded)
            val b64Url = EncodeUtil.base64UrlEncode(raw)
            val b64UrlDecoded = EncodeUtil.base64UrlDecode(b64Url)
            val invalidFallback = EncodeUtil.base64Decode("%%%invalid%%%").ifEmpty { "<empty>" }
            val sha256Short = EncodeUtil.sha256(raw).take(12)
            setStatus(
                "EncodeUtil",
                getString(
                    R.string.util_status_encode_demo,
                    decoded,
                    urlDecoded,
                    b64UrlDecoded,
                    sha256Short,
                    invalidFallback,
                ),
            )
        }

        findViewById<Button>(R.id.btn_encrypt_util_demo).setOnClickListener {
            val raw = etValue.text.toString().ifBlank { "android-base-kit" }
            val key = etKey.text.toString().ifBlank { "demo_key_2026" }
            val encrypted = EncryptUtil.aesEncrypt(raw, key)
            val decrypted = EncryptUtil.aesDecrypt(encrypted, key).orEmpty()
            val hmac = EncryptUtil.hmacSha256(raw, key).take(16)
            setStatus("EncryptUtil", getString(R.string.util_status_encrypt_demo, encrypted.take(24), decrypted, hmac))
        }

        findViewById<Button>(R.id.btn_file_util_demo).setOnClickListener {
            val fileName = etKey.text.toString().trim().ifBlank { "demo.txt" }
            val content = etValue.text.toString().ifBlank { "file util demo" }
            FileUtil.mkdirs()
            val writeOk = FileUtil.writeText(fileName, content)
            val appendOk = FileUtil.appendText(fileName, " | append")
            val readBack = FileUtil.readText(fileName)
            val size = FormatUtil.formatFileSize(FileUtil.size(fileName))
            val modified = DateTimeUtil.format(FileUtil.lastModified(fileName))
            val files = FileUtil.listFiles().joinToString(prefix = "[", postfix = "]")
            val message = "${readBack} | modified=$modified | files=$files"
            setStatus("FileUtil", getString(R.string.util_status_file_demo, (writeOk && appendOk).toString(), message, size))
        }

        findViewById<Button>(R.id.btn_uri_file_util_demo).setOnClickListener {
            val uriText = etValue.text.toString().trim()
            val uri = if (uriText.isNotEmpty()) {
                uriText.toUri()
            } else {
                val sample = FileUtil.file("demo.txt")
                Uri.fromFile(sample)
            }
            val name = UriFileUtil.fileName(uri)
            val mime = UriFileUtil.mimeType(uri)
            val size = UriFileUtil.size(uri)
            setStatus("UriFileUtil", getString(R.string.util_status_uri_file_demo, name, mime.ifBlank { "<unknown>" }, size))
        }

        findViewById<Button>(R.id.btn_file_util_clear).setOnClickListener {
            val beforeCount = FileUtil.listFiles().size
            val cleared = FileUtil.clearDir()
            setStatus("FileUtil", getString(R.string.util_status_file_clear_demo, "$cleared, before=$beforeCount"))
        }

        findViewById<Button>(R.id.btn_log_demo).setOnClickListener {
            LogUtil.d("UtilDemo debug log")
            LogUtil.i("UtilDemo info log")
            LogUtil.w("UtilDemo warn log")
            LogUtil.e("UtilDemo error log")
            setStatus("LogUtil", getString(R.string.util_status_log_done))
        }

        findViewById<Button>(R.id.btn_crash_util_demo).setOnClickListener {
            if (!CrashUtil.isInstalled()) {
                CrashUtil.install(CrashUtil.Config()) { info ->
                    LogUtil.e("CrashUtil", "captured crash file=${info.file?.name.orEmpty()}")
                }
            }
            val file = CrashUtil.recordHandled(IllegalStateException("demo handled crash"))
            val count = CrashUtil.listCrashFiles().size
            setStatus(
                "CrashUtil",
                getString(R.string.util_status_crash_util_demo, file?.name.orEmpty(), count),
            )
        }

        findViewById<Button>(R.id.btn_string_util_demo).setOnClickListener {
            val raw = etValue.text.toString().ifBlank { "  Android Base Kit  " }
            val trimmed = StringUtil.orEmptyTrim(raw)
            val short = StringUtil.ellipsize(trimmed, maxLength = 8)
            val nullOrValue = StringUtil.nullIfBlank(etValue.text.toString()) ?: "<null>"
            setStatus("StringUtil", getString(R.string.util_status_string_demo, trimmed, short, nullOrValue))
        }

        findViewById<Button>(R.id.btn_number_util_demo).setOnClickListener {
            val raw = etValue.text.toString().ifBlank { "12.3456" }
            val intVal = NumberUtil.parseInt(raw, -1)
            val doubleVal = NumberUtil.parseDouble(raw)
            val clamped = NumberUtil.clamp(intVal, 0, 10)
            val round2 = NumberUtil.formatDecimal(doubleVal, scale = 2)
            setStatus("NumberUtil", getString(R.string.util_status_number_demo, intVal, doubleVal.toString(), clamped, round2))
        }

        findViewById<Button>(R.id.btn_collection_util_demo).setOnClickListener {
            val source = etValue.text.toString().ifBlank { "a,b,c,a" }
            val list = source.split(',').map { it.trim() }.filter { it.isNotEmpty() }
            val distinct = CollectionUtil.distinctStable(list)
            val chunks = CollectionUtil.chunkedSafe(distinct, 2)
            setStatus(
                "CollectionUtil",
                getString(
                    R.string.util_status_collection_demo,
                    list.size,
                    CollectionUtil.joinToStringSafe(distinct, "|"),
                    chunks.joinToString(prefix = "[", postfix = "]") { it.joinToString(separator = "/") },
                ),
            )
        }

        findViewById<Button>(R.id.btn_regex_util_demo).setOnClickListener {
            val raw = etValue.text.toString().ifBlank { "A-12 B-99 C-7" }
            val first = RegexUtil.findFirst(raw, "\\d+")
            val all = RegexUtil.findAll(raw, "\\d+")
            val replaced = RegexUtil.replace(raw, "\\d+", "*")
            setStatus(
                "RegexUtil",
                getString(R.string.util_status_regex_demo, first, all.joinToString(prefix = "[", postfix = "]"), replaced),
            )
        }

        findViewById<Button>(R.id.btn_id_util_demo).setOnClickListener {
            val uuid = IdUtil.uuid().take(10)
            val shortId = IdUtil.shortId(8)
            val timeId = IdUtil.timeBasedId(prefix = "demo_")
            setStatus("IdUtil", getString(R.string.util_status_id_demo, uuid, shortId, timeId))
        }

        findViewById<Button>(R.id.btn_retry_util_demo).setOnClickListener {
            var failCount = 0
            val result = RetryUtil.retryOrNull(
                RetryUtil.Config(maxAttempts = 3, initialDelayMs = 100L, backoffFactor = 1.5, maxDelayMs = 300L),
            ) { attempt ->
                if (attempt < 3) {
                    failCount++
                    error("mock fail#$attempt")
                }
                "success@$attempt"
            }
            setStatus("RetryUtil", getString(R.string.util_status_retry_util_demo, failCount, result ?: "<null>"))
        }

        findViewById<Button>(R.id.btn_random_util_demo).setOnClickListener {
            val valueInt = RandomUtil.nextInt(10, 100)
            val valueBool = RandomUtil.nextBoolean()
            val valueStr = RandomUtil.randomString(8)
            setStatus("RandomUtil", getString(R.string.util_status_random_demo, valueInt, valueBool.toString(), valueStr))
        }

        findViewById<Button>(R.id.btn_math_util_demo).setOnClickListener {
            val raw = etValue.text.toString().ifBlank { "24,36" }
            val parts = raw.split(',').map { it.trim() }
            val a = parts.getOrNull(0)?.toLongOrNull() ?: 24L
            val b = parts.getOrNull(1)?.toLongOrNull() ?: 36L
            val gcd = MathUtil.gcd(a, b)
            val lcm = MathUtil.lcm(a, b)
            val percent = MathUtil.percentage(a.toDouble(), b.toDouble())
            setStatus("MathUtil", getString(R.string.util_status_math_demo, gcd, lcm, percent.toString()))
        }

        findViewById<Button>(R.id.btn_cache_util_demo).setOnClickListener {
            val key = etKey.text.toString().trim().ifBlank { "cache_demo_key" }
            val value = etValue.text.toString().ifBlank { "cache_demo_value" }
            CacheUtil.put(key, value, ttlMs = 10_000L)
            val readBack = CacheUtil.get<String>(key).orEmpty()
            val size = CacheUtil.size()
            setStatus("CacheUtil", getString(R.string.util_status_cache_demo, key, readBack, size))
        }

        findViewById<Button>(R.id.btn_cache_clear_demo).setOnClickListener {
            CacheUtil.clear()
            setStatus("CacheUtil", getString(R.string.util_status_cache_clear, CacheUtil.size()))
        }

        findViewById<Button>(R.id.btn_benchmark_util_demo).setOnClickListener {
            val result = BenchmarkUtil.measure {
                var sum = 0L
                repeat(20_000) { sum += it }
                sum
            }
            setStatus("BenchmarkUtil", getString(R.string.util_status_benchmark_demo, result.value, result.costMs))
        }

        findViewById<Button>(R.id.btn_boolean_util_demo).setOnClickListener {
            val raw = etValue.text.toString().ifBlank { "yes" }
            val value = BooleanUtil.parse(raw)
            setStatus("BooleanUtil", getString(R.string.util_status_boolean_demo, value.toString(), BooleanUtil.toInt(value)))
        }

        findViewById<Button>(R.id.btn_map_util_demo).setOnClickListener {
            val base = mapOf("name" to "kit", "lang" to "kotlin")
            val override = mapOf("lang" to "java", "year" to "2026")
            val merged = MapUtil.merge(base, override)
            val lang = MapUtil.getOrDefault(merged, "lang", "unknown")
            setStatus("MapUtil", getString(R.string.util_status_map_demo, lang, merged.size, merged.toString()))
        }

        findViewById<Button>(R.id.btn_url_param_util_demo).setOnClickListener {
            val rawUrl = etValue.text.toString().ifBlank { "https://api.demo.com/path" }
            val full = UrlParamUtil.build(rawUrl, mapOf("type" to 1, "q" to "android kit"))
            val parsed = UrlParamUtil.parse(full)
            setStatus("UrlParamUtil", getString(R.string.util_status_url_param_demo, full, parsed.toString()))
        }

        findViewById<Button>(R.id.btn_version_util_demo).setOnClickListener {
            val current = etValue.text.toString().ifBlank { "1.2.10" }
            val target = "1.2.3"
            val compare = VersionUtil.compare(current, target)
            val atLeast = VersionUtil.isAtLeast(current, target)
            setStatus("VersionUtil", getString(R.string.util_status_version_demo, compare, atLeast.toString(), current, target))
        }

        findViewById<Button>(R.id.btn_mask_util_demo).setOnClickListener {
            val raw = etValue.text.toString().ifBlank { "13812345678" }
            val maskedPhone = MaskUtil.maskPhone(raw)
            val maskedEmail = MaskUtil.maskEmail("demo@example.com")
            setStatus("MaskUtil", getString(R.string.util_status_mask_demo, maskedPhone, maskedEmail))
        }

        findViewById<Button>(R.id.btn_case_util_demo).setOnClickListener {
            val raw = etValue.text.toString().ifBlank { "helloWorldCase" }
            val snake = CaseUtil.camelToSnake(raw)
            val camel = CaseUtil.snakeToCamel(snake)
            setStatus("CaseUtil", getString(R.string.util_status_case_demo, snake, camel))
        }

        findViewById<Button>(R.id.btn_hex_util_demo).setOnClickListener {
            val raw = etValue.text.toString().ifBlank { "android-kit" }
            val hex = HexUtil.stringToHexUtf8(raw)
            val decoded = HexUtil.hexToStringUtf8(hex)
            setStatus("HexUtil", getString(R.string.util_status_hex_demo, hex, decoded))
        }

        findViewById<Button>(R.id.btn_checksum_util_demo).setOnClickListener {
            val raw = etValue.text.toString().ifBlank { "android-kit" }
            val crc = ChecksumUtil.crc32(raw)
            val adler = ChecksumUtil.adler32(raw)
            setStatus("ChecksumUtil", getString(R.string.util_status_checksum_demo, crc, adler))
        }

        findViewById<Button>(R.id.btn_decimal_util_demo).setOnClickListener {
            val raw = etValue.text.toString().ifBlank { "12.5,3" }
            val parts = raw.split(',').map { it.trim() }
            val left = parts.getOrNull(0).orEmpty()
            val right = parts.getOrNull(1).orEmpty()
            val add = DecimalUtil.add(left, right)
            val divide = DecimalUtil.divide(left, right)
            setStatus("DecimalUtil", getString(R.string.util_status_decimal_demo, add, divide))
        }

        findViewById<Button>(R.id.btn_template_util_demo).setOnClickListener {
            val tpl = etValue.text.toString().ifBlank { "Hello %{name}, today is %{day}" }
            val text = TemplateUtil.render(tpl, mapOf("name" to "BaseKit", "day" to DateTimeUtil.format(pattern = "MM-dd")))
            val keys = TemplateUtil.keys(tpl).joinToString(prefix = "[", postfix = "]")
            setStatus("TemplateUtil", getString(R.string.util_status_template_demo, keys, text))
        }

        findViewById<Button>(R.id.btn_file_path_util_demo).setOnClickListener {
            val rawPath = etValue.text.toString().ifBlank { "demo/folder/test_file.txt" }
            val name = FilePathUtil.fileName(rawPath)
            val ext = FilePathUtil.extension(rawPath)
            val parent = FilePathUtil.parent(rawPath)
            setStatus("FilePathUtil", getString(R.string.util_status_file_path_demo, name, ext, parent))
        }

        findViewById<Button>(R.id.btn_date_range_util_demo).setOnClickListener {
            val now = DateTimeUtil.nowMillis()
            val start = DateRangeUtil.startOfDay(now)
            val end = DateRangeUtil.endOfDay(now)
            val inToday = DateRangeUtil.isInRange(now, start, end)
            setStatus(
                "DateRangeUtil",
                getString(
                    R.string.util_status_date_range_demo,
                    DateTimeUtil.format(start, "HH:mm:ss"),
                    DateTimeUtil.format(end, "HH:mm:ss"),
                    inToday.toString(),
                ),
            )
        }
    }


    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        netStateListenerToken?.let { NetStateListenerUtil.unregister(it) }
        netStateListenerToken = null
    }

    private fun readKey(): String? {
        val key = etKey.text.toString().trim()
        if (key.isEmpty()) {
            setStatus("KvUtil", getString(R.string.util_status_key_empty))
            return null
        }
        return key
    }

    private fun readNamespace(): String? {
        val mmapId = etNamespace.text.toString().trim()
        if (mmapId.isEmpty()) {
            setStatus("KvUtil", getString(R.string.util_status_namespace_empty))
            return null
        }
        return mmapId
    }

    private fun setStatus(scope: String, message: String) {
        tvStatus.text = getString(R.string.util_status_scope_message, scope, message)
    }

    private fun clearStatus() {
        tvStatus.text = getString(R.string.util_status_default)
    }

    private fun applyDisplayMode(mode: String?) {
        val allCards = listOf(
            R.id.card_util_intro,
            R.id.card_util_input,
            R.id.card_util_kv,
            R.id.card_util_click_ui,
            R.id.card_util_system,
            R.id.card_util_log,
            R.id.card_util_extra,
            R.id.card_util_text_tools,
            R.id.card_util_id_cache,
        )

        val visibleCards = when (mode) {
            MODE_BASIC -> listOf(
                R.id.card_util_intro,
                R.id.card_util_input,
                R.id.card_util_kv,
                R.id.card_util_click_ui,
                R.id.card_util_system,
                R.id.card_util_log,
                R.id.card_util_extra,
            )

            MODE_TEXT -> listOf(
                R.id.card_util_intro,
                R.id.card_util_text_tools,
            )

            MODE_STORAGE_PERF -> listOf(
                R.id.card_util_intro,
                R.id.card_util_input,
                R.id.card_util_kv,
                R.id.card_util_id_cache,
            )

            else -> allCards
        }

        allCards.forEach { id ->
            findViewById<View>(id)?.visibility = if (visibleCards.contains(id)) View.VISIBLE else View.GONE
        }
    }

    private fun modeTitleRes(mode: String?): Int = when (mode) {
        MODE_BASIC -> R.string.util_mode_title_basic
        MODE_TEXT -> R.string.util_mode_title_text
        MODE_STORAGE_PERF -> R.string.util_mode_title_storage_perf
        else -> R.string.util_mode_title_all
    }

    private fun modeHintRes(mode: String?): Int = when (mode) {
        MODE_BASIC -> R.string.util_mode_hint_basic
        MODE_TEXT -> R.string.util_mode_hint_text
        MODE_STORAGE_PERF -> R.string.util_mode_hint_storage_perf
        else -> R.string.util_mode_hint_all
    }

    private fun persistLastMode(mode: String) {
        getSharedPreferences(PREF_UTIL_DEMO, MODE_PRIVATE).edit {
            putString(KEY_LAST_MODE, mode)
        }
    }

    private fun normalizeMode(mode: String?): String {
        return when (mode) {
            MODE_BASIC, MODE_TEXT, MODE_STORAGE_PERF, MODE_ALL -> mode
            else -> MODE_ALL
        }
    }
}
