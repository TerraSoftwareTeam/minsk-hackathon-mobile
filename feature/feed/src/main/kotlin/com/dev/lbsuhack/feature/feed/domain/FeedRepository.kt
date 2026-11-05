package com.dev.lbsuhack.feature.feed.domain

import com.dev.lbsuhack.core.domain.DomainResult

interface FeedRepository {

    suspend fun getFeed(): DomainResult<List<FeedPost>>

    suspend fun getPost(id: String): DomainResult<FeedPost>
}
