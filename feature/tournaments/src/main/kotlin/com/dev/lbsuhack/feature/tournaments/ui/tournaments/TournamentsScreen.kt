package com.dev.lbsuhack.feature.tournaments.ui.tournaments

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dev.lbsuhack.core.designsystem.theme.hack
import com.dev.lbsuhack.core.localization.text.asString
import com.dev.lbsuhack.feature.tournaments.R
import com.dev.lbsuhack.feature.tournaments.presentation.tournaments.TournamentsViewModel
import com.dev.lbsuhack.feature.tournaments.store.tournaments.TournamentsStore
import com.dev.lbsuhack.feature.tournaments.ui.board.TournamentBracketBoard
import com.dev.lbsuhack.feature.tournaments.ui.board.TournamentMatchDetailsSheet
import com.dev.lbsuhack.feature.tournaments.ui.board.TournamentStageSelector
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentsScreen(
    onOpenDetails: (tournamentId: String) -> Unit = {},
    viewModel: TournamentsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.accept(TournamentsStore.Intent.Load)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.tournaments_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when {
                state.isLoading && state.bracket == null -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                state.error != null && state.bracket == null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = state.error!!.asString(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        TextButton(onClick = { viewModel.accept(TournamentsStore.Intent.Refresh) }) {
                            Text(stringResource(R.string.tournaments_retry))
                        }
                    }
                }

                state.bracket != null -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        TournamentStageSelector(
                            currentStage = state.stage,
                            bracket = state.bracket,
                            onSelectStage = { viewModel.accept(TournamentsStore.Intent.SelectStage(it)) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        )

                        HorizontalDivider(
                            color = MaterialTheme.hack.gridLine,
                            thickness = 1.dp,
                        )

                        TournamentBracketBoard(
                            bracket = state.bracket!!,
                            selectedMatchId = state.selectedMatch?.id,
                            onSelectMatch = { viewModel.accept(TournamentsStore.Intent.SelectMatch(it)) },
                            resetTrigger = state.resetViewportTrigger,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .clipToBounds(),
                        )
                    }
                }
            }

            TournamentMatchDetailsSheet(
                match = state.selectedMatch,
                onDismiss = { viewModel.accept(TournamentsStore.Intent.SelectMatch(null)) },
            )
        }
    }
}
