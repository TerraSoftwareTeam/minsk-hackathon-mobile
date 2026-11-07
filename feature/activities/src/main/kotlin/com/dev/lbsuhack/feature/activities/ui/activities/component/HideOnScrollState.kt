package com.dev.lbsuhack.feature.activities.ui.activities.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Stable
internal class HideOnScrollState(private val thresholdPx: Float) {

    var isVisible by mutableStateOf(true)
        private set

    private var accumulated = 0f

    val connection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            val delta = available.y
            if (delta == 0f) return Offset.Zero
            if (delta > 0f != accumulated > 0f) accumulated = 0f
            accumulated += delta
            when {
                accumulated <= -thresholdPx && isVisible -> isVisible = false
                accumulated >= thresholdPx && !isVisible -> isVisible = true
            }
            return Offset.Zero
        }

        override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
            if (available.y > 0f) isVisible = true
            return Offset.Zero
        }
    }
}

@Composable
internal fun rememberHideOnScrollState(): HideOnScrollState {
    val thresholdPx = with(LocalDensity.current) { HIDE_THRESHOLD.toPx() }
    return remember(thresholdPx) { HideOnScrollState(thresholdPx) }
}

private val HIDE_THRESHOLD = 24.dp
