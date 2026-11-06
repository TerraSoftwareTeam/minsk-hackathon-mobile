package com.dev.lbsuhack.feature.auth.data

import com.dev.lbsuhack.core.network.generated.model.UserResponseDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuthUserMapperTest {
    private fun backendUser(role: UserResponseDto.Role) = UserResponseDto(
        id = "backend-7",
        schoolsById = "3575208",
        name = "Иван",
        surname = "Иванов",
        propertyClass = "10-А",
        role = role,
    )

    @Test
    fun schoolNameIsPreservedForPupilAndTeacherSessionsAndCache() {
        listOf(UserResponseDto.Role.PUPIL, UserResponseDto.Role.TEACHER).forEach { role ->
            val user = backendUser(role)
            val session = user.toSessionUser("Гимназия №41")
            val cached = user.toDomain("Гимназия №41")

            assertEquals("Гимназия №41", session.schoolName)
            assertEquals("Гимназия №41", cached.schoolName)
            assertEquals("3575208", session.schoolsById)
            assertEquals("backend-7", session.id)
        }
    }

    @Test
    fun missingSchoolIsNeverReplacedWithAnIdentifier() {
        val user = backendUser(UserResponseDto.Role.PUPIL)
        assertNull(user.toSessionUser().schoolName)
        assertNull(user.toSessionUser("  ").schoolName)
        assertNull(user.toDomain().schoolName)
    }
}
