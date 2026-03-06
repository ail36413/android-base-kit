package com.ail.android_base_kit

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ail.android_base_kit.image.ImageDemoActivity
import com.ail.android_base_kit.network.http.http.NetActivity
import com.ail.android_base_kit.util.UtilDemoActivity
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        findViewById<CardView>(R.id.cardImage).setOnClickListener {
            startActivity(Intent(this, ImageDemoActivity::class.java))
        }
        findViewById<CardView>(R.id.cardHttp).setOnClickListener {
            startActivity(Intent(this, NetActivity::class.java))
        }
        findViewById<CardView>(R.id.cardWebSocket).setOnClickListener {
            startActivity(Intent(this, com.ail.android_base_kit.network.websocket.WebSocketDemoActivity::class.java))
        }
        findViewById<CardView>(R.id.cardUtil).setOnClickListener {
            startActivity(Intent(this, UtilDemoActivity::class.java))
        }
    }
}