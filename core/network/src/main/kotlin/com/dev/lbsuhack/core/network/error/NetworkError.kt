package com.dev.lbsuhack.core.network.error

import com.dev.lbsuhack.core.domain.DomainError

sealed interface NetworkError {
    data object Unauthorized : NetworkError
    data object Forbidden : NetworkError
    data object NotFound : NetworkError
    data object Timeout : NetworkError
    data object NoInternet : NetworkError
    data object Unknown : NetworkError
    data class ClientError(val code: Int, val message: String? = null) : NetworkError
    data class ServerError(val code: Int) : NetworkError
    data class Exception(val cause: Throwable) : NetworkError
}

fun NetworkError.toDomain(): DomainError = when (this) {
    NetworkError.Unauthorized -> DomainError.Unauthorized
    NetworkError.Forbidden -> DomainError.Forbidden
    NetworkError.NotFound -> DomainError.NotFound
    NetworkError.Timeout -> DomainError.Timeout
    NetworkError.NoInternet -> DomainError.Network
    is NetworkError.ClientError -> if (message != null) {
        DomainError.BusinessRule(code.toString(), message)
    } else if (code == 400) {
        DomainError.Validation
    } else {
        DomainError.Unknown
    }
    is NetworkError.ServerError -> DomainError.Network
    is NetworkError.Exception -> DomainError.Exception(cause)
    NetworkError.Unknown -> DomainError.Unknown
}
