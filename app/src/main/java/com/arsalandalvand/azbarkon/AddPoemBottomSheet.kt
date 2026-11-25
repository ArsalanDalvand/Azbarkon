package com.arsalandalvand.azbarkon

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.arsalandalvand.azbarkon.ui.theme.NastaliqFontFamily
import com.arsalandalvand.azbarkon.ui.theme.VazirmatnFontFamily
import saman.zamani.persiandate.PersianDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPoemBottomSheet(
    existingBeyt: Beyt? = null,
    defaultStartT: String = "08:00",
    defaultEndT: String = "09:00",
    allTags: List<Tag>,
    loadTags: suspend (Int) -> List<Long>,
    onAddNewTag: (String, Long?) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int, String, String, String, String, String, String, List<Long>) -> Unit
) {
    // اجبار جهت راست به چپ برای کل باتم‌شیت
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val focusManager = LocalFocusManager.current
        val clipboardManager = LocalClipboardManager.current

        // --- متغیرهای داده (State) ---
        var beyt1 by remember { mutableStateOf(existingBeyt?.beyt1 ?: "") }
        var beyt2 by remember { mutableStateOf(existingBeyt?.beyt2 ?: "") }

        // --- لاجیک چسباندن هوشمند (Smart Paste) ---
        fun smartPaste() {
            clipboardManager.getText()?.text?.let { text ->
                if (text.contains("\n")) {
                    val parts = text.split("\n").filter { it.isNotBlank() }
                    if (parts.isNotEmpty()) beyt1 = parts[0].trim()
                    if (parts.size > 1) beyt2 = parts[1].trim()
                } else {
                    if (beyt1.isBlank()) beyt1 = text.trim() else beyt2 = text.trim()
                }
            }
        }

        var selectedKind by remember { mutableIntStateOf(existingBeyt?.kind ?: 1) }
        var startT by remember { mutableStateOf(existingBeyt?.startT ?: defaultStartT) }
        var endT by remember { mutableStateOf(existingBeyt?.endT ?: defaultEndT) }

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

        // --- وضعیت‌ها (سوییچ) ---
        var isMemorized by remember { mutableStateOf(existingBeyt?.kind == 5) }
        var isVisible by remember { mutableStateOf(existingBeyt?.sh != "0") }

        // --- تگ‌ها ---
        val selectedTags = remember { mutableStateListOf<Long>() }
        LaunchedEffect(existingBeyt) {
            if (existingBeyt?.id != null) {
                val tags = loadTags(existingBeyt.id)
                selectedTags.clear()
                selectedTags.addAll(tags)
            }
        }

        // --- پیکرها (Time/Date Pickers) ---
        var showTimePickerFor by remember { mutableStateOf<String?>(null) }
        var showDatePickerFor by remember { mutableStateOf<String?>(null) }

        // -----------------------------------------------------------
        // *** ساختار اصلی: ModalBottomSheet ***
        // -----------------------------------------------------------
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            // هدر ثابت
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (existingBeyt == null) "شعر تازه" else "ویرایش شعر",
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = VazirmatnFontFamily,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = {
                        val finalKind = if (isMemorized) 5 else selectedKind
                        val finalSh = if (isVisible) "1" else "0"
                        onConfirm(beyt1, beyt2, finalKind, startT, endT, weekDays, startD, endD, finalSh, selectedTags.toList())
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("ذخیره", fontFamily = VazirmatnFontFamily, fontWeight = FontWeight.Bold)
                }
            }

            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            // محتوای اسکرول‌خور
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 50.dp)
            ) {
                // 1. بخش ورودی شاعرانه
                Box(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { smartPaste() }) {
                                Icon(Icons.Outlined.ContentPaste, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("چسباندن هوشمند", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        PoeticInputField(
                            value = beyt1,
                            onValueChange = { beyt1 = it },
                            hint = "مصرع اول ...",
                            isFirst = true,
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        PoeticInputField(
                            value = beyt2,
                            onValueChange = { beyt2 = it },
                            hint = "مصرع دوم ...",
                            isFirst = false,
                            onNext = { focusManager.clearFocus() }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 2. بخش تگ‌ها
                Text("دسته‌بندی", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                TagSelector(
                    allTags = allTags,
                    selectedTagIds = selectedTags,
                    onTagToggle = { id -> if (selectedTags.contains(id)) selectedTags.remove(id) else selectedTags.add(id) },
                    onAddNewTag = onAddNewTag
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 3. تنظیمات وضعیت
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                ) {
                    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        // سوییچ حفظ شده
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isMemorized = !isMemorized }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("حفظ شده", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, fontFamily = VazirmatnFontFamily)
                                Text("انتقال به سیستم مرور (لایتنر)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(checked = isMemorized, onCheckedChange = { isMemorized = it })
                        }

                        if (!isMemorized) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isVisible = !isVisible }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("نمایش فعال", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, fontFamily = VazirmatnFontFamily)
                                    Text(if (isVisible) "شعر در چرخه نمایش است" else "فعلاً نمایش داده نمی‌شود", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Switch(checked = isVisible, onCheckedChange = { isVisible = it })
                            }
                        }
                    }
                }

                // 4. زمان‌بندی هوشمند
                AnimatedVisibility(
                    visible = !isMemorized,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(modifier = Modifier.padding(top = 24.dp)) {
                        Text("برنامه یادآوری", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(12.dp))

                        val options = listOf("روزانه", "هفتگی", "ماهانه", "بازه")
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            options.forEachIndexed { index, label ->
                                SegmentedButton(
                                    selected = selectedKind == index + 1,
                                    onClick = { selectedKind = index + 1 },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                                ) {
                                    Text(label, style = MaterialTheme.typography.labelSmall, fontFamily = VazirmatnFontFamily)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // الف: انتخاب ساعت
                                if (selectedKind == 1 || selectedKind == 4) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Schedule, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("ساعت نمایش:", style = MaterialTheme.typography.bodySmall)
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        TimeSelectionCard(
                                            label = "از ساعت",
                                            time = startT,
                                            onClick = { showTimePickerFor = "start" },
                                            modifier = Modifier.weight(1f)
                                        )
                                        TimeSelectionCard(
                                            label = "تا ساعت",
                                            time = endT,
                                            onClick = { showTimePickerFor = "end" },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }

                                // ب: روزهای هفته
                                if (selectedKind == 2) {
                                    Text("چه روزهایی نمایش داده شود؟", style = MaterialTheme.typography.bodySmall)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    WeekDaySelector(weekDays) { weekDays = it }
                                }

                                // ج: روز ماه (طراحی جدید Grid)
                                if (selectedKind == 3) {
                                    val currentDay = startD.takeLast(2).toIntOrNull() ?: 1
                                    Text("چندم ماه نمایش داده شود؟", style = MaterialTheme.typography.bodySmall)
                                    Spacer(modifier = Modifier.height(12.dp))

                                    // کامپوننت جدید انتخاب روز
                                    MonthDayGridSelector(
                                        selectedDay = currentDay,
                                        onDaySelected = { day ->
                                            val d = String.format("%02d", day)
                                            startD = "1403/01/$d" // سال و ماه فرضی
                                        }
                                    )
                                }

                                // د: بازه تقویمی
                                if (selectedKind == 4) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.DateRange, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("بازه تقویمی:", style = MaterialTheme.typography.bodySmall)
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        DateSelectionCard(
                                            label = "شروع",
                                            date = startD,
                                            onClick = { showDatePickerFor = "start" },
                                            modifier = Modifier.weight(1f)
                                        )
                                        DateSelectionCard(
                                            label = "پایان",
                                            date = endD,
                                            onClick = { showDatePickerFor = "end" },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- دیالوگ‌های انتخاب ---

        // 1. دیالوگ ساعت مدرن
        if (showTimePickerFor != null) {
            val initialTime = if (showTimePickerFor == "start") startT else endT
            val (h, m) = initialTime.split(":").map { it.toInt() }

            AdvancedTimePickerDialog(
                initialHour = h,
                initialMinute = m,
                onDismiss = { showTimePickerFor = null },
                onTimeSelected = { hour, minute ->
                    val timeStr = "${String.format("%02d", hour)}:${String.format("%02d", minute)}"
                    if (showTimePickerFor == "start") startT = timeStr else endT = timeStr
                    showTimePickerFor = null
                }
            )
        }

        // 2. دیالوگ تاریخ
        if (showDatePickerFor != null) {
            val initialDateStr = if (showDatePickerFor == "start") startD else endD
            val parts = initialDateStr.split("/").map { it.toIntOrNull() ?: 1 }
            val pDate = PersianDate().apply { shYear = parts[0]; shMonth = parts[1]; shDay = parts[2] }

            // استفاده از همان دیالوگی که در MainScreen هم استفاده می‌شود
            PersianDatePickerDialog(
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
// *** کامپوننت‌های UI جدید و بهبود یافته ***
// ------------------------------------------------------------------

@Composable
fun TimeSelectionCard(label: String, time: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(time.toPersianDigits(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, fontFamily = VazirmatnFontFamily)
        }
    }
}

@Composable
fun DateSelectionCard(label: String, date: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            // نمایش فقط ماه و روز برای کوتاهی (یا تاریخ کامل)
            val parts = date.split("/")
            val prettyDate = if(parts.size == 3) "${parts[2]} ${getMonthName(parts[1].toInt())}" else date
            Text(prettyDate.toPersianDigits(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, fontFamily = VazirmatnFontFamily)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedTimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onTimeSelected: (Int, Int) -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            tonalElevation = 6.dp,
            modifier = Modifier
                .width(IntrinsicSize.Min)
                .background(shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "انتخاب زمان",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 20.dp).align(Alignment.Start)
                )

                TimePicker(state = timePickerState)

                Row(
                    modifier = Modifier
                        .height(40.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("لغو") }
                    TextButton(onClick = {
                        onTimeSelected(timePickerState.hour, timePickerState.minute)
                    }) { Text("تایید") }
                }
            }
        }
    }
}

@Composable
fun MonthDayGridSelector(selectedDay: Int, onDaySelected: (Int) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.height(220.dp), // ارتفاع مناسب
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(31) { index ->
            val day = index + 1
            val isSelected = (day == selectedDay)

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .clickable { onDaySelected(day) }
            ) {
                Text(
                    text = "$day".toPersianDigits(),
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

// تابع کمکی برای اینپوت متن شعر (بدون تغییر زیاد)
@Composable
fun PoeticInputField(value: String, onValueChange: (String) -> Unit, hint: String, isFirst: Boolean, onNext: () -> Unit) {
    val paddingModifier = if (isFirst) Modifier.padding(end = 40.dp) else Modifier.padding(start = 40.dp)
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(hint, style = TextStyle(fontFamily = NastaliqFontFamily, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))) },
        textStyle = TextStyle(fontFamily = NastaliqFontFamily, fontSize = 28.sp, lineHeight = 55.sp, textAlign = TextAlign.Start, color = MaterialTheme.colorScheme.onSurface),
        colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, disabledContainerColor = Color.Transparent, focusedIndicatorColor = MaterialTheme.colorScheme.primary, unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
        keyboardOptions = KeyboardOptions(imeAction = if (isFirst) ImeAction.Next else ImeAction.Done),
        keyboardActions = KeyboardActions(onNext = { onNext() }, onDone = { onNext() }),
        singleLine = true,
        modifier = Modifier.fillMaxWidth().then(paddingModifier)
    )
}