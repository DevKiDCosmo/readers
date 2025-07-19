package com.devkid.readers.elements

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun reader(
    file: String,
    chapter: String,
    part: String,
    book: String,
    modifier: Modifier = Modifier
) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Title(
                name = "$book $part"
            )
            val scrollState =
                rememberSaveable(saver = ScrollState.Saver) { ScrollState(0) }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
            ) {
                ChapterTitle(
                    name = chapter
                )

                SmallText(
                    name = read(context = LocalContext.current, fileName = file),
                )

                Row(modifier = Modifier.padding(16.dp)) {
                    CustomButton(
                        name = "Previous",
                        onClick = { /* Handle previous action */ },
                        modifier = Modifier.padding(start = 8.dp)
                    )
                    CustomButton(
                        name = "Next",
                        onClick = { /* Handle next action */ },
                    )
                }
            }
        }
    }
}