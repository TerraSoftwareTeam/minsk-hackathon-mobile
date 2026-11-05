package com.dev.lbsuhack.feature.feed.domain

data class FeedPost(
    val id: String,
    val title: String,
    val summary: String,
    val body: String,
    val author: String,
    val publishedAt: String,
)
