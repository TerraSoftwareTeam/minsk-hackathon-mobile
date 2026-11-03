package com.dev.lbsuhack.feature.home.store

import com.arkivanov.mvikotlin.core.store.Store

interface HomeStore : Store<HomeStore.Intent, HomeStore.State, HomeStore.Label> {

    sealed interface Intent {
        data object Refresh : Intent
        data class ClickItem(val id: String, val title: String) : Intent
    }

    data class State(
        val items: List<Item> = listOf(
            Item("1", "Обзор хакатона", "Правила, расписание и полезные ресурсы для участников"),
            Item("2", "Навигация в Compose", "Пример перехода в стек Navigation 3 с аргументами"),
            Item("3", "Архитектура MVI", "Состояния, интенты и сайд-эффекты через MVIKotlin"),
        ),
        val isLoading: Boolean = false,
    ) {
        data class Item(
            val id: String,
            val title: String,
            val description: String,
        )
    }

    sealed interface Label {
        data class OpenDetails(val itemId: String, val title: String) : Label
    }
}
