package com.dev.lbsuhack.feature.activities.ui.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dev.lbsuhack.feature.activities.R

@Composable
internal fun ActivityVotersSummary(voters: List<String>, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        if (voters.isNotEmpty()) {
            VoterAvatarStack(voters = voters)
            Spacer(modifier = Modifier.width(10.dp))
        }
        Text(
            text = votersText(voters),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun votersText(voters: List<String>): String {
    val names = voters.map(::shortName)
    return when (names.size) {
        0 -> stringResource(R.string.activities_voters_empty)
        1 -> stringResource(R.string.activities_voters_one, names[0])
        2 -> stringResource(R.string.activities_voters_two, names[0], names[1])
        else -> stringResource(R.string.activities_voters_many, names[0], names[1], names.size - 2)
    }
}
