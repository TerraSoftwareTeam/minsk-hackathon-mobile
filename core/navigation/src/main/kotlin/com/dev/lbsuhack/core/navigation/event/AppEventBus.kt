package com.dev.lbsuhack.core.navigation.event

import com.dev.lbsuhack.core.localization.UiText
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow


sealed interface AppEvent {
    data class Error(val text: UiText) : AppEvent
    data class Toast(val text: UiText) : AppEvent
}

interface AppEventBus {
    val events: SharedFlow<AppEvent>
    suspend fun emit(event: AppEvent)
    fun tryEmit(event: AppEvent): Boolean
}

class DefaultAppEventBus : AppEventBus {
    private val _events = MutableSharedFlow<AppEvent>(extraBufferCapacity = 64)
    override val events: SharedFlow<AppEvent> = _events.asSharedFlow()

    override suspend fun emit(event: AppEvent) {
        _events.emit(event)
    }

    override fun tryEmit(event: AppEvent): Boolean {
        return _events.tryEmit(event)
    }
}
