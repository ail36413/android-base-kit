package com.ail.lib_image

import androidx.annotation.MainThread
import com.bumptech.glide.load.engine.DiskCacheStrategy

/**
 * Global defaults for image loading. Per-call arguments always win.
 */
data class ImageGlobalDefaults(
    val placeholder: Int? = null,
    val error: Int? = null,
    val cacheStrategy: DiskCacheStrategy? = null,
    val disableTransition: Boolean = false,
    val cancelOnDetach: Boolean = false,
    val resumeOnReattach: Boolean = true
)

object ImageLoaderDefaults {
    @Volatile
    var config: ImageGlobalDefaults = ImageGlobalDefaults()

    @MainThread
    fun update(transform: (ImageGlobalDefaults) -> ImageGlobalDefaults) {
        config = transform(config)
    }

    internal fun merge(local: ImageLoadOptions): ImageLoadOptions {
        val defaults = config
        val effectiveCancelOnDetach = local.cancelOnDetach || defaults.cancelOnDetach
        val effectiveResumeOnReattach = if (local.cancelOnDetach) {
            local.resumeOnReattach
        } else {
            if (defaults.cancelOnDetach) defaults.resumeOnReattach else local.resumeOnReattach
        }

        return local.copy(
            placeholder = local.placeholder ?: defaults.placeholder,
            error = local.error ?: defaults.error,
            cacheStrategy = if (local.cacheStrategy == DiskCacheStrategy.AUTOMATIC) {
                defaults.cacheStrategy ?: local.cacheStrategy
            } else {
                local.cacheStrategy
            },
            disableTransition = local.disableTransition || defaults.disableTransition,
            cancelOnDetach = effectiveCancelOnDetach,
            resumeOnReattach = effectiveResumeOnReattach
        )
    }
}

