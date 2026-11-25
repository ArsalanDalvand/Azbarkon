package com.arsalandalvand.azbarkon

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import saman.zamani.persiandate.PersianDate
import kotlin.math.roundToInt

// ------------------------------------------------------------------
// *** دیالوگ اصلی افزودن/ویرایش شعر (نسخه نهایی) ***
// ------------------------------------------------------------------
@Composable
fun AddPoemDialog(
    existingBeyt: Beyt? = null,
    defaultStartT: String = "08:00",
    defaultEndT: String = "09:00",
    allTags: List<Tag>,
    initialSelectedTagIds: List<Long> = emptyList(),
    loadTags: suspend (Int) -> List<Long>,
    onAddNewTag: (String, Long?) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int, String, String, String, String, String, String, List<Long>) -> Unit
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {

        // --- متغیرهای داده ---
        var beyt1 by remember { mutableStateOf(existingBeyt?.beyt1 ?: "") }
        var beyt2 by remember { mutableStateOf(existingBeyt?.beyt2 ?: "") }
        var selectedKind by remember { mutableIntStateOf(existingBeyt?.kind ?: 1) }

        var startT by remember { mutableStateOf(existingBeyt?.startT ?: defaultStartT) }
        var endT by remember { mutableStateOf(existingBeyt?.endT ?: defaultEndT) }

        // تاریخ
        val now = PersianDate()
        val defaultDate = "${now.shYear}/${String.format("%02d", now.shMonth)}/${String.format("%02d", now.shDay)}"
        var startD by remember { mutableStateOf(existingBeyt?.startD ?: defaultDate) }
        var endD by remember { mutableStateOf(existingBeyt?.endD ?: defaultDate) }

        // روزهای هفته
        val defaultWeekDays = remember {
            val chars = CharArray(7) { '0' }
            val dayIndex = now.dayOfWeek()
            if (dayIndex in 0..6) chars[dayIndex] = '1'
            String(chars)
        }
        var weekDays by remember { mutableStateOf(existingBeyt?.weekDays ?: defaultWeekDays) }

        // وضعیت‌ها
        var isMemorized by remember { mutableStateOf(existingBeyt?.kind == 5) }
        // **بازگردانی قابلیت نمایش/عدم نمایش**
        // اگر شعر جدید است پیش‌فرض نمایش داده شود (true)، اگر قدیمی است از دیتابیس بخوان
        var isVisible by remember { mutableStateOf(existingBeyt?.sh != "0") }

        // تگ‌ها
        val selectedTags = remember { mutableStateListOf<Long>() }
        LaunchedEffect(existingBeyt) {
            if (existingBeyt?.id != null) {
                val tags = loadTags(existingBeyt.id)
                selectedTags.clear()
                selectedTags.addAll(tags)
            }
        }

        // --- متغیرهای UI ---
        var showAdvancedSettings by remember { mutableStateOf(false) }
        var showTimePickerFor by remember { mutableStateOf<String?>(null) }
        var showDatePickerFor by remember { mutableStateOf<String?>(null) }

        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(0.95f).clip(RoundedCornerShape(24.dp)),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // هدر
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (existingBeyt == null) "شعر جدید" else "ویرایش شعر",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, "بستن") }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // ورودی‌های متن
                    PoemInputField(beyt1, { beyt1 = it }, "مصرع اول...", true)
                    Spacer(modifier = Modifier.height(12.dp))
                    PoemInputField(beyt2, { beyt2 = it }, "مصرع دوم...", false)

                    Spacer(modifier = Modifier.height(24.dp))

                    // انتخاب تگ
                    AdvancedTagSelector(
                        allTags = allTags,
                        selectedTagIds = selectedTags,
                        onTagToggle = { id -> if (selectedTags.contains(id)) selectedTags.remove(id) else selectedTags.add(id) },
                        onAddNewTag = onAddNewTag
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // تنظیمات زمان‌بندی (فقط اگر حفظ نشده باشد فعال است)
                    if (!isMemorized) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            modifier = Modifier.fillMaxWidth().clickable { showAdvancedSettings = !showAdvancedSettings }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AccessTime, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text("زمان‌بندی مرور", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                            Text(
                                                getScheduleSummary(selectedKind, startT, endT, startD),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Icon(Icons.Default.ExpandMore, null, modifier = Modifier.rotate(if (showAdvancedSettings) 180f else 0f))
                                }

                                AnimatedVisibility(visible = showAdvancedSettings) {
                                    Column(modifier = Modifier.padding(top = 16.dp)) {
                                        SimpleTabRow(
                                            options = listOf("روزانه", "هفتگی", "ماهانه", "بازه"),
                                            selectedIndex = if (selectedKind in 1..4) selectedKind - 1 else 3,
                                            onSelect = { selectedKind = it + 1 }
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        // 1. ساعت
                                        if (selectedKind == 1 || selectedKind == 4) {
                                            Text("ساعت یادآوری:", style = MaterialTheme.typography.labelSmall)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(Modifier.fillMaxWidth()) {
                                                Button(onClick = { showTimePickerFor = "start" }, modifier = Modifier.weight(1f)) { Text("از: ${startT.toPersianDigits()}") }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Button(onClick = { showTimePickerFor = "end" }, modifier = Modifier.weight(1f)) { Text("تا: ${endT.toPersianDigits()}") }
                                            }
                                        }

                                        // 2. روزهای هفته
                                        if (selectedKind == 2) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            WeekDaySelector(weekDays) { weekDays = it }
                                        }

                                        // 3. روز ماه
                                        if (selectedKind == 3) {
                                            val currentDay = startD.takeLast(2).toIntOrNull() ?: 1
                                            var sliderValue by remember(currentDay) { mutableFloatStateOf(currentDay.toFloat()) }

                                            Text("نمایش در روز ${sliderValue.toInt().toString().toPersianDigits()}م هر ماه", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                                            Slider(
                                                value = sliderValue,
                                                onValueChange = {
                                                    sliderValue = it
                                                    val d = String.format("%02d", it.toInt())
                                                    startD = "1403/01/$d"
                                                },
                                                valueRange = 1f..31f,
                                                steps = 30
                                            )
                                        }

                                        // 4. بازه تاریخ
                                        if (selectedKind == 4) {
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Row(Modifier.fillMaxWidth()) {
                                                OutlinedButton(onClick = { showDatePickerFor = "start" }, modifier = Modifier.weight(1f)) { Text("از: ${startD.toPersianDigits()}") }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                OutlinedButton(onClick = { showDatePickerFor = "end" }, modifier = Modifier.weight(1f)) { Text("تا: ${endD.toPersianDigits()}") }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // --- بخش وضعیت نمایش و حفظ (آپدیت شده) ---
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(8.dp)
                    ) {
                        // 1. چک‌باکس نمایش/عدم نمایش (بازگردانده شد)
                        if (!isMemorized) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isVisible = !isVisible }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(checked = isVisible, onCheckedChange = { isVisible = it })
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("نمایش فعال باشد", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    Text(
                                        if (isVisible) "شعر در نوبت نمایش قرار می‌گیرد" else "فعلاً نمایش داده نشود (توقف موقت)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        }

                        // 2. چک‌باکس حفظ کردم
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isMemorized = !isMemorized }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(checked = isMemorized, onCheckedChange = { isMemorized = it })
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("این شعر را حفظ هستم", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text("به لیست حفظ‌شده‌ها منتقل می‌شود", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            val finalKind = if (isMemorized) 5 else selectedKind
                            // تبدیل وضعیت visible به "1" یا "0"
                            val finalSh = if (isVisible) "1" else "0"

                            onConfirm(beyt1, beyt2, finalKind, startT, endT, weekDays, startD, endD, finalSh, selectedTags.toList())
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(if (existingBeyt == null) "افزودن به مجموعه" else "ذخیره تغییرات", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- دیالوگ‌های تاریخ و ساعت ---
        if (showTimePickerFor != null) {
            val initialTime = if (showTimePickerFor == "start") startT else endT
            val (h, m) = initialTime.split(":").map { it.toInt() }
            CustomPersianTimePicker(
                initialHour = h, initialMinute = m,
                onDismiss = { showTimePickerFor = null },
                onTimeSelected = { time ->
                    if (showTimePickerFor == "start") startT = time else endT = time
                    showTimePickerFor = null
                }
            )
        }

        if (showDatePickerFor != null) {
            val initialDateStr = if (showDatePickerFor == "start") startD else endD
            val parts = initialDateStr.split("/").map { it.toIntOrNull() ?: 1 }
            val pDate = PersianDate().apply { shYear = parts[0]; shMonth = parts[1]; shDay = parts[2] }

            CustomPersianDatePickerDialog(
                initialDate = pDate,
                onDismiss = { showDatePickerFor = null },
                onDateSelected = { date ->
                    val dStr = "${date.shYear}/${String.format("%02d", date.shMonth)}/${String.format("%02d", date.shDay)}"
                    if (showDatePickerFor == "start") startD = dStr else endD = dStr
                    showDatePickerFor = null
                }
            )
        }
    }
}

// ------------------------------------------------------------------
// *** کامپوننت‌های کمکی (بدون تغییر نسبت به قبل، فقط کپی شده برای کامل بودن فایل) ***
// ------------------------------------------------------------------

@Composable
fun AdvancedTagSelector(
    allTags: List<Tag>,
    selectedTagIds: List<Long>,
    onTagToggle: (Long) -> Unit,
    onAddNewTag: (String, Long?) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var newTagName by remember { mutableStateOf("") }
    var currentParentId by remember { mutableStateOf<Long?>(null) }

    val selectedTagsObjects = allTags.filter { selectedTagIds.contains(it.id) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (selectedTagsObjects.isEmpty()) {
                Text("بدون برچسب", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                    items(selectedTagsObjects) { tag ->
                        InputChip(
                            selected = true, onClick = { onTagToggle(tag.id) },
                            label = { Text(tag.title) },
                            trailingIcon = { Icon(Icons.Default.Close, null, Modifier.size(14.dp)) },
                            colors = InputChipDefaults.inputChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer)
                        )
                    }
                }
            }
            IconButton(onClick = { isExpanded = !isExpanded }) {
                Icon(if (isExpanded) Icons.Default.Close else Icons.Default.Add, "مدیریت تگ‌ها", tint = MaterialTheme.colorScheme.primary)
            }
        }

        AnimatedVisibility(visible = isExpanded, enter = expandVertically(), exit = shrinkVertically()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    if (currentParentId != null) {
                        val parent = allTags.find { it.id == currentParentId }
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { currentParentId = parent?.parentId }.padding(bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ArrowUpward, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("بازگشت از ${parent?.title ?: "..."}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        Text("دسته‌بندی‌ها (ریشه)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(bottom = 8.dp))
                    }

                    val currentLevelTags = allTags.filter { it.parentId == currentParentId }

                    if (currentLevelTags.isEmpty()) {
                        Text("این پوشه خالی است.", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(8.dp), color = Color.Gray)
                    }

                    currentLevelTags.forEach { tag ->
                        val isSelected = selectedTagIds.contains(tag.id)
                        val hasChildren = allTags.any { it.parentId == tag.id }

                        Row(
                            modifier = Modifier.fillMaxWidth().height(40.dp).clickable { if (hasChildren) currentParentId = tag.id else onTagToggle(tag.id) },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (hasChildren) Icons.Default.Folder else Icons.Default.Tag,
                                null,
                                tint = if (hasChildren) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(tag.title, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))

                            IconButton(onClick = { currentParentId = tag.id }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, "زیرمجموعه", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                            }

                            if (isSelected) Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        }
                    }

                    Divider(Modifier.padding(vertical = 8.dp))
                    TextButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("ساخت تگ جدید در اینجا")
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("نام برچسب جدید") },
            text = { OutlinedTextField(value = newTagName, onValueChange = { newTagName = it }, singleLine = true, modifier = Modifier.fillMaxWidth()) },
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

@Composable
fun CustomPersianTimePicker(initialHour: Int, initialMinute: Int, onDismiss: () -> Unit, onTimeSelected: (String) -> Unit) {
    var selectedHour by remember { mutableIntStateOf(initialHour) }
    var selectedMinute by remember { mutableIntStateOf(initialMinute) }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("انتخاب زمان", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    NumberPickerList(range = 0..59, initial = selectedMinute, onValueChange = { selectedMinute = it })
                    Text(" : ", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                    NumberPickerList(range = 0..23, initial = selectedHour, onValueChange = { selectedHour = it })
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = { onTimeSelected(String.format("%02d:%02d", selectedHour, selectedMinute)) }, modifier = Modifier.fillMaxWidth()) { Text("تایید") }
            }
        }
    }
}

@Composable
fun NumberPickerList(range: IntRange, initial: Int, onValueChange: (Int) -> Unit) {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initial)
    val items = range.toList()
    Box(modifier = Modifier.height(120.dp).width(60.dp)) {
        LazyColumn(state = listState, horizontalAlignment = Alignment.CenterHorizontally, contentPadding = PaddingValues(vertical = 40.dp)) {
            items(items) { item ->
                Text(
                    text = String.format("%02d", item).toPersianDigits(),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(vertical = 8.dp).clickable { onValueChange(item) },
                    color = if (item == initial) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }
        }
    }
}

@Composable
fun CustomPersianDatePickerDialog(initialDate: PersianDate, onDismiss: () -> Unit, onDateSelected: (PersianDate) -> Unit) {
    var currentYear by remember { mutableIntStateOf(initialDate.shYear) }
    var currentMonth by remember { mutableIntStateOf(initialDate.shMonth) }
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { if (currentMonth > 1) currentMonth-- else { currentMonth = 12; currentYear-- } }) { Icon(Icons.AutoMirrored.Filled.ArrowForward, "قبل") }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(getMonthName(currentMonth), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("$currentYear".toPersianDigits(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                    }
                    IconButton(onClick = { if (currentMonth < 12) currentMonth++ else { currentMonth = 1; currentYear++ } }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "بعد") }
                }
                LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.height(240.dp)) {
                    val days = listOf("ش", "ی", "د", "س", "چ", "پ", "ج")
                    items(days) { Text(it, textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, color = Color.Gray) }
                    val startDayOfWeek = PersianDate().apply { shYear = currentYear; shMonth = currentMonth; shDay = 1 }.dayOfWeek()
                    val daysInMonth = if (currentMonth <= 6) 31 else if (currentMonth <= 11) 30 else if (PersianDate().isLeap(currentYear)) 30 else 29
                    items(startDayOfWeek) { Spacer(Modifier) }
                    items(daysInMonth) { index ->
                        val day = index + 1
                        val isSelected = (day == initialDate.shDay && currentMonth == initialDate.shMonth && currentYear == initialDate.shYear)
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.aspectRatio(1f).padding(2.dp).clip(CircleShape)
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { onDateSelected(PersianDate().apply { shYear = currentYear; shMonth = currentMonth; shDay = day }) }) {
                            Text("$day".toPersianDigits(), color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PoemInputField(value: String, onValueChange: (String) -> Unit, placeholder: String, isFirstLine: Boolean) {
    TextField(
        value = value, onValueChange = onValueChange,
        placeholder = { Text(placeholder, textAlign = if (isFirstLine) TextAlign.Start else TextAlign.End, modifier = Modifier.fillMaxWidth(), color = Color.Gray) },
        textStyle = MaterialTheme.typography.headlineSmall.copy(textAlign = if (isFirstLine) TextAlign.Start else TextAlign.End),
        colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = MaterialTheme.colorScheme.primary, unfocusedIndicatorColor = Color.LightGray.copy(0.3f)),
        modifier = Modifier.fillMaxWidth(), singleLine = true
    )
}

@Composable
fun SimpleTabRow(options: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(4.dp)) {
        options.forEachIndexed { index, text ->
            val isSelected = index == selectedIndex
            Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(if (isSelected) MaterialTheme.colorScheme.background else Color.Transparent).clickable { onSelect(index) }.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                Text(text, style = MaterialTheme.typography.labelMedium, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun WeekDaySelector(selectedDays: String, onSelectionChanged: (String) -> Unit) {
    val dayNames = listOf("ش", "ی", "د", "س", "چ", "پ", "ج")
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        dayNames.forEachIndexed { index, name ->
            val isSelected = selectedDays.getOrElse(index) { '0' } == '1'
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(36.dp).clip(CircleShape)
                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                .clickable {
                    val chars = if (selectedDays.length >= 7) selectedDays.toCharArray() else CharArray(7) { '0' }
                    chars[index] = if (isSelected) '0' else '1'
                    onSelectionChanged(String(chars))
                }) {
                Text(name, color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
            }
        }
    }
}

fun getScheduleSummary(kind: Int, startT: String, endT: String, startD: String): String {
    return when(kind) {
        1 -> "روزانه • ${startT.toPersianDigits()} تا ${endT.toPersianDigits()}"
        2 -> "هفتگی"
        3 -> {
            val day = startD.takeLast(2).toIntOrNull() ?: 1
            "ماهانه • روز ${day.toString().toPersianDigits()}م هر ماه"
        }
        4 -> "بازه زمانی مشخص"
        else -> "سفارشی"
    }
}

// fun String.toPersianDigits(): String = this.replace("0", "۰").replace("1", "۱").replace("2", "۲").replace("3", "۳").replace("4", "۴").replace("5", "۵").replace("6", "۶").replace("7", "۷").replace("8", "۸").replace("9", "۹")
// تابع getMonthName چون در فایل‌های دیگر هم استفاده شده، اینجا کامنت شده است. اگر ارور Unresolved داد، خط زیر را از کامنت خارج کنید:
// fun getMonthName(month: Int): String = when(month) { 1->"فروردین" 2->"اردیبهشت" 3->"خرداد" 4->"تیر" 5->"مرداد" 6->"شهریور" 7->"مهر" 8->"آبان" 9->"آذر" 10->"دی" 11->"بهمن" 12->"اسفند" else->"" }