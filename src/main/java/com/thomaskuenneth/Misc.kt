/*
 * Copyright 2020 Thomas Kuenneth
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.thomaskuenneth

import com.github.tkuenneth.nativeparameterstoreaccess.MacOSDefaults
import com.github.tkuenneth.nativeparameterstoreaccess.NativeParameterStoreAccess
import com.github.tkuenneth.nativeparameterstoreaccess.WindowsRegistry

fun isSystemInDarkTheme(): Boolean {
    return when {
        NativeParameterStoreAccess.IS_WINDOWS -> {
            val result = WindowsRegistry.getWindowsRegistryEntry(
                    "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize",
                    "AppsUseLightTheme")
            result == 0x0
        }
        NativeParameterStoreAccess.IS_MACOS -> {
            val result = MacOSDefaults.getDefaultsEntry("AppleInterfaceStyle")
            result == "Dark"
        }
        else -> false
    }
}
