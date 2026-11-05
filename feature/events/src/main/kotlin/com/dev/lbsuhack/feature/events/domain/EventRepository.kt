package com.dev.lbsuhack.feature.events.domain

import com.dev.lbsuhack.core.domain.DomainResult

interface EventRepository {

    suspend fun getEvents(): DomainResult<List<Event>>

    suspend fun getEvent(id: String): DomainResult<Event>
}
