package com.dev.lbsuhack.feature.auth.ui.login.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.dev.lbsuhack.feature.auth.R
import com.dev.lbsuhack.feature.auth.ui.component.AuthClassPicker
import com.dev.lbsuhack.feature.auth.ui.component.AuthTextField

@Composable
internal fun ManualIdentityFields(
    surname: String,
    name: String,
    className: String,
    onSurnameChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onClassNameChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        AuthTextField(
            value = surname,
            onValueChange = onSurnameChange,
            label = stringResource(R.string.auth_surname),
            placeholder = stringResource(R.string.auth_surname_placeholder),
            leadingIcon = Icons.Rounded.Badge,
            imeAction = ImeAction.Next,
        )

        AuthTextField(
            value = name,
            onValueChange = onNameChange,
            label = stringResource(R.string.auth_name),
            placeholder = stringResource(R.string.auth_name_placeholder),
            leadingIcon = Icons.Rounded.Person,
            imeAction = ImeAction.Done,
        )

        AuthClassPicker(
            value = className,
            onValueChange = onClassNameChange,
        )
    }
}
