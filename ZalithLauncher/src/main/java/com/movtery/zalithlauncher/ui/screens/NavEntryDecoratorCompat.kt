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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.ui.screens

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator as rememberLegacyDecorator
import androidx.navigation3.runtime.NavEntryDecorator

/**
 * Lifecycle ViewModel Navigation 1.0.0-alpha04 predates Navigation3's typed decorator API and
 * returns a decorator keyed as [Any]. The implementation is key-type agnostic and uses each
 * entry's content key, so this only adapts the Kotlin type for typed back stacks.
 */
@Composable
@Suppress("UNCHECKED_CAST")
fun <T : Any> rememberViewModelStoreNavEntryDecorator(): NavEntryDecorator<T> =
    rememberLegacyDecorator() as NavEntryDecorator<T>
