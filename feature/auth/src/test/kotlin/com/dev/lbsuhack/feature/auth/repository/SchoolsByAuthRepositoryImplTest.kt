package com.dev.lbsuhack.feature.auth.data.repository

import com.dev.lbsuhack.core.domain.DomainError
import com.dev.lbsuhack.core.domain.DomainResult
import com.dev.lbsuhack.core.domain.model.Pupil
import com.dev.lbsuhack.core.domain.model.Teacher
import com.dev.lbsuhack.core.domain.model.UserRole
import com.dev.lbsuhack.feature.auth.data.model.RemoteProfileResult
import com.dev.lbsuhack.feature.auth.data.parser.SchoolsByProfileParser
import com.dev.lbsuhack.feature.auth.data.remote.SchoolsByRemoteDataSource
import com.dev.lbsuhack.feature.auth.data.remote.SchoolsByRemoteDataSourceImpl
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class SchoolsByAuthRepositoryImplTest {

    private val parser = SchoolsByProfileParser()

    @Test
    fun loginAndGetUser_pupil_success_returns_domain_pupil() = runTest {
        val fakeDataSource = object : SchoolsByRemoteDataSource {
            override suspend fun loginAndFetchProfileHtml(
                username: String,
                password: String,
            ): RemoteProfileResult {
                return RemoteProfileResult.Success(
                    html = "<h1>Смирнов Владислав, <a href=\"/class/1\">11 &quot;М&quot;</a> класс, ID 3575208</h1>",
                    profileUrl = "https://gym41.schools.by/pupil/3575208"
                )
            }
        }

        val repository = SchoolsByAuthRepositoryImpl(fakeDataSource, parser)
        val result = repository.loginAndGetUser("002", "1234554321", role = UserRole.PUPIL)

        assertTrue(result is DomainResult.Success)
        val user = (result as DomainResult.Success).value
        assertTrue(user is Pupil)
        val pupil = user as Pupil
        assertEquals("Смирнов Владислав", pupil.name)
        assertEquals("11 \"М\"", pupil.pupilClass)
        assertEquals("3575208", pupil.id)
    }

    @Test
    fun loginAndGetUser_teacher_success_returns_domain_teacher() = runTest {
        val fakeDataSource = object : SchoolsByRemoteDataSource {
            override suspend fun loginAndFetchProfileHtml(
                username: String,
                password: String,
            ): RemoteProfileResult {
                return RemoteProfileResult.Success(
                    html = "<h1>Божедомова Ирина Валерьевна, Учитель, ID 1234567</h1>",
                    profileUrl = "https://gym41.schools.by/teacher/1234567"
                )
            }
        }

        val repository = SchoolsByAuthRepositoryImpl(fakeDataSource, parser)
        val result = repository.loginAndGetUser("teacher1", "pass123", role = UserRole.TEACHER)

        assertTrue(result is DomainResult.Success)
        val user = (result as DomainResult.Success).value
        assertTrue(user is Teacher)
        val teacher = user as Teacher
        assertEquals("Божедомова Ирина Валерьевна", teacher.name)
        assertEquals("1234567", teacher.id)
    }

    @Test
    fun loginAndGetUser_blank_username_returns_validation_error() = runTest {
        val fakeDataSource = object : SchoolsByRemoteDataSource {
            override suspend fun loginAndFetchProfileHtml(
                username: String,
                password: String,
            ): RemoteProfileResult = throw UnsupportedOperationException()
        }

        val repository = SchoolsByAuthRepositoryImpl(fakeDataSource, parser)
        val result = repository.loginAndGetUser("   ", "1234554321", role = UserRole.PUPIL)

        assertEquals(DomainResult.Failure(DomainError.Validation), result)
    }

    @Test
    fun loginAndGetUser_invalid_credentials_returns_unauthorized() = runTest {
        val fakeDataSource = object : SchoolsByRemoteDataSource {
            override suspend fun loginAndFetchProfileHtml(
                username: String,
                password: String,
            ): RemoteProfileResult = RemoteProfileResult.InvalidCredentials
        }

        val repository = SchoolsByAuthRepositoryImpl(fakeDataSource, parser)
        val result = repository.loginAndGetUser("002", "wrong", role = UserRole.PUPIL)

        assertEquals(DomainResult.Failure(DomainError.Unauthorized), result)
    }

    @Test
    fun loginAndGetUser_io_exception_returns_network_error() = runTest {
        val fakeDataSource = object : SchoolsByRemoteDataSource {
            override suspend fun loginAndFetchProfileHtml(
                username: String,
                password: String,
            ): RemoteProfileResult = RemoteProfileResult.Error(IOException("Connection failed"))
        }

        val repository = SchoolsByAuthRepositoryImpl(fakeDataSource, parser)
        val result = repository.loginAndGetUser("002", "1234554321", role = UserRole.PUPIL)

        assertEquals(DomainResult.Failure(DomainError.Network), result)
    }

    @Test
    fun live_login_with_provided_credentials() = runTest {
        val dataSource = SchoolsByRemoteDataSourceImpl()
        val repository = SchoolsByAuthRepositoryImpl(dataSource, parser)

        val result = repository.loginAndGetUser("002", "1234554321", role = UserRole.PUPIL)
        assertTrue(result is DomainResult.Success)
        val user = (result as DomainResult.Success).value
        assertTrue(user is Pupil)
        val pupil = user as Pupil
        assertEquals("Смирнов Владислав", pupil.name)
        assertEquals("11 \"М\"", pupil.pupilClass)
        assertEquals("3575208", pupil.id)
    }
}
