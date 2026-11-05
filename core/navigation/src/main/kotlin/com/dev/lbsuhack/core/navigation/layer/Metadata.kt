package com.dev.lbsuhack.core.navigation.layer

object LayerKeys {
    const val LAYER = "layer"
    const val AUTH = "auth"
    const val GLOBAL = "global"
}

object AuthLayerMeta {
    fun entry(): Map<String, Any> = mapOf(LayerKeys.LAYER to LayerKeys.AUTH)
}

object GlobalLayerMeta {
    fun entry(): Map<String, Any> = mapOf(LayerKeys.LAYER to LayerKeys.GLOBAL)
}
