import androidx.compose.desktop.Window
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

fun main() = Window {
    Box(contentAlignment = Alignment.Center,
    modifier = Modifier.fillMaxSize()) {
        Button(onClick = {
            while (true) {
                println("ohoho...")
            }
        }) {
            Text("Hallo")
        }
    }
}