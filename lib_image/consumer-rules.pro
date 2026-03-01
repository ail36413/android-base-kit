# Glide 混淆保护
-keep class com.bumptech.glide.** { *; }
-keep interface com.bumptech.glide.** { *; }
-keep class com.bumptech.glide.load.** { *; }
-keep class com.bumptech.glide.request.** { *; }
-keep class com.bumptech.glide.manager.** { *; }
-keep class com.bumptech.glide.module.** { *; }
-keep class com.bumptech.glide.annotation.** { *; }
-keep class com.bumptech.glide.integration.** { *; }
-keep class com.bumptech.glide.signature.** { *; }
-keep class com.bumptech.glide.util.** { *; }

# glide-transformations 混淆保护
-keep class jp.wasabeef.glide.transformations.** { *; }

# 本库公开 API 保护（防止被混淆导致外部无法调用）
-keep class com.ail.lib_image.** { *; }

# 保留 Kotlin 元数据
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes EnclosingMethod