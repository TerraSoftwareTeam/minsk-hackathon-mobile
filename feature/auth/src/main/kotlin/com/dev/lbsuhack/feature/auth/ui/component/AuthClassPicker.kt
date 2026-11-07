package com.dev.lbsuhack.feature.auth.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.lbsuhack.feature.auth.R
import com.dev.lbsuhack.feature.auth.SCHOOL_CLASS_LETTERS

private val CLASS_GRADES = (1..11).map(Int::toString)
private val CLASS_LETTERS = SCHOOL_CLASS_LETTERS.map(Char::toString)

@Composable
internal fun AuthClassPicker(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val grade = value.takeWhile(Char::isDigit)
    val letter = value.dropWhile(Char::isDigit)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.auth_class),
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ClassDropdown(
                label = stringResource(R.string.auth_class_grade),
                selected = grade,
                options = CLASS_GRADES.map { it to it },
                onSelect = { onValueChange(it + letter) },
                modifier = Modifier.weight(1f),
            )
            ClassDropdown(
                label = stringResource(R.string.auth_class_letter),
                selected = letter,
                options = CLASS_LETTERS.map { it to it },
                onSelect = { onValueChange(grade + it) },
                enabled = grade.isNotEmpty(),
                modifier = Modifier.weight(1f),
            )
        }
    }
}
