package com.dev.lbsuhack.feature.auth.ui.login.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dev.lbsuhack.core.domain.model.UserRole
import com.dev.lbsuhack.feature.auth.R
import com.dev.lbsuhack.feature.auth.store.login.LoginStore
import com.dev.lbsuhack.feature.auth.store.login.PupilLoginMethod
import com.dev.lbsuhack.feature.auth.ui.component.AuthDemoBanner
import com.dev.lbsuhack.feature.auth.ui.component.AuthError
import com.dev.lbsuhack.feature.auth.ui.component.AuthPrimaryButton

@Composable
internal fun LoginCredentialsContent(
    state: LoginStore.State,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSurnameChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onClassNameChange: (String) -> Unit,
    onPupilMethodChange: (PupilLoginMethod) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        when (state.role) {
            UserRole.ORGANIZATOR -> Text(
                text = stringResource(R.string.auth_login_organizer_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            UserRole.TEACHER -> ManualIdentityFields(
                surname = state.surname,
                name = state.name,
                className = state.className,
                onSurnameChange = onSurnameChange,
                onNameChange = onNameChange,
                onClassNameChange = onClassNameChange,
            )

            UserRole.PUPIL -> {
                PupilLoginMethodSwitch(
                    selected = state.pupilMethod,
                    onSelect = onPupilMethodChange,
                    enabled = !state.isLoading,
                )

                AnimatedContent(
                    targetState = state.pupilMethod,
                    modifier = Modifier.fillMaxWidth(),
                    transitionSpec = {
                        val direction = if (targetState == PupilLoginMethod.MANUAL) 1 else -1
                        (slideInHorizontally(tween(FORM_ANIMATION_MS)) { width -> direction * width / 4 } +
                            fadeIn(tween(FORM_ANIMATION_MS))) togetherWith
                            (slideOutHorizontally(tween(FORM_ANIMATION_MS)) { width -> -direction * width / 4 } +
                                fadeOut(tween(FORM_ANIMATION_MS))) using
                            SizeTransform(clip = false)
                    },
                    label = "PupilLoginMethodForm",
                ) { method ->
                    when (method) {
                        PupilLoginMethod.SCHOOLS_BY -> Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                            SchoolsByCredentialsFields(
                                username = state.username,
                                password = state.password,
                                onUsernameChange = onUsernameChange,
                                onPasswordChange = onPasswordChange,
                                onSubmit = onSubmit,
                            )
                            AuthDemoBanner()
                        }

                        PupilLoginMethod.MANUAL -> Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                            ManualIdentityFields(
                                surname = state.surname,
                                name = state.name,
                                className = state.className,
                                onSurnameChange = onSurnameChange,
                                onNameChange = onNameChange,
                                onClassNameChange = onClassNameChange,
                            )
                            AuthDemoBanner(text = stringResource(R.string.auth_pupil_manual_hint))
                        }
                    }
                }
            }
        }

        AuthError(error = state.error)

        Spacer(modifier = Modifier.height(4.dp))

        AuthPrimaryButton(
            text = when (state.role) {
                UserRole.ORGANIZATOR -> stringResource(R.string.auth_login_action_organizer)
                UserRole.TEACHER -> stringResource(R.string.auth_login_action_teacher)
                UserRole.PUPIL -> when (state.pupilMethod) {
                    PupilLoginMethod.SCHOOLS_BY -> stringResource(R.string.auth_login_action)
                    PupilLoginMethod.MANUAL -> stringResource(R.string.auth_login_action_pupil_manual)
                }
            },
            isLoading = state.isLoading,
            onClick = onSubmit,
        )
    }
}

private const val FORM_ANIMATION_MS = 220
