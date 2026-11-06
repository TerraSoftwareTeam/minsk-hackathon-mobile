package com.dev.lbsuhack.feature.activities.data

import com.dev.lbsuhack.feature.activities.domain.ActivityInitiative
import com.dev.lbsuhack.feature.activities.domain.ActivityCategory
import com.dev.lbsuhack.feature.activities.domain.ActivityParticipant
import com.dev.lbsuhack.feature.activities.domain.ActivityProposal
import com.dev.lbsuhack.feature.activities.domain.ActivityRepository
import com.dev.lbsuhack.feature.activities.domain.ActivityStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class ActivityRepositoryImpl : ActivityRepository {

    private val activities = MutableStateFlow(createInitialActivities())

    override fun observeActivities(): Flow<List<ActivityProposal>> = activities.asStateFlow()

    override suspend fun getActivities(): List<ActivityProposal> = activities.value

    override suspend fun getActivityById(id: String): ActivityProposal? =
        activities.value.find { it.id == id }

    override suspend fun createProposal(
        title: String,
        category: ActivityCategory,
        description: String,
        targetClass: String,
        minParticipants: Int,
        maxParticipants: Int,
        preferenceNotes: String,
        authorId: String,
        authorName: String,
        authorClass: String,
        photoUri: String?,
        initiative: ActivityInitiative,
    ): ActivityProposal {
        val newProposal = ActivityProposal(
            id = UUID.randomUUID().toString(),
            title = title.trim(),
            category = category,
            description = description.trim(),
            targetClass = targetClass.trim().ifEmpty { "Все классы" },
            minParticipants = minParticipants,
            maxParticipants = maxParticipants,
            authorId = authorId,
            authorName = authorName,
            authorClass = authorClass,
            status = ActivityStatus.MODERATING,
            participants = listOf(
                ActivityParticipant(
                    pupilId = authorId,
                    name = authorName,
                    pupilClass = authorClass,
                )
            ),
            preferenceNotes = preferenceNotes.trim(),
            photoUri = photoUri,
            initiative = initiative,
        )

        activities.update { current -> listOf(newProposal) + current }
        return newProposal
    }

    override suspend fun joinActivity(
        activityId: String,
        participant: ActivityParticipant,
    ): ActivityProposal {
        var updatedItem: ActivityProposal? = null
        activities.update { currentList ->
            currentList.map { item ->
                if (item.id == activityId) {
                    if (item.participants.any { it.pupilId == participant.pupilId }) {
                        updatedItem = item
                        item
                    } else if (item.participants.size >= item.maxParticipants) {
                        updatedItem = item
                        item
                    } else {
                        val updated = item.copy(
                            participants = item.participants + participant,
                        )
                        updatedItem = updated
                        updated
                    }
                } else {
                    item
                }
            }
        }
        return updatedItem ?: error("Activity not found: $activityId")
    }

    override suspend fun leaveActivity(activityId: String, pupilId: String): ActivityProposal {
        var updatedItem: ActivityProposal? = null
        activities.update { currentList ->
            currentList.map { item ->
                if (item.id == activityId) {
                    val updated = item.copy(
                        participants = item.participants.filterNot { it.pupilId == pupilId },
                    )
                    updatedItem = updated
                    updated
                } else {
                    item
                }
            }
        }
        return updatedItem ?: error("Activity not found: $activityId")
    }

    override suspend fun transitionToAdmin(activityId: String): ActivityProposal {
        var updatedItem: ActivityProposal? = null
        activities.update { currentList ->
            currentList.map { item ->
                if (item.id == activityId) {
                    val updated = item.copy(status = ActivityStatus.PENDING_ADMIN)
                    updatedItem = updated
                    updated
                } else {
                    item
                }
            }
        }
        return updatedItem ?: error("Activity not found: $activityId")
    }

    override suspend fun approveActivity(
        activityId: String,
        confirmedTime: String,
        confirmedLocation: String,
    ): ActivityProposal {
        var updatedItem: ActivityProposal? = null
        activities.update { currentList ->
            currentList.map { item ->
                if (item.id == activityId) {
                    val updated = item.copy(
                        status = ActivityStatus.APPROVED,
                        confirmedTime = confirmedTime,
                        confirmedLocation = confirmedLocation,
                    )
                    updatedItem = updated
                    updated
                } else {
                    item
                }
            }
        }
        return updatedItem ?: error("Activity not found: $activityId")
    }

    private fun createInitialActivities(): List<ActivityProposal> = listOf(
        ActivityProposal(
            id = "act-1",
            title = "Турнир по настольным играм: Каркассон и Catan",
            category = ActivityCategory.BOARD_GAMES,
            description = "Собираемся в субботу поиграть в любимые стратегии! Берите с собой настолки, если есть, печенье и хорошее настроение. Новичков обучим правилам.",
            targetClass = "7–11 классы",
            minParticipants = 4,
            maxParticipants = 10,
            authorId = "101",
            authorName = "Никита Ковалев",
            authorClass = "10-А",
            status = ActivityStatus.RECRUITING,
            participants = listOf(
                ActivityParticipant("101", "Никита Ковалев", "10-А"),
                ActivityParticipant("102", "Мария Соколова", "10-А"),
                ActivityParticipant("103", "Иван Кузнецов", "9-Б"),
                ActivityParticipant("104", "Александр Морозов", "11-В"),
                ActivityParticipant("105", "Екатерина Белова", "8-А"),
                ActivityParticipant("106", "Дмитрий Новик", "10-Б"),
            ),
            preferenceNotes = "Нужен просторный кабинет со сдвинутыми партами (например 215)",
        ),
        ActivityProposal(
            id = "act-2",
            title = "Поход в кинотеатр «Победа» на новый фильм",
            category = ActivityCategory.CINEMA,
            description = "Организуем совместный культпоход на утренний сеанс. Обсудим фильм после просмотра в кафе рядом со школой.",
            targetClass = "8–11 классы",
            minParticipants = 8,
            maxParticipants = 15,
            authorId = "107",
            authorName = "Полина Васильева",
            authorClass = "11-А",
            status = ActivityStatus.PENDING_ADMIN,
            participants = listOf(
                ActivityParticipant("107", "Полина Васильева", "11-А"),
                ActivityParticipant("108", "Артем Громов", "11-А"),
                ActivityParticipant("109", "Ольга Смирнова", "10-Б"),
                ActivityParticipant("110", "Илья Козлов", "9-А"),
                ActivityParticipant("111", "София Лебедева", "10-А"),
                ActivityParticipant("112", "Максим Орлов", "11-Б"),
                ActivityParticipant("113", "Анна Макарова", "8-Б"),
                ActivityParticipant("114", "Роман Воронов", "10-А"),
                ActivityParticipant("115", "Елизавета Павлова", "11-А"),
            ),
            preferenceNotes = "Сбор в холле школы в 11:00",
        ),
        ActivityProposal(
            id = "act-3",
            title = "Волейбольный матч: 9-е против 10-х классов",
            category = ActivityCategory.SPORTS,
            description = "Дружеский волейбольный поединок между параллелями. Нужна спортивная форма и сменная обувь. Болельщики тоже приглашаются!",
            targetClass = "9–10 классы",
            minParticipants = 12,
            maxParticipants = 16,
            authorId = "116",
            authorName = "Кирилл Зайцев",
            authorClass = "10-Б",
            status = ActivityStatus.APPROVED,
            participants = listOf(
                ActivityParticipant("116", "Кирилл Зайцев", "10-Б"),
                ActivityParticipant("117", "Денис Мельников", "10-Б"),
                ActivityParticipant("118", "Егор Богданов", "9-А"),
                ActivityParticipant("119", "Тимофей Жуков", "9-А"),
                ActivityParticipant("120", "Павел Сидоров", "10-А"),
                ActivityParticipant("121", "Глеб Тарасов", "9-Б"),
                ActivityParticipant("122", "Виктория Федорова", "10-А"),
                ActivityParticipant("123", "Владислав Волков", "9-Б"),
                ActivityParticipant("124", "Ярослав Семенов", "10-Б"),
                ActivityParticipant("125", "Артур Романов", "9-А"),
                ActivityParticipant("126", "Матвей Данилов", "10-А"),
                ActivityParticipant("127", "Алина Королева", "9-Б"),
            ),
            preferenceNotes = "Сетка и мячи в инвентарной",
            confirmedTime = "Суббота, 11:30 – 13:30",
            confirmedLocation = "Большой спортивный зал",
        ),
        ActivityProposal(
            id = "act-4",
            title = "Мастер-класс по цифровой иллюстрации и скетчингу",
            category = ActivityCategory.CREATIVITY,
            description = "Учимся рисовать персонажей и окружение на планшетах и бумаге. Поделимся кистями, лайфхаками и сделаем общую выставку рисунков в школьном коридоре.",
            targetClass = "5–11 классы",
            minParticipants = 3,
            maxParticipants = 8,
            authorId = "128",
            authorName = "Дарья Ильина",
            authorClass = "9-В",
            status = ActivityStatus.RECRUITING,
            participants = listOf(
                ActivityParticipant("128", "Дарья Ильина", "9-В"),
                ActivityParticipant("129", "Алиса Карпова", "8-А"),
                ActivityParticipant("130", "Михаил Григорьев", "10-А"),
            ),
            preferenceNotes = "Кабинет ИЗО или любой с хорошим освещением",
        ),
        ActivityProposal(
            id = "act-5",
            title = "Школьный IT-хакатон: мини-игры на Python и Scratch",
            category = ActivityCategory.SCIENCE,
            description = "Командный хакатон на 3 часа: создаем мини-игру на тему космоса. Защита проектов перед учителем информатики и ценные призы победителям!",
            targetClass = "8–11 классы",
            minParticipants = 6,
            maxParticipants = 12,
            authorId = "131",
            authorName = "Борис Щербаков",
            authorClass = "11-Б",
            status = ActivityStatus.APPROVED,
            participants = listOf(
                ActivityParticipant("131", "Борис Щербаков", "11-Б"),
                ActivityParticipant("132", "Константин Кудрявцев", "10-А"),
                ActivityParticipant("133", "Вадим Марков", "10-А"),
                ActivityParticipant("134", "Сергей Алексеев", "11-Б"),
                ActivityParticipant("135", "Наталья Попова", "9-А"),
                ActivityParticipant("136", "Олег Кузьмин", "10-Б"),
                ActivityParticipant("137", "Степан Лазарев", "11-А"),
                ActivityParticipant("138", "Ксения Гусева", "8-Б"),
            ),
            preferenceNotes = "Компьютеры с выходом в интернет",
            confirmedTime = "Суббота, 10:00 – 13:00",
            confirmedLocation = "Кабинет информатики (№204)",
        ),
    )
}
