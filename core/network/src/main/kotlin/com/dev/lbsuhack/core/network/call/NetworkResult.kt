package com.dev.lbsuhack.core.network.call

import com.dev.lbsuhack.core.network.error.NetworkError

sealed interface NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>
    data class Error(val error: NetworkError) : NetworkResult<Nothing>
}

inline fun <T, R> NetworkResult<T>.map(transform: (T) -> R): NetworkResult<R> {
    return when (this) {
        is NetworkResult.Success -> NetworkResult.Success(transform(data))
        is NetworkResult.Error -> this
    }
}
