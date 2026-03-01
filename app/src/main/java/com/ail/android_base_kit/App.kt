package com.ail.android_base_kit

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.ail.android_base_kit.network.http.LoginActivity
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean

@HiltAndroidApp
class App : Application() {

    companion object {
        lateinit var instance: App
            private set
    }

    // Thread-safe list of activities currently alive in the app
    private val activities = CopyOnWriteArrayList<Activity>()
    private val mainHandler = Handler(Looper.getMainLooper())
    // Guard to avoid triggering multiple simultaneous navigations to LoginActivity
    private val navigating = AtomicBoolean(false)

    override fun onCreate() {
        super.onCreate()
        instance = this

        // register callbacks to track activities
        registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: android.os.Bundle?) {
                activities.add(activity)
            }

            override fun onActivityDestroyed(activity: Activity) {
                activities.remove(activity)
            }

            // unused lifecycle methods
            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: android.os.Bundle) {}
        })


    }

    /**
     * Finish all activities and navigate to LoginActivity on the main thread.
     * This method is safe to call from background threads.
     */
    fun logoutAndGoLogin() {
        // ensure UI operations happen on main thread and guard duplicates
        mainHandler.post {
            // if already navigating, skip
            if (!navigating.compareAndSet(false, true)) return@post
            try {
                for (a in activities) {
                    try { a.finish() } catch (_: Exception) {}
                }
                activities.clear()

                val intent = Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
            } catch (e: Exception) {
                // swallow to avoid crashing network thread callers
            }
        }
    }

}