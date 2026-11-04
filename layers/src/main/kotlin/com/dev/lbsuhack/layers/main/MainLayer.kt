package com.dev.lbsuhack.layers.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import com.dev.lbsuhack.core.navigation.layer.LayerKeys
import com.dev.lbsuhack.core.navigation.api.LocalNavigator
import com.dev.lbsuhack.core.navigation.api.MainTabInstaller
import com.dev.lbsuhack.core.navigation.layer.NavigationLayer

class MainLayer(
    private val tabs: List<MainTabInstaller>,
) : NavigationLayer {

    override val sceneStrategy: SceneStrategy<NavKey> = object : SceneStrategy<NavKey> {
        override fun SceneStrategyScope<NavKey>.calculateScene(
            entries: List<NavEntry<NavKey>>,
        ): Scene<NavKey>? {
            val last = entries.lastOrNull() ?: return null
            if (last.metadata[LayerKeys.LAYER] != LayerKeys.MAIN) return null

            return MainScene(
                entry = last,
                previousEntries = entries.dropLast(1),
                allEntries = entries,
                tabs = tabs.sortedBy { it.order },
            )
        }
    }
}


class MainScene(
    val entry: NavEntry<NavKey>,
    override val previousEntries: List<NavEntry<NavKey>>,
    private val allEntries: List<NavEntry<NavKey>>,
    private val tabs: List<MainTabInstaller>,
) : Scene<NavKey> {

    override val entries: List<NavEntry<NavKey>> = listOf(entry)

    override val key: Any = "main-scene"

    override val content: @Composable () -> Unit = {
        val navigator = LocalNavigator.current

        val activeTabId = allEntries.asReversed()
            .firstNotNullOfOrNull { it.metadata[LayerKeys.TAB_ID] as? String }
            ?: tabs.first().id

        Scaffold(
            bottomBar = {
                NavigationBar {
                    tabs.forEach { tab ->
                        val title = stringResource(tab.titleRes)
                        NavigationBarItem(
                            selected = tab.id == activeTabId,
                            onClick = {
                                if (tab.id != activeTabId) navigator.switchTab(tab.rootRoute)
                            },
                            icon = { Icon(tab.icon, contentDescription = title) },
                            label = { Text(title) },
                        )
                    }
                }
            },
        ) { paddingValues ->
            Box(Modifier.fillMaxSize().padding(paddingValues)) {
                entry.Content()
            }
        }
    }
}
