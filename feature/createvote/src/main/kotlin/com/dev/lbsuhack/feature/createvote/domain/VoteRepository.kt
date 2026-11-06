package com.dev.lbsuhack.feature.createvote.domain

import kotlinx.coroutines.flow.Flow

interface VoteRepository {

    fun observeEvents(): Flow<List<VoteEvent>>

    fun observeVotes(): Flow<List<CreatedVote>>

    suspend fun createVote(draft: VoteDraft): CreatedVote
}
