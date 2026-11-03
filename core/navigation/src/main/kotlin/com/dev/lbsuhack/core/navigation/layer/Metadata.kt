package com.dev.lbsuhack.core.navigation.layer

object LayerKeys {
    const val LAYER = "layer"
    const val MAIN = "main"
    const val SURVEY = "survey"

    const val TAB_ID = "tabId"
}

object MainLayerMeta {
    fun entry(): Map<String, Any> = mapOf(LayerKeys.LAYER to LayerKeys.MAIN)

    fun tab(id: String): Map<String, Any> = mapOf(
        LayerKeys.LAYER to LayerKeys.MAIN,
        LayerKeys.TAB_ID to id,
    )
}

object SurveyLayerMeta {
    fun entry(): Map<String, Any> = mapOf(LayerKeys.LAYER to LayerKeys.SURVEY)
}
