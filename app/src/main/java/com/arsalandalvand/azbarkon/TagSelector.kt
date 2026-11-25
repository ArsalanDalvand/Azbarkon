package com.arsalandalvand.azbarkon

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun TagSelector(
    allTags: List<Tag>,
    selectedTagIds: List<Long>,
    onTagToggle: (Long) -> Unit,
    onAddNewTag: (String, Long?) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var newTagName by remember { mutableStateOf("") }
    var currentParentId by remember { mutableStateOf<Long?>(null) }

    // تگ‌هایی که الان انتخاب شده‌اند (برای نمایش در بالای لیست)
    val selectedTagsObjects = allTags.filter { selectedTagIds.contains(it.id) }

    Column(modifier = Modifier.fillMaxWidth()) {
        // --- بخش نمایش تگ‌های انتخاب شده (Chips) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (selectedTagsObjects.isEmpty()) {
                Text(
                    "بدون برچسب",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(selectedTagsObjects) { tag ->
                        InputChip(
                            selected = true,
                            onClick = { onTagToggle(tag.id) },
                            label = { Text(tag.title) },
                            trailingIcon = { Icon(Icons.Default.Close, null, Modifier.size(14.dp)) },
                            colors = InputChipDefaults.inputChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            border = null
                        )
                    }
                }
            }

            // دکمه باز/بسته کردن لیست تگ‌ها
            IconButton(onClick = { isExpanded = !isExpanded }) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = "مدیریت تگ‌ها",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // --- بخش انتخابگر درختی (Expandable) ---
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // نوار بازگشت (Breadcrumb ساده)
                    if (currentParentId != null) {
                        val parent = allTags.find { it.id == currentParentId }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { currentParentId = parent?.parentId }
                                .padding(bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ExpandMore, null, modifier = Modifier.rotate(90f))
                            Text(
                                "بازگشت از ${parent?.title ?: "..."}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    val currentLevelTags = allTags.filter { it.parentId == currentParentId }

                    if (currentLevelTags.isEmpty()) {
                        Text("موردی یافت نشد.", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(8.dp))
                    }

                    // لیست تگ‌های لایه جاری
                    currentLevelTags.forEach { tag ->
                        val isSelected = selectedTagIds.contains(tag.id)
                        val hasChildren = allTags.any { it.parentId == tag.id }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .clickable {
                                    if (hasChildren) currentParentId = tag.id else onTagToggle(tag.id)
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (hasChildren) Icons.Default.Folder else Icons.Default.Tag,
                                contentDescription = null,
                                tint = if (hasChildren) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                tag.title,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )

                            if (isSelected) {
                                Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    Divider(Modifier.padding(vertical = 8.dp))

                    // دکمه ساخت تگ جدید
                    TextButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("ساخت تگ جدید در اینجا")
                    }
                }
            }
        }
    }

    // دیالوگ نام تگ جدید
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("نام برچسب جدید") },
            text = {
                OutlinedTextField(
                    value = newTagName,
                    onValueChange = { newTagName = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (newTagName.isNotBlank()) {
                        onAddNewTag(newTagName, currentParentId)
                        newTagName = ""
                        showAddDialog = false
                    }
                }) { Text("افزودن") }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("لغو") } }
        )
    }
}

// تابع کمکی برای چرخش آیکون (چون AutoMirrored برای ExpandMore نداریم)
fun androidx.compose.ui.Modifier.rotate(degrees: Float) = this.then(
    androidx.compose.ui.Modifier.graphicsLayer { rotationZ = degrees }
)