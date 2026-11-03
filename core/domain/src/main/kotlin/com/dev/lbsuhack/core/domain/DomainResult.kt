package com.dev.lbsuhack.core.domain

sealed interface DomainResult<out T> {
    data class Success<T>(val value: T) : DomainResult<T>
    data class Failure(val error: DomainError) : DomainResult<Nothing>
}

inline fun <T, R> DomainResult<T>.map(
    transform: (T) -> R
): DomainResult<R> = when (this) {
    is DomainResult.Success -> DomainResult.Success(transform(value))
    is DomainResult.Failure -> this
}

inline fun <T, R> DomainResult<T>.flatMap(
    transform: (T) -> DomainResult<R>
): DomainResult<R> = when (this) {
    is DomainResult.Success -> transform(value)
    is DomainResult.Failure -> this
}

inline fun <T> DomainResult<T>.mapError(
    transform: (DomainError) -> DomainError
): DomainResult<T> = when (this) {
    is DomainResult.Success -> this
    is DomainResult.Failure -> DomainResult.Failure(transform(error))
}

inline fun <T> DomainResult<T>.onSuccess(
    action: (T) -> Unit
): DomainResult<T> = also {
    if (this is DomainResult.Success) action(value)
}

inline fun <T> DomainResult<T>.onFailure(
    action: (DomainError) -> Unit
): DomainResult<T> = also {
    if (this is DomainResult.Failure) action(error)
}

inline fun <T, R> DomainResult<T>.fold(
    onSuccess: (T) -> R,
    onFailure: (DomainError) -> R
): R = when (this) {
    is DomainResult.Success -> onSuccess(value)
    is DomainResult.Failure -> onFailure(error)
}

fun <T> DomainResult<T>.getOrNull(): T? = when (this) {
    is DomainResult.Success -> value
    is DomainResult.Failure -> null
}

fun <T> DomainResult<T>.getOrElse(defaultValue: () -> T): T = when (this) {
    is DomainResult.Success -> value
    is DomainResult.Failure -> defaultValue()
}
