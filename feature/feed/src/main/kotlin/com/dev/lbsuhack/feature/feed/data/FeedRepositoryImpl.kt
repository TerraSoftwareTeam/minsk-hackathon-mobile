package com.dev.lbsuhack.feature.feed.data

import com.dev.lbsuhack.core.domain.DomainError
import com.dev.lbsuhack.core.domain.DomainResult
import com.dev.lbsuhack.feature.feed.domain.FeedPost
import com.dev.lbsuhack.feature.feed.domain.FeedRepository

class FeedRepositoryImpl : FeedRepository {

    override suspend fun getFeed(): DomainResult<List<FeedPost>> =
        DomainResult.Success(posts)

    override suspend fun getPost(id: String): DomainResult<FeedPost> =
        posts.firstOrNull { it.id == id }
            ?.let { post -> DomainResult.Success(post) }
            ?: DomainResult.Failure(DomainError.NotFound)

    private companion object {
        val posts = listOf(
            FeedPost(
                id = "1",
                title = "Welcome to the hackathon",
                summary = "Schedule, rules and the first tasks are now available.",
                body = "The opening ceremony starts at 10:00. Check the schedule, join your team channel and make sure your development environment is ready before the first sprint.",
                author = "Organising team",
                publishedAt = "Today, 09:00",
            ),
            FeedPost(
                id = "2",
                title = "Navigation 3 in practice",
                summary = "How the new navigation library keeps multi-screen features simple.",
                body = "Navigation 3 stores the back stack as a plain list of keys. Each entry owns its ViewModelStore, so every screen gets an independent ViewModel and Store even when two screens use the same ViewModel class.",
                author = "Android team",
                publishedAt = "Yesterday, 18:20",
            ),
            FeedPost(
                id = "3",
                title = "MVIKotlin: one store per screen",
                summary = "Intent, state and labels without sharing mutable state between screens.",
                body = "A feature owns its domain and data packages. Each screen creates its own store in the ViewModel. Shared infrastructure lives in core modules, so features stay independent and can be developed in parallel.",
                author = "Architecture team",
                publishedAt = "Yesterday, 12:10",
            ),
            FeedPost(
                id = "4",
                title = "Mentor sessions",
                summary = "Book a 20-minute slot with a mentor for architecture or backend help.",
                body = "Mentors are available from 14:00 to 20:00. Bring a concrete question and a link to the code you want to discuss. Slots are limited, so book early.",
                author = "Mentors",
                publishedAt = "2 days ago",
            ),
            FeedPost(
                id = "5",
                title = "Demo checklist",
                summary = "What to prepare before the final presentation.",
                body = "Prepare a working build, a short scenario, and a fallback recording in case the emulator fails. Keep the demo under five minutes and focus on the user problem, not the implementation details.",
                author = "Organising team",
                publishedAt = "3 days ago",
            ),
        )
    }
}
