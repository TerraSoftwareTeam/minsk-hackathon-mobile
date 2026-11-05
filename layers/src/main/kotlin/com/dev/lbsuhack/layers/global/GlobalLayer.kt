package com.dev.lbsuhack.layers.global

import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.SceneStrategy
import com.dev.lbsuhack.core.navigation.layer.LayerKeys
import com.dev.lbsuhack.core.navigation.layer.NavigationLayer
import com.dev.lbsuhack.layers.layer.LayerSceneStrategy

class GlobalLayer : NavigationLayer {
    override val sceneStrategy: SceneStrategy<NavKey> =
        LayerSceneStrategy(layer = LayerKeys.GLOBAL)
}
