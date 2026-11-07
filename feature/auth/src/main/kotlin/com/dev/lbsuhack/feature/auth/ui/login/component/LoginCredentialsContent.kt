package com.dev.lbsuhack.feature.auth.ui.login.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dev.lbsuhack.core.domain.model.UserRole
import com.dev.lbsuhack.feature.auth.R
import com.dev.lbsuhack.feature.auth.store.login.LoginStore
import com.dev.lbsuhack.feature.auth.ui.component.AuthClassPicker
import com.dev.lbsuhack.feature.auth.ui.component.AuthDemoBanner
import com.dev.lbsuhack.feature.auth.ui.component.AuthError
import com.dev.lbsuhack.feature.auth.ui.component.AuthPasswordField
import com.dev.lbsuhack.feature.auth.ui.component.AuthPrimaryButton
import com.dev.lbsuhack.feature.auth.ui.component.AuthTextField

@Composable
internal fun LoginCredentialsContent(
    state: LoginStore.State,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSurnameChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onClassNameChange: (String) -> Unit,
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

            UserRole.TEACHER -> {
                AuthTextField(
                    value = state.surname,
                    onValueChange = onSurnameChange,
                    label = stringResource(R.string.auth_surname),
                    placeholder = stringResource(R.string.auth_surname_placeholder),
                    leadingIcon = Icons.Rounded.Badge,
                    imeAction = ImeAction.Next,
                )

                AuthTextField(
                    value = state.name,
                    onValueChange = onNameChange,
                    label = stringResource(R.string.auth_name),
                    placeholder = stringResource(R.string.auth_name_placeholder),
                    leadingIcon = Icons.Rounded.Person,
                    imeAction = ImeAction.Done,
                )

                AuthClassPicker(
                    value = state.className,
                    onValueChange = onClassNameChange,
                )
            }

            UserRole.PUPIL -> {
                AuthTextField(
                    value = state.username,
                    onValueChange = onUsernameChange,
                    label = stringResource(R.string.auth_username),
                    placeholder = stringResource(R.string.auth_username_placeholder),
                    leadingIcon = Icons.Rounded.Person,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next,
                )

                AuthPasswordField(
                    value = state.password,
                    onValueChange = onPasswordChange,
                    imeAction = ImeAction.Done,
                    onImeAction = onSubmit,
                )
            }
        }

        AuthError(error = state.error)

        Spacer(modifier = Modifier.height(4.dp))

        AuthPrimaryButton(
            text = when (state.role) {
                UserRole.ORGANIZATOR -> stringResource(R.string.auth_login_action_organizer)
                UserRole.TEACHER -> stringResource(R.string.auth_login_action_teacher)
                UserRole.PUPIL -> stringResource(R.string.auth_login_action)
            },
            isLoading = state.isLoading,
            onClick = onSubmit,
        )

        if (state.role == UserRole.PUPIL) {
            AuthDemoBanner()
        }
    }
}
