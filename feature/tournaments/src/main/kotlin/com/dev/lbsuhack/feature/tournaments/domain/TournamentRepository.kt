package com.dev.lbsuhack.feature.tournaments.domain

import com.dev.lbsuhack.core.domain.DomainResult

interface TournamentRepository {

    suspend fun getTournaments(): DomainResult<List<Tournament>>

    suspend fun getTournament(id: String): DomainResult<Tournament>
}
