package com.devkid.readers.elements

import android.content.Context
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.io.BufferedReader

@Composable
fun Title(name: String, modifier: Modifier = Modifier) {
    androidx.compose.material3.Text(
        text = name,
        style = MaterialTheme.typography.titleLarge.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Serif),
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
            .padding(
                start = 16.dp,
                top = 16.dp,
                end = 16.dp,
                bottom = 8.dp
            )
            .then(
                Modifier.padding(8.dp)
            )
    )
}

@Composable
fun ChapterTitle(name: String, modifier: Modifier = Modifier) {
    // "Margin" außen, "Padding" innen, Serif-Schriftart
    androidx.compose.material3.Text(
        text = name,
        style = MaterialTheme.typography.titleMedium.copy(
            fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
        ),
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
            .padding(
                start = 16.dp,
                top = 0.dp,
                end = 16.dp,
                bottom = 0.dp
            )
            .then(
                Modifier.padding(8.dp)
            )
    )
}

@Composable
fun SmallText(name: String, modifier: Modifier = Modifier) {
    androidx.compose.material3.Text(
        text = name,
        style = MaterialTheme.typography.bodySmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Serif),
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
            .padding(
                start = 16.dp,
                top = 0.dp,
                end = 16.dp,
                bottom = 0.dp
            )
            .then(Modifier.padding(8.dp))
    )
}

@Composable
fun CustomButton(name: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier
            .padding(16.dp)
            .then(Modifier.padding(8.dp))
    ) {
        androidx.compose.material3.Text(
            text = name,
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif),
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
fun BookContent(assetPath: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var content by remember { mutableStateOf("") }

    LaunchedEffect(assetPath) {
        content = context.assets.open(assetPath)
            .bufferedReader()
            .use(BufferedReader::readText)
    }

    androidx.compose.material3.Text(
        text = content,
        modifier = modifier
    )
}

fun read(context: Context, fileName: String): String {
    return try {
        context.assets.open(fileName).bufferedReader().use { it.readText() }
    } catch (e: Exception) {
        // Going home - now only alert
        android.widget.Toast.makeText(
            context,
            "Fehler beim Lesen der Datei: ${e.message ?: "Unbekannter Fehler"}",
            android.widget.Toast.LENGTH_LONG
        ).show()
        "Error reading file: ${e.message ?: "Unknown error"}"
    }
}