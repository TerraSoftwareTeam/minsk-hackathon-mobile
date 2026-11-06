package com.dev.lbsuhack.core.domain.model

enum class UserRole {
    PUPIL,
    TEACHER,
    ORGANIZATOR,
}

sealed interface User {
    val id: String
    val name: String
    val role: UserRole
    val surname: String? get() = null
    val schoolsById: String? get() = null
    val schoolName: String? get() = null

    data class Pupil(
        override val id: String,
        override val name: String,
        override val surname: String? = null,
        override val schoolsById: String? = null,
        override val schoolName: String? = null,
        val pupilClass: String,
    ) : User {
        override val role: UserRole get() = UserRole.PUPIL
        val `class`: String get() = pupilClass
    }

    data class Teacher(
        override val id: String,
        override val name: String,
        override val surname: String? = null,
        override val schoolsById: String? = null,
        override val schoolName: String? = null,
    ) : User {
        override val role: UserRole get() = UserRole.TEACHER
    }

    data class Organizator(
        override val id: String,
        override val name: String,
        override val surname: String? = null,
    ) : User {
        override val role: UserRole get() = UserRole.ORGANIZATOR
    }
}

typealias Pupil = User.Pupil
typealias Teacher = User.Teacher
typealias Organizator = User.Organizator
