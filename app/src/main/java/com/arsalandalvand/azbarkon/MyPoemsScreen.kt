package com.arsalandalvand.azbarkon

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// تعریف تب‌ها برای دسترسی آسان
enum class PoemTab(val title: String, val kinds: List<Int>) {
    All("همه", listOf(1, 2, 3, 4, 5)),
    Daily("روزانه", listOf(1)),
    Weekly("هفتگی", listOf(2)),
    Monthly("ماهانه", listOf(3)),
    Custom("سفارشی", listOf(4)),
    Memorized("حفظ شده", listOf(5))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPoemsScreen(
    allBeyts: List<Beyt>,
    allTags: List<Tag>,
    loadTags: suspend (Int) -> List<Long>,
    onAddNewTag: (String, Long?) -> Unit,
    onBack: () -> Unit,
    onDeletePoem: (Beyt) -> Unit,
    onMemorizePoem: (Beyt) -> Unit,
    onEditPoem: (Beyt, String, String, Int, String, String, String, String, String, String, List<Long>) -> Unit
) {
    // --- State Management ---
    var selectedTab by remember { mutableStateOf(PoemTab.All) }
    var searchQuery by remember { mutableStateOf("") }
    var showFilterSheet by remember { mutableStateOf(false) }

    // فیلترهای پیشرفته
    var selectedTagIds by remember { mutableStateOf(setOf<Long>()) }
    // فیلتر برای نمایش فقط "نمایش نده‌ها" (فرض بر این است که منطقی برایش دارید)
    var onlyHidden by remember { mutableStateOf(false) }

    // State برای ادیت
    var showEditDialog by remember { mutableStateOf(false) }
    var editingBeyt by remember { mutableStateOf<Beyt?>(null) }

    // کش کردن تگ‌های هر شعر برای جلوگیری از افت فریم
    var beytTagsMap by remember { mutableStateOf<Map<Int, List<Long>>>(emptyMap()) }

    LaunchedEffect(allBeyts) {
        withContext(Dispatchers.IO) {
            val map = mutableMapOf<Int, List<Long>>()
            allBeyts.forEach { beyt ->
                beyt.id?.let { id ->
                    map[id] = loadTags(id)
                }
            }
            beytTagsMap = map
        }
    }

    // --- منطق اصلی فیلتر کردن ---
    val filteredList by remember(
        allBeyts, selectedTab, searchQuery, selectedTagIds, onlyHidden, beytTagsMap
    ) {
        derivedStateOf {
            allBeyts.filter { beyt ->
                // 1. فیلتر نوع (Tab)
                // اگر کاربر تب "حفظ شده" را انتخاب کرد، فقط kind=5 را نشان بده
                // اگر تب دیگری بود، چک کن که kind جزو آن دسته باشد
                val tabMatch = beyt.kind in selectedTab.kinds

                // 2. فیلتر تگ (با پشتیبانی از والد/فرزند)
                val tagMatch = if (selectedTagIds.isEmpty()) true else {
                    val poemTags = beytTagsMap[beyt.id] ?: emptyList()
                    val allowedTags = resolveTagsWithChildren(selectedTagIds, allTags)
                    poemTags.any { it in allowedTags }
                }

                // 3. فیلتر وضعیت (Hidden / Show)
                // اگر فیلتر "فقط مخفی‌ها" روشن باشد، باید منطق آن چک شود.
                // چون فیلد isHidden را در کد ندیدم، فعلاً این بخش را باز می‌گذارم.
                // اگر منظور از "نمایش نده"، همان حفظ شده‌هاست که در تب هندل شده.
                // اگر فیلد جداگانه‌ای در دیتابیس دارید، اینجا اضافه کنید:
                // val hiddenMatch = if (onlyHidden) beyt.isHidden else true
                val hiddenMatch = true

                val baseMatch = tabMatch && tagMatch && hiddenMatch

                // 4. جستجو (فقط در نتایج فیلتر شده)
                if (baseMatch && searchQuery.isNotBlank()) {
                    beyt.beyt1.contains(searchQuery) || beyt.beyt2.contains(searchQuery)
                } else {
                    baseMatch
                }
            }
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                Column(Modifier.background(MaterialTheme.colorScheme.background)) {
                    MyPoemsTopBar(
                        searchQuery = searchQuery,
                        onSearchChange = { searchQuery = it },
                        onBack = onBack,
                        onFilterClick = { showFilterSheet = true },
                        hasActiveFilters = selectedTagIds.isNotEmpty() || onlyHidden
                    )

                    ScrollableTabRow(
                        selectedTabIndex = selectedTab.ordinal,
                        containerColor = MaterialTheme.colorScheme.background,
                        contentColor = MaterialTheme.colorScheme.primary,
                        edgePadding = 16.dp,
                        divider = { HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)) },
                        indicator = { tabPositions ->
                            if (selectedTab.ordinal < tabPositions.size) {
                                TabRowDefaults.SecondaryIndicator(
                                    Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    ) {
                        PoemTab.values().forEach { tab ->
                            Tab(
                                selected = selectedTab == tab,
                                onClick = { selectedTab = tab },
                                text = {
                                    Text(
                                        text = tab.title,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = if(selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                if (filteredList.isEmpty()) {
                    EmptyStateView(
                        isSearchActive = searchQuery.isNotEmpty() || selectedTagIds.isNotEmpty(),
                        message = if (allBeyts.isEmpty()) "هنوز شعری ندارید" else "شعری با این مشخصات یافت نشد"
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
                    ) {
                        items(items = filteredList, key = { it.id ?: 0 }) { beyt ->
                            SwipeToDeleteContainer(item = beyt, onDelete = { onDeletePoem(beyt) }) {
                                PoemCard(
                                    beyt = beyt,
                                    isQuizMode = false,
                                    allTags = allTags,
                                    loadTags = loadTags,
                                    onClick = {
                                        editingBeyt = beyt
                                        showEditDialog = true
                                    },
                                    onMemorizeClick = { onMemorizePoem(beyt) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- Bottom Sheet فیلتر ---
        if (showFilterSheet) {
            FilterBottomSheet(
                allTags = allTags,
                selectedTagIds = selectedTagIds,
                onlyHidden = onlyHidden,
                onApply = { tags, hidden ->
                    selectedTagIds = tags
                    onlyHidden = hidden
                    showFilterSheet = false
                },
                onDismiss = { showFilterSheet = false }
            )
        }

        // --- دیالوگ ویرایش ---
        if (showEditDialog && editingBeyt != null) {
            AddPoemBottomSheet(
                existingBeyt = editingBeyt,
                defaultStartT = editingBeyt!!.startT,
                defaultEndT = editingBeyt!!.endT,
                allTags = allTags,
                loadTags = loadTags,
                onAddNewTag = onAddNewTag,
                onDismiss = { showEditDialog = false },
                onConfirm = { b1, b2, k, sT, eT, wD, sD, eD, sh, tags ->
                    onEditPoem(editingBeyt!!, b1, b2, k, sT, eT, wD, sD, eD, sh, tags)
                    showEditDialog = false
                    editingBeyt = null
                }
            )
        }
    }
}

// -------------------------------------------------------------------------
// توابع کمکی
// -------------------------------------------------------------------------

fun resolveTagsWithChildren(selectedIds: Set<Long>, allTags: List<Tag>): Set<Long> {
    val result = selectedIds.toMutableSet()
    val queue = ArrayDeque(selectedIds)
    while (queue.isNotEmpty()) {
        val currentId = queue.removeFirst()
        val children = allTags.filter { it.parentId == currentId }
        children.forEach { child ->
            if (result.add(child.id)) {
                queue.add(child.id)
            }
        }
    }
    return result
}

@Composable
fun MyPoemsTopBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onBack: () -> Unit,
    onFilterClick: () -> Unit,
    hasActiveFilters: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "بازگشت")
        }

        Spacer(modifier = Modifier.width(8.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier
                .weight(1f)
                .height(50.dp),
            placeholder = { Text("جستجو...", style = MaterialTheme.typography.bodyMedium) },
            leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(20.dp)) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(Icons.Default.Clear, "پاک کردن", modifier = Modifier.size(20.dp))
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            textStyle = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.width(8.dp))

        Box {
            IconButton(
                onClick = onFilterClick,
                modifier = Modifier
                    .background(
                        color = if(hasActiveFilters) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                        shape = CircleShape
                    )
            ) {
                Icon(
                    Icons.Default.FilterList,
                    "فیلتر",
                    tint = if(hasActiveFilters) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (hasActiveFilters) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .align(Alignment.TopEnd)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterBottomSheet(
    allTags: List<Tag>,
    selectedTagIds: Set<Long>,
    onlyHidden: Boolean,
    onApply: (Set<Long>, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var tempSelectedTags by remember { mutableStateOf(selectedTagIds) }
    var tempOnlyHidden by remember { mutableStateOf(onlyHidden) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("فیلترها", style = MaterialTheme.typography.titleLarge)
                TextButton(onClick = {
                    tempSelectedTags = emptySet()
                    tempOnlyHidden = false
                }) {
                    Text("حذف همه")
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 12.dp))

            // گزینه فیلتر مخفی‌ها (در صورت نیاز فعال کنید)
            /*
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().clickable { tempOnlyHidden = !tempOnlyHidden }
            ) {
                Checkbox(checked = tempOnlyHidden, onCheckedChange = { tempOnlyHidden = it })
                Text("فقط نمایش مخفی شده‌ها", style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(16.dp))
            */

            if (allTags.isNotEmpty()) {
                Text("تگ‌ها (انتخاب والد = انتخاب فرزندان)", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(12.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    allTags.forEach { tag ->
                        val isSelected = tag.id in tempSelectedTags
                        val isParent = tag.parentId == null

                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                tempSelectedTags = if (isSelected) tempSelectedTags - tag.id else tempSelectedTags + tag.id
                            },
                            // *** اصلاح مهم: استفاده از tag.title به جای tag.name ***
                            label = { Text(tag.title) },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = if(isParent) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent,
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            )
                        )
                    }
                }
            } else {
                Text("تگی وجود ندارد", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = { onApply(tempSelectedTags, tempOnlyHidden) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("اعمال فیلتر")
            }
        }
    }
}

@Composable
fun EmptyStateView(isSearchActive: Boolean, message: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (isSearchActive) Icons.Default.FilterListOff else Icons.Default.SentimentDissatisfied,
            contentDescription = null,
            modifier = Modifier.size(80.dp).alpha(0.3f),
            tint = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(message, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}