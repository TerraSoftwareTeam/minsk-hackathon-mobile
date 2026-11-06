package com.dev.lbsuhack.feature.auth.data

import com.dev.lbsuhack.core.domain.model.User
import com.dev.lbsuhack.core.domain.model.UserRole
import com.dev.lbsuhack.core.domain.session.SessionUser
import com.dev.lbsuhack.core.network.generated.model.UserResponseDto

internal fun UserResponseDto.toSessionUser(schoolName: String? = null): SessionUser = SessionUser(
    id = id,
    name = name,
    surname = surname,
    schoolsById = schoolsById,
    schoolName = schoolName?.takeIf { it.isNotBlank() },
    role = role.toDomainRole(),
    pupilClass = propertyClass,
)

internal fun UserResponseDto.toDomain(schoolName: String? = null): User = when (role.toDomainRole()) {
    UserRole.PUPIL -> User.Pupil(
        id = id,
        name = name,
        surname = surname,
        schoolsById = schoolsById,
        schoolName = schoolName?.takeIf { it.isNotBlank() },
        pupilClass = propertyClass.orEmpty(),
    )
    UserRole.TEACHER -> User.Teacher(
        id = id,
        name = name,
        surname = surname,
        schoolsById = schoolsById,
        schoolName = schoolName?.takeIf { it.isNotBlank() },
    )
    UserRole.ORGANIZATOR -> User.Organizator(
        id = id,
        name = name,
        surname = surname,
    )
}

private fun UserResponseDto.Role.toDomainRole(): UserRole = when (this) {
    UserResponseDto.Role.PUPIL -> UserRole.PUPIL
    UserResponseDto.Role.TEACHER -> UserRole.TEACHER
    UserResponseDto.Role.ADMIN -> UserRole.ORGANIZATOR
}
