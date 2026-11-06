package com.dev.lbsuhack.feature.profile.data

import com.dev.lbsuhack.core.domain.DomainResult
import com.dev.lbsuhack.core.domain.model.User
import com.dev.lbsuhack.core.network.call.NetworkResult
import com.dev.lbsuhack.core.network.call.safeCall
import com.dev.lbsuhack.core.network.error.toDomain
import com.dev.lbsuhack.core.network.generated.model.UpdateUserDto
import com.dev.lbsuhack.core.network.generated.model.UserResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserProfileApi(
    private val httpClient: HttpClient,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

    suspend fun updateProfile(
        userId: String,
        name: String,
        surname: String,
        fathersName: String,
    ): DomainResult<User> = withContext(ioDispatcher) {
        val dto = UpdateUserDto(
            name = name.trim(),
            surname = surname.trim(),
            fathersName = fathersName.trim(),
        )

        when (
            val result = safeCall {
                httpClient.patch("api/v1/users/$userId") {
                    contentType(ContentType.Application.Json)
                    setBody(dto)
                }.body<UserResponseDto>()
            }
        ) {
            is NetworkResult.Success -> DomainResult.Success(result.data.toDomain())
            is NetworkResult.Error -> DomainResult.Failure(result.error.toDomain())
        }
    }

    private fun UserResponseDto.toDomain(): User = when (role) {
        UserResponseDto.Role.PUPIL -> User.Pupil(
            id = id,
            name = name,
            surname = surname,
            fathersName = fathersName,
            schoolsById = schoolsById,
            pupilClass = propertyClass.orEmpty(),
        )

        UserResponseDto.Role.TEACHER -> User.Teacher(
            id = id,
            name = name,
            surname = surname,
            fathersName = fathersName,
            schoolsById = schoolsById,
        )

        UserResponseDto.Role.ADMIN -> User.Organizator(
            id = id,
            name = name,
            surname = surname,
            fathersName = fathersName,
        )
    }
}
