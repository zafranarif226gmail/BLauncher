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

package com.movtery.zalithlauncher.ui

import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import java.util.Locale
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * 获取不受应用内语言设置影响的系统语言列表
 */
private fun getSystemLocales(context: Context): List<Locale> {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        context.getSystemService(LocaleManager::class.java)?.systemLocales?.let { locales ->
            return (0 until locales.size()).map(locales::get)
        }
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        val locales = Resources.getSystem().configuration.locales
        return (0 until locales.size()).map(locales::get)
    }
    return listOf(Resources.getSystem().configuration.locale ?: Locale.getDefault())
}

/**
 * 按系统语言解析字符串资源，不随应用内语言设置变化
 */
@Composable
fun systemLocaleStringResource(@StringRes id: Int): String {
    val context = LocalContext.current
    val systemLocales = remember { getSystemLocales(context) }
    return remember(id, systemLocales) {
        val configuration = Configuration()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocales(LocaleList(*systemLocales.toTypedArray()))
        } else {
            configuration.locale = systemLocales.firstOrNull() ?: Locale.getDefault()
        }
        context.createConfigurationContext(configuration).getString(id)
    }
}
