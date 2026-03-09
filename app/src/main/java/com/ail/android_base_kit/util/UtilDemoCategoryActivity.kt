package com.ail.android_base_kit.util

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.ail.android_base_kit.R
import com.ail.android_base_kit.ui.bindToolbar

class UtilDemoCategoryActivity : AppCompatActivity() {

    private lateinit var cardModeAll: CardView
    private lateinit var cardModeBasic: CardView
    private lateinit var cardModeText: CardView
    private lateinit var cardModeStoragePerf: CardView
    private lateinit var cardRecentMode: CardView
    private lateinit var tvRecentMode: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_util_demo_category)

        bindToolbar(R.id.toolbar_util_category)

        cardModeAll = findViewById(R.id.card_util_mode_all)
        cardModeBasic = findViewById(R.id.card_util_mode_basic)
        cardModeText = findViewById(R.id.card_util_mode_text)
        cardModeStoragePerf = findViewById(R.id.card_util_mode_storage_perf)
        cardRecentMode = findViewById(R.id.card_util_recent)
        tvRecentMode = findViewById(R.id.tv_util_mode_recent)

        bindModeEntry(
            card = cardModeAll,
            mode = UtilDemoActivity.MODE_ALL,
        )
        bindModeEntry(
            card = cardModeBasic,
            mode = UtilDemoActivity.MODE_BASIC,
        )
        bindModeEntry(
            card = cardModeText,
            mode = UtilDemoActivity.MODE_TEXT,
        )
        bindModeEntry(
            card = cardModeStoragePerf,
            mode = UtilDemoActivity.MODE_STORAGE_PERF,
        )

        bindRecentModeEntry()
    }

    override fun onResume() {
        super.onResume()
        bindRecentModeEntry()
    }

    private fun openMode(mode: String) {
        startActivity(
            Intent(this, UtilDemoActivity::class.java)
                .putExtra(UtilDemoActivity.EXTRA_MODE, mode),
        )
    }

    private fun bindRecentModeEntry() {
        val mode = readLastMode()
        if (mode == null) {
            cardRecentMode.visibility = View.GONE
            applyModeHighlight(null)
            return
        }

        val modeName = getString(modeNameRes(mode))
        cardRecentMode.visibility = View.VISIBLE
        tvRecentMode.text = getString(R.string.util_category_recent_desc_fmt, modeName)
        cardRecentMode.setOnClickListener { openMode(mode) }
        applyModeHighlight(mode)
    }

    private fun readLastMode(): String? {
        val mode = getSharedPreferences(UtilDemoActivity.PREF_UTIL_DEMO, MODE_PRIVATE)
            .getString(UtilDemoActivity.KEY_LAST_MODE, null)
        return when (mode) {
            UtilDemoActivity.MODE_ALL,
            UtilDemoActivity.MODE_BASIC,
            UtilDemoActivity.MODE_TEXT,
            UtilDemoActivity.MODE_STORAGE_PERF -> mode
            else -> null
        }
    }

    private fun modeNameRes(mode: String): Int = when (mode) {
        UtilDemoActivity.MODE_BASIC -> R.string.util_category_btn_basic
        UtilDemoActivity.MODE_TEXT -> R.string.util_category_btn_text
        UtilDemoActivity.MODE_STORAGE_PERF -> R.string.util_category_btn_storage_perf
        else -> R.string.util_category_btn_all
    }

    private fun bindModeEntry(card: CardView, mode: String) {
        card.setOnClickListener { openMode(mode) }
    }

    private fun applyModeHighlight(lastMode: String?) {
        val selectedColor = ContextCompat.getColor(this, R.color.primary_soft)
        val normalColor = ContextCompat.getColor(this, R.color.card_bg)

        val mapping = listOf(
            UtilDemoActivity.MODE_ALL to cardModeAll,
            UtilDemoActivity.MODE_BASIC to cardModeBasic,
            UtilDemoActivity.MODE_TEXT to cardModeText,
            UtilDemoActivity.MODE_STORAGE_PERF to cardModeStoragePerf,
        )
        mapping.forEach { (mode, card) ->
            card.setCardBackgroundColor(if (mode == lastMode) selectedColor else normalColor)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
