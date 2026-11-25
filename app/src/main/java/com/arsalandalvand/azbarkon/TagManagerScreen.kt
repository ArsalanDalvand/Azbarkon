package com.arsalandalvand.azbarkon

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.arsalandalvand.azbarkon.ui.theme.VazirmatnFontFamily // فرض بر وجود فونت

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TagManagerScreen(
    viewModel: TagViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    // وضعیت دیالوگ‌ها
    var showAddDialog by remember { mutableStateOf(false) }
    var tagToEdit by remember { mutableStateOf<Tag?>(null) } // اگر پر باشد یعنی دیالوگ ویرایش/حذف باز شود
    var newTagName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("مدیریت دسته‌بندی‌ها", fontFamily = VazirmatnFontFamily) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, "بازگشت")
                    }
                },
                actions = {
                    if (state.currentParentId != null) {
                        IconButton(onClick = { viewModel.navigateToRoot() }) {
                            Icon(Icons.Default.Home, "خانه")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { newTagName = ""; showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, "افزودن")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            // --- نوار مسیر (Breadcrumbs) ---
            BreadcrumbBar(
                breadcrumbs = state.breadcrumbs,
                onRootClick = { viewModel.navigateToRoot() },
                onNodeClick = { viewModel.navigateTo(it.id) },
                onBackClick = { viewModel.navigateUp() },
                canGoBack = state.currentParentId != null
            )

            Divider(color = MaterialTheme.colorScheme.surfaceVariant)

            // --- لیست تگ‌ها ---
            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (state.currentTags.isEmpty()) {
                EmptyFolderView()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp) // فضا برای FAB
                ) {
                    items(state.currentTags, key = { it.id }) { tag ->
                        TagListItem(
                            tag = tag,
                            onClick = { viewModel.navigateTo(tag.id) },
                            onLongClick = { tagToEdit = tag } // باز کردن منوی ادیت با نگه داشتن
                        )
                    }
                }
            }
        }
    }

    // --- دیالوگ افزودن ---
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("ساخت دسته جدید") },
            text = {
                OutlinedTextField(
                    value = newTagName,
                    onValueChange = { newTagName = it },
                    label = { Text("عنوان") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.addTag(newTagName)
                    showAddDialog = false
                }) { Text("افزودن") }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("لغو") } }
        )
    }

    // --- دیالوگ ویرایش/حذف ---
    if (tagToEdit != null) {
        EditTagDialog(
            tag = tagToEdit!!,
            onDismiss = { tagToEdit = null },
            onEdit = { tag, newName ->
                viewModel.updateTag(tag, newName)
                tagToEdit = null
            },
            onDelete = { tag ->
                viewModel.deleteTag(tag)
                tagToEdit = null
            }
        )
    }
}

// --- کامپوننت‌های UI ---

@Composable
fun BreadcrumbBar(
    breadcrumbs: List<Tag>,
    onRootClick: () -> Unit,
    onNodeClick: (Tag) -> Unit,
    onBackClick: () -> Unit,
    canGoBack: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (canGoBack) {
            IconButton(onClick = onBackClick, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.ArrowUpward, "بالا", modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        LazyRow(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            item {
                Text(
                    "خانه",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (breadcrumbs.isEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (breadcrumbs.isEmpty()) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onRootClick() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            items(breadcrumbs) { tag ->
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack, // فلش جدا کننده
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Text(
                    tag.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (tag == breadcrumbs.last()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (tag == breadcrumbs.last()) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onNodeClick(tag) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TagListItem(
    tag: Tag,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // آیکون
            Icon(
                imageVector = Icons.Default.Folder, // همه تگ‌ها قابلیت داشتن زیرمجموعه دارند پس آیکون فولدر مناسب است
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = tag.title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )

            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "ورود",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun EmptyFolderView() {
    Column(
        modifier = Modifier.fillMaxSize().padding(bottom = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.FolderOpen,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "این پوشه خالی است",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            "با دکمه + یک مورد جدید اضافه کنید",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class) // <--- این خط مشکل را حل می‌کند
@Composable
fun EditTagDialog(
    tag: Tag,
    onDismiss: () -> Unit,
    onEdit: (Tag, String) -> Unit,
    onDelete: (Tag) -> Unit
) {
    var isEditingName by remember { mutableStateOf(false) }
    var tempName by remember { mutableStateOf(tag.title) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            icon = { Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("حذف دسته") },
            text = { Text("آیا از حذف '${tag.title}' و تمام زیرمجموعه‌های آن اطمینان دارید؟ این عمل غیرقابل بازگشت است.") },
            confirmButton = {
                Button(
                    onClick = { onDelete(tag) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("حذف کن") }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("لغو") } }
        )
    } else if (isEditingName) {
        AlertDialog(
            onDismissRequest = { isEditingName = false },
            title = { Text("تغییر نام") },
            text = { OutlinedTextField(value = tempName, onValueChange = { tempName = it }, singleLine = true) },
            confirmButton = {
                Button(onClick = { onEdit(tag, tempName) }) { Text("ذخیره") }
            },
            dismissButton = { TextButton(onClick = { isEditingName = false }) { Text("لغو") } }
        )
    } else {
        ModalBottomSheet(onDismissRequest = onDismiss) {
            Column(modifier = Modifier.padding(16.dp).padding(bottom = 24.dp)) {
                Text(tag.title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))

                ListItem(
                    headlineContent = { Text("تغییر نام") },
                    leadingContent = { Icon(Icons.Default.Edit, null) },
                    modifier = Modifier.clickable { isEditingName = true }
                )
                ListItem(
                    headlineContent = { Text("حذف", color = MaterialTheme.colorScheme.error) },
                    leadingContent = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
                    modifier = Modifier.clickable { showDeleteConfirm = true }
                )
            }
        }
    }
}