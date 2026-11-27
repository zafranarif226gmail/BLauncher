/*
 * Zalith Launcher 2
 * Copyright (C) 2025-2026 MovTery <movtery228@qq.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.ui.theme

import androidx.activity.BackEventCompat
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigationevent.NavigationEvent
import androidx.navigationevent.NavigationEventDispatcher
import androidx.navigationevent.NavigationEventDispatcherOwner
import androidx.navigationevent.NavigationEventInput
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import kotlinx.coroutines.CancellationException

/**
 * Supplies Navigation 3 with an event dispatcher when the host Activity predates Activity 1.12.
 * The app stays on Activity 1.11 for API 21 support, so its ComponentActivity cannot provide this
 * owner itself. A Compose predictive-back handler forwards gesture progress and completion into
 * Navigation 3.
 */
@Composable
internal fun NavigationEventHost(content: @Composable () -> Unit) {
    val dispatcher = remember { NavigationEventDispatcher() }
    val owner = remember(dispatcher) {
        object : NavigationEventDispatcherOwner {
            override val navigationEventDispatcher: NavigationEventDispatcher = dispatcher
        }
    }
    val backInput = remember(dispatcher) { ComposeBackNavigationEventInput() }

    DisposableEffect(dispatcher, backInput) {
        dispatcher.addInput(backInput)
        onDispose {
            dispatcher.dispose()
        }
    }

    PredictiveBackHandler(enabled = backInput.backEnabled) { progressEvents ->
        var gestureStarted = false
        try {
            progressEvents.collect { backEvent ->
                val navigationEvent = backEvent.toNavigationEvent()
                if (gestureStarted) {
                    backInput.dispatchBackProgressed(navigationEvent)
                } else {
                    backInput.dispatchBackStarted(navigationEvent)
                    gestureStarted = true
                }
            }
            // Empty progress flow is a regular button back; NavigationEventInput synthesizes
            // the required non-predictive start before completing it.
            backInput.dispatchBackCompleted()
        } catch (cancelled: CancellationException) {
            if (gestureStarted) {
                backInput.dispatchBackCancelled()
            }
            throw cancelled
        }
    }

    CompositionLocalProvider(
        LocalNavigationEventDispatcherOwner provides owner,
        content = content
    )
}

private class ComposeBackNavigationEventInput : NavigationEventInput() {
    var backEnabled by mutableStateOf(false)
        private set

    // Navigation3 1.2.0 brings NavigationEvent 1.1.1. Use its common callback rather than the
    // directional callback introduced in NavigationEvent 1.2.0.
    override fun onHasEnabledHandlersChanged(hasEnabledHandlers: Boolean) {
        backEnabled = hasEnabledHandlers
    }

    fun dispatchBackStarted(event: NavigationEvent) {
        dispatchOnBackStarted(event)
    }

    fun dispatchBackProgressed(event: NavigationEvent) {
        dispatchOnBackProgressed(event)
    }

    fun dispatchBackCompleted() {
        dispatchOnBackCompleted()
    }

    fun dispatchBackCancelled() {
        dispatchOnBackCancelled()
    }
}

private fun BackEventCompat.toNavigationEvent(): NavigationEvent =
    NavigationEvent(
        swipeEdge = when (swipeEdge) {
            BackEventCompat.EDGE_LEFT -> NavigationEvent.EDGE_LEFT
            BackEventCompat.EDGE_RIGHT -> NavigationEvent.EDGE_RIGHT
            else -> NavigationEvent.EDGE_NONE
        },
        progress = progress,
        touchX = touchX,
        touchY = touchY
    )
