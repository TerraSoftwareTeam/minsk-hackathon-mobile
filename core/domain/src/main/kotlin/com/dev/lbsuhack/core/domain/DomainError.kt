package com.dev.lbsuhack.core.domain

sealed interface DomainError {
    data object Unknown : DomainError
    data object Network : DomainError
    data object Timeout : DomainError
    data object Unauthorized : DomainError
    data object Forbidden : DomainError
    data object NotFound : DomainError
    data object Validation : DomainError
    data class BusinessRule(val code: String? = null, val message: String? = null) : DomainError
    
    // Exception-based error for unhandled crashes (useful for mapping standard Result)
    data class Exception(val throwable: kotlin.Throwable) : DomainError
}
