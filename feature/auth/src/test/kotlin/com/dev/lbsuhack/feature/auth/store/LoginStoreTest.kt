package com.dev.lbsuhack.feature.auth.store

import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.dev.lbsuhack.core.domain.DomainError
import com.dev.lbsuhack.core.domain.DomainResult
import com.dev.lbsuhack.core.domain.model.UserRole
import com.dev.lbsuhack.core.domain.session.SaveSessionUseCase
import com.dev.lbsuhack.core.domain.session.SessionUser
import com.dev.lbsuhack.core.localization.text.UiText
import com.dev.lbsuhack.feature.auth.domain.AuthRepository
import com.dev.lbsuhack.feature.auth.store.login.LoginStore
import com.dev.lbsuhack.feature.auth.store.login.LoginStoreFactory
import com.dev.lbsuhack.feature.auth.store.login.PupilLoginMethod
import com.dev.lbsuhack.feature.auth.util.FakeSessionRepository
import com.dev.lbsuhack.feature.auth.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginStoreTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sessionRepository = FakeSessionRepository()

    private val fakeAuthRepository = object : AuthRepository {
        var loginId: String? = null
        var registeredSchoolsById: String? = null
        var registeredClassName: String? = null
        var registeredRole: UserRole? = null

        override suspend fun login(
            username: String,
            password: String,
            role: UserRole,
        ): DomainResult<SessionUser> {
            loginId = username
            if (role == UserRole.ORGANIZATOR) {
                return DomainResult.Success(SessionUser(id = username, name = "Админ", role = role))
            }
            return if (username == "002" && password == "secret") {
                DomainResult.Success(
                    SessionUser(
                        id = "3575208",
                        name = "Владислав Смирнов",
                        schoolsById = "3575208",
                        role = role,
                        pupilClass = "11 \"М\"",
                    )
                )
            } else {
                DomainResult.Failure(DomainError.Unauthorized)
            }
        }

        override suspend fun registerOrganizer(schoolsById: String): DomainResult<SessionUser> {
            loginId = schoolsById
            return DomainResult.Success(SessionUser(id = schoolsById, name = "Админ", role = UserRole.ORGANIZATOR))
        }

        override suspend fun registerTeacher(
            schoolsById: String,
            name: String,
            surname: String,
            className: String,
        ): DomainResult<SessionUser> {
            return register(schoolsById, name, surname, className, UserRole.TEACHER)
        }

        override suspend fun registerPupil(
            schoolsById: String,
            name: String,
            surname: String,
            className: String,
        ): DomainResult<SessionUser> = register(schoolsById, name, surname, className, UserRole.PUPIL)

        private fun register(
            schoolsById: String,
            name: String,
            surname: String,
            className: String,
            role: UserRole,
        ): DomainResult<SessionUser> {
            registeredSchoolsById = schoolsById
            registeredClassName = className
            registeredRole = role
            return DomainResult.Success(
                SessionUser(
                    id = schoolsById,
                    name = name,
                    surname = surname,
                    schoolsById = schoolsById,
                    role = role,
                    pupilClass = className,
                ),
            )
        }
    }

    private fun createStore(): LoginStore = LoginStoreFactory(
        storeFactory = DefaultStoreFactory(),
        authRepository = fakeAuthRepository,
        saveSession = SaveSessionUseCase(sessionRepository),
    ).create()

    @Test
    fun `initial state is empty and default role is pupil`() {
        val store = createStore()

        assertEquals("", store.state.username)
        assertEquals("", store.state.password)
        assertEquals(UserRole.PUPIL, store.state.role)
        assertFalse(store.state.isLoading)
        assertNull(store.state.error)
    }

    @Test
    fun `pupil submit with blank data shows validation error and does not save session`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = createStore()

            store.accept(LoginStore.Intent.UsernameChanged("   "))
            store.accept(LoginStore.Intent.PasswordChanged(""))
            store.accept(LoginStore.Intent.Submit)
            runCurrent()

            assertFalse(store.state.isLoading)
            assertTrue(store.state.error is UiText.Resource)
            assertNull(sessionRepository.savedUser)
        }

    @Test
    fun `pupil submit with valid data saves session with class`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = createStore()

            store.accept(LoginStore.Intent.UsernameChanged("002"))
            store.accept(LoginStore.Intent.PasswordChanged("secret"))
            store.accept(LoginStore.Intent.Submit)
            runCurrent()

            assertFalse(store.state.isLoading)
            assertNull(store.state.error)
            assertEquals("3575208", sessionRepository.savedUser?.schoolsById)
            assertEquals(UserRole.PUPIL, sessionRepository.savedUser?.role)
            assertEquals("11 \"М\"", sessionRepository.savedUser?.pupilClass)
        }

    @Test
    fun `teacher submit registers with a generated numeric id and normalized class`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = createStore()

            store.accept(LoginStore.Intent.RoleChanged(UserRole.TEACHER))
            store.accept(LoginStore.Intent.SurnameChanged("Иванов"))
            store.accept(LoginStore.Intent.NameChanged("Иван"))
            store.accept(LoginStore.Intent.ClassNameChanged("10 а"))
            store.accept(LoginStore.Intent.Submit)
            runCurrent()

            assertNull(store.state.error)
            assertEquals("10А", fakeAuthRepository.registeredClassName)
            assertNotNull(fakeAuthRepository.registeredSchoolsById)
            assertTrue(fakeAuthRepository.registeredSchoolsById!!.all { it.isDigit() })
            assertEquals(UserRole.TEACHER, sessionRepository.savedUser?.role)
        }

    @Test
    fun `teacher submit with invalid class shows an error`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = createStore()

            store.accept(LoginStore.Intent.RoleChanged(UserRole.TEACHER))
            store.accept(LoginStore.Intent.SurnameChanged("Иванов"))
            store.accept(LoginStore.Intent.NameChanged("Иван"))
            store.accept(LoginStore.Intent.ClassNameChanged("десятый"))
            store.accept(LoginStore.Intent.Submit)
            runCurrent()

            assertNotNull(store.state.error)
            assertNull(fakeAuthRepository.registeredClassName)
            assertNull(sessionRepository.savedUser)
        }

    @Test
    fun `teacher submit without a class letter shows an error`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = createStore()

            store.accept(LoginStore.Intent.RoleChanged(UserRole.TEACHER))
            store.accept(LoginStore.Intent.SurnameChanged("Иванов"))
            store.accept(LoginStore.Intent.NameChanged("Иван"))
            store.accept(LoginStore.Intent.ClassNameChanged("10"))
            store.accept(LoginStore.Intent.Submit)
            runCurrent()

            assertNotNull(store.state.error)
            assertNull(fakeAuthRepository.registeredClassName)
        }

    @Test
    fun `manual pupil submit registers a pupil with a stable id`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val first = createStore()
            first.accept(LoginStore.Intent.PupilMethodChanged(PupilLoginMethod.MANUAL))
            first.accept(LoginStore.Intent.SurnameChanged("Петров"))
            first.accept(LoginStore.Intent.NameChanged("Пётр"))
            first.accept(LoginStore.Intent.ClassNameChanged("9Б"))
            first.accept(LoginStore.Intent.Submit)
            runCurrent()

            assertNull(first.state.error)
            assertEquals(UserRole.PUPIL, fakeAuthRepository.registeredRole)
            assertEquals("9Б", fakeAuthRepository.registeredClassName)
            assertEquals(UserRole.PUPIL, sessionRepository.savedUser?.role)
            val firstId = fakeAuthRepository.registeredSchoolsById

            val second = createStore()
            second.accept(LoginStore.Intent.PupilMethodChanged(PupilLoginMethod.MANUAL))
            second.accept(LoginStore.Intent.SurnameChanged(" петров "))
            second.accept(LoginStore.Intent.NameChanged("ПЁТР"))
            second.accept(LoginStore.Intent.ClassNameChanged("9Б"))
            second.accept(LoginStore.Intent.Submit)
            runCurrent()

            assertEquals(firstId, fakeAuthRepository.registeredSchoolsById)
            assertEquals("", second.state.name)
            assertEquals("", second.state.surname)
            assertEquals("", second.state.className)
            assertTrue(firstId!!.length == 12 && firstId.all { it.isDigit() })
        }

    @Test
    fun `manual pupil submit without name shows an error and ignores schools by fields`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = createStore()
            store.accept(LoginStore.Intent.UsernameChanged("002"))
            store.accept(LoginStore.Intent.PasswordChanged("secret"))
            store.accept(LoginStore.Intent.PupilMethodChanged(PupilLoginMethod.MANUAL))
            store.accept(LoginStore.Intent.Submit)
            runCurrent()

            assertNotNull(store.state.error)
            assertNull(fakeAuthRepository.loginId)
            assertNull(sessionRepository.savedUser)
        }

    @Test
    fun `organizer submit registers with a generated numeric schools id`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val store = createStore()

            store.accept(LoginStore.Intent.RoleChanged(UserRole.ORGANIZATOR))
            store.accept(LoginStore.Intent.Submit)
            runCurrent()

            assertNull(store.state.error)
            assertNotNull(fakeAuthRepository.loginId)
            assertTrue(fakeAuthRepository.loginId!!.all { it.isDigit() })
            assertEquals(UserRole.ORGANIZATOR, sessionRepository.savedUser?.role)
        }
}
