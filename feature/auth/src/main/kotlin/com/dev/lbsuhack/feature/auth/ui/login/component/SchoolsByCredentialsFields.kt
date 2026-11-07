package com.dev.lbsuhack.feature.auth.ui.login.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dev.lbsuhack.feature.auth.R
import com.dev.lbsuhack.feature.auth.ui.component.AuthPasswordField
import com.dev.lbsuhack.feature.auth.ui.component.AuthTextField

@Composable
internal fun SchoolsByCredentialsFields(
    username: String,
    password: String,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        AuthTextField(
            value = username,
            onValueChange = onUsernameChange,
            label = stringResource(R.string.auth_username),
            placeholder = stringResource(R.string.auth_username_placeholder),
            leadingIcon = Icons.Rounded.Person,
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Next,
        )

        AuthPasswordField(
            value = password,
            onValueChange = onPasswordChange,
            imeAction = ImeAction.Done,
            onImeAction = onSubmit,
        )
    }
}
