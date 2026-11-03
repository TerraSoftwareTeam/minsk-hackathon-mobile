package com.dev.lbsuhack.core.navigation.layer

import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.SceneStrategy

interface NavigationLayer {
    val sceneStrategy: SceneStrategy<NavKey>
}
