package com.thomaskuenneth

import androidx.compose.desktop.Window
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

fun main() = Window {
    Box(contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()) {
        Button(onClick = {
            GlobalScope.launch {
                while (isActive) {
                // while (true) {
                    println("ohoho...")
                }
            }
        }) {
            Text("Hallo")
        }
    }
}