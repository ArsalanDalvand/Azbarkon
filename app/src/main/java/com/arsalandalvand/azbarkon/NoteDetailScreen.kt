package com.arsalandalvand.azbarkon

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import saman.zamani.persiandate.PersianDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    viewModel: NoteViewModel,
    noteIdToEdit: Int? = null,
    existingNote: NoteWithTags? = null,
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf(existingNote?.note?.title ?: "") }
    var description by remember { mutableStateOf(existingNote?.note?.description ?: "") }

    val initialTagIds = existingNote?.tags?.map { it.id } ?: emptyList()
    var selectedTagIds by remember { mutableStateOf(initialTagIds) }

    val allTags by viewModel.allTags.collectAsState()

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(if (noteIdToEdit == null || noteIdToEdit == 0) "شعر جدید" else "ویرایش شعر") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "بازگشت")
                        }
                    },
                    actions = {
                        if (existingNote != null) {
                            IconButton(onClick = {
                                viewModel.deleteNote(existingNote.note)
                                onBack()
                            }) {
                                Icon(Icons.Default.Delete, "حذف", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                        IconButton(onClick = {
                            if (title.isNotBlank()) {
                                val now = PersianDate()
                                val dateStr = "${now.shYear}/${String.format("%02d", now.shMonth)}/${String.format("%02d", now.shDay)}"

                                val note = Note(
                                    id = existingNote?.note?.id ?: 0,
                                    title = title,
                                    description = description,
                                    date = dateStr
                                )
                                viewModel.saveNote(note, selectedTagIds)
                                onBack()
                            }
                        }) {
                            Icon(Icons.Default.Save, "ذخیره")
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("نام شاعر یا عنوان") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(
                            "برچسب‌ها",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(8.dp))

                        TagSelector(
                            allTags = allTags,
                            selectedTagIds = selectedTagIds,
                            onTagToggle = { tagId ->
                                selectedTagIds = if (selectedTagIds.contains(tagId)) {
                                    selectedTagIds - tagId
                                } else {
                                    selectedTagIds + tagId
                                }
                            },
                            onAddNewTag = { name, parentId ->
                                viewModel.addNewTag(name, parentId)
                            }
                        )
                    }
                }

                // اصلاح ارور OutlinedTextField
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("متن شعر") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Start),
                    placeholder = { Text("متن شعر را اینجا بنویسید...") }
                )
            }
        }
    }
}