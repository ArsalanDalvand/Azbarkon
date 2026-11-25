package com.arsalandalvand.azbarkon

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.arsalandalvand.azbarkon.ui.theme.AzbarkonTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.app.NotificationManagerCompat
import androidx.compose.ui.platform.LocalContext
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import androidx.glance.appwidget.GlanceAppWidgetManager


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = AppDatabase.getDatabase(this)
        val beytDao = db.beytDao()
        val settings = SettingsManager(this)
        val noteDao = db.noteDao()
        val tagDao = db.tagDao()

        setContent {
            var isDarkTheme by remember { mutableStateOf(settings.isDarkMode) }

            // ساخت ViewModel با استفاده از Factory (چون ورودی دارد)
            val noteViewModel: NoteViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return NoteViewModel(noteDao, tagDao) as T
                    }
                }
            )

            // *** این بخش را اضافه کنید ***
            val tagViewModel: TagViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return TagViewModel(tagDao) as T
                    }
                }
            )

            // ** اضافه شده: متغیری برای کنترل باز شدن خودکار دیالوگ **
            var autoOpenAddDialog by remember { mutableStateOf(false) }

            // ---------------------------------------------------------
            // *** بخش جدید: مدیریت مجوز نوتیفیکیشن ***
            // ---------------------------------------------------------
            var showPermissionDialog by remember { mutableStateOf(false) }
            val context = LocalContext.current

            // لانچر برای درخواست مجوز در اندروید 13 به بالا
            val requestPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (!isGranted) {
                    // اگر کاربر رد کرد، می‌توانیم یک پیام اسنک‌بار نشان دهیم یا بیخیال شویم
                    // اما چون دیالوگ خودمان "بعداً" داشت، اینجا کار خاصی لازم نیست
                }
                showPermissionDialog = false
            }

            // بررسی وضعیت مجوز به محض اجرای برنامه
            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    // برای اندروید 13+: چک کردن پرمیشن runtime
                    val permissionCheck = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    )
                    if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                        showPermissionDialog = true
                    }
                } else {
                    // برای اندرویدهای قدیمی‌تر: چک کردن کلی وضعیت نوتیفیکیشن
                    if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                        showPermissionDialog = true
                    }
                }
            }

            // نمایش دیالوگ در صورت نیاز
            if (showPermissionDialog) {
                NotificationPermissionDialog(
                    onDismiss = { showPermissionDialog = false },
                    onConfirm = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            // درخواست مستقیم پرمیشن در اندروید 13
                            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            // هدایت به تنظیمات در اندرویدهای قدیمی
                            openNotificationSettings(context)
                            showPermissionDialog = false
                        }
                    }
                )
            }
            // ---------------------------------------------------------
            // *** پایان بخش مجوز ***
            // ---------------------------------------------------------

            // بررسی Intent هنگام شروع برنامه
            LaunchedEffect(Unit) {
                // چک می‌کنیم آیا کلیدی که در ویجت ست کردیم (OPEN_ADD_POEM) مقدار true دارد یا خیر
                // توجه: گلس پارامترها را به عنوان Intent Extra می‌فرستد.
                // نام کلید در گلس دقیقاً همان رشته‌ای است که در ActionParameters.Key تعریف کردید.
                val fromWidget = intent?.extras?.getBoolean("OPEN_ADD_POEM") ?: false
                if (fromWidget) {
                    autoOpenAddDialog = true
                    // پاک کردن اکسترا برای اینکه با چرخش صفحه دوباره باز نشود
                    intent?.removeExtra("OPEN_ADD_POEM")
                }
            }

            AzbarkonTheme(darkTheme = isDarkTheme) {
                var poemList by remember { mutableStateOf(listOf<Beyt>()) }
                var currentScreen by remember { mutableStateOf(AppDestinations.HOME.route) }
                val scope = rememberCoroutineScope()

                // تغییر مهم: نوع داده انتخابی باید NoteWithTags باشد نه Note خالی
                var selectedNoteWithTags by remember { mutableStateOf<NoteWithTags?>(null) }

                val allTags by tagDao.getAllTags().collectAsState(initial = emptyList())

                // --- اکشن‌های مربوط به ابیات (بدون تغییر) ---
                val loadTagsAction: suspend (Int) -> List<Long> = { beytId -> tagDao.getTagIdsForBeyt(beytId) }
                val createTagAction: (String, Long?) -> Unit = { title, parentId -> scope.launch(Dispatchers.IO) { tagDao.insertTag(Tag(title = title, parentId = parentId)) } }

                val addPoemAction: (String, String, Int, String, String, String, String, String, String, List<Long>) -> Unit = { b1, b2, k, sT, eT, wDays, sD, eD, shVal, selectedTags ->
                    scope.launch(Dispatchers.IO) {
                        val newBeytId = beytDao.insertBeyt(Beyt(beyt1 = b1, beyt2 = b2, kind = k, startT = sT, endT = eT, weekDays = wDays, startD = sD, endD = eD, sh = shVal, repeat = "1")).toInt()
                        selectedTags.forEach { tagId -> tagDao.insertBeytTagCrossRef(BeytTagCrossRef(newBeytId, tagId)) }
                        reloadServiceData(); val updated = beytDao.getAllBeyts(); withContext(Dispatchers.Main) { poemList = updated }
                    }
                }

                val editPoemAction: (Beyt, String, String, Int, String, String, String, String, String, String, List<Long>) -> Unit = { oldBeyt, b1, b2, k, sT, eT, wDays, sD, eD, shVal, selectedTagIds ->
                    scope.launch(Dispatchers.IO) {
                        beytDao.updateBeyt(oldBeyt.copy(beyt1 = b1, beyt2 = b2, kind = k, startT = sT, endT = eT, weekDays = wDays, startD = sD, endD = eD, sh = shVal))
                        oldBeyt.id?.let { bId -> tagDao.clearTagsForBeyt(bId); selectedTagIds.forEach { tagId -> tagDao.insertBeytTagCrossRef(BeytTagCrossRef(bId, tagId)) } }
                        reloadServiceData(); val updated = beytDao.getAllBeyts(); withContext(Dispatchers.Main) { poemList = updated }
                    }
                }

                val deletePoemAction: (Beyt) -> Unit = { beytToDelete ->
                    scope.launch(Dispatchers.IO) { beytDao.deleteBeyt(beytToDelete); reloadServiceData(); val updated = beytDao.getAllBeyts(); withContext(Dispatchers.Main) { poemList = updated } }
                }

                val memorizeAction: (Beyt) -> Unit = { beyt ->
                    scope.launch(Dispatchers.IO) { beytDao.updateBeyt(beyt.copy(kind = 5)); reloadServiceData(); val updated = beytDao.getAllBeyts(); withContext(Dispatchers.Main) { poemList = updated } }
                }

                LaunchedEffect(Unit) {
                    scope.launch(Dispatchers.IO) { val list = beytDao.getAllBeyts(); withContext(Dispatchers.Main) { poemList = list } }
                    checkServiceState(settings.isNotificationEnabled)
                }

                var hasWidgetInstalled by remember { mutableStateOf(true) }

// از LocalLifecycleOwner برای تشخیص بازگشت کاربر به برنامه استفاده می‌کنیم
                val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

                DisposableEffect(lifecycleOwner) {
                    val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                        if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                            // هر بار که کاربر به برنامه برمی‌گردد چک می‌کنیم
                            scope.launch {
                                hasWidgetInstalled = hasWidgetInstalled()
                            }
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }

                // ----------------------------------------------------
                // *** مدیریت صفحات ***
                // ----------------------------------------------------

                if (currentScreen == "settings") {
                    SettingsScreen(
                        onBack = { currentScreen = AppDestinations.HOME.route },
                        onThemeChanged = { isDark -> isDarkTheme = isDark },
                        onNotificationChanged = { isEnabled -> checkServiceState(isEnabled) }
                    )
                } else if (currentScreen == "note_detail") {
                    // تغییر مهم: استفاده از ساختار جدید NoteDetailScreen با ViewModel
                    NoteDetailScreen(
                        viewModel = noteViewModel,
                        noteIdToEdit = selectedNoteWithTags?.note?.id, // آیدی یادداشت برای ویرایش
                        existingNote = selectedNoteWithTags, // آبجکت کامل برای نمایش تگ‌ها
                        onBack = { currentScreen = AppDestinations.MY_POEMS.route }
                    )
                } else {
                    // نمایش صفحه اصلی با نوار ناوبری
                    MainScreenContainer(
                        currentScreen = currentScreen,
                        onScreenChange = { route -> currentScreen = route },
                        beyts = poemList,
                        allTags = allTags,
                        loadTags = loadTagsAction,
                        onAddNewTag = createTagAction,
                        onAddPoem = addPoemAction,
                        onEditPoem = editPoemAction,
                        onDeletePoem = deletePoemAction,
                        onMemorizePoem = memorizeAction,
                        onOpenSettings = { currentScreen = "settings" },
                        // ** اضافه شده: پاس دادن وضعیت باز شدن خودکار **
                        shouldOpenAddDialog = autoOpenAddDialog,
                        onAddDialogOpened = { autoOpenAddDialog = false }, // کال‌بک برای ریست کردن وضعیت

                        // *** اضافه کردن پارامترهای جدید در اینجا ***
                        hasWidget = hasWidgetInstalled, // متغیر وضعیتی که در MainActivity تعریف کردید
                        onRequestWidget = { requestPinWidget() }, // تابعی که در MainActivity تعریف کردید



                        contentAllPoems = {
                            MyPoemsScreen(
                                allBeyts = poemList,
                                allTags = allTags,
                                loadTags = loadTagsAction,
                                onAddNewTag = createTagAction,
                                onBack = { currentScreen = AppDestinations.HOME.route },
                                onDeletePoem = deletePoemAction,
                                onEditPoem = editPoemAction,
                                onMemorizePoem = memorizeAction
                            )
                        },
                        contentMyNotes = {
                            // تغییر مهم: استفاده از NoteListScreen جدید که خودش ViewModel دارد
                            // دیگر نیازی به فیلتر دستی لیست نوت‌ها در اینجا نیست
                            NoteListScreen(
                                viewModel = noteViewModel,
                                onAddNote = {
                                    selectedNoteWithTags = null // نال یعنی یادداشت جدید
                                    currentScreen = "note_detail"
                                },
                                onNoteClick = { noteWithTags ->
                                    selectedNoteWithTags = noteWithTags // ست کردن یادداشت برای ویرایش
                                    currentScreen = "note_detail"
                                },
                                onBack = { /* در تب اصلی بک نداریم */ }
                            )
                        },
                        contentSuggestions = {
                            SuggestionScreen(
                                onBack = { /* در تب اصلی بک نداریم */ },
                                onAddPoem = { beyt -> scope.launch(Dispatchers.IO) { beytDao.insertBeyt(beyt); reloadServiceData(); val updated = beytDao.getAllBeyts(); withContext(Dispatchers.Main) { poemList = updated } } }
                            )
                        },
                        // *** اضافه کردن این خط جدید ***
                        contentTagManager = {
                            TagManagerScreen(
                                viewModel = tagViewModel,
                                onBack = {
                                    // اگر کاربر در تب تگ دکمه بازگشت زد، برود به خانه
                                    currentScreen = AppDestinations.HOME.route
                                }
                            )
                        }
                    )
                }
            }
        }
    }

    private fun reloadServiceData() {
        val intent = Intent(this, PoemForegroundService::class.java)
        intent.action = PoemForegroundService.ACTION_RELOAD_DATA
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) { startForegroundService(intent) } else { startService(intent) }
    }

    private fun checkServiceState(isEnabled: Boolean) {
        val intent = Intent(this, PoemForegroundService::class.java)
        if (isEnabled) { if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) { startForegroundService(intent) } else { startService(intent) } } else { stopService(intent) }
    }
    /**
     * بررسی می‌کند آیا ویجتی از این برنامه روی صفحه کاربر وجود دارد یا خیر
     */
    private suspend fun hasWidgetInstalled(): Boolean {
        val manager = GlanceAppWidgetManager(this)
        // لیست تمام ویجت‌های فعال مربوط به کلاس PoemWidget را می‌گیرد
        val glanceIds = manager.getGlanceIds(PoemWidget::class.java)
        return glanceIds.isNotEmpty()
    }

    /**
     * درخواست پین کردن ویجت به صفحه اصلی (مخصوص اندروید ۸+)
     */
    private fun requestPinWidget() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val appWidgetManager = AppWidgetManager.getInstance(this)

            // نکته مهم: اینجا باید کلاس Receiver ویجت را بدهید، نه خود کلاس PoemWidget را.
            // معمولاً نامش PoemWidgetReceiver است که در AndroidManifest ثبت شده.
            // اگر نام دیگری دارد، آن را جایگزین کنید.
            val myProvider = ComponentName(this, PoemWidgetReceiver::class.java)

            if (appWidgetManager.isRequestPinAppWidgetSupported) {
                // این متد یک دیالوگ سیستمی باز می‌کند و از کاربر می‌پرسد "آیا ویجت اضافه شود؟"
                appWidgetManager.requestPinAppWidget(myProvider, null, null)
            }
        } else {
            // برای اندرویدهای قدیمی‌تر (کمتر از ۸) نمی‌توان درخواست مستقیم داد.
            // می‌توانید یک توست یا دیالوگ آموزشی نمایش دهید.
            // Toast.makeText(this, "لطفاً ویجت را از منوی گوشی اضافه کنید", Toast.LENGTH_LONG).show()
        }
    }
}