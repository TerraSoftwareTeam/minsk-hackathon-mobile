package com.dev.lbsuhack.core.network.error

import com.dev.lbsuhack.core.domain.DomainError
import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Test

class NetworkErrorMappingTest {

    @Test
    fun mapsUnauthorized() {
        assertEquals(DomainError.Unauthorized, NetworkError.Unauthorized.toDomain())
    }

    @Test
    fun mapsForbidden() {
        assertEquals(DomainError.Forbidden, NetworkError.Forbidden.toDomain())
    }

    @Test
    fun mapsNotFound() {
        assertEquals(DomainError.NotFound, NetworkError.NotFound.toDomain())
    }

    @Test
    fun mapsTimeout() {
        assertEquals(DomainError.Timeout, NetworkError.Timeout.toDomain())
    }

    @Test
    fun mapsNoInternetToNetwork() {
        assertEquals(DomainError.Network, NetworkError.NoInternet.toDomain())
    }

    @Test
    fun mapsUnknownToUnknown() {
        assertEquals(DomainError.Unknown, NetworkError.Unknown.toDomain())
    }

    @Test
    fun mapsClientErrorWithMessageToBusinessRule() {
        val domain = NetworkError.ClientError(code = 423, message = "locked").toDomain()

        assertEquals(DomainError.BusinessRule("423", "locked"), domain)
    }

    @Test
    fun mapsClientError400WithoutMessageToValidation() {
        assertEquals(DomainError.Validation, NetworkError.ClientError(400, null).toDomain())
    }

    @Test
    fun mapsClientErrorWithoutMessageToUnknown() {
        assertEquals(DomainError.Unknown, NetworkError.ClientError(409, null).toDomain())
    }

    @Test
    fun mapsServerErrorToNetwork() {
        assertEquals(DomainError.Network, NetworkError.ServerError(503).toDomain())
    }

    @Test
    fun mapsExceptionWithCause() {
        val cause = IOException("offline")

        val domain = NetworkError.Exception(cause).toDomain()

        assertEquals(DomainError.Exception(cause), domain)
    }
}
