package com.ail.lib_util

import android.content.Context
import com.tencent.mmkv.MMKV
import timber.log.Timber

/**
 * Entry of lib_util. Call [init] once in Application.
 */
object UtilKit {

    @Volatile
    private var initialized = false

    @Volatile
    private var currentConfig: UtilConfig = UtilConfig()

    lateinit var appContext: Context
        private set

    /** 是否已完成初始化。 */
    fun isInitialized(): Boolean = initialized

    /** 获取当前全局配置快照。 */
    fun config(): UtilConfig = currentConfig

    /**
     * 初始化工具库（建议仅在 Application.onCreate 调用一次）。
     *
     * @param context 任意 Context，内部会自动持有 applicationContext。
     * @param config 全局配置，控制日志与 MMKV 初始化行为。
     */
    @Synchronized
    fun init(context: Context, config: UtilConfig = UtilConfig()) {
        if (initialized) return

        appContext = context.applicationContext
        currentConfig = config

        if (config.mmkvRootDir.isNullOrBlank()) {
            MMKV.initialize(appContext)
        } else {
            MMKV.initialize(appContext, config.mmkvRootDir)
        }

        if (config.enableTimber && Timber.forest().isEmpty()) {
            if (config.debugLog) {
                Timber.plant(Timber.DebugTree())
            } else {
                Timber.plant(ReleaseTree(config.tagPrefix))
            }
        }

        initialized = true
    }

    /**
     * 要求外部先调用 [init]，否则抛出明确异常。
     */
    internal fun requireInit() {
        check(initialized) {
            "UtilKit is not initialized. Call UtilKit.init(applicationContext) in Application.onCreate()."
        }
    }

    private class ReleaseTree(private val defaultTag: String) : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            // Release mode keeps error logs only by default.
            if (priority < android.util.Log.ERROR) return
            android.util.Log.println(priority, tag ?: defaultTag, message)
            if (t != null) {
                android.util.Log.println(priority, tag ?: defaultTag, t.stackTraceToString())
            }
        }
    }
}
