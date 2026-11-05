package com.dev.lbsuhack.core.domain.model

enum class UserRole {
    PUPIL,
    TEACHER,
}

sealed interface User {
    val id: String
    val name: String
    val role: UserRole

    data class Pupil(
        override val id: String,
        override val name: String,
        val pupilClass: String,
    ) : User {
        override val role: UserRole get() = UserRole.PUPIL
        val `class`: String get() = pupilClass
    }

    data class Teacher(
        override val id: String,
        override val name: String,
    ) : User {
        override val role: UserRole get() = UserRole.TEACHER
    }
}

typealias Pupil = User.Pupil
typealias Teacher = User.Teacher
