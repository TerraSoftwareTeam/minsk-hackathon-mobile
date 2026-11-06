package com.dev.lbsuhack.feature.activevote.data

import com.dev.lbsuhack.feature.activevote.domain.ActiveVote
import com.dev.lbsuhack.feature.activevote.domain.ActiveVoteEvent
import com.dev.lbsuhack.feature.activevote.domain.ActiveVoteRepository
import com.dev.lbsuhack.feature.activevote.domain.ActiveVoteUser
import com.dev.lbsuhack.feature.activevote.domain.VoteAcceptance
import com.dev.lbsuhack.feature.activevote.domain.VoteOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class HardcodedActiveVoteRepository : ActiveVoteRepository {

    private val votes = MutableStateFlow(createVotes())
    private val results = MutableStateFlow<List<VoteAcceptance>>(emptyList())

    override fun observeVotes(): Flow<List<ActiveVote>> = votes.asStateFlow()

    override fun observeResults(): Flow<List<VoteAcceptance>> = results.asStateFlow()

    override suspend fun submitVote(voteId: String, optionId: String): VoteAcceptance {
        val vote = votes.value.firstOrNull { it.id == voteId }
            ?: error("Vote not found: $voteId")
        require(vote.options.any { it.id == optionId })
        require(results.value.none { it.voteId == voteId })

        val acceptance = VoteAcceptance(voteId = voteId, optionId = optionId)
        results.update { currentResults -> currentResults + acceptance }
        return acceptance
    }

    private fun createVotes(): List<ActiveVote> = listOf(
        ActiveVote(
            id = "active-quiz",
            eventTitle = "Школьный квиз «Что? Где? Когда?»",
            question = "Какой формат квиза выбрать?",
            deadline = "Пятница, 18:00",
            options = listOf(
                VoteOption("quiz-team", "Командный (по 5 человек)"),
                VoteOption("quiz-solo", "Индивидуальный зачёт"),
                VoteOption("quiz-mixed", "Смешанный формат (очно + онлайн)"),
            ),
            events = listOf(
                ActiveVoteEvent(
                    id = "event-quiz-main",
                    title = "Школьный квиз «Что? Где? Когда?»",
                    category = "КВИЗ",
                    day = "28",
                    month = "ноя",
                    time = "16:00 – 18:00",
                    place = "Актовый зал, 2 этаж",
                    summary = "Интеллектуальная битва между классами на знание науки, IT и логики.",
                    participants = 28,
                    capacity = 36,
                ),
                ActiveVoteEvent(
                    id = "event-quiz-prep",
                    title = "Тренировочный раунд для капитанов",
                    category = "ТРЕНИРОВКА",
                    day = "26",
                    month = "ноя",
                    time = "15:30 – 16:30",
                    place = "Медиатека",
                    summary = "Разбор регламента и демонстрационных вопросов перед основным турниром.",
                    participants = 12,
                    capacity = 15,
                ),
            ),
            users = listOf(
                ActiveVoteUser(
                    id = "user-1",
                    name = "Алексей Смирнов",
                    role = "11-А класс · Капитан",
                    hasVoted = true,
                ),
                ActiveVoteUser(
                    id = "user-2",
                    name = "Мария Васильева",
                    role = "10-Б класс",
                    hasVoted = true,
                ),
                ActiveVoteUser(
                    id = "user-3",
                    name = "Иван Морозов",
                    role = "9-В класс",
                    hasVoted = false,
                ),
                ActiveVoteUser(
                    id = "user-4",
                    name = "Дарья Кузнецова",
                    role = "11-Б класс",
                    hasVoted = false,
                ),
                ActiveVoteUser(
                    id = "user-5",
                    name = "Никита Павлов",
                    role = "10-А класс",
                    hasVoted = false,
                ),
            ),
        ),
        ActiveVote(
            id = "active-tennis",
            eventTitle = "Турнир по настольному теннису",
            question = "Когда провести финал?",
            deadline = "Четверг, 20:00",
            options = listOf(
                VoteOption("tennis-noon", "Суббота, 12:00"),
                VoteOption("tennis-afternoon", "Суббота, 15:00"),
            ),
            events = listOf(
                ActiveVoteEvent(
                    id = "event-tennis-1",
                    title = "Турнир по настольному теннису",
                    category = "СПОРТ",
                    day = "30",
                    month = "ноя",
                    time = "14:00 – 18:00",
                    place = "Спортивный зал №1",
                    summary = "Финальная стадия соревнований по настольному теннису среди старшеклассников.",
                    participants = 16,
                    capacity = 16,
                ),
            ),
            users = listOf(
                ActiveVoteUser(
                    id = "user-t-1",
                    name = "Максим Орлов",
                    role = "11-А класс",
                    hasVoted = true,
                ),
                ActiveVoteUser(
                    id = "user-t-2",
                    name = "Егор Федоров",
                    role = "10-А класс",
                    hasVoted = false,
                ),
                ActiveVoteUser(
                    id = "user-t-3",
                    name = "Полина Соколова",
                    role = "11-В класс",
                    hasVoted = false,
                ),
            ),
        ),
    )
}
