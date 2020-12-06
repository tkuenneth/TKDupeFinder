package com.thomaskuenneth.tkdupefinder

import androidx.compose.desktop.AppManager
import androidx.compose.desktop.AppWindow
import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumnForIndexed
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.KeyStroke
import androidx.compose.ui.window.Menu
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.MenuItem
import java.awt.datatransfer.DataFlavor
import java.awt.dnd.DnDConstants
import java.awt.dnd.DropTarget
import java.awt.dnd.DropTargetDropEvent
import java.io.File
import javax.swing.SwingUtilities.invokeLater
import kotlin.properties.Delegates.observable

private val df = TKDupeFinder()

// toggle in menubar
private var isInDarkMode: Boolean by observable(true /* isSystemInDarkTheme() */) { _, oldValue, newValue ->
    onIsInDarkModeChanged?.let { it(oldValue, newValue) }
}
private var onIsInDarkModeChanged: ((Boolean, Boolean) -> Unit)? = null

@ExperimentalComposeApi
fun main() {
    invokeLater {
        AppManager.setMenu(
                // currently the menu item name is not updated upon changes
                MenuBar(Menu("Appearance", MenuItem(
                        name = if (isInDarkMode) "Light Mode" else "Dark Mode",
                        onClick = {
                            isInDarkMode = !isInDarkMode
                        },
                        shortcut = KeyStroke(Key.L)
                )))
        )
        AppWindow(title = "TKDupeFinder",
                size = IntSize(600, 400)).show {
            TKDupeFinderContent()
        }
    }
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
    DesktopMaterialTheme(colors = colors) {
        Column() {
            FirstRow(name, currentPos, checksums)
            SecondRow(currentPos, checksums.value.size, selected)
            ThirdRow(currentPos.value, checksums.value, selected)
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
    AppManager.windows.first().window.contentPane.dropTarget = target
}

@Composable
fun FirstRow(name: MutableState<TextFieldValue>,
             currentPos: MutableState<Int>,
             checksums: MutableState<List<String>>) {
    Row(
            modifier = Modifier.fillMaxWidth()
                    .padding(8.dp),
    ) {
        TextField(
                value = name.value,
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
                onClick = {
                    df.clear()
                    df.scanDir(name.value.text, true)
                    df.removeSingles()
                    currentPos.value = 0
                    checksums.value = df.checksums.toList()
                },
                modifier = Modifier.alignByBaseline(),
                enabled = File(name.value.text).isDirectory
        ) {
            Text("Find")
        }
    }
}

@Composable
fun SecondRow(currentPos: MutableState<Int>, checksumsSize: Int,
              selected: SnapshotStateMap<Int, Boolean>) {
    val current = currentPos.value
    selected.clear()
    Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
                    .padding(8.dp),
    ) {
        Button(onClick = {
            currentPos.value -= 1
        },
                enabled = current > 0) {
            Text("\u140A")
        }
        MySpacer()
        Button(onClick = {
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
    LazyColumnForIndexed(items,
            modifier = Modifier.fillMaxSize().padding(8.dp),
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
}