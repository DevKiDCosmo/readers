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
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
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
import org.json.JSONArray
import java.io.File
import java.io.InputStream
import org.json.JSONObject
import androidx.compose.ui.text.style.TextAlign
import java.util.zip.ZipInputStream
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset


data class Book(
    val name: String,
    val author: String,
    val description: String = "",
    val part: Int,
    val chapter: Int,
    val uri: Uri,
    val coverImageUri: Uri? = null,
    val language: String? = null
)

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterialApi::class)
@Composable
fun bookElement(
    name: String,
    author: String,
    part: Int,
    chapter: Int,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dismissState = rememberDismissState(
        confirmStateChange = { dismissValue ->
            if (dismissValue == DismissValue.DismissedToEnd) {
                onDelete()
                true
            } else {
                false
            }
        }
    )

    SwipeToDismiss(
        state = dismissState,
        directions = setOf(DismissDirection.StartToEnd),
        background = {
            val shape = RoundedCornerShape(16.dp)
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp) // gleich wie Button
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min) // an Button-Höhe anpassen
                    .clip(shape)
                    .background(Color.Red),
                contentAlignment = Alignment.CenterStart
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.White,
                    modifier = Modifier
                        .padding(start = 20.dp)
                        .size(24.dp)
                )
            }
        },
        dismissContent = {
            Button(
                modifier = modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                onClick = onClick,
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(2f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Serif),
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text(
                            text = "by $author",
                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Serif)
                        )
                    }

                    Text(
                        text = "Pt. $part, Ch. $chapter",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                    )

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Open",
                        modifier = Modifier
                            .size(20.dp)
                            .padding(start = 4.dp)
                    )
                }
            }
        }
    )
}


fun saveBooks(context: Context, books: List<Book>) {
    val jsonArray = JSONArray()
    books.forEach { book ->
        val obj = JSONObject()
        obj.put("name", book.name)
        obj.put("author", book.author)
        obj.put("description", book.description)
        obj.put("part", book.part)
        obj.put("chapter", book.chapter)
        obj.put("coverImageUri", book.coverImageUri?.toString())
        obj.put("language", book.language)
        obj.put("uri", book.uri.toString())
        jsonArray.put(obj)
    }
    val file = File(context.filesDir, "books.json")
    file.writeText(jsonArray.toString())
}


fun loadBooks(context: Context): List<Book> {
    val file = File(context.filesDir, "books.json")
    if (!file.exists()) return emptyList()
    val jsonArray = JSONArray(file.readText())
    return List(jsonArray.length()) { i ->
        val obj = jsonArray.getJSONObject(i)
        Book(
            name = obj.optString("name"),
            author = obj.optString("author"),
            description = obj.optString("description"),
            part = obj.optInt("part"),
            chapter = obj.optInt("chapter"),
            coverImageUri = obj.optString("coverImageUri").takeIf { it.isNotBlank() }?.toUri(),
            language = obj.optString("language").takeIf { it.isNotBlank() },
            uri = obj.optString("uri").takeIf { it.isNotBlank() }?.toUri() ?: Uri.EMPTY
        )
    }
}


fun createBooks(
    name: String,
    author: String,
    description: String,
    uri: Uri,
    coverImageUri: Uri?,
    language: String?
): Book {
    return Book(
        name = name,
        author = author,
        description = description,
        part = 1,
        chapter = 1,
        coverImageUri = coverImageUri,
        language = language,
        uri = uri
    )
}

