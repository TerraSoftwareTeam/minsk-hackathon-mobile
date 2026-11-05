package com.dev.lbsuhack.layers.layer

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import com.dev.lbsuhack.core.navigation.layer.LayerKeys

class LayerScene(
    private val entry: NavEntry<NavKey>,
    override val previousEntries: List<NavEntry<NavKey>>,
) : Scene<NavKey> {

    override val key: Any = entry.contentKey

    override val entries: List<NavEntry<NavKey>> = listOf(entry)

    override val content: @Composable () -> Unit = {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            entry.Content()
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LayerScene) return false
        return key == other.key &&
            entry == other.entry &&
            previousEntries == other.previousEntries
    }

    override fun hashCode(): Int = 31 * key.hashCode() + entry.hashCode()
}

class LayerSceneStrategy(
    private val layer: String,
) : SceneStrategy<NavKey> {

    override fun SceneStrategyScope<NavKey>.calculateScene(
        entries: List<NavEntry<NavKey>>,
    ): Scene<NavKey>? {
        val last = entries.lastOrNull() ?: return null
        if (last.metadata[LayerKeys.LAYER] != layer) return null

        return LayerScene(
            entry = last,
            previousEntries = entries.dropLast(1),
        )
    }
}
