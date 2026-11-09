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

package com.movtery.zalithlauncher.game.download.addon.platform.blaunchermod

import com.movtery.zalithlauncher.game.download.addon.platform.PlatformClasses

/**
 * BLauncherMod 网站（https://blaunchermod.netlify.app）源
 *
 * 该网站为普通 Web 站点（hash 路由），没有公开的 API，
 * 因此这里只保留真实存在的页面链接：启动器内置一个“打开网站”的入口。
 * 以下分类页面路由均来自该站点的自身实现（#/modrinth?type=mod、#/c/<分类id>）。
 */
object BLauncherModSite {
    /** BLauncherMod 主页 */
    const val URL_HOME = "https://blaunchermod.netlify.app/"

    /** 分类资源页面（网站自身实现的 hash 路由）*/
    private const val HASH_MODRINTH_CATALOGUE = "#/modrinth?type=mod"
    private const val HASH_RESOURCE_PACKS_JAVA = "#/c/resourcepacks-java"
    private const val HASH_DATA_PACKS_JAVA = "#/c/datapacks-java"
    private const val HASH_MAPS_JAVA = "#/c/maps-java"
    private const val HASH_SHADERS_JAVA = "#/c/shaders-java"

    /**
     * 根据资源分类获取对应的 BLauncherMod 页面链接
     * 如果该分类没有对应页面，则返回网站主页
     */
    fun urlFor(classes: PlatformClasses): String = when (classes) {
        PlatformClasses.MOD -> URL_HOME + HASH_MODRINTH_CATALOGUE
        PlatformClasses.RESOURCE_PACK -> URL_HOME + HASH_RESOURCE_PACKS_JAVA
        PlatformClasses.DATA_PACK -> URL_HOME + HASH_DATA_PACKS_JAVA
        PlatformClasses.SAVES -> URL_HOME + HASH_MAPS_JAVA
        PlatformClasses.SHADERS -> URL_HOME + HASH_SHADERS_JAVA
        else -> URL_HOME
    }
}
