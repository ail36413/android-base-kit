package com.ail.android_base_kit.network.http

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

class LoginActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // simple placeholder UI
        val tv = TextView(this)
        tv.text = "Login Screen (demo)"
        tv.textSize = 20f
        setContentView(tv)
    }
}
