package com.dev.lbsuhack.feature.activevote.domain

import kotlinx.coroutines.flow.Flow

interface ActiveVoteRepository {

    fun observeVotes(): Flow<List<ActiveVote>>

    fun observeResults(): Flow<List<VoteAcceptance>>

    suspend fun submitVote(voteId: String, optionId: String): VoteAcceptance
}
