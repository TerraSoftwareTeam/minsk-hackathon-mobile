package com.dev.lbsuhack.feature.home.store

import com.arkivanov.mvikotlin.core.store.Store
import com.dev.lbsuhack.core.localization.text.UiText
import com.dev.lbsuhack.feature.home.R

interface HomeStore : Store<HomeStore.Intent, HomeStore.State, HomeStore.Label> {

    sealed interface Intent {
        data object Refresh : Intent
        data class ClickItem(val id: String) : Intent
    }

    data class State(
        val items: List<Item> = listOf(
            Item(
                id = "1",
                title = UiText.Resource(R.string.home_item_1_title),
                description = UiText.Resource(R.string.home_item_1_description),
            ),
            Item(
                id = "2",
                title = UiText.Resource(R.string.home_item_2_title),
                description = UiText.Resource(R.string.home_item_2_description),
            ),
            Item(
                id = "3",
                title = UiText.Resource(R.string.home_item_3_title),
                description = UiText.Resource(R.string.home_item_3_description),
            ),
        ),
        val isLoading: Boolean = false,
    ) {
        data class Item(
            val id: String,
            val title: UiText,
            val description: UiText,
        )
    }

    sealed interface Label {
        data class OpenDetails(val itemId: String) : Label
    }
}
