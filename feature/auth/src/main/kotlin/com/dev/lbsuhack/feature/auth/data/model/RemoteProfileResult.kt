package com.dev.lbsuhack.feature.auth.data.model

sealed interface RemoteProfileResult {
    data class Success(val html: String, val profileUrl: String) : RemoteProfileResult
    data object InvalidCredentials : RemoteProfileResult
    data class Error(val cause: Throwable) : RemoteProfileResult
}
