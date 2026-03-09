package com.ail.android_base_kit.ui

import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.ail.android_base_kit.R

fun AppCompatActivity.bindToolbar(@IdRes toolbarId: Int, enableBack: Boolean = true): Toolbar {
    val toolbar = findViewById<Toolbar>(toolbarId)
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(enableBack)

    if (enableBack) {
        val white = ContextCompat.getColor(this, R.color.white)
        val upIcon = AppCompatResources
            .getDrawable(this, androidx.appcompat.R.drawable.abc_ic_ab_back_material)
            ?.mutate()
        upIcon?.setTint(white)
        supportActionBar?.setHomeAsUpIndicator(upIcon)
    }

    return toolbar
}
