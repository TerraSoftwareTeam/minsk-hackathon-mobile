package com.dev.lbsuhack.feature.activities.ui.component

import androidx.compose.runtime.staticCompositionLocalOf

val LocalActivityPhotoLoader = staticCompositionLocalOf<suspend (String) -> ByteArray> {
    { error("Image loader is unavailable") }
}
