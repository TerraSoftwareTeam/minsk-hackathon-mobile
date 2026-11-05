package com.dev.lbsuhack.feature.tournaments.domain

enum class TournamentStage {
    SCHOOL,
    DISTRICT,
    CITY,
}

enum class StageStatus {
    ACTIVE,
    COMPLETED,
    UPCOMING,
}

enum class MatchStatus {
    SCHEDULED,
    LIVE,
    COMPLETED,
    BYE,
}

data class TournamentTeam(
    val id: String,
    val name: String,
    val subtitle: String? = null,
    val score: Int? = null,
    val isBye: Boolean = false,
)

data class TournamentMatch(
    val id: String,
    val roundIndex: Int,
    val matchIndex: Int,
    val teamA: TournamentTeam,
    val teamB: TournamentTeam?,
    val winnerId: String?,
    val status: MatchStatus,
)

data class TournamentRound(
    val index: Int,
    val title: String,
    val matches: List<TournamentMatch>,
)

data class TournamentBracket(
    val stage: TournamentStage,
    val stageName: String,
    val stageSubtitle: String,
    val status: StageStatus,
    val teamsCount: Int,
    val rounds: List<TournamentRound>,
)
