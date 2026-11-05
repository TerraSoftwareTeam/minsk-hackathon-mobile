package com.dev.lbsuhack.feature.tournaments.data

import com.dev.lbsuhack.feature.tournaments.domain.MatchStatus
import com.dev.lbsuhack.feature.tournaments.domain.StageStatus
import com.dev.lbsuhack.feature.tournaments.domain.TournamentStage
import com.dev.lbsuhack.feature.tournaments.domain.TournamentTeam
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TournamentBracketGeneratorTest {

    @Test
    fun `generate bracket with 7 teams creates 3 rounds and exactly 1 BYE match in round 0`() {
        val teams = (1..7).map {
            TournamentTeam(id = "team-$it", name = "Team $it")
        }

        val bracket = TournamentBracketGenerator.generateBracket(
            stage = TournamentStage.SCHOOL,
            stageName = "Школьный этап",
            stageSubtitle = "Гимназия №41",
            status = StageStatus.ACTIVE,
            teams = teams,
        )

        assertEquals(TournamentStage.SCHOOL, bracket.stage)
        assertEquals(7, bracket.teamsCount)
        assertEquals(3, bracket.rounds.size)

        val round0 = bracket.rounds[0]
        assertEquals(4, round0.matches.size)

        val byeMatch = round0.matches.firstOrNull { it.status == MatchStatus.BYE }
        assertNotNull(byeMatch)
        assertEquals("team-7", byeMatch?.teamA?.id)
        assertTrue(byeMatch?.teamB?.isBye == true)
        assertEquals("team-7", byeMatch?.winnerId)

        val round1 = bracket.rounds[1]
        assertEquals(2, round1.matches.size)

        val round2 = bracket.rounds[2]
        assertEquals(1, round2.matches.size)
        assertEquals("Финал", round2.title)
    }

    @Test
    fun `generate bracket with 5 teams creates 3 rounds and 3 BYE matches in round 0`() {
        val teams = (1..5).map {
            TournamentTeam(id = "team-$it", name = "Team $it")
        }

        val bracket = TournamentBracketGenerator.generateBracket(
            stage = TournamentStage.DISTRICT,
            stageName = "Районный этап",
            stageSubtitle = "Московский район",
            status = StageStatus.UPCOMING,
            teams = teams,
        )

        assertEquals(5, bracket.teamsCount)
        assertEquals(3, bracket.rounds.size)

        val round0 = bracket.rounds[0]
        val byeMatches = round0.matches.filter { it.status == MatchStatus.BYE }
        assertEquals(3, byeMatches.size)
    }

    @Test
    fun `generate bracket with 9 teams creates 4 rounds and 7 BYE matches in round 0`() {
        val teams = (1..9).map {
            TournamentTeam(id = "team-$it", name = "Team $it")
        }

        val bracket = TournamentBracketGenerator.generateBracket(
            stage = TournamentStage.CITY,
            stageName = "Городской этап",
            stageSubtitle = "Минск",
            status = StageStatus.UPCOMING,
            teams = teams,
        )

        assertEquals(9, bracket.teamsCount)
        assertEquals(4, bracket.rounds.size)

        val round0 = bracket.rounds[0]
        assertEquals(8, round0.matches.size)
        val byeMatches = round0.matches.filter { it.status == MatchStatus.BYE }
        assertEquals(7, byeMatches.size)
        val regularMatches = round0.matches.filter { it.status != MatchStatus.BYE }
        assertEquals(1, regularMatches.size)
    }
}
