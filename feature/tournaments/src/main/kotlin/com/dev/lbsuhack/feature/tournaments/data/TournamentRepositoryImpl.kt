package com.dev.lbsuhack.feature.tournaments.data

import com.dev.lbsuhack.core.domain.DomainError
import com.dev.lbsuhack.core.domain.DomainResult
import com.dev.lbsuhack.feature.tournaments.domain.Tournament
import com.dev.lbsuhack.feature.tournaments.domain.TournamentRepository

class TournamentRepositoryImpl : TournamentRepository {

    override suspend fun getTournaments(): DomainResult<List<Tournament>> =
        DomainResult.Success(tournaments)

    override suspend fun getTournament(id: String): DomainResult<Tournament> =
        tournaments.firstOrNull { it.id == id }
            ?.let { DomainResult.Success(it) }
            ?: DomainResult.Failure(DomainError.NotFound)

    private companion object {

        val tournaments = listOf(
            Tournament(
                id = "1",
                title = "Spring Mobile Cup",
                description = "A bracket tournament for mobile teams with a live final.",
                date = "12 May, 10:00",
                place = "Minsk, Main Hall",
                participants = 32,
                capacity = 64,
            ),
            Tournament(
                id = "2",
                title = "Compose Challenge",
                description = "Build a small app in a day and present it to the jury.",
                date = "18 May, 09:30",
                place = "Minsk, IT Hub",
                participants = 18,
                capacity = 40,
            ),
            Tournament(
                id = "3",
                title = "Backend Arena",
                description = "Teams solve API and data problems under time pressure.",
                date = "25 May, 11:00",
                place = "Minsk, Office 3",
                participants = 24,
                capacity = 24,
            ),
        )
    }
}
