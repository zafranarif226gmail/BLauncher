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

package com.movtery.zalithlauncher.ui.screens.content.download

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.download.addon.DependencyRequest
import com.movtery.zalithlauncher.game.download.addon.downloadDependenciesForVersions
import com.movtery.zalithlauncher.game.download.addon.downloadSingleForVersions
import com.movtery.zalithlauncher.game.download.addon.platform.PlatformClasses
import com.movtery.zalithlauncher.game.version.installed.VersionFolders
import com.movtery.zalithlauncher.ui.screens.NestedNavDisplay
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.content.download.addon.download.DownloadAddonScreen
import com.movtery.zalithlauncher.ui.screens.content.download.addon.elements.DownloadSingleOperation
import com.movtery.zalithlauncher.ui.screens.content.download.addon.search.SearchDataPackScreen
import com.movtery.zalithlauncher.ui.screens.navigateTo
import com.movtery.zalithlauncher.ui.screens.onBack
import com.movtery.zalithlauncher.ui.screens.rememberTransitionSpec
import com.movtery.zalithlauncher.utils.androidText
import com.movtery.zalithlauncher.utils.network.isUsingMobileData
import com.movtery.zalithlauncher.viewmodel.ErrorViewModel
import com.movtery.zalithlauncher.viewmodel.EventViewModel
import java.io.File

@Composable
fun DownloadDataPackScreen(
    key: NestedNavKey.DownloadDataPack,
    mainScreenKey: TitledNavKey?,
    downloadScreenKey: TitledNavKey?,
    downloadDataPackScreenKey: TitledNavKey?,
    onCurrentKeyChange: (TitledNavKey?) -> Unit,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit,
    eventViewModel: EventViewModel
) {
    val backStack = key.backStack
    val stackTopKey = backStack.lastOrNull()
    LaunchedEffect(stackTopKey) {
        onCurrentKeyChange(stackTopKey)
    }

    val context = LocalContext.current

    //数据包将安装到选中存档的 datapacks 文件夹中
    var operation by remember { mutableStateOf<DownloadSingleOperation>(DownloadSingleOperation.None) }
    DownloadSingleOperation(
        operation = operation,
        changeOperation = { operation = it },
        doInstall = { classes, version, versions, dependencies, world ->
            if (classes == PlatformClasses.DATA_PACK && world != null) {
                //只安装到确实拥有该存档的游戏版本（不同版本可能使用不同的游戏目录）
                val targetVersions = versions.filter { gameVersion ->
                    val savesDir = VersionFolders.SAVES.getDir(gameVersion.getGameDir())
                    File(savesDir, world).isDirectory
                }
                if (targetVersions.isEmpty()) {
                    submitError(
                        ErrorViewModel.ThrowableMessage(
                            title = androidText(R.string.download_addon_install_failed),
                            message = androidText(R.string.download_addon_world_missing_in_versions, world)
                        )
                    )
                    return@DownloadSingleOperation
                }
                downloadSingleForVersions(
                    version = version,
                    versions = targetVersions,
                    folder = "${VersionFolders.SAVES.folderName}/$world/datapacks",
                    submitError = submitError
                )
                downloadDependenciesForVersions(
                    requests = dependencies,
                    versions = targetVersions,
                    submitError = submitError,
                    world = world
                )
            } else {
                downloadSingleForVersions(
                    version = version,
                    versions = versions,
                    folder = classes.versionFolder.folderName,
                    submitError = submitError
                )
                downloadDependenciesForVersions(
                    requests = dependencies,
                    versions = versions,
                    submitError = submitError
                )
            }
        },
        onDependencyClicked = { platform, projectId, classes ->
            backStack.navigateTo(
                NormalNavKey.DownloadAddon(platform, projectId, classes)
            )
        }
    )

    if (backStack.isNotEmpty()) {
        NestedNavDisplay(
            ownerKey = key,
            backStack = backStack,
            modifier = Modifier.fillMaxSize(),
            onBack = {
                onBack(backStack)
            },
            extraDecorators = listOf(
                rememberViewModelStoreNavEntryDecorator()
            ),
            transitionSpec = rememberTransitionSpec(),
            popTransitionSpec = rememberTransitionSpec(),
            entryProvider = entryProvider {
                entry<NormalNavKey.SearchDataPack> {
                    SearchDataPackScreen(
                        mainScreenKey = mainScreenKey,
                        downloadScreenKey = downloadScreenKey,
                        downloadDataPackScreenKey = key,
                        downloadDataPackScreenCurrentKey = downloadDataPackScreenKey
                    ) { platform, projectId, _ ->
                        backStack.navigateTo(
                            NormalNavKey.DownloadAddon(platform, projectId, PlatformClasses.DATA_PACK)
                        )
                    }
                }
                entry<NormalNavKey.DownloadAddon> { addonKey ->
                    DownloadAddonScreen(
                        mainScreenKey = mainScreenKey,
                        parentScreenKey = key,
                        parentCurrentKey = downloadScreenKey,
                        currentKey = downloadDataPackScreenKey,
                        key = addonKey,
                        eventViewModel = eventViewModel,
                        onItemClicked = { classes, version, _, deps ->
                            operation = if (isUsingMobileData(context)) {
                                DownloadSingleOperation.WarningForMobileData(classes, version, deps)
                            } else {
                                DownloadSingleOperation.SelectVersion(classes, version, deps)
                            }
                        }
                    )
                }
            }
        )
    } else {
        Box(Modifier.fillMaxSize())
    }
}
