package com.thomaskuenneth.tkdupefinder

import androidx.compose.desktop.AppManager
import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.desktop.Window
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.KeyStroke
import androidx.compose.ui.window.Menu
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.MenuItem

fun main() {
    androidx.compose.desktop.AppManager.setMenu(
            MenuBar(Menu("File", MenuItem(
                    name = "Exit",
                    onClick = { AppManager.exit() },
                    shortcut = KeyStroke(Key.X)
            )))
    )
    Window(title = "TKDupeFinder",
            size = IntSize(600, 400)) {
        DesktopMaterialTheme {
            Column() {
                FirstRow()
                SecondRow()
                ThirdRow()
            }
        }
    }
}

@Composable
fun FirstRow() {
    val name = remember { mutableStateOf(TextFieldValue("")) }
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
                onClick = {},
                modifier = Modifier.alignByBaseline(),
        ) {
            Text("Find")
        }
    }
}

@Composable
fun SecondRow() {
    Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
                    .padding(8.dp),
    ) {
        Button(onClick = {}) {
            Text("\u140A")
        }
        MySpacer()
        Button(onClick = {}) {
            Text("\u1405")
        }
        MySpacer()
        Text(text = "1 of 42")
    }
}

@Composable
fun MySpacer() {
    Spacer(modifier = Modifier.width(8.dp))
}

@Composable
fun ThirdRow() {
    val scrollState = rememberScrollState()
    ScrollableColumn(
            scrollState = scrollState,
            modifier = Modifier.fillMaxSize().padding(8.dp),
    ) {
        Text("1")
        Text("2")
        Text("3")
    }
}