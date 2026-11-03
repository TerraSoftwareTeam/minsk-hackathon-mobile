package com.dev.lbsuhack.core.network.error

sealed interface NetworkError {
    data object Unauthorized : NetworkError
    data object Forbidden : NetworkError
    data object NotFound : NetworkError
    data object Timeout : NetworkError
    data object NoInternet : NetworkError
    data object Unknown : NetworkError
    data class ServerError(val code: Int) : NetworkError
    data class Exception(val cause: Throwable) : NetworkError
}
