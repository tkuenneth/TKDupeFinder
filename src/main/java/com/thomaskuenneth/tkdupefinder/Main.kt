/*
 * Copyright 2011 - 2020 Thomas Kuenneth
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

import androidx.compose.desktop.AppManager
import androidx.compose.desktop.AppWindow
import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumnForIndexed
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.KeyStroke
import androidx.compose.ui.window.Menu
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.MenuItem
import java.awt.Desktop
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.dnd.DnDConstants
import java.awt.dnd.DropTarget
import java.awt.dnd.DropTargetDropEvent
import java.io.File
import javax.swing.SwingUtilities.invokeLater
import kotlin.concurrent.thread
import kotlin.properties.Delegates.observable

private val df = TKDupeFinder()

private var isInDarkMode: Boolean by observable(false) { _, oldValue, newValue ->
    onIsInDarkModeChanged?.let { it(oldValue, newValue) }
}
private var onIsInDarkModeChanged: ((Boolean, Boolean) -> Unit)? = null

@ExperimentalComposeApi
fun main() {
    invokeLater {
        configureMenuBar()
        AppWindow(title = "TKDupeFinder",
                size = IntSize(600, 400)).show {
            TKDupeFinderContent()
        }
    }
}

private fun configureMenuBar() {
    val menuBar = MenuBar()
    if (!System.getProperty("os.name", "").contains("mac os x", true)) {
        menuBar.add(Menu("File", MenuItem(
                name = "Quit",
                onClick = {
                    AppManager.exit()
                },
                shortcut = KeyStroke(Key.X)
        )))
    }
    menuBar.add(Menu("Appearance", MenuItem(
            name = "Toggle Colors",
            onClick = {
                isInDarkMode = !isInDarkMode
            },
            shortcut = KeyStroke(Key.T)
    )))
    AppManager.setMenu(menuBar)
}

private fun colors(): Colors = if (isInDarkMode) {
    darkColors()
} else {
    lightColors()
}

@Composable
fun TKDupeFinderContent() {
    var colors by remember { mutableStateOf(colors()) }
    onIsInDarkModeChanged = { _, _ ->
        colors = colors()
    }
    val name = remember { mutableStateOf(TextFieldValue(System.getProperty("user.home"))) }
    val currentPos = remember { mutableStateOf(0) }
    val checksums = remember { mutableStateOf<List<String>>(emptyList()) }
    val selected = remember { mutableStateMapOf<Int, Boolean>() }
    val scanning = remember { mutableStateOf(false) }
    DesktopMaterialTheme(colors = colors) {
        Column() {
            FirstRow(name, currentPos, checksums, selected, scanning)
            if (scanning.value) {
                Box(modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                SecondRow(currentPos, checksums.value.size, selected)
                ThirdRow(currentPos.value, checksums.value, selected)
            }
        }
    }
    val target = object : DropTarget() {
        @Synchronized
        override fun drop(evt: DropTargetDropEvent) {
            try {
                evt.acceptDrop(DnDConstants.ACTION_REFERENCE)
                val droppedFiles = evt
                        .transferable.getTransferData(
                                DataFlavor.javaFileListFlavor) as List<*>
                droppedFiles.first()?.let {
                    name.value = TextFieldValue((it as File).absolutePath)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }
    val window = AppManager.windows.first().window
    window.contentPane.dropTarget = target
    window.iconImage = Toolkit.getDefaultToolkit().getImage("app_icon.png")
}

@Composable
fun FirstRow(name: MutableState<TextFieldValue>,
             currentPos: MutableState<Int>,
             checksums: MutableState<List<String>>,
             selected: SnapshotStateMap<Int, Boolean>,
             scanning: MutableState<Boolean>) {
    val enabled = scanning.value || File(name.value.text).isDirectory
    val function = {
        if (enabled) {
            if (scanning.value) {
                stopScan(currentPos, checksums, scanning)
            } else {
                scanning.value = true
                selected.clear()
                checksums.value = emptyList()
                startScan(name.value.text, currentPos, checksums, scanning)
            }
        }
    }
    Row(
            modifier = Modifier.fillMaxWidth()
                    .padding(8.dp)
    ) {
        TextField(
                value = name.value,
                singleLine = true,
                keyboardOptions = KeyboardOptions(autoCorrect = false, imeAction = ImeAction.Search),
                onImeActionPerformed = { _, _ ->
                    function()
                },
                placeholder = {
                    Text("Base directory")
                },
                modifier = Modifier.alignBy(LastBaseline)
                        .weight(1.0f),
                onValueChange = {
                    name.value = it
                },
        )
        MySpacer()
        Button(
                onClick = function,
                modifier = Modifier.alignByBaseline().width(100.dp),
                enabled = enabled
        ) {
            Text(if (scanning.value) "Cancel" else "Find")
        }
    }
}

@Composable
fun SecondRow(currentPos: MutableState<Int>, checksumsSize: Int,
              selected: SnapshotStateMap<Int, Boolean>) {
    val current = currentPos.value
    Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
                    .padding(8.dp),
    ) {
        Button(onClick = {
            selected.clear()
            currentPos.value -= 1
        },
                enabled = current > 0) {
            Text("\u140A")
        }
        MySpacer()
        Button(onClick = {
            selected.clear()
            currentPos.value += 1
        },
                enabled = (current + 1) < checksumsSize) {
            Text("\u1405")
        }
        MySpacer()
        Text(text = if (checksumsSize > 0) {
            "${currentPos.value + 1} of $checksumsSize"
        } else "No duplicates found")
    }
}

@Composable
fun MySpacer() {
    Spacer(modifier = Modifier.width(8.dp))
}

@Composable
fun ThirdRow(currentPos: Int, checksums: List<String>, selected: SnapshotStateMap<Int, Boolean>) {
    val items = if (checksums.isNotEmpty())
        df.getFiles(checksums[currentPos]) else emptyList()
    val selectedFiles = mutableListOf<File>()
    selected.entries.forEachIndexed { index, entry ->
        if (entry.value) selectedFiles.add(items[index])
    }
    val numSelected = selectedFiles.size
    var isConfirmDialogVisible by remember { mutableStateOf(false) }
    Row(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        LazyColumnForIndexed(items,
                modifier = Modifier.weight(1.0f),
                itemContent = { index, item ->
                    val current = selected[index] ?: false
                    ListItem(secondaryText = { Text(item.parent) },
                            modifier = Modifier.toggleable(onValueChange = {
                                selected[index] = !current
                            },
                                    value = current)
                                    .background(if (current)
                                        Color.LightGray else Color.Transparent),
                            text = { Text(item.name) })
                })
        MySpacer()
        Column() {
            Button(onClick = {
                selectedFiles.forEach {
                    Desktop.getDesktop().open(it)
                }
            },
                    modifier = Modifier.width(100.dp),
                    enabled = numSelected > 0) {
                Text("Show")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                isConfirmDialogVisible = true
            },
                    modifier = Modifier.width(100.dp),
                    enabled = numSelected > 0 && numSelected < items.size) {
                Text("Delete")
            }
            if (isConfirmDialogVisible) {
                AlertDialog(onDismissRequest = {
                    isConfirmDialogVisible = false
                },
                        title = {
                            Text("Confirm deletion of...")
                        },
                        text = {
                            ScrollableColumn {
                                val sb = StringBuilder()
                                selectedFiles.forEach {
                                    sb.append(checksums[currentPos], it.name).appendLine()
                                }
                                Text(sb.toString())
                            }
                        },
                        confirmButton = {
                            Button(onClick = {
                                isConfirmDialogVisible = false
                                selectedFiles.forEach {
                                    df.deleteFile(checksums[currentPos], it)
                                }
                                selected.clear()
                            }) {
                                Text("Delete")
                            }
                        })
            }
        }
    }
}

private var worker: Thread? = null
private fun startScan(baseDir: String, currentPos: MutableState<Int>,
                      checksums: MutableState<List<String>>,
                      scanning: MutableState<Boolean>) {
    worker = thread {
        df.clear()
        df.scanDir(baseDir, true)
        df.removeSingles()
        invokeLater {
            stopScan(currentPos, checksums, scanning)
        }
    }
}

private fun stopScan(currentPos: MutableState<Int>,
                     checksums: MutableState<List<String>>,
                     scanning: MutableState<Boolean>) {
    if (worker?.isAlive == true) {
        worker?.stop()
    }
    currentPos.value = 0
    checksums.value = df.checksums.toList()
    scanning.value = false
}