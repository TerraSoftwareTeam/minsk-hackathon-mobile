package com.dev.lbsuhack.feature.tournaments.data

import com.dev.lbsuhack.feature.tournaments.domain.MatchStatus
import com.dev.lbsuhack.feature.tournaments.domain.StageStatus
import com.dev.lbsuhack.feature.tournaments.domain.TournamentBracket
import com.dev.lbsuhack.feature.tournaments.domain.TournamentMatch
import com.dev.lbsuhack.feature.tournaments.domain.TournamentRound
import com.dev.lbsuhack.feature.tournaments.domain.TournamentStage
import com.dev.lbsuhack.feature.tournaments.domain.TournamentTeam

object TournamentBracketGenerator {

    fun generateBracket(
        stage: TournamentStage,
        stageName: String,
        stageSubtitle: String,
        status: StageStatus,
        teams: List<TournamentTeam>,
        predefinedResults: Map<String, MatchResult> = emptyMap(),
    ): TournamentBracket {
        val totalTeams = teams.size
        if (totalTeams < 2) {
            return TournamentBracket(
                stage = stage,
                stageName = stageName,
                stageSubtitle = stageSubtitle,
                status = status,
                teamsCount = totalTeams,
                rounds = emptyList(),
            )
        }

        var powerOfTwo = 2
        while (powerOfTwo < totalTeams) {
            powerOfTwo *= 2
        }

        var roundCount = 0
        var tempPower = powerOfTwo
        while (tempPower > 1) {
            roundCount++
            tempPower /= 2
        }

        val rounds = mutableListOf<TournamentRound>()

        val round0MatchesCount = powerOfTwo / 2
        val byeMatchesCount = powerOfTwo - totalTeams
        val regularMatchesCount = round0MatchesCount - byeMatchesCount
        val round0Matches = mutableListOf<TournamentMatch>()

        var teamCursor = 0
        for (m in 0 until round0MatchesCount) {
            val isByeMatch = m >= regularMatchesCount

            val teamA: TournamentTeam
            val teamB: TournamentTeam

            if (!isByeMatch) {
                teamA = teams[teamCursor++]
                teamB = teams[teamCursor++]
            } else {
                teamA = teams[teamCursor++]
                teamB = TournamentTeam(
                    id = "bye-$m",
                    name = "BYE",
                    isBye = true,
                )
            }

            val matchId = "${stage.name.lowercase()}-r0-m$m"
            val override = predefinedResults[matchId]

            val matchTeamA = if (override?.scoreA != null) teamA.copy(score = override.scoreA) else teamA
            val matchTeamB = if (override?.scoreB != null) teamB.copy(score = override.scoreB) else teamB

            val (matchStatus, winnerId) = when {
                teamB.isBye -> MatchStatus.BYE to teamA.id
                teamA.isBye -> MatchStatus.BYE to teamB.id
                override != null -> override.status to override.winnerId
                matchTeamA.score != null && matchTeamB.score != null -> {
                    val winner = if (matchTeamA.score > matchTeamB.score) matchTeamA.id else matchTeamB.id
                    MatchStatus.COMPLETED to winner
                }
                else -> MatchStatus.SCHEDULED to null
            }

            round0Matches.add(
                TournamentMatch(
                    id = matchId,
                    roundIndex = 0,
                    matchIndex = m,
                    teamA = matchTeamA,
                    teamB = matchTeamB,
                    winnerId = winnerId,
                    status = matchStatus,
                ),
            )
        }

        rounds.add(
            TournamentRound(
                index = 0,
                title = resolveRoundTitle(round0MatchesCount),
                matches = round0Matches,
            ),
        )

        var previousMatches = round0Matches
        for (r in 1 until roundCount) {
            val matchesInRound = previousMatches.size / 2
            val currentRoundMatches = mutableListOf<TournamentMatch>()

            for (m in 0 until matchesInRound) {
                val prevMatchA = previousMatches[2 * m]
                val prevMatchB = previousMatches[2 * m + 1]

                val resolvedTeamA = resolveWinnerTeam(prevMatchA) ?: TournamentTeam(
                    id = "pending-$r-${2 * m}",
                    name = "Победитель пары ${2 * m + 1}",
                    subtitle = "Ожидание",
                )
                val resolvedTeamB = resolveWinnerTeam(prevMatchB) ?: TournamentTeam(
                    id = "pending-$r-${2 * m + 1}",
                    name = "Победитель пары ${2 * m + 2}",
                    subtitle = "Ожидание",
                )

                val matchId = "${stage.name.lowercase()}-r$r-m$m"
                val override = predefinedResults[matchId]

                val finalTeamA = if (override?.scoreA != null) resolvedTeamA.copy(score = override.scoreA) else resolvedTeamA
                val finalTeamB = if (override?.scoreB != null) resolvedTeamB.copy(score = override.scoreB) else resolvedTeamB

                val (matchStatus, winnerId) = when {
                    override != null -> override.status to override.winnerId
                    finalTeamA.score != null && finalTeamB.score != null && !finalTeamA.id.startsWith("pending-") && !finalTeamB.id.startsWith("pending-") -> {
                        val winner = if (finalTeamA.score > finalTeamB.score) finalTeamA.id else finalTeamB.id
                        MatchStatus.COMPLETED to winner
                    }
                    else -> MatchStatus.SCHEDULED to null
                }

                currentRoundMatches.add(
                    TournamentMatch(
                        id = matchId,
                        roundIndex = r,
                        matchIndex = m,
                        teamA = finalTeamA,
                        teamB = finalTeamB,
                        winnerId = winnerId,
                        status = matchStatus,
                    ),
                )
            }

            rounds.add(
                TournamentRound(
                    index = r,
                    title = resolveRoundTitle(matchesInRound),
                    matches = currentRoundMatches,
                ),
            )
            previousMatches = currentRoundMatches
        }

        return TournamentBracket(
            stage = stage,
            stageName = stageName,
            stageSubtitle = stageSubtitle,
            status = status,
            teamsCount = totalTeams,
            rounds = rounds,
        )
    }

    private fun resolveWinnerTeam(match: TournamentMatch): TournamentTeam? {
        val winnerId = match.winnerId ?: return null
        return when (winnerId) {
            match.teamA.id -> match.teamA.copy(score = null)
            match.teamB?.id -> match.teamB.copy(score = null)
            else -> null
        }
    }

    private fun resolveRoundTitle(matchesCount: Int): String = when (matchesCount) {
        1 -> "Финал"
        2 -> "Полуфинал"
        4 -> "1/4 финала"
        8 -> "1/8 финала"
        16 -> "1/16 финала"
        else -> "Раунд ($matchesCount матчей)"
    }

    data class MatchResult(
        val scoreA: Int? = null,
        val scoreB: Int? = null,
        val winnerId: String? = null,
        val status: MatchStatus = MatchStatus.COMPLETED,
    )
}
