package com.ail.android_base_kit.network.http

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ail.android_base_kit.R
import com.ail.android_base_kit.ui.bindToolbar

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        bindToolbar(R.id.toolbar_login)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
