package com.devkid.readers

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import com.devkid.readers.ui.theme.ReadersTheme
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.devkid.readers.elements.*
import java.io.File
import java.io.InputStream
import org.json.JSONObject
import java.util.zip.ZipInputStream

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
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .padding(16.dp)
                .weight(1f)

        )
        Text(
            text = author,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .padding(16.dp)
                .weight(1f)
        )

        Text(
            text = "Part $part, Chapter $chapter",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .padding(16.dp)
                .weight(1f)
        )
    }
}


fun saveBooks(context: Context, books: List<Book>) {
}


@Composable
fun loadBooks(): List<Unit> {
    // Hier sollte die Logik zum Laden der Bücher implementiert werden
    // Zum Beispiel aus einer Datenbank oder einer Datei
    return listOf(
        bookElement(name = "Buch 1", author = "Autor 1", part = 1, chapter = 1),
    )
}

fun createBooks(name: String, author: String, part: Int, chapter: Int): Book {
    return Book(name = name, author = author, part = part, chapter = chapter)
}


class MainActivity : ComponentActivity() {

    // Hilfsfunktion: Dateinamen aus Uri extrahieren
    fun getFileNameFromUri(uri: Uri): String? {
        var name: String? = null
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val idx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) name = it.getString(idx)
            }
        }
        // Fallback: Dateiname aus dem Pfad extrahieren, falls DISPLAY_NAME nicht verfügbar ist
        val fileName = name ?: uri.path?.split("/")?.lastOrNull()
        return fileName?.substringBeforeLast(".zip")
    }

    // ZIP entpacken
    fun unzip(inputStream: InputStream, destDir: File) {
        ZipInputStream(inputStream).use { zis ->
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

    private lateinit var openDocumentLauncher: ActivityResultLauncher<Array<String>>

    fun openBook() {
        // Jetzt sicher nutzbar!
        openDocumentLauncher.launch(arrayOf("application/zip"))
    }

    fun readBookManifest(destDir: File): Book? {
        // Search for a manifest file or a json that has the same name as the zip file
        val manifestFile = File(destDir, "manifest.json")
        val informationFile =
            File(destDir, (getFileNameFromUri(destDir.toUri()) ?: "manifest") + ".json")

        // Check if one of the files exists
        if (manifestFile.exists()) {
            Toast.makeText(this, "Manifest file found", Toast.LENGTH_SHORT).show()
            return Book(name = "Example Book", author = "Author", part = 1, chapter = 1)
        } else if (informationFile.exists()) {
            // JSON-Inhalt aus der Datei extrahieren
            val inputStream = informationFile.inputStream()
            val jsonContent = inputStream.bufferedReader().use { it.readText() }
            if (jsonContent.isBlank()) {
                Toast.makeText(this, "No JSON content found", Toast.LENGTH_SHORT).show()
                return null
            }
            val jsonObj = JSONObject(jsonContent)
            val name = jsonObj.optString("title").ifBlank { "Unknown Book" }
            val author = jsonObj.optString("author").ifBlank { "Unknown Author" }
            return Book(name = name, author = author, part = 1, chapter = 1)
        }

        return null;
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        openDocumentLauncher = registerForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri ->
            uri?.let {
                val zipName = getFileNameFromUri(it) ?: "book"
                val destDir = File(
                    getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS),
                    "books/$zipName"
                )
                destDir.mkdirs()
                contentResolver.openInputStream(it)?.use { inputStream ->
                    unzip(inputStream, destDir)
                }
                readBookManifest(destDir)?.let { book ->
                    Toast.makeText(this, "Book loaded: ${book.name}", Toast.LENGTH_SHORT).show()
                } ?: run {
                    Toast.makeText(this, "Failed to read book manifest", Toast.LENGTH_SHORT).show()
                }
            }
        }

        enableEdgeToEdge()
        setContent {
            ReadersTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    floatingActionButton = {
                        // Plus-Button zum Auswählen einer ZIP-Datei
                        FloatingActionButton(onClick = {
                            openBook()
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
                        Title(name = "Readers")

                        loadBooks();
                    }
                }
            }
        }
    }
}
