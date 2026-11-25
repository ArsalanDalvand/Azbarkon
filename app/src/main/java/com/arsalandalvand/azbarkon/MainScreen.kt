package com.arsalandalvand.azbarkon

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import saman.zamani.persiandate.PersianDate
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight

@Composable
fun MainScreenContainer(
    currentScreen: String,
    onScreenChange: (String) -> Unit,
    // پارامترهای دیتابیس و اکشن‌ها
    beyts: List<Beyt>,
    allTags: List<Tag>,
    loadTags: suspend (Int) -> List<Long>,
    onAddNewTag: (String, Long?) -> Unit,
    onAddPoem: (String, String, Int, String, String, String, String, String, String, List<Long>) -> Unit,
    onEditPoem: (Beyt, String, String, Int, String, String, String, String, String, String, List<Long>) -> Unit,
    onDeletePoem: (Beyt) -> Unit,
    onMemorizePoem: (Beyt) -> Unit,
    onOpenSettings: () -> Unit,
    // محتواهای صفحات دیگر (که بعداً طراحی می‌کنیم)
    contentAllPoems: @Composable () -> Unit,
    contentMyNotes: @Composable () -> Unit,
    contentSuggestions: @Composable () -> Unit,
    contentTagManager: @Composable () -> Unit, // *** پارامتر جدید ***
    shouldOpenAddDialog: Boolean = false,
    onAddDialogOpened: () -> Unit = {},

    // *** پارامترهای جدید که باید اضافه شوند ***
    hasWidget: Boolean,
    onRequestWidget: () -> Unit
) {
    // اجبار راست‌چین (RTL) برای کل اپلیکیشن
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            bottomBar = {
                AppBottomNavigation(
                    currentScreen = currentScreen,
                    onScreenSelected = onScreenChange
                )
            },
            // دکمه FAB فقط در صفحه خانه نمایش داده شود
            floatingActionButton = {
                if (currentScreen == AppDestinations.HOME.route) {
                    // FAB اینجاست اما مدیریتش را به HomeContent می‌سپاریم یا همینجا هندل می‌کنیم
                    // برای تمیزی کد، FAB را داخل HomeContent می‌گذاریم تا مختص همان صفحه باشد
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                // انیمیشن نرم هنگام تغییر صفحات
                Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
                    when (screen) {
                        AppDestinations.HOME.route -> {
                            HomeContent(
                                beyts = beyts,
                                allTags = allTags,
                                loadTags = loadTags,
                                onAddNewTag = onAddNewTag,
                                onAddPoem = onAddPoem,
                                onEditPoem = onEditPoem,
                                onDeletePoem = onDeletePoem,
                                onMemorizePoem = onMemorizePoem,
                                onOpenSettings = onOpenSettings,
                                // ** اضافه شده: انتقال پارامترها به HomeContent **
                                shouldOpenAddDialog = shouldOpenAddDialog,
                                onAddDialogOpened = onAddDialogOpened,
// *** حالا اینجا مقادیر دریافتی را به HomeContent می‌دهیم ***
                                hasWidget = hasWidget,
                                onRequestWidget = onRequestWidget
                            )
                        }
                        AppDestinations.ALL_POEMS.route -> contentAllPoems()
                        AppDestinations.MY_POEMS.route -> contentMyNotes()
                        AppDestinations.SUGGESTIONS.route -> contentSuggestions()
                        AppDestinations.TAGS.route -> contentTagManager()
                        else -> { /* هندل کردن صفحات فرعی مثل settings یا detail */ }
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------
// *** محتوای صفحه اصلی (HomeContent) ***
// همان PoemListScreen قبلی که حالا تمیز شده است
// ------------------------------------------------------------------
@Composable
fun HomeContent(
    beyts: List<Beyt>,
    allTags: List<Tag>,
    loadTags: suspend (Int) -> List<Long>,
    onAddNewTag: (String, Long?) -> Unit,
    onAddPoem: (String, String, Int, String, String, String, String, String, String, List<Long>) -> Unit,
    onEditPoem: (Beyt, String, String, Int, String, String, String, String, String, String, List<Long>) -> Unit,
    onDeletePoem: (Beyt) -> Unit,
    onMemorizePoem: (Beyt) -> Unit,
    onOpenSettings: () -> Unit,
    shouldOpenAddDialog: Boolean,
    onAddDialogOpened: () -> Unit,
    // اضافه کردن پارامترهای جدید برای ویجت:
    hasWidget: Boolean,
    onRequestWidget: () -> Unit
) {
    var selectedDate by remember { mutableStateOf(PersianDate()) }
    val isToday = remember(selectedDate) {
        val today = PersianDate()
        selectedDate.shYear == today.shYear &&
                selectedDate.shMonth == today.shMonth &&
                selectedDate.shDay == today.shDay
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var isQuizMode by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    // ** اضافه شده: نظارت بر درخواست باز شدن دیالوگ **
    LaunchedEffect(shouldOpenAddDialog) {
        if (shouldOpenAddDialog) {
            showDialog = true
            onAddDialogOpened() // اطلاع می‌دهیم که باز شد تا دوباره باز نشود
        }
    }

    var editingBeyt by remember { mutableStateOf<Beyt?>(null) }
    val smartDefaults = remember(beyts) { calculateSmartDefaults(beyts) }

// وضعیت نمایش بنر (اگر کاربر بستش، دیگر نشان ندهیم)
    var showWidgetPromo by remember { mutableStateOf(true) }

    val todayBeyts = remember(beyts, selectedDate) {
        val dateStr = "${selectedDate.shYear}/${String.format("%02d", selectedDate.shMonth)}/${String.format("%02d", selectedDate.shDay)}"
        val dayOfWeekIndex = selectedDate.dayOfWeek()

        beyts.filter { beyt ->
            if (beyt.kind == 5) return@filter false
            when (beyt.kind) {
                1 -> true
                2 -> beyt.weekDays.getOrElse(dayOfWeekIndex) { '0' } == '1'
                3 -> {
                    val dayInMonth = beyt.startD.takeLast(2)
                    val selectedDay = String.format("%02d", selectedDate.shDay)
                    dayInMonth == selectedDay
                }
                4 -> {
                    beyt.startD <= dateStr && dateStr <= beyt.endD && beyt.weekDays.getOrElse(dayOfWeekIndex) { '0' } == '1'
                }
                else -> false
            }
        }
    }

    Scaffold(
        topBar = {
            Column {
                ModernTopBar(
                    currentDate = selectedDate,
                    isToday = isToday,
                    isQuizMode = isQuizMode,
                    onDateClick = { showDatePicker = true },
                    onReturnToToday = { selectedDate = PersianDate() },
                    onToggleQuizMode = { isQuizMode = !isQuizMode },
                    onSettingsClick = onOpenSettings
                )
                ModernCalendarStrip(centerDate = selectedDate, onDateSelected = { selectedDate = it })
                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { editingBeyt = null; showDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) { Icon(Icons.Default.Add, "افزودن") }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            if (todayBeyts.isEmpty()) {
                EmptyStateMessage(selectedDate)
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {


                    // *** اضافه کردن بنر ویجت در بالای لیست ***
                    // شرط: ویجت ندارد AND کاربر بنر را نبسته است
                    if (!hasWidget && showWidgetPromo) {
                        item {
                            WidgetPromoCard(
                                onAddClick = onRequestWidget,
                                onDismiss = { showWidgetPromo = false }
                            )
                        }
                    }

                    items(items = todayBeyts, key = { it.id ?: 0 }) { beyt ->
                        SwipeToDeleteContainer(item = beyt, onDelete = { onDeletePoem(beyt) }) {
                            PoemCard(
                                beyt = beyt,
                                isQuizMode = isQuizMode,
                                allTags = allTags,
                                loadTags = loadTags,
                                onClick = { editingBeyt = beyt; showDialog = true },
                                onMemorizeClick = { onMemorizePoem(beyt) }
                            )
                        }
                    }
                }
            }

            // دیالوگ افزودن/ویرایش
// ... داخل HomeContent در MainScreen.kt ...

// نام متغیر showDialog را ترجیحا به showAddSheet تغییر دهید تا خوانا باشد، یا همان showDialog را نگه دارید
            if (showDialog) {
                AddPoemBottomSheet(
                    existingBeyt = editingBeyt,
                    // مقادیر پیش‌فرض اسمارت که در کد قبلی داشتید
                    defaultStartT = smartDefaults.first,
                    defaultEndT = smartDefaults.second,

                    // ورودی‌های دیتابیس
                    allTags = allTags,
                    loadTags = loadTags,
                    onAddNewTag = onAddNewTag,

                    // بستن شیت
                    onDismiss = { showDialog = false },

                    // ذخیره کردن
                    onConfirm = { b1, b2, k, sT, eT, wD, sD, eD, sh, tags ->
                        if (editingBeyt == null) {
                            onAddPoem(b1, b2, k, sT, eT, wD, sD, eD, sh, tags)
                        } else {
                            onEditPoem(editingBeyt!!, b1, b2, k, sT, eT, wD, sD, eD, sh, tags)
                        }
                        showDialog = false
                    }
                )
            }

            if (showDatePicker) {
                PersianCalendarBottomSheet(
                    initialDate = selectedDate,
                    onDismiss = { showDatePicker = false },
                    onDateSelected = { date ->
                        selectedDate = date
                        showDatePicker = false
                    }
                )
            }
        }
    }
}

// ------------------------------------------------------------------
// *** هدر مدرن و تقویم (همان کدهای قبلی با کمی اصلاح) ***
// ------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernTopBar(
    currentDate: PersianDate,
    isToday: Boolean,
    isQuizMode: Boolean,
    onDateClick: () -> Unit,
    onReturnToToday: () -> Unit,
    onToggleQuizMode: () -> Unit,
    onSettingsClick: () -> Unit
) {
    TopAppBar(
        title = {
            Column(modifier = Modifier.clickable { onDateClick() }.padding(end = 8.dp)) {
                Text(text = "ازبر", style = MaterialTheme.typography.titleLarge)
                val dateText = "${currentDate.dayName()}، ${
                    currentDate.shDay.toString().toPersianDigits()
                } ${currentDate.monthName} ${currentDate.shYear.toString().toPersianDigits()}"

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = dateText,
                        style = MaterialTheme.typography.labelMedium,
                        // تغییر مهم: رنگ متن تاریخ از secondary (طلایی) به onSurfaceVariant تغییر کرد
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        // آیکون فلش می‌تواند طلایی بماند چون گرافیک است
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        },
        actions = {
            AnimatedVisibility(visible = !isToday, enter = fadeIn(), exit = fadeOut()) {
                IconButton(onClick = onReturnToToday) { Icon(Icons.Default.Today, "امروز", tint = MaterialTheme.colorScheme.primary) }
            }
            IconButton(onClick = onToggleQuizMode) {
                Icon(if (isQuizMode) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, "حالت مرور", tint = if (isQuizMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onSettingsClick) { Icon(Icons.Default.Settings, "تنظیمات") }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
    )
}

@Composable
fun ModernCalendarStrip(centerDate: PersianDate, onDateSelected: (PersianDate) -> Unit) {
    // محاسبه روزهای اطراف (۳ روز قبل و ۳ روز بعد)
    val days = remember(centerDate) {
        val list = mutableListOf<PersianDate>()
        val baseTime = centerDate.time
        val oneDayMillis = 86_400_000L
        for (i in -3..3) {
            val newTime = baseTime + (i * oneDayMillis)
            list.add(PersianDate(newTime))
        }
        list
    }

    // گرفتن تاریخ امروز واقعی برای مقایسه
    val today = remember { PersianDate() }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp, top = 4.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(days) { date ->
            // آیا این آیتم انتخاب شده است؟ (طبق تاریخی که کاربر در حال دیدن آن است)
            val isSelected = date.shDay == centerDate.shDay &&
                    date.shMonth == centerDate.shMonth &&
                    date.shYear == centerDate.shYear

            // آیا این آیتم "امروز" واقعی است؟
            val isTodayItem = date.shDay == today.shDay &&
                    date.shMonth == today.shMonth &&
                    date.shYear == today.shYear

            Surface(
                onClick = { onDateSelected(date) },
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .width(54.dp)
                    .height(75.dp),
                // بردر را حذف کردیم تا ظاهر تمیزتر شود و به جای آن از نقطه استفاده می‌کنیم
                border = null
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // نام روز هفته (ش، ی، د...)
                    Text(
                        text = date.dayName().take(1),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.alpha(0.7f)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // شماره روز
                    Text(
                        text = "${date.shDay}".toPersianDigits(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // نشانگر "امروز" (نقطه کوچک)
                    if (isTodayItem) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                // اگر آیتم سلکت شده باشد، نقطه باید رنگ متضاد (onPrimary) باشد
                                // اگر سلکت نشده باشد، نقطه باید رنگ اصلی (Primary) باشد تا دیده شود
                                .background(if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary)
                        )
                    } else {
                        // برای حفظ ارتفاع ثابت، اگر امروز نیست یک فضای خالی هم‌اندازه می‌گذاریم
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

// توابع کمکی تبدیل تاریخ و اعداد
fun String.toPersianDigits(): String = this.replace("0", "۰").replace("1", "۱").replace("2", "۲").replace("3", "۳").replace("4", "۴").replace("5", "۵").replace("6", "۶").replace("7", "۷").replace("8", "۸").replace("9", "۹")
fun getMonthName(month: Int): String = when(month) { 1->"فروردین" 2->"اردیبهشت" 3->"خرداد" 4->"تیر" 5->"مرداد" 6->"شهریور" 7->"مهر" 8->"آبان" 9->"آذر" 10->"دی" 11->"بهمن" 12->"اسفند" else->"" }
fun calculateSmartDefaults(beyts: List<Beyt>): Pair<String, String> {
    // فقط شعرهای فعال را بررسی می‌کنیم (شعرهای حفظ شده یا غیرفعال معمولاً نباید مانع پیشنهاد تایم شوند)
    // اما طبق منطق شما، "اگر شعری وجود داشته باشد"، پس همه انواع فعال را چک می‌کنیم.
    val activeList = beyts.filter { it.kind != 5 }

    // ۱. بررسی بازه‌های ساعتی از ۸ صبح تا ۱۲ شب (۲۳ تا ۰۰)
    // حلقه از ساعت ۸ تا ۲۳
    for (hour in 8..23) {
        // چک می‌کنیم آیا شعری هست که ساعت شروعش با این ساعت یکی باشد؟
        // مثلا برای ساعت ۸، دنبال شعری می‌گردیم که startT آن با "08" شروع شود
        val prefix = String.format("%02d", hour)
        val hasPoemInThisSlot = activeList.any { it.startT.startsWith(prefix) }

        if (!hasPoemInThisSlot) {
            val startT = String.format("%02d:00", hour)
            val nextHour = if (hour == 23) 0 else hour + 1
            val endT = String.format("%02d:00", nextHour)
            return startT to endT
        }
    }

    // ۲. بررسی بازه شبانه (۰۰:۰۰ تا ۰۷:۰۰)
    // اگر شعری باشد که ساعت شروعش بین ۰ تا ۶ باشد (یعنی ۰۰ تا ۰۶:۵۹)
    val hasNightPoem = activeList.any {
        val h = it.startT.take(2).toIntOrNull() ?: -1
        h in 0..6
    }

    if (!hasNightPoem) {
        return "00:00" to "07:00"
    }

    // ۳. اگر همه پر بودند، طبق درخواست شما برمی‌گردد به ۸ تا ۹
    return "08:00" to "09:00"
}
// کامپوننت‌های کمکی (SwipeToDelete, Dialogs, EmptyState)
// همان‌هایی که در کد قبلی MainScreen بود را اینجا هم نگه دارید یا به فایل PoemComponents منتقل کنید.
// برای جلوگیری از طولانی شدن بیش از حد، کد SwipeToDelete, PersianDatePickerDialog و EmptyStateMessage را اینجا دوباره تکرار نمی‌کنم چون در پاسخ قبلی بودند.
// لطفا مطمئن شوید آنها در فایل وجود دارند.


@Composable
fun PersianDatePickerDialog(initialDate: PersianDate, onDismiss: () -> Unit, onDateSelected: (PersianDate) -> Unit) {
    var currentYear by remember { mutableIntStateOf(initialDate.shYear) }
    var currentMonth by remember { mutableIntStateOf(initialDate.shMonth) }

    // اجبار RTL برای چیدمان صحیح
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Dialog(onDismissRequest = onDismiss) {
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // هدر تقویم
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween, // فاصله بین المان‌ها
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // دکمه سمت راست: ماه قبل (Past) -> فلش راست
                        IconButton(onClick = {
                            if (currentMonth > 1) currentMonth-- else { currentMonth = 12; currentYear-- }
                        }) {
                            Icon(Icons.Default.KeyboardArrowRight, "ماه قبل")
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = getMonthName(currentMonth), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(text = "$currentYear".toPersianDigits(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                        }

                        // دکمه سمت چپ: ماه بعد (Future) -> فلش چپ
                        IconButton(onClick = {
                            if (currentMonth < 12) currentMonth++ else { currentMonth = 1; currentYear++ }
                        }) {
                            Icon(Icons.Default.KeyboardArrowLeft, "ماه بعد")
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // بدنه تقویم (Grid)
                    LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.height(240.dp)) {
                        val startDayOfWeek = PersianDate().apply { shYear = currentYear; shMonth = currentMonth; shDay = 1 }.dayOfWeek()
                        val daysInMonth = if (currentMonth <= 6) 31 else if (currentMonth <= 11) 30 else if (PersianDate().isLeap(currentYear)) 30 else 29

                        items(listOf("ش", "ی", "د", "س", "چ", "پ", "ج")) {
                            Text(text = it, textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                        }

                        items(startDayOfWeek) { Spacer(modifier = Modifier) }

                        items(daysInMonth) { index ->
                            val day = index + 1
                            val isSelected = (day == initialDate.shDay && currentMonth == initialDate.shMonth && currentYear == initialDate.shYear)
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .padding(2.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    .clickable { onDateSelected(PersianDate().apply { shYear = currentYear; shMonth = currentMonth; shDay = day }) }
                            ) {
                                Text(
                                    text = "$day".toPersianDigits(),
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable fun EmptyStateMessage(date: PersianDate) { Column(Modifier.fillMaxSize().padding(bottom = 100.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { Icon(Icons.Default.MenuBook, null, modifier = Modifier.size(64.dp).alpha(0.3f), tint = MaterialTheme.colorScheme.primary); Spacer(modifier = Modifier.height(16.dp)); Text("برای این تاریخ شعری ندارید", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant) } }

@Composable
fun WidgetPromoCard(
    onAddClick: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), // رنگ ملایم
        ),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // بخش هدر: عنوان و دکمه بستن
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "مرور شعر بدون باز کردن برنامه",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "ویجت را به صفحه اصلی اضافه کنید تا همیشه چشمتان به ابیات باشد.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "بستن",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // بخش بصری: شبیه‌سازی ویجت روی والپیپر
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF2B32B2), // آبی تیره
                                Color(0xFF1488CC)  // آبی روشن (شبیه آسمان/والپیپر)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // شبیه‌سازی ظاهر ویجت (همانطور که در PoemWidget.kt طراحی کردید)
                Column(
                    modifier = Modifier
                        .width(200.dp) // عرض فرضی ویجت
                        .padding(8.dp)
                        .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "مرا عهدیست با جانان...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "که تا جان در بدن دارم...",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // دکمه اقدام (Call to Action)
            Button(
                onClick = onAddClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.Widgets, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("افزودن ویجت به صفحه اصلی")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersianCalendarBottomSheet(
    initialDate: PersianDate,
    onDismiss: () -> Unit,
    onDateSelected: (PersianDate) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var currentYear by remember { mutableIntStateOf(initialDate.shYear) }
    var currentMonth by remember { mutableIntStateOf(initialDate.shMonth) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp)
            ) {
                // هدر تقویم
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // دکمه سمت راست (ماه قبل)
                    IconButton(onClick = {
                        if (currentMonth > 1) currentMonth-- else { currentMonth = 12; currentYear-- }
                    }) {
                        Icon(Icons.Default.KeyboardArrowRight, "ماه قبل")
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = getMonthName(currentMonth),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$currentYear".toPersianDigits(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    // دکمه سمت چپ (ماه بعد)
                    IconButton(onClick = {
                        if (currentMonth < 12) currentMonth++ else { currentMonth = 1; currentYear++ }
                    }) {
                        Icon(Icons.Default.KeyboardArrowLeft, "ماه بعد")
                    }
                }

                // ... (بقیه کدهای Grid روزها که درست بودند)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.height(280.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // ... کدهای نمایش روزها تغییری نکرده‌اند ...
                    // (همان کدی که در سوال قبلی فرستادید را اینجا قرار دهید، فقط فلش‌ها عوض شدند)
                    val weekDays = listOf("ش", "ی", "د", "س", "چ", "پ", "ج")
                    items(weekDays.size) { index ->
                        Text(text = weekDays[index], textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                    }

                    val startDayOfWeek = PersianDate().apply { shYear = currentYear; shMonth = currentMonth; shDay = 1 }.dayOfWeek()
                    val daysInMonth = if (currentMonth <= 6) 31 else if (currentMonth <= 11) 30 else if (PersianDate().isLeap(currentYear)) 30 else 29

                    items(startDayOfWeek) { Spacer(modifier = Modifier) }

                    items(daysInMonth) { index ->
                        val day = index + 1
                        val isSelected = (day == initialDate.shDay && currentMonth == initialDate.shMonth && currentYear == initialDate.shYear)
                        val today = PersianDate()
                        val isToday = (day == today.shDay && currentMonth == today.shMonth && currentYear == today.shYear)

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .border(1.dp, if (isToday && !isSelected) MaterialTheme.colorScheme.primary else Color.Transparent, CircleShape)
                                .clickable {
                                    onDateSelected(PersianDate().apply { shYear = currentYear; shMonth = currentMonth; shDay = day })
                                }
                        ) {
                            Text(
                                text = "$day".toPersianDigits(),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}
