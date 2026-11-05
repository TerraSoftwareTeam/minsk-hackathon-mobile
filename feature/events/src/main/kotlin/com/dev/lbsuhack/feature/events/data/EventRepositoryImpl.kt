package com.dev.lbsuhack.feature.events.data

import com.dev.lbsuhack.core.domain.DomainError
import com.dev.lbsuhack.core.domain.DomainResult
import com.dev.lbsuhack.feature.events.domain.Event
import com.dev.lbsuhack.feature.events.domain.EventRepository

class EventRepositoryImpl : EventRepository {

    override suspend fun getEvents(): DomainResult<List<Event>> =
        DomainResult.Success(events)

    override suspend fun getEvent(id: String): DomainResult<Event> =
        events.firstOrNull { it.id == id }
            ?.let { DomainResult.Success(it) }
            ?: DomainResult.Failure(DomainError.NotFound)

    private companion object {

        val events = listOf(
            Event(
                id = "1",
                title = "Open Source Day",
                description = "A one-time community day with talks, workshops, and a demo floor.",
                date = "10 May, 11:00",
                place = "Minsk, IT Hub",
            ),
            Event(
                id = "2",
                title = "Product Night",
                description = "An evening of short product stories and networking.",
                date = "16 May, 18:30",
                place = "Minsk, Office 3",
            ),
            Event(
                id = "3",
                title = "Career Fair",
                description = "Meet hiring teams and get feedback on your CV.",
                date = "22 May, 10:00",
                place = "Minsk, Main Hall",
            ),
        )
    }
}
