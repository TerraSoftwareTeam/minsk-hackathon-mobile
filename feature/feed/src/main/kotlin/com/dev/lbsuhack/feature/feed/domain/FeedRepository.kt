package com.dev.lbsuhack.feature.feed.domain

import com.dev.lbsuhack.core.domain.DomainResult

interface FeedRepository {

    suspend fun getEvents(): DomainResult<List<FeedEvent>>

    suspend fun getEvent(id: String): DomainResult<FeedEvent>

    suspend fun setSubscribed(id: String, subscribed: Boolean): DomainResult<FeedEvent>
}
