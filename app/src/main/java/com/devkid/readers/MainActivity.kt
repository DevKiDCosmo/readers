package com.devkid.readers

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import com.devkid.readers.ui.theme.ReadersTheme
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import com.devkid.readers.elements.*
import java.io.File
import java.io.InputStream

data class Book(
    val name: String,
    val author: String,
    val part: Int,
    val chapter: Int
)

@Composable
fun bookElement(
    name: String,
    author: String,
    part: Int,
    chapter: Int,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        Text(
            text = name,
            style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .padding(16.dp)
                .weight(1f)

        )
        Text(
            text = author,
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .padding(16.dp)
                .weight(1f)
        )

        Text(
            text = "Part $part, Chapter $chapter",
            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .padding(16.dp)
                .weight(1f)
        )
    }
}

fun saveBooks() {

}

@Composable
fun loadBooks(): List<Unit> {
    // Hier sollte die Logik zum Laden der Bücher implementiert werden
    // Zum Beispiel aus einer Datenbank oder einer Datei
    return listOf(
        bookElement(name = "Buch 1", author = "Autor 1", part = 1, chapter = 1),
    )
}


class MainActivity : ComponentActivity() {
    // In MainActivity:
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                val zipName = getFileNameFromUri(uri) ?: "book"
                val destDir = File(filesDir, "books/$zipName")
                destDir.mkdirs()
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    unzip(inputStream, destDir)
                }
            }
        }
    }

    // Hilfsfunktion: Dateinamen aus Uri extrahieren
    fun getFileNameFromUri(uri: android.net.Uri): String? {
        var name: String? = null
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val idx = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) name = it.getString(idx)
            }
        }
        return name?.substringBeforeLast(".zip")
    }

    // ZIP entpacken
    fun unzip(inputStream: InputStream, destDir: File) {
        java.util.zip.ZipInputStream(inputStream).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                val file = File(destDir, entry.name)
                if (entry.isDirectory) {
                    file.mkdirs()
                } else {
                    file.parentFile?.mkdirs()
                    file.outputStream().use { fos ->
                        zis.copyTo(fos)
                    }
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
    }

    fun openBook(activity: ComponentActivity) {
        val intent = android.content.Intent(android.content.Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(android.content.Intent.CATEGORY_OPENABLE)
            type = "application/zip"
            putExtra(android.content.Intent.EXTRA_TITLE, "Select your BOOK-ZIP")
        }
        activity.startActivityForResult(intent, 1001)
        // Hinweis: Die Dateiauswahl ist asynchron.
        // Die ausgewählte Datei steht erst in onActivityResult zur Verfügung!
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReadersTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    floatingActionButton = {
                        // Plus-Button zum Auswählen einer ZIP-Datei
                        FloatingActionButton(onClick = {
                            openBook(this)
                        }) {
                            Text("+")
                        }
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Title(name = "Reader")

                        loadBooks();
                    }
                }
            }
        }
    }
}
