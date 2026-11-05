package com.dev.lbsuhack.feature.feed.data

import com.dev.lbsuhack.core.domain.DomainError
import com.dev.lbsuhack.core.domain.DomainResult
import com.dev.lbsuhack.feature.feed.domain.EventCategory
import com.dev.lbsuhack.feature.feed.domain.FeedEvent
import com.dev.lbsuhack.feature.feed.domain.FeedRepository

class FeedRepositoryImpl : FeedRepository {

    private val subscribedIds = mutableSetOf("2", "6")

    override suspend fun getEvents(): DomainResult<List<FeedEvent>> =
        DomainResult.Success(events.map(::withSubscription))

    override suspend fun getEvent(id: String): DomainResult<FeedEvent> =
        events.firstOrNull { it.id == id }
            ?.let(::withSubscription)
            ?.let { DomainResult.Success(it) }
            ?: DomainResult.Failure(DomainError.NotFound)

    override suspend fun setSubscribed(id: String, subscribed: Boolean): DomainResult<FeedEvent> {
        val event = events.firstOrNull { it.id == id }
            ?: return DomainResult.Failure(DomainError.NotFound)

        if (subscribed) {
            subscribedIds += id
        } else {
            subscribedIds -= id
        }
        return DomainResult.Success(withSubscription(event))
    }

    private fun withSubscription(event: FeedEvent): FeedEvent =
        event.copy(isSubscribed = event.id in subscribedIds)

    private companion object {

        val events = listOf(
            FeedEvent(
                id = "1",
                title = "Хакатон «Цифровой прорыв»",
                category = EventCategory.HACKATHON,
                day = "12",
                month = "нояб",
                time = "10:00 – 22:00",
                place = "БГУ, главный корпус",
                organizer = "Оргкомитет ЛБСУ",
                summary = "48 часов кода, менторы и призовой фонд.",
                description = "Открытие в 10:00, распределение по командам и тематические треки: ИИ, мобильная разработка, финтех. Работают менторы из индустрии, финал — защита решений перед жюри. Приносите ноутбук и идеи.",
                tags = listOf("ai", "mobile", "fintech"),
                participants = 86,
                capacity = 120,
            ),
            FeedEvent(
                id = "2",
                title = "Митап Android-разработчиков",
                category = EventCategory.MEETUP,
                day = "14",
                month = "нояб",
                time = "18:30 – 21:00",
                place = "IT-хаб «Платан», Минск",
                organizer = "Minsk Android Community",
                summary = "Два доклада про Compose и навигацию + нетворкинг.",
                description = "Разбираем Navigation 3 на реальном кейсе и живую миграцию XML-экранов на Compose. После докладов — пицца, кофе и общение с командами из других проектов.",
                tags = listOf("android", "compose"),
                participants = 34,
                capacity = 50,
            ),
            FeedEvent(
                id = "3",
                title = "Мастер-класс: Jetpack Compose",
                category = EventCategory.WORKSHOP,
                day = "18",
                month = "нояб",
                time = "16:00 – 18:00",
                place = "БГУ, корпус №2, ауд. 310",
                organizer = "Android team",
                summary = "Практика: собираем экран ленты с нуля.",
                description = "Пишем экран со списком, состояниями загрузки и анимациями. Разбираем состояние, рекомпозицию и как не ломать производительность. Нужен ноутбук с Android Studio.",
                tags = listOf("compose", "ui"),
                participants = 18,
                capacity = 18,
            ),
            FeedEvent(
                id = "4",
                title = "Лекция: введение в машинное обучение",
                category = EventCategory.LECTURE,
                day = "20",
                month = "нояб",
                time = "12:00 – 13:30",
                place = "БГУ, ФАМ, ауд. 105",
                organizer = "AI Lab БГУ",
                summary = "Как устроены нейросети без формул на 40 минут.",
                description = "Минимум математики и максимум интуиции: от градиентного спуска до трансформеров. Разберём, какие задачи хакатона реально закрыть готовыми моделями, а где придётся думать самому.",
                tags = listOf("ai", "ml"),
                participants = 57,
                capacity = 90,
            ),
            FeedEvent(
                id = "5",
                title = "Кофе-брейк с менторами",
                category = EventCategory.MEETUP,
                day = "21",
                month = "нояб",
                time = "15:00 – 16:00",
                place = "БГУ, коворкинг 1 этажа",
                organizer = "Оргкомитет ЛБСУ",
                summary = "Неформальный разговор о проектах и карьере.",
                description = "Приходите с вопросами про архитектуру, сборку или собеседования. Менторы сидят за отдельными столиками, записываться не нужно — просто подходите.",
                tags = listOf("networking"),
                participants = 12,
                capacity = 30,
            ),
            FeedEvent(
                id = "6",
                title = "Мастер-класс: дизайн-системы в Figma",
                category = EventCategory.WORKSHOP,
                day = "25",
                month = "нояб",
                time = "17:00 – 19:00",
                place = "Онлайн",
                organizer = "Product design team",
                summary = "Токены, темы и передача макетов в код.",
                description = "Собираем мини-дизайн-систему: цветовые и типографические токены, автолейауты, компоненты. Показываем, как связать палитру Figma с темой Compose-приложения.",
                tags = listOf("design", "figma"),
                participants = 41,
                capacity = 60,
            ),
            FeedEvent(
                id = "7",
                title = "Демо-день команд",
                category = EventCategory.HACKATHON,
                day = "28",
                month = "нояб",
                time = "18:00 – 21:00",
                place = "БГУ, актовый зал",
                organizer = "Оргкомитет ЛБСУ",
                summary = "Команды показывают, что успели собрать.",
                description = "Пять минут на команду: живое демо и честные вопросы из зала. Приходите поддержать и подсмотреть идеи для своего проекта.",
                tags = listOf("demo", "pitch"),
                participants = 64,
                capacity = 100,
            ),
        )
    }
}
