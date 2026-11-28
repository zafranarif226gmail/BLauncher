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

package com.movtery.zalithlauncher.game.download.addon.platform.curseforge.models

import com.movtery.zalithlauncher.game.download.addon.platform.PlatformDependencyType
import com.movtery.zalithlauncher.game.download.addon.platform.PlatformReleaseType
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class CurseForgeApiModelTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun fileIndexReadsTheApiFilenameProperty() {
        val index = json.decodeFromString(
            CurseForgeFileIndex.serializer(),
            """{"gameVersion":"1.21.1","fileId":12345,"filename":"example.jar","releaseType":1,"modLoader":4}"""
        )

        assertEquals("example.jar", index.filename)
        assertEquals(CurseForgeModLoader.FABRIC, index.modLoader)
    }

    @Test
    fun projectDataDecodesTheFilenameInLatestFilesIndexes() {
        val data = json.decodeFromString(
            CurseForgeData.serializer(),
            """{"id":1,"gameId":432,"name":"Example","slug":"example","links":{},"summary":"","status":4,"downloadCount":3,"isFeatured":false,"primaryCategoryId":0,"categories":[],"authors":[],"mainFileId":12345,"latestFiles":[],"latestFilesIndexes":[{"gameVersion":"1.21.1","fileId":12345,"filename":"example.jar","releaseType":1,"modLoader":4}],"dateCreated":"2024-01-01T00:00:00Z","dateModified":"2024-01-01T00:00:00Z","dateReleased":"2024-01-01T00:00:00Z","gamePopularityRank":1,"isAvailable":true,"thumbsUpCount":0}"""
        )

        assertEquals("example.jar", data.latestFilesIndexes.single().filename)
        assertEquals(CurseForgeModLoader.FABRIC, data.latestFilesIndexes.single().modLoader)
    }

    @Test
    fun releaseTypeAcceptsCurseForgeNumericCodesAndPlatformNames() {
        assertEquals(
            PlatformReleaseType.RELEASE,
            json.decodeFromString(PlatformReleaseType.serializer(), "1")
        )
        assertEquals(
            PlatformReleaseType.BETA,
            json.decodeFromString(PlatformReleaseType.serializer(), "\"beta\"")
        )
    }

    @Test
    fun dependencyTypeAcceptsEveryPlatformNameAndCurseForgeCodes() {
        assertEquals(
            PlatformDependencyType.TOOL,
            json.decodeFromString(PlatformDependencyType.serializer(), "\"tool\"")
        )
        assertEquals(
            PlatformDependencyType.INCLUDE,
            json.decodeFromString(PlatformDependencyType.serializer(), "\"include\"")
        )
        assertEquals(
            PlatformDependencyType.TOOL,
            json.decodeFromString(PlatformDependencyType.serializer(), "4")
        )
        assertEquals(
            PlatformDependencyType.INCLUDE,
            json.decodeFromString(PlatformDependencyType.serializer(), "6")
        )
    }
}
