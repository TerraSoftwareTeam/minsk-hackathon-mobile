package com.dev.lbsuhack.feature.activities.navigation

import java.net.URI

object ActivityLink {
    fun create(activityId: String): String {
        require(activityId.isNotBlank() && activityId.length <= 128)
        return URI("kola", "activity", "/$activityId", null).toASCIIString()
    }

    fun parse(value: String?): String? = runCatching {
        val uri = URI(value ?: return null)
        if (uri.scheme != "kola" || uri.rawAuthority != "activity" ||
            uri.rawQuery != null || uri.rawFragment != null
        ) return null
        val id = uri.path?.removePrefix("/") ?: return null
        id.takeIf { it.isNotBlank() && it.length <= 128 && '/' !in it }
    }.getOrNull()
}
