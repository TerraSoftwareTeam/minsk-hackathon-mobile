package com.dev.lbsuhack.feature.activities.ui.details.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dev.lbsuhack.core.designsystem.theme.hack
import com.dev.lbsuhack.feature.activities.domain.ActivityProposal
import com.dev.lbsuhack.feature.activities.ui.activities.component.ActivityCardCover
import com.dev.lbsuhack.feature.activities.ui.activities.component.ActivityMetadata
import com.dev.lbsuhack.feature.activities.ui.component.ActivityAuthorRow

@Composable
internal fun ActivityHeroCard(proposal: ActivityProposal, isAuthor: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = MaterialTheme.shapes.extraLarge,
            ),
    ) {
        proposal.photoUri?.let { photoUri ->
            ActivityCardCover(proposal = proposal, photoUri = photoUri, isAuthor = isAuthor)
        }
        Column(modifier = Modifier.padding(20.dp)) {
            if (proposal.photoUri == null) {
                ActivityMetadata(proposal = proposal, isAuthor = isAuthor)
                Spacer(modifier = Modifier.height(16.dp))
            }
            Text(
                text = proposal.title,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(16.dp))

            ActivityAuthorRow(proposal = proposal)

            if (proposal.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(18.dp))
                HorizontalDivider(color = MaterialTheme.hack.gridLine)
                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = proposal.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}
