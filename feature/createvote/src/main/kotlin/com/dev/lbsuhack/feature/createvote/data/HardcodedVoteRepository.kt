package com.dev.lbsuhack.feature.createvote.data

import com.dev.lbsuhack.feature.createvote.domain.CreatedVote
import com.dev.lbsuhack.feature.createvote.domain.VoteDraft
import com.dev.lbsuhack.feature.createvote.domain.VoteEvent
import com.dev.lbsuhack.feature.createvote.domain.VoteParticipant
import com.dev.lbsuhack.feature.createvote.domain.VoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class HardcodedVoteRepository : VoteRepository {

    private val events = MutableStateFlow(createEvents())
    private val votes = MutableStateFlow<List<CreatedVote>>(emptyList())
    private var nextVoteNumber = 1

    override fun observeEvents(): Flow<List<VoteEvent>> = events.asStateFlow()

    override fun observeVotes(): Flow<List<CreatedVote>> = votes.asStateFlow()

    override suspend fun createVote(draft: VoteDraft): CreatedVote {
        val event = events.value.firstOrNull { it.id == draft.eventId }
            ?: error("Event not found: ${draft.eventId}")
        require(draft.title.isNotBlank())
        require(draft.deadline.isNotBlank())
        require(draft.participantIds.isNotEmpty())

        val eventParticipantIds = event.participants.map { it.id }.toSet()
        require(eventParticipantIds.containsAll(draft.participantIds))

        val created = CreatedVote(
            id = "vote-${nextVoteNumber++}",
            eventId = draft.eventId,
            title = draft.title.trim(),
            description = draft.description.trim(),
            deadline = draft.deadline.trim(),
            participantIds = draft.participantIds,
        )
        votes.update { currentVotes -> currentVotes + created }
        return created
    }

    private fun createEvents(): List<VoteEvent> = listOf(
        VoteEvent(
            id = "event-quiz",
            title = "Школьный квиз «Что? Где? Когда?»",
            date = "Пятница, 16:00",
            participants = listOf(
                VoteParticipant("quiz-pupil-1", "Никита Ковалев", "10-А"),
                VoteParticipant("quiz-pupil-2", "Мария Соколова", "10-А"),
                VoteParticipant("quiz-pupil-3", "Дарья Ильина", "9-В"),
                VoteParticipant("quiz-pupil-4", "Артём Громов", "11-А"),
                VoteParticipant("quiz-pupil-5", "Полина Васильева", "11-А"),
                VoteParticipant("quiz-pupil-6", "Илья Козлов", "9-А"),
            ),
        ),
        VoteEvent(
            id = "event-tennis",
            title = "Турнир по настольному теннису",
            date = "Суббота, 12:00",
            participants = listOf(
                VoteParticipant("tennis-pupil-1", "Кирилл Зайцев", "10-Б"),
                VoteParticipant("tennis-pupil-2", "Денис Мельников", "10-Б"),
                VoteParticipant("tennis-pupil-3", "Егор Богданов", "9-А"),
                VoteParticipant("tennis-pupil-4", "Тимофей Жуков", "9-А"),
                VoteParticipant("tennis-pupil-5", "Павел Сидоров", "10-А"),
            ),
        ),
        VoteEvent(
            id = "event-museum",
            title = "Экскурсия в Национальный художественный музей",
            date = "Суббота, 10:30",
            participants = listOf(
                VoteParticipant("museum-pupil-1", "Анна Макарова", "8-Б"),
                VoteParticipant("museum-pupil-2", "Роман Воронов", "10-А"),
                VoteParticipant("museum-pupil-3", "Елизавета Павлова", "11-А"),
                VoteParticipant("museum-pupil-4", "Степан Лазарев", "11-А"),
                VoteParticipant("museum-pupil-5", "Ольга Смирнова", "10-Б"),
                VoteParticipant("museum-pupil-6", "София Лебедева", "10-А"),
                VoteParticipant("museum-pupil-7", "Максим Орлов", "11-Б"),
            ),
        ),
        VoteEvent(
            id = "event-illustration",
            title = "Мастер-класс по цифровой иллюстрации",
            date = "Среда, 15:30",
            participants = listOf(
                VoteParticipant("art-pupil-1", "Виктория Федорова", "10-А"),
                VoteParticipant("art-pupil-2", "Глеб Тарасов", "9-Б"),
                VoteParticipant("art-pupil-3", "Владислав Волков", "9-Б"),
                VoteParticipant("art-pupil-4", "Борис Щербаков", "11-Б"),
            ),
        ),
    )
}
