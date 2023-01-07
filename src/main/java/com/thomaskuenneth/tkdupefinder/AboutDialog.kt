/*
 * Copyright 2020 - 2023 Thomas Kuenneth
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
package com.thomaskuenneth.tkdupefinder

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.util.ResourceBundle.getBundle

@Composable
fun AboutDialog(showAboutDialog: MutableState<Boolean>) {
    if (showAboutDialog.value) {
        Dialog(
            onCloseRequest = { showAboutDialog.value = false },
            icon = getAppIcon(),
            resizable = false,
            title = RESOURCE_BUNDLE.getString("about")
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(8.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(RESOURCE_BUNDLE.getString("tkdupefinder"))
                Image(
                    painter = getAppIcon(),
                    null,
                    modifier = Modifier.requiredSize(96.dp)
                )
                Text(getBundle("version").getString("VERSION"))
            }
        }
    }
}
