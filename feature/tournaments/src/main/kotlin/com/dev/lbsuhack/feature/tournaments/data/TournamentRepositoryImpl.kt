package com.dev.lbsuhack.feature.tournaments.data

import com.dev.lbsuhack.core.domain.DomainError
import com.dev.lbsuhack.core.domain.DomainResult
import com.dev.lbsuhack.feature.tournaments.domain.MatchStatus
import com.dev.lbsuhack.feature.tournaments.domain.StageStatus
import com.dev.lbsuhack.feature.tournaments.domain.Tournament
import com.dev.lbsuhack.feature.tournaments.domain.TournamentBracket
import com.dev.lbsuhack.feature.tournaments.domain.TournamentRepository
import com.dev.lbsuhack.feature.tournaments.domain.TournamentStage
import com.dev.lbsuhack.feature.tournaments.domain.TournamentTeam

class TournamentRepositoryImpl : TournamentRepository {

    override suspend fun getBracket(stage: TournamentStage): DomainResult<TournamentBracket> {
        val bracket = when (stage) {
            TournamentStage.SCHOOL -> TournamentBracketGenerator.generateBracket(
                stage = TournamentStage.SCHOOL,
                stageName = "Школьный этап",
                stageSubtitle = "Гимназия №41 им. В.Х. Серебряного",
                status = StageStatus.ACTIVE,
                teams = schoolTeams,
                predefinedResults = schoolResults,
            )

            TournamentStage.DISTRICT -> TournamentBracketGenerator.generateBracket(
                stage = TournamentStage.DISTRICT,
                stageName = "Районный этап",
                stageSubtitle = "Московский район г. Минска",
                status = StageStatus.UPCOMING,
                teams = districtTeams,
                predefinedResults = districtResults,
            )

            TournamentStage.CITY -> TournamentBracketGenerator.generateBracket(
                stage = TournamentStage.CITY,
                stageName = "Городской этап",
                stageSubtitle = "Минск • Финал столицы",
                status = StageStatus.UPCOMING,
                teams = cityTeams,
                predefinedResults = cityResults,
            )
        }
        return DomainResult.Success(bracket)
    }

    override suspend fun getTournaments(): DomainResult<List<Tournament>> =
        DomainResult.Success(tournaments)

    override suspend fun getTournament(id: String): DomainResult<Tournament> =
        tournaments.firstOrNull { it.id == id }
            ?.let { DomainResult.Success(it) }
            ?: DomainResult.Failure(DomainError.NotFound)

    private companion object {

        val schoolTeams = listOf(
            TournamentTeam(id = "team-11m", name = "11 «М» класс", subtitle = "Физ-мат профиль"),
            TournamentTeam(id = "team-10b", name = "10 «Б» класс", subtitle = "Инфо-тех профиль"),
            TournamentTeam(id = "team-11a", name = "11 «А» класс", subtitle = "Хим-био профиль"),
            TournamentTeam(id = "team-9v", name = "9 «В» класс", subtitle = "Юниоры"),
            TournamentTeam(id = "team-10a", name = "10 «А» класс", subtitle = "Лингвисты"),
            TournamentTeam(id = "team-9a", name = "9 «А» класс", subtitle = "Эко-отряд"),
            TournamentTeam(id = "team-11b", name = "11 «Б» класс", subtitle = "Активисты школы"),
        )

        val schoolResults = mapOf(
            "school-r0-m0" to TournamentBracketGenerator.MatchResult(
                scoreA = 2,
                scoreB = 1,
                winnerId = "team-11m",
                status = MatchStatus.COMPLETED,
            ),
            "school-r0-m1" to TournamentBracketGenerator.MatchResult(
                scoreA = 3,
                scoreB = 0,
                winnerId = "team-11a",
                status = MatchStatus.COMPLETED,
            ),
            "school-r0-m2" to TournamentBracketGenerator.MatchResult(
                scoreA = 2,
                scoreB = 1,
                winnerId = "team-10a",
                status = MatchStatus.COMPLETED,
            ),
            "school-r1-m0" to TournamentBracketGenerator.MatchResult(
                scoreA = 3,
                scoreB = 2,
                winnerId = "team-11m",
                status = MatchStatus.COMPLETED,
            ),
            "school-r1-m1" to TournamentBracketGenerator.MatchResult(
                scoreA = 1,
                scoreB = 2,
                winnerId = "team-11b",
                status = MatchStatus.COMPLETED,
            ),
            "school-r2-m0" to TournamentBracketGenerator.MatchResult(
                scoreA = 1,
                scoreB = 1,
                winnerId = null,
                status = MatchStatus.LIVE,
            ),
        )

        val districtTeams = listOf(
            TournamentTeam(id = "dist-gym41", name = "Гимназия №41", subtitle = "Победитель школы"),
            TournamentTeam(id = "dist-lic1", name = "Лицей №1", subtitle = "Московский район"),
            TournamentTeam(id = "dist-sch145", name = "СШ №145", subtitle = "Московский район"),
            TournamentTeam(id = "dist-sch208", name = "СШ №208", subtitle = "Московский район"),
            TournamentTeam(id = "dist-gym12", name = "Гимназия №12", subtitle = "Московский район"),
        )

        val districtResults = emptyMap<String, TournamentBracketGenerator.MatchResult>()

        val cityTeams = listOf(
            TournamentTeam(id = "city-moscow", name = "Московский район", subtitle = "Сборная района"),
            TournamentTeam(id = "city-central", name = "Центральный район", subtitle = "Сборная района"),
            TournamentTeam(id = "city-soviet", name = "Советский район", subtitle = "Сборная района"),
            TournamentTeam(id = "city-pervomay", name = "Первомайский район", subtitle = "Сборная района"),
            TournamentTeam(id = "city-partizan", name = "Партизанский район", subtitle = "Сборная района"),
            TournamentTeam(id = "city-zavod", name = "Заводской район", subtitle = "Сборная района"),
            TournamentTeam(id = "city-lenin", name = "Ленинский район", subtitle = "Сборная района"),
            TournamentTeam(id = "city-october", name = "Октябрьский район", subtitle = "Сборная района"),
            TournamentTeam(id = "city-frunze", name = "Фрунзенский район", subtitle = "Сборная района"),
        )

        val cityResults = emptyMap<String, TournamentBracketGenerator.MatchResult>()

        val tournaments = listOf(
            Tournament(
                id = "1",
                title = "Эко-Хакатон Чемпионат",
                description = "Турнир между школами, районами и городом по сбору и эко-инициативам.",
                date = "12–25 Мая, 2026",
                place = "Минск, Все районы",
                participants = 21,
                capacity = 32,
            ),
            Tournament(
                id = "2",
                title = "Весенний кубок по экологии",
                description = "Соревнование среди старших классов школ Минска.",
                date = "28–30 Мая, 2026",
                place = "Минск, СШ №208",
                participants = 14,
                capacity = 16,
            ),
            Tournament(
                id = "3",
                title = "Городской финал инициатив",
                description = "Финальный этап городского экологического чемпионата.",
                date = "5–7 Июня, 2026",
                place = "Минск-Арена",
                participants = 8,
                capacity = 8,
            ),
        )
    }
}