fun checkIfBookExists(book: List<Book>): List<String> {
    // Check if the loaded books still exist in the file system
    val missingBooks = mutableListOf<String>()
    for (i in book.indices) {
        val bookUri = book[i].uri
        if (!File(bookUri.path ?: "").exists()) {
            // If the file does not exist, return the URI to remove it from the list
            missingBooks.add(book[i].uri.toString())
        }
    }
    return missingBooks
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
        openDocumentLauncher.launch(arrayOf("application/zip"))
    }

    fun readBookManifest(destDir: File): Int {
        val jsonFiles = destDir.listFiles()?.filter { it.extension == "json" } ?: return 1
        val informationFile = if (jsonFiles.size == 1) {
            jsonFiles.first()
        } else {
            jsonFiles.firstOrNull { it.name == "manifest.json" } ?: return 1
        }

        if (informationFile.exists()) {
            val inputStream = informationFile.inputStream()
            val jsonContent = inputStream.bufferedReader().use { it.readText() }
            if (jsonContent.isBlank()) {
                Toast.makeText(this, "No JSON content found", Toast.LENGTH_SHORT).show()
                return 1
            }
            val jsonObj = JSONObject(jsonContent)
            val name = jsonObj.optString("title").ifBlank { "Unknown Book" }
            val author = jsonObj.optString("author").ifBlank { "Unknown Author" }
            val description = jsonObj.optString("description").ifBlank { "" }
            val coverImageUri =
                jsonObj.optString("coverImageUri").takeIf { it.isNotBlank() }?.toUri()
            val language = jsonObj.optString("language").takeIf { it.isNotBlank() }

            val book =
                createBooks(name, author, description, destDir.toUri(), coverImageUri, language)
            val books = loadBooks(this).toMutableList()
            books.add(book)
            saveBooks(this, books)
            return 0
        }

        return 1;
    }

    @Composable
    fun BookListScreen(
        books: List<Book>, modifier: Modifier = Modifier
    ) {
        LazyColumn(
            modifier = modifier
        ) {
            if (books.isEmpty()) {
                item {
                    Text(
                        text = "No books downloaded yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .padding(
                                start = 16.dp, top = 0.dp, end = 16.dp, bottom = 0.dp
                            )
                            .then(Modifier.padding(8.dp))
                    )
                    Text(
                        text = "Click the + button to add a book.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .padding(
                                start = 16.dp, top = 0.dp, end = 16.dp, bottom = 0.dp
                            )
                            .then(Modifier.padding(8.dp))
                    )
                }
            } else {
                items(books, key = { it.uri }) { book ->
                    bookElement(
                        name = book.name,
                        author = book.author,
                        part = book.part,
                        chapter = book.chapter,
                        onClick = {
                            Toast.makeText(
                                this@MainActivity,
                                "Opening ${book.name}",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onDelete = {
                            val updatedBooks = booksState.value.filterNot { it.uri == book.uri }
                            booksState.value = updatedBooks
                            saveBooks(this@MainActivity, updatedBooks)

                            // Delete unzipped book directory
                            val bookDir = File(
                                getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS),
                                "books/${book.name}"
                            )
                            if (bookDir.exists()) {
                                bookDir.deleteRecursively()
                            }

                            Toast.makeText(
                                this@MainActivity,
                                "Deleted ${book.name}",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        // Want to have a smooth animation when the book is removed
                    )
                }
            }
        }
    }

    private val booksState = mutableStateOf<List<Book>>(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        booksState.value = loadBooks(this)

        openDocumentLauncher = registerForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri ->
            uri?.let {
                val zipName = getFileNameFromUri(it) ?: "book"
                var destDir = File(
                    getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS),
                    "books/$zipName"
                )
                var counter = 1
                while (destDir.exists()) {
                    destDir = File(
                        getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS),
                        "books/${zipName}_$counter"
                    )
                    counter++
                }
                destDir.mkdirs()
                contentResolver.openInputStream(it)?.use { inputStream ->
                    unzip(inputStream, destDir)
                }
                readBookManifest(destDir).let { result ->
                    if (result == 0) {
                        booksState.value = loadBooks(this) // <-- Liste neu laden!

                        Toast.makeText(this, "Book added successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Failed to read book manifest", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }

        checkIfBookExists(booksState.value).let { missingBooks ->
            if (missingBooks.isNotEmpty()) {
                val updatedBooks = booksState.value.filterNot { book ->
                    missingBooks.contains(book.uri.toString())
                }
                booksState.value = updatedBooks
                saveBooks(this, updatedBooks)
                Toast.makeText(
                    this,
                    "Removed missing books: ${missingBooks.joinToString(", ")}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        enableEdgeToEdge()
        setContent {
            ReadersTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(), floatingActionButton = {
                        // Plus-Button zum Auswählen einer ZIP-Datei
                        FloatingActionButton(onClick = {
                            openBook()
                        }) {
                            Text("+")
                        }
                    }) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                    ) {
                        Title(name = "Readers")
                        BookListScreen(
                            books = booksState.value,
                            modifier = Modifier
                                .padding(innerPadding)
                                .then(Modifier.fillMaxSize())
                        )
                    }
                }
            }
        }
    }
}
