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

package com.movtery.zalithlauncher.ui.screens.content.download.addon.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.movtery.zalithlauncher.game.download.addon.platform.Platform
import com.movtery.zalithlauncher.game.download.addon.platform.PlatformClasses
import com.movtery.zalithlauncher.game.download.addon.platform.curseforge.models.CurseForgeDataPackCategory
import com.movtery.zalithlauncher.game.download.addon.platform.modrinth.models.ModrinthDataPackCategory
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey

@Composable
fun SearchDataPackScreen(
    mainScreenKey: TitledNavKey?,
    downloadScreenKey: TitledNavKey?,
    downloadDataPackScreenKey: TitledNavKey,
    downloadDataPackScreenCurrentKey: TitledNavKey?,
    swapToDownload: (Platform, projectId: String, iconUrl: String?) -> Unit = { _, _, _ -> },
    openLink: (String) -> Unit = {}
) {
    val initialPlatform = remember {
        AllSettings.searchDataPackPlatform.getValue()
    }
    SearchAddonScreen(
        mainScreenKey = mainScreenKey,
        parentScreenKey = downloadDataPackScreenKey,
        parentCurrentKey = downloadScreenKey,
        screenKey = NormalNavKey.SearchDataPack,
        currentKey = downloadDataPackScreenCurrentKey,
        platformClasses = PlatformClasses.DATA_PACK,
        initialPlatform = initialPlatform,
        onPlatformChange = {
            AllSettings.searchDataPackPlatform.save(it)
        },
        getCategories = { platform ->
            when (platform) {
                Platform.CURSEFORGE -> CurseForgeDataPackCategory.entries
                Platform.MODRINTH -> ModrinthDataPackCategory.entries
                Platform.BLAUNCHERMOD -> emptyList()
            }
        },
        mapCategories = { platform, string ->
            when (platform) {
                Platform.MODRINTH -> {
                    ModrinthDataPackCategory.entries.find { it.facetValue() == string }
                }
                Platform.CURSEFORGE -> {
                    CurseForgeDataPackCategory.entries.find { it.describe() == string }
                }
                Platform.BLAUNCHERMOD -> null
            }
        },
        openLink = openLink,
        swapToDownload = swapToDownload
    )
}
