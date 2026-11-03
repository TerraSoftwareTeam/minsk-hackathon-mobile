package com.dev.lbsuhack.feature.profile.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor

class ProfileStoreFactory(
    private val storeFactory: StoreFactory,
) {
    fun create(): ProfileStore =
        object : ProfileStore, Store<ProfileStore.Intent, ProfileStore.State, ProfileStore.Label> by storeFactory.create(
            name = "ProfileStore",
            initialState = ProfileStore.State(),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private sealed interface Message {
        data class UsernameChanged(val username: String) : Message
        data class NotificationsToggled(val enabled: Boolean) : Message
        data object SavedIncremented : Message
    }

    private class ExecutorImpl : CoroutineExecutor<ProfileStore.Intent, Nothing, ProfileStore.State, Message, ProfileStore.Label>() {
        override fun executeIntent(intent: ProfileStore.Intent) {
            when (intent) {
                is ProfileStore.Intent.SetUsername -> dispatch(Message.UsernameChanged(intent.username))
                is ProfileStore.Intent.ToggleNotifications -> {
                    val next = !state().notificationsEnabled
                    dispatch(Message.NotificationsToggled(next))
                    publish(ProfileStore.Label.NotificationToggled(next))
                }
                is ProfileStore.Intent.IncrementSaved -> dispatch(Message.SavedIncremented)
            }
        }
    }

    private object ReducerImpl : Reducer<ProfileStore.State, Message> {
        override fun ProfileStore.State.reduce(msg: Message): ProfileStore.State =
            when (msg) {
                is Message.UsernameChanged -> copy(username = msg.username)
                is Message.NotificationsToggled -> copy(notificationsEnabled = msg.enabled)
                is Message.SavedIncremented -> copy(savedItemsCount = savedItemsCount + 1)
            }
    }
}
