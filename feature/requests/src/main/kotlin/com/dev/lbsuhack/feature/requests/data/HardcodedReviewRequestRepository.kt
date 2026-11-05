package com.dev.lbsuhack.feature.requests.data

import com.dev.lbsuhack.feature.requests.domain.RequestAuthor
import com.dev.lbsuhack.feature.requests.domain.RequestParticipant
import com.dev.lbsuhack.feature.requests.domain.ReviewRequest
import com.dev.lbsuhack.feature.requests.domain.ReviewRequestRepository
import com.dev.lbsuhack.feature.requests.domain.ReviewStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class HardcodedReviewRequestRepository : ReviewRequestRepository {

    private val requests = MutableStateFlow(createInitialRequests())

    override fun observeRequests(): Flow<List<ReviewRequest>> = requests.asStateFlow()

    override suspend fun approvePublication(requestId: String): ReviewRequest =
        updateRequest(requestId) { request ->
            require(request is ReviewRequest.Publication)
            require(request.status == ReviewStatus.PENDING_TEACHER)
            request.copy(
                status = ReviewStatus.APPROVED,
                declineReason = null,
            )
        }

    override suspend fun approveVisit(
        requestId: String,
        confirmedDate: String,
        confirmedLocation: String,
    ): ReviewRequest = updateRequest(requestId) { request ->
        require(request is ReviewRequest.Visit)
        require(request.status == ReviewStatus.PENDING_ADMIN)
        require(confirmedDate.isNotBlank())
        require(confirmedLocation.isNotBlank())
        request.copy(
            status = ReviewStatus.APPROVED,
            confirmedDate = confirmedDate.trim(),
            confirmedLocation = confirmedLocation.trim(),
            declineReason = null,
        )
    }

    override suspend fun decline(
        requestId: String,
        reason: String?,
    ): ReviewRequest = updateRequest(requestId) { request ->
        require(request.status == ReviewStatus.PENDING_TEACHER || request.status == ReviewStatus.PENDING_ADMIN)
        val normalizedReason = reason?.trim()?.ifBlank { null }
        when (request) {
            is ReviewRequest.Publication -> request.copy(
                status = ReviewStatus.DECLINED,
                declineReason = normalizedReason,
            )

            is ReviewRequest.Visit -> request.copy(
                status = ReviewStatus.DECLINED,
                declineReason = normalizedReason,
            )
        }
    }

    private suspend fun updateRequest(
        requestId: String,
        transform: (ReviewRequest) -> ReviewRequest,
    ): ReviewRequest {
        var updatedRequest: ReviewRequest? = null
        requests.update { currentRequests ->
            currentRequests.map { request ->
                if (request.id == requestId) {
                    transform(request).also { updatedRequest = it }
                } else {
                    request
                }
            }
        }
        return updatedRequest ?: error("Request not found: $requestId")
    }

    private fun createInitialRequests(): List<ReviewRequest> = listOf(
        ReviewRequest.Publication(
            id = "publication-1",
            title = "Кинопоказ «Остров сокровищ» в актовом зале",
            description = "Предлагаю в пятницу после уроков показать советский мультфильм и обсудить его с младшими классами. Нужен проектор и колонки.",
            proposedDate = "Пятница, 16:00",
            author = RequestAuthor("pupil-1", "Никита Ковалев", "10-А"),
            status = ReviewStatus.PENDING_TEACHER,
        ),
        ReviewRequest.Publication(
            id = "publication-2",
            title = "Турнир по настольному теннису между параллелями",
            description = "Хотим провести короткий турнир на большой перемене. Участвуют все желающие из 8–10 классов, инвентарь есть в спортзале.",
            proposedDate = "Суббота, 12:00",
            author = RequestAuthor("pupil-2", "Мария Соколова", "10-А"),
            status = ReviewStatus.PENDING_TEACHER,
        ),
        ReviewRequest.Publication(
            id = "publication-3",
            title = "Мастер-класс по цифровой иллюстрации",
            description = "Покажем, как рисовать персонажей на планшетах, и соберём выставку работ в школьном коридоре.",
            proposedDate = "Суббота, 14:00",
            author = RequestAuthor("pupil-3", "Дарья Ильина", "9-В"),
            status = ReviewStatus.APPROVED,
        ),
        ReviewRequest.Publication(
            id = "publication-4",
            title = "Ночная игра по школе без сопровождения взрослых",
            description = "Хотим провести квест по всем этажам после закрытия школы.",
            proposedDate = "Суббота, 22:00",
            author = RequestAuthor("pupil-4", "Артём Громов", "11-А"),
            status = ReviewStatus.DECLINED,
            declineReason = "Мероприятие после закрытия школы требует отдельного согласования с администрацией и родителями.",
        ),
        ReviewRequest.Visit(
            id = "visit-1",
            title = "Экскурсия в Национальный художественный музей",
            description = "Выездное посещение постоянной экспозиции и мастер-класса по живописи. Просим согласовать время и сопровождающего педагога.",
            proposedDate = "Суббота, 10:30",
            proposedLocation = "Национальный художественный музей",
            author = RequestAuthor("pupil-5", "Полина Васильева", "11-А"),
            participants = listOf(
                RequestParticipant("pupil-5", "Полина Васильева", "11-А"),
                RequestParticipant("pupil-6", "Артём Громов", "11-А"),
                RequestParticipant("pupil-7", "Ольга Смирнова", "10-Б"),
                RequestParticipant("pupil-8", "Илья Козлов", "9-А"),
                RequestParticipant("pupil-9", "София Лебедева", "10-А"),
                RequestParticipant("pupil-10", "Максим Орлов", "11-Б"),
            ),
            status = ReviewStatus.PENDING_ADMIN,
        ),
        ReviewRequest.Visit(
            id = "visit-2",
            title = "Посещение бассейна «Волна»",
            description = "Планируем оздоровительное плавание для сборной школы. Нужен автобус и второй сопровождающий.",
            proposedDate = "Воскресенье, 09:00",
            proposedLocation = "Бассейн «Волна»",
            author = RequestAuthor("pupil-11", "Кирилл Зайцев", "10-Б"),
            participants = listOf(
                RequestParticipant("pupil-11", "Кирилл Зайцев", "10-Б"),
                RequestParticipant("pupil-12", "Денис Мельников", "10-Б"),
                RequestParticipant("pupil-13", "Егор Богданов", "9-А"),
                RequestParticipant("pupil-14", "Тимофей Жуков", "9-А"),
                RequestParticipant("pupil-15", "Павел Сидоров", "10-А"),
                RequestParticipant("pupil-16", "Глеб Тарасов", "9-Б"),
                RequestParticipant("pupil-17", "Виктория Федорова", "10-А"),
                RequestParticipant("pupil-18", "Владислав Волков", "9-Б"),
            ),
            status = ReviewStatus.PENDING_ADMIN,
        ),
        ReviewRequest.Visit(
            id = "visit-3",
            title = "Выезд в театр юного зрителя",
            description = "Классный поход на спектакль по программе литературы.",
            proposedDate = "Суббота, 18:00",
            proposedLocation = "Театр юного зрителя",
            author = RequestAuthor("pupil-19", "Анна Макарова", "8-Б"),
            participants = listOf(
                RequestParticipant("pupil-19", "Анна Макарова", "8-Б"),
                RequestParticipant("pupil-20", "Роман Воронов", "10-А"),
                RequestParticipant("pupil-21", "Елизавета Павлова", "11-А"),
                RequestParticipant("pupil-22", "Степан Лазарев", "11-А"),
            ),
            status = ReviewStatus.APPROVED,
            confirmedDate = "Суббота, 17:30",
            confirmedLocation = "Театр юного зрителя, главный вход",
        ),
        ReviewRequest.Visit(
            id = "visit-4",
            title = "Двухдневный поход с ночёвкой",
            description = "Хотим выехать за город с палатками без педагогического сопровождения.",
            proposedDate = "Суббота — воскресенье",
            proposedLocation = "Лесопарк за городом",
            author = RequestAuthor("pupil-23", "Борис Щербаков", "11-Б"),
            participants = listOf(
                RequestParticipant("pupil-23", "Борис Щербаков", "11-Б"),
                RequestParticipant("pupil-24", "Константин Кудрявцев", "10-А"),
                RequestParticipant("pupil-25", "Вадим Марков", "10-А"),
            ),
            status = ReviewStatus.DECLINED,
            declineReason = "Выезд с ночёвкой без педагогического сопровождения не допускается.",
        ),
    )
}
