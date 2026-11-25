package com.arsalandalvand.azbarkon

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(
    viewModel: NoteViewModel,
    onAddNote: () -> Unit,
    onNoteClick: (NoteWithTags) -> Unit,
    onBack: () -> Unit
) {
    val notes by viewModel.filteredNotes.collectAsState()
    val allTags by viewModel.allTags.collectAsState()
    val selectedFilterIds by viewModel.selectedFilterTagIds.collectAsState() // استفاده از متغیر عمومی شده

    var isGridView by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                if (showSearch) {
                    // استفاده از کامپوننت جستجوی اختصاصی
                    AppSearchBar(
                        query = searchQuery,
                        onQueryChange = {
                            searchQuery = it
                            viewModel.onSearchQueryChanged(it)
                        },
                        onClose = {
                            showSearch = false
                            searchQuery = ""
                            viewModel.onSearchQueryChanged("")
                        }
                    )
                } else {
                    TopAppBar(
                        title = { Text("گنجینه اشعار") },
                        navigationIcon = {
                            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "بازگشت") }
                        },
                        actions = {
                            IconButton(onClick = { showSearch = true }) {
                                Icon(Icons.Default.Search, "جستجو")
                            }
                            IconButton(onClick = { showFilterSheet = true }) {
                                val hasFilter = selectedFilterIds.isNotEmpty()
                                Icon(
                                    Icons.Default.FilterList,
                                    "فیلتر",
                                    tint = if (hasFilter) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            IconButton(onClick = { isGridView = !isGridView }) {
                                Icon(if (isGridView) Icons.Default.List else Icons.Default.GridView, "تغییر نما")
                            }
                        }
                    )
                }
            },
            floatingActionButton = {
                FloatingActionButton(onClick = onAddNote) {
                    Icon(Icons.Default.Add, "شعر جدید")
                }
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {

                if (selectedFilterIds.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = true,
                                onClick = { viewModel.clearFilters() },
                                label = { Text("حذف همه") },
                                leadingIcon = { Icon(Icons.Default.Clear, null, Modifier.size(16.dp)) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer
                                )
                            )
                        }

                        val activeTags = allTags.filter { it.id in selectedFilterIds }
                        items(activeTags) { tag ->
                            FilterChip(
                                selected = true,
                                onClick = { viewModel.onFilterTagToggle(tag.id) },
                                label = { Text(tag.title) },
                                trailingIcon = { Icon(Icons.Default.Close, null, Modifier.size(14.dp)) }
                            )
                        }
                    }
                    Divider()
                }

                Box(modifier = Modifier.weight(1f)) {
                    if (notes.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Description, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.surfaceVariant)
                            Spacer(Modifier.height(16.dp))
                            Text(
                                if (searchQuery.isNotEmpty() || selectedFilterIds.isNotEmpty()) "موردی با این مشخصات یافت نشد" else "هنوز شعری ثبت نکرده‌اید",
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    } else {
                        if (isGridView) {
                            LazyVerticalStaggeredGrid(
                                columns = StaggeredGridCells.Fixed(2),
                                contentPadding = PaddingValues(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalItemSpacing = 8.dp
                            ) {
                                items(notes) { noteWithTags ->
                                    NoteCard(noteWithTags, onNoteClick)
                                }
                            }
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(notes) { noteWithTags ->
                                    NoteCard(noteWithTags, onNoteClick)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showFilterSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFilterSheet = false }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        "فیلتر بر اساس تگ‌ها",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    TagSelector(
                        allTags = allTags,
                        selectedTagIds = selectedFilterIds.toList(),
                        onTagToggle = { tagId ->
                            viewModel.onFilterTagToggle(tagId)
                        },
                        onAddNewTag = { _, _ -> }
                    )

                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

// کامپوننت جستجوی اختصاصی برای رفع ارور SearchBar
@Composable
fun AppSearchBar(query: String, onQueryChange: (String) -> Unit, onClose: () -> Unit) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("جستجو در شعرها...") },
        modifier = Modifier.fillMaxWidth(),
        leadingIcon = { Icon(Icons.Default.Search, null) },
        trailingIcon = { IconButton(onClick = onClose) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "بستن") } },
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
fun NoteCard(item: NoteWithTags, onClick: (NoteWithTags) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(item) },
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = item.note.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.note.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Start
            )

            if (item.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    // اصلاح ارور Alignment: تراز عمودی باید اینجا روی LazyRow تنظیم شود
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(item.tags.take(3)) { tag ->
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = tag.title,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                    if (item.tags.size > 3) {
                        item {
                            // حذف Modifier.align() که باعث ارور شده بود
                            Text(
                                text = "+${item.tags.size - 3}",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.note.date,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}