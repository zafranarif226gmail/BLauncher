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

package com.movtery.zalithlauncher.utils.network

import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import com.movtery.zalithlauncher.context.GlobalContext

/**
 * 获取设备当前网络的真实 DNS 服务器
 */
fun getSystemDnsServerAddresses(): List<String>? {
    // activeNetwork was added in API 23; older Android versions use the resolver defaults.
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return null

    return runCatching {
        val connectivityManager = GlobalContext.applicationContext
            .getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return null
        val network = connectivityManager.activeNetwork ?: return null
        val linkProperties = connectivityManager.getLinkProperties(network) ?: return null

        linkProperties.dnsServers
            .mapNotNull { it.hostAddress?.takeIf(String::isNotEmpty) }
            .takeIf(List<String>::isNotEmpty)
    }.getOrNull()
}
