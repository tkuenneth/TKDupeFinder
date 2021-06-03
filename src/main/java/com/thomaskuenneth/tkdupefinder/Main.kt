/*
 * Copyright 2011 - 2021 Thomas Kuenneth
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
import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.desktop.Window
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.application
import androidx.compose.ui.window.v1.DialogProperties
import androidx.compose.ui.window.v1.Menu
import androidx.compose.ui.window.v1.MenuBar
import androidx.compose.ui.window.v1.MenuItem
import com.github.tkuenneth.nativeparameterstoreaccess.NativeParameterStoreAccess.IS_MACOS
import com.thomaskuenneth.isSystemInDarkTheme
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.awt.Desktop
import java.awt.datatransfer.DataFlavor
import java.awt.dnd.DnDConstants
import java.awt.dnd.DropTarget
import java.awt.dnd.DropTargetDropEvent
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
import java.io.File
import java.util.*
import javax.swing.KeyStroke
import javax.swing.SwingUtilities.invokeLater
import kotlin.concurrent.thread
import kotlin.properties.Delegates.observable

val RESOURCE_BUNDLE: ResourceBundle = ResourceBundle.getBundle("strings")

private val df = TKDupeFinder()

private var isInDarkMode by observable(false) { _, oldValue, newValue ->
    onIsInDarkModeChanged?.let { it(oldValue, newValue) }
}
private var onIsInDarkModeChanged: ((Boolean, Boolean) -> Unit)? = null

private lateinit var showAboutDialog: MutableState<Boolean>

@ExperimentalMaterialApi
@ExperimentalComposeApi
@OptIn(ExperimentalComposeUiApi::class)
fun main() = application {
    GlobalScope.launch {
        while (isActive) {
            val newMode = isSystemInDarkTheme()
            if (isInDarkMode != newMode) {
                isInDarkMode = newMode
            }
            delay(1000)
        }
    }
    Window(title = RESOURCE_BUNDLE.getString("tkdupefinder"),
            icon = appIcon(),
            menuBar = createMenuBar()) {
        TKDupeFinderContent()
    }
}

private fun createMenuBar() = MenuBar().apply {
    if (!IS_MACOS) {
        add(
                Menu(
                        RESOURCE_BUNDLE.getString("file"), MenuItem(
                        name = RESOURCE_BUNDLE.getString("quit"),
                        onClick = {
                            AppManager.exit()
                        },
                        shortcut = KeyStroke.getKeyStroke(
                                KeyEvent.VK_F4, ActionEvent.ALT_MASK
                        )
                )
                )
        )
        add(Menu(RESOURCE_BUNDLE.getString("help"), MenuItem(
                name = RESOURCE_BUNDLE.getString("about"),
                onClick = {
                    showAboutDialog.value = true
                }
        )))
    }
}

@ExperimentalMaterialApi
@Composable
fun TKDupeFinderContent() {
    showAboutDialog = remember { mutableStateOf(false) }
    var colors by remember { mutableStateOf(colors()) }
    onIsInDarkModeChanged = { _, _ ->
        colors = colors()
    }
    val name = remember { mutableStateOf(TextFieldValue(System.getProperty("user.home"))) }
    val currentPos = remember { mutableStateOf(0) }
    val checksums = remember { mutableStateOf<List<String>>(emptyList()) }
    val selected = remember { mutableStateMapOf<Int, Boolean>() }
    val scanning = remember { mutableStateOf(false) }
    val alreadyScanned = remember { mutableStateOf(false) }
    DesktopMaterialTheme(colors = colors) {
        Surface {
            Column() {
                FirstRow(name, currentPos, checksums, selected, scanning, alreadyScanned)
                if (scanning.value) {
                    Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    SecondRow(currentPos, checksums.value.size, selected, alreadyScanned.value)
                    ThirdRow(currentPos.value, checksums.value, selected)
                }
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
                                DataFlavor.javaFileListFlavor
                        ) as List<*>
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
    AboutDialog(showAboutDialog)
}

@Composable
fun FirstRow(
        name: MutableState<TextFieldValue>,
        currentPos: MutableState<Int>,
        checksums: MutableState<List<String>>,
        selected: SnapshotStateMap<Int, Boolean>,
        scanning: MutableState<Boolean>,
        alreadyScanned: MutableState<Boolean>
) {
    val enabled = scanning.value || File(name.value.text).isDirectory
    val function = {
        if (enabled) {
            if (scanning.value) {
                stopScan(currentPos, checksums, scanning)
            } else {
                alreadyScanned.value = true
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
                onValueChange = { name.value = it },
                modifier = Modifier.alignBy(LastBaseline)
                        .weight(1.0f),
                singleLine = true,
                placeholder = {
                    Text(RESOURCE_BUNDLE.getString("base_directory"))
                },
                keyboardOptions = KeyboardOptions(
                        autoCorrect = false,
                        keyboardType = KeyboardType.Uri,
                        capitalization = KeyboardCapitalization.None,
                        imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(onAny = {
                    function()
                })
        )
        MySpacer()
        Button(
                onClick = function,
                modifier = Modifier.alignByBaseline().width(100.dp),
                enabled = enabled
        ) {
            Text(RESOURCE_BUNDLE.getString(if (scanning.value) "cancel" else "find"))
        }
    }
}

@Composable
fun SecondRow(
        currentPos: MutableState<Int>, checksumsSize: Int,
        selected: SnapshotStateMap<Int, Boolean>,
        alreadyScanned: Boolean
) {
    val current = currentPos.value
    Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
                    .padding(8.dp),
    ) {
        Button(
                onClick = {
                    selected.clear()
                    currentPos.value -= 1
                },
                enabled = current > 0
        ) {
            Text("\u140A")
        }
        MySpacer()
        Button(
                onClick = {
                    selected.clear()
                    currentPos.value += 1
                },
                enabled = (current + 1) < checksumsSize
        ) {
            Text("\u1405")
        }
        MySpacer()
        var msg = if (checksumsSize > 0) {
            "${currentPos.value + 1} of $checksumsSize"
        } else RESOURCE_BUNDLE.getString(if (alreadyScanned) "no_duplicates_found" else "click_find")
        if (scanInProgress) {
            msg = "$msg (${RESOURCE_BUNDLE.getString("cancelled")})"
        }
        Text(text = msg)
    }
}

@Composable
fun MySpacer() {
    Spacer(modifier = Modifier.width(8.dp))
}

@ExperimentalMaterialApi
@Composable
fun ThirdRow(currentPos: Int, checksums: List<String>, selected: SnapshotStateMap<Int, Boolean>) {
    val items = if (checksums.isNotEmpty())
        df.getFiles(checksums[currentPos]) else emptyList()
    val selectedFiles = mutableListOf<File>()
    selected.entries.forEach { entry ->
        if (entry.value) selectedFiles.add(items[entry.key])
    }
    val numSelected = selectedFiles.size
    val isConfirmDialogVisible = remember { mutableStateOf(false) }
    Row(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        LazyColumn(
                modifier = Modifier.weight(1.0f)
        )
        {
            itemsIndexed(items) { index, item ->
                val current = selected[index] ?: false
                ListItem(secondaryText = { Text(item.parent) },
                        modifier = Modifier.toggleable(
                                onValueChange = {
                                    selected[index] = !current
                                },
                                value = current)
                                .pointerInput(Unit) {
                                    forEachGesture {
                                        awaitPointerEventScope {
                                            awaitPointerEvent().mouseEvent?.run {
                                                if ((clickCount == 2) and (button == MouseEvent.BUTTON1)) {
                                                    selected.clear()
                                                    Desktop.getDesktop().open(items[index])
                                                }
                                            }
                                        }
                                    }
                                }
                                .background(
                                        if (current)
                                            Color.LightGray else Color.Transparent
                                ),
                        text = { Text(item.name) })
            }
        }
        MySpacer()
        Column() {
            Button(
                    onClick = {
                        selectedFiles.forEach {
                            Desktop.getDesktop().open(it)
                        }
                    },
                    modifier = Modifier.width(100.dp),
                    enabled = numSelected > 0
            ) {
                Text(RESOURCE_BUNDLE.getString("show"))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                    onClick = {
                        isConfirmDialogVisible.value = true
                    },
                    modifier = Modifier.width(100.dp),
                    enabled = numSelected > 0 && numSelected < items.size
            ) {
                Text(RESOURCE_BUNDLE.getString("delete"))
            }
        }
    }
    ConfirmDeleteDialog(isConfirmDialogVisible, selectedFiles, checksums, currentPos, selected)
}

@ExperimentalMaterialApi
@Composable
fun ConfirmDeleteDialog(
        isConfirmDialogVisible: MutableState<Boolean>,
        selectedFiles: MutableList<File>,
        checksums: List<String>,
        currentPos: Int,
        selected: SnapshotStateMap<Int, Boolean>
) {
    if (isConfirmDialogVisible.value) {
        AlertDialog(onDismissRequest = {
            isConfirmDialogVisible.value = false
        },
                properties = DialogProperties(
                        title = RESOURCE_BUNDLE.getString("confirm_deletion"),
                        icon = appIcon(),
                        size = IntSize(400, 150),
                        resizable = false
                ),
                text = {
                    Column(modifier = Modifier.fillMaxHeight()) {
                        Text(String.format(RESOURCE_BUNDLE.getString("confirm_delete"),
                                selectedFiles.size))
                    }
                },
                dismissButton = {
                    Button(onClick = {
                        isConfirmDialogVisible.value = false
                    }) {
                        Text(RESOURCE_BUNDLE.getString("cancel"))
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        isConfirmDialogVisible.value = false
                        selectedFiles.forEach {
                            df.deleteFile(checksums[currentPos], it)
                        }
                        selected.clear()
                    }) {
                        Text(RESOURCE_BUNDLE.getString("delete"))
                    }
                })
    }
}

private var worker: Thread? = null
private var scanInProgress = false
private fun startScan(
        baseDir: String, currentPos: MutableState<Int>,
        checksums: MutableState<List<String>>,
        scanning: MutableState<Boolean>
) {
    worker = thread {
        df.clear()
        scanInProgress = true
        df.scanDir(baseDir, true)
        scanInProgress = false
        invokeLater {
            stopScan(currentPos, checksums, scanning)
        }
    }
}

private fun stopScan(
        currentPos: MutableState<Int>,
        checksums: MutableState<List<String>>,
        scanning: MutableState<Boolean>
) {
    if (worker?.isAlive == true) {
        worker?.stop()
    }
    df.removeSingles()
    currentPos.value = 0
    checksums.value = df.checksums.toList()
    scanning.value = false
}

private fun colors(): Colors = if (isInDarkMode) {
    darkColors()
} else {
    lightColors()
}