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

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigationevent.NavigationEventDispatcher
import androidx.navigationevent.NavigationEventDispatcherOwner
import androidx.navigationevent.NavigationEventInput
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner

/**
 * Supplies Navigation 3 with an event dispatcher when the host Activity predates Activity 1.12.
 * The app stays on Activity 1.11 for API 21 support, so its ComponentActivity cannot provide this
 * owner itself. A legacy Compose back handler forwards completed back presses into Navigation 3.
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

    BackHandler(enabled = backInput.backEnabled) {
        backInput.dispatchBackCompleted()
    }

    CompositionLocalProvider(
        LocalNavigationEventDispatcherOwner provides owner,
        content = content
    )
}

private class ComposeBackNavigationEventInput : NavigationEventInput() {
    var backEnabled by mutableStateOf(false)
        private set

    override fun onHasEnabledBackHandlersChanged(hasEnabledBackHandlers: Boolean) {
        backEnabled = hasEnabledBackHandlers
    }

    fun dispatchBackCompleted() {
        dispatchOnBackCompleted()
    }
}
