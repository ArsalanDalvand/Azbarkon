// این بسته (package) شناسه‌ی منحصر به فرد کد شما در پروژه است. با استفاده از این شناسه، اندروید و ابزارهای دیگر، کدهای شما را از کدهای سایر برنامه‌ها و کتابخانه‌ها متمایز می‌کنند.
package com.arsalandalvand.azbarkon

// وارد کردن کلاس‌ها و توابع مورد نیاز از کتابخانه‌های اندروید و سایر کتابخانه‌ها.
// هر import به برنامه اجازه می‌دهد تا از کدی که توسط دیگران نوشته شده، استفاده کند.

import android.app.Notification
import android.app.NotificationChannel // برای ساخت "کانال نوتیفیکیشن". در اندروید ۸ (Oreo) و بالاتر، هر نوتیفیکیشن باید به یک کانال تعلق داشته باشد.
import android.app.NotificationManager // این کلاس برای مدیریت نوتیفیکیشن‌ها (ایجاد کانال، نمایش، حذف نوتیفیکیشن) به کار می‌رود.
import android.app.Service // کلاس پایه‌ای برای ساخت انواع "سرویس" در اندروید. سرویس کامپوننتی است که برای کارهای طولانی‌مدت در پس‌زمینه استفاده می‌شود.
import android.content.Context // این کلاس امکان دسترسی به اطلاعات و سرویس‌های کلی برنامه (مانند دسترسی به منابع، SharedPreferences و...) را فراهم می‌کند.
import android.content.Intent // از این کلاس برای ارسال پیام و جابجایی داده بین کامپوننت‌های مختلف اندروید (مثل Activity, Service, BroadcastReceiver) استفاده می‌شود.
import android.os.Build // برای دسترسی به اطلاعات نسخه اندروید دستگاه. مثلاً برای اجرای کدهای شرطی که فقط روی نسخه‌های خاصی از اندروید کار می‌کنند.
import android.os.IBinder // این یک رابط کاربری برای "سرویس‌های متصل" (Bound Services) است که در این پروژه استفاده نمی‌شود، اما پیاده‌سازی متد onBind برای هر سرویسی الزامی است.
import androidx.core.app.NotificationCompat // یک کلاس کمکی از کتابخانه Jetpack Core که ساخت نوتیفیکیشن‌ها را ساده‌تر کرده و سازگاری با نسخه‌های قدیمی‌تر اندروید را تضمین می‌کند.
import androidx.glance.appwidget.updateAll // تابعی از کتابخانه Glance برای ارسال درخواست به‌روزرسانی به تمام ویجت‌های اپلیکیشن که با Glance ساخته شده‌اند.
import kotlinx.coroutines.* // وارد کردن تمام ابزارهای مربوط به کتابخانه کوروتین‌های کاتلین. کوروتین‌ها برای مدیریت عملیات همزمان و اجرای کد در پس‌زمینه به روشی ساده و خوانا استفاده می‌شوند.
import saman.zamani.persiandate.PersianDate // وارد کردن یک کتابخانه خارجی برای کار با تاریخ و زمان شمسی.
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import android.util.Log // اضافه شد برای دیباگ
import android.app.PendingIntent // برای ایجاد Intent که در آینده (توسط نوتیفیکیشن) اجرا می‌شود
import android.content.BroadcastReceiver // برای دریافت پیام‌های سیستمی (مثل خاموش شدن صفحه)
import android.content.IntentFilter // برای فیلتر کردن پیام‌های برودکست خاص
import android.hardware.Sensor // کلاس پایه سنسورها
import android.hardware.SensorEvent // رویداد تغییر مقادیر سنسور
import android.hardware.SensorEventListener // رابط برای شنیدن تغییرات سنسور
import android.hardware.SensorManager // مدیریت کلی سنسورهای دستگاه
import android.os.PowerManager // برای مدیریت مصرف برق و بیدار نگه داشتن پردازنده (WakeLock)
import kotlin.math.sqrt // تابع ریاضی برای محاسبه جذر (برای فرمول شتاب)
import android.content.pm.ServiceInfo // *** ایمپورت حیاتی برای اندروید ۱۴ ***
import android.os.Handler
import android.os.Looper

/**
 * این یک "سرویس پیش‌زمینه" (Foreground Service) است.
 * سرویس‌ها برای انجام کارهای طولانی‌مدت در پس‌زمینه استفاده می‌شوند، حتی اگر کاربر از برنامه خارج شود.
 * یک سرویس "پیش‌زمینه" نوع خاصی از سرویس است که کاربر از وجود آن آگاه است، زیرا یک نوتیفیکیشن دائمی به او نمایش داده می‌شود.
 * اندروید به این نوع سرویس‌ها اولویت بالاتری می‌دهد و به راحتی آن‌ها را متوقف نمی‌کند (مثلاً به دلیل کمبود حافظه).
 * وظیفه این سرویس، بررسی مداوم زمان و پیدا کردن شعر مناسب برای نمایش در ویجت و نوتیفیکیشن است.
 * قابلیت‌های جدید اضافه شده:
 *  * ۱. مدیریت سنسور شتاب‌سنج برای تشخیص برداشتن گوشی.
 *  * ۲. مدیریت هوشمند باتری (سنسور فقط وقتی صفحه خاموش است فعال می‌شود).
 *  * ۳. استفاده از "Full Screen Intent" برای باز کردن صفحه روی قفل گوشی (استاندارد جدید اندروید).
 *
 */

class PoemForegroundService : Service(), SensorEventListener {
    // تعریف کلاس سرویس که از کلاس Service اندروید ارث‌بری می‌کند.

    // یک CoroutineScope برای مدیریت چرخه حیات کوروتین‌های این سرویس.
    // MainScope یک Scope پیش‌فرض است که به Dispatchers.Main متصل است اما می‌توان با آن Jobهای فرزند را روی ترد‌های دیگر اجرا کرد.
    // استفاده از SupervisorJob به این معنی است که اگر یکی از کارهای فرزند (child job) شکست بخورد، سایر کارها و خود Scope لغو نمی‌شوند.
    private val serviceScope = MainScope()

    // اضافه کردن این متغیر برای کنترل حلقه
    private var updateJob: Job? = null

    // یک متغیر برای نگهداری اندیس شعر بعدی که باید نمایش داده شود.
    // این متغیر به ما کمک می‌کند تا در لیست اشعار قابل نمایش حرکت کنیم.
    private var poemIndex = 0

    // شناسه‌ی آخرین شعری که نمایش داده شده است.
    // این متغیر برای بهینه‌سازی استفاده می‌شود تا از آپدیت‌های تکراری نوتیفیکیشن و ویجت با یک شعر یکسان جلوگیری شود.
    private var lastDisplayedPoemId: Int? = null

    // این دو متغیر همیشه آخرین شعر انتخاب شده را در حافظه رم نگه می‌دارند.
    // ما به این‌ها نیاز داریم تا وقتی سنسور عمل کرد، سریعاً (بدون مراجعه به دیتابیس) متن شعر را به نوتیفیکیشن بدهیم.
    private var currentM1: String = "در حال بارگذاری..."
    private var currentM2: String = "..."

    // --- متغیرهای مربوط به سنسور و مدیریت انرژی (NEW) ---
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    // WakeLock ابزاری است که اجازه می‌دهد وقتی سنسور حرکتی حس کرد،
    // پردازنده (CPU) گوشی را برای چند ثانیه بیدار نگه داریم تا بتوانیم صفحه را روشن کنیم.
    private var wakeLock: PowerManager.WakeLock? = null

    // متغیرهای محاسبه شتاب برای تشخیص تکان خوردن
    private var accelerationCurrentValue = 0f
    private var accelerationPreviousValue = 0f

    // آستانه حساسیت (Shake Threshold).
    // اگر این عدد را کمتر کنید (مثلا ۱۰)، با کوچکترین لرزشی صفحه روشن می‌شود.
    // اگر بیشتر کنید (مثلا ۱۵)، باید گوشی را محکم‌تر تکان دهید. ۱۱.۵ عدد متعادلی برای "برداشتن از روی میز" است.
    private val SHAKE_THRESHOLD = 11.5f

    // برای جلوگیری از اجراهای پشت سر هم (Debouncing)
    private var lastShakeTime: Long = 0

    // --- دریافت‌کننده وضعیت صفحه نمایش (BroadcastReceiver) ---
    // این قسمت برای صرفه‌جویی در باتری حیاتی است.
    // ما نمی‌خواهیم وقتی کاربر با گوشی کار می‌کند (صفحه روشن است)، سنسور شتاب‌سنج فعال باشد.
    private val screenStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    // وقتی صفحه خاموش شد -> سنسور را روشن کن تا منتظر برداشتن گوشی باشیم.
                    Log.d(TAG, "Screen OFF detected: Registering Accelerometer")
                    registerSensor()
                }

                Intent.ACTION_SCREEN_ON -> {
                    // وقتی صفحه روشن شد -> سنسور را خاموش کن (چون نیازی به Wakeup نیست و باتری مصرف می‌شود).
                    Log.d(TAG, "Screen ON detected: Unregistering Accelerometer")
                    unregisterSensor()
                }
            }
        }
    }

    // اضافه کردن متغیر برای درخواست ریلود
    @Volatile
    private var reloadRequested = false

    // companion object یک بلوک است که اعضای آن به کلاس تعلق دارند، نه به یک نمونه (instance) از کلاس. معادل اعضای static در جاوا.
    companion object {
        // یک شناسه‌ی منحصر به فرد برای نوتیفیکیشن این سرویس. اگر با همین ID نوتیفیکیشن جدیدی بفرستیم، نوتیفیکیشن قبلی آپدیت می‌شود.
        private const val NOTIFICATION_ID = 1

        // شناسه‌ی منحصر به فرد برای کانال نوتیفیکیشن. این ID برای اتصال نوتیفیکیشن به کانال مربوطه‌اش استفاده می‌شود.
// کانال اول: برای نمایش دائمی و ساکت
        private const val CHANNEL_ID_SILENT = "poem_service_silent_channel"

        // کانال دوم: برای بیدار کردن صفحه (همان کانال قبلی یا جدید)
        private const val CHANNEL_ID_WAKEUP = "poem_wakeup_channel_final"

        // نام کانال نوتیفیکیشن که در تنظیمات نوتیفیکیشن برنامه به کاربر نمایش داده می‌شود.
        private const val CHANNEL_NAME = "Poem Smart Service"

        // نام فایل SharedPreferences که برای ذخیره‌سازی داده‌های ساده (مثل آخرین شعر) استفاده می‌شود.
        private const val PREFS_NAME = "poem_prefs"

        // کلید برای ذخیره‌سازی مصراع اول شعر در SharedPreferences.
        private const val KEY_LAST_M1 = "last_m1"

        // کلید برای ذخیره‌سازی مصراع دوم شعر در SharedPreferences.
        private const val KEY_LAST_M2 = "last_m2"

        // فاصله زمانی بین هر بار بررسی برای آپدیت شعر، به میلی‌ثانیه. در اینجا ۲۰ ثانیه تنظیم شده است.
        private const val UPDATE_INTERVAL_MS = 20000L

        private const val TAG = "PoemService" // تگ برای لاگ‌ها

        // ۱. تعریف یک اکشن جدید برای دستور ریلود
        const val ACTION_RELOAD_DATA = "com.arsalandalvand.azbarkon.ACTION_RELOAD_DATA"
    }

    /**
     * متد onCreate اولین جایی است که سرویس ساخته می‌شود.
     * بهترین جا برای مقداردهی اولیه ابزارها (سنسور، پاور منیجر، رسیورها) است.
     */
    override fun onCreate() {
        super.onCreate()

        // ۱. ساخت کانال نوتیفیکیشن همین اول کار (بسیار مهم برای جلوگیری از کراش)
        createNotificationChannel()

        // ۱. دریافت سرویس مدیریت سنسورها
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        // ۲. دریافت سنسور شتاب‌سنج (Accelerometer)
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // مقداردهی اولیه مقادیر شتاب (جاذبه زمین حدود ۹.۸ است)
        accelerationCurrentValue = SensorManager.GRAVITY_EARTH
        accelerationPreviousValue = SensorManager.GRAVITY_EARTH

        // ۳. تنظیم WakeLock
        // PARTIAL_WAKE_LOCK یعنی: CPU روشن بماند، اما صفحه نمایش می‌تواند خاموش باشد.
        // ما از این استفاده می‌کنیم تا وقتی تکان خورد، CPU بیدار شود و سپس ما دستور روشن شدن صفحه را بدهیم.
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock =
            powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AzBarkon:SensorWakeLock")

        // ۴. ثبت رسیور برای فهمیدن خاموش/روشن شدن صفحه
        val screenFilter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF) // خبر بده وقتی صفحه خاموش شد
            addAction(Intent.ACTION_SCREEN_ON)  // خبر بده وقتی صفحه روشن شد
        }
        registerReceiver(screenStateReceiver, screenFilter)
    }

    /**
     * این متد زمانی فراخوانی می‌شود که سرویس توسط کامپوننتی دیگر (مثلاً یک Activity یا BroadcastReceiver) با دستور startService() شروع به کار می‌کند.
     * این متد قلب تپنده سرویس ما است و منطق اصلی در آن قرار دارد.
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        // *** اصلاح حیاتی برای اندروید ۱۴ و ۱۵ ***
        // ما باید سرویس را فوراً به حالت Foreground ببریم.
        // در اندرویدهای جدید، باید نوع سرویس (dataSync) را هم مشخص کنیم.
        val notification = buildNotification(currentM1, currentM2, false)

        try {
            if (Build.VERSION.SDK_INT >= 34) { // Android 14+
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start foreground service", e)
            // اگر نتوانستیم فورگراند شویم، بهتر است سرویس را متوقف کنیم تا کراش نکند
            stopSelf()
            return START_NOT_STICKY
        }

        // ۱. قبل از هرچیز، کانال نوتیفیکیشن را می‌سازیم (اگر وجود نداشته باشد). این کار برای اندروید ۸ و بالاتر ضروری است.
        // createNotificationChannel()
        // ۲. یک نوتیفیکیشن اولیه نمایش می‌دهیم و با فراخوانی startForeground، سرویس را به حالت "پیش‌زمینه" تبدیل می‌کنیم.
        // این کار به اندروید می‌گوید که این سرویس برای کاربر مهم است و نباید به راحتی متوقف شود.
        // نکته مهم: اگر سرویس در حال اجراست، نوتیفیکیشن را دوباره نسازید که پرش نداشته باشد
        // مگر اینکه بخواهید وضعیت را تغییر دهید.
        // showNotification("در حال اجرا...", "سرویس شعر در حال آماده‌سازی است")

        // ۲. بررسی اینکه آیا دستور فقط برای ریلود کردن است؟
        if (intent?.action == ACTION_RELOAD_DATA) {
            reloadRequested = true
            Log.i(TAG, "دستور ریلود دریافت شد: لیست اشعار در سیکل بعدی آپدیت می‌شود.")
            // اگر سرویس از قبل در حال اجراست، نیازی به ساخت دوباره Job نیست
            if (updateJob?.isActive == true) {
                return START_STICKY
            }
        }

        // اگر جاب قبلی وجود دارد و فعال است، آن را کنسل کن تا دو تا نشوند!
        if (updateJob?.isActive == true) {
            Log.w(TAG, "Old loop is still running. Cancelling it to start a fresh one.")
            updateJob?.cancel()
        }

        // ۳. یک کار (job) جدید در محدوده `serviceScope` راه‌اندازی می‌کنیم. این کار باعث می‌شود منطق اصلی سرویس در یک کوروتین اجرا شود.
        // از Dispatchers.IO استفاده می‌کنیم، چون قرار است با دیتابیس (یک عملیات I/O یا ورودی/خروجی) کار کنیم. این کار از مسدود شدن ترد اصلی برنامه جلوگیری می‌کند.
        updateJob = serviceScope.launch(Dispatchers.IO) {

            // یک نمونه از دیتابیس Room برنامه را دریافت می‌کنیم.
            val db = AppDatabase.getDatabase(applicationContext)
            // یک نمونه از رابط دسترسی به داده (DAO) برای جدول Beyt را از دیتابیس می‌گیریم.
            val beytDao = db.beytDao()
            // یک نمونه از کلاس کمکی WorkTime برای کار با منطق‌های زمانی می‌سازیم.
            val workTime = WorkTime()

            // بهینه‌سازی: به جای اینکه در هر حلقه به دیتابیس مراجعه کنیم، کل اشعار را فقط یک بار در ابتدا از دیتابیس می‌خوانیم و در حافظه نگه می‌داریم.
            var allBeyts = beytDao.getAllBeyts()

            Log.d(TAG, "Total poems found in DB: ${allBeyts.size}") // لاگ تعداد کل شعرها

            // یک حلقه بی‌نهایت که تا زمانی که کوروتین (و در نتیجه سرویس) فعال است، به کار خود ادامه می‌دهد.
            while (isActive) { // `isActive` یک property از CoroutineScope است که در صورت لغو شدن Scope، برابر `false` می‌شود.
                try { // استفاده از بلوک try-catch برای جلوگیری از کرش کردن سرویس در صورت بروز هرگونه خطا.

                    // ۴. چک کردن درخواست ریلود در ابتدای هر حلقه
                    if (reloadRequested) {
                        Log.i(TAG, "Reloading poems from database...")
                        allBeyts = beytDao.getAllBeyts()
                        reloadRequested = false // ریست کردن پرچم
                        Log.i(TAG, "Reload complete. New count: ${allBeyts.size}")
                    }

                    // زمان فعلی را با استفاده از کتابخانه تاریخ شمسی دریافت می‌کنیم.
                    val now = PersianDate()
                    // بیت‌های مناسب برای نمایش را بر اساس زمان فعلی از لیست کل بیت‌ها فیلتر می‌کنیم.
                    val displayableBeyts = filterDisplayableBeyts(allBeyts, now, workTime)

                    Log.d(
                        TAG,
                        "Found ${displayableBeyts.size} poems for current time."
                    ) // لاگ تعداد شعرهای فیلتر شده

                    // بررسی می‌کنیم که آیا حداقل یک شعر برای نمایش در زمان فعلی وجود دارد یا نه.
                    if (displayableBeyts.isNotEmpty()) {
                        // با استفاده از عملگر باقیمانده (modulo)، اطمینان حاصل می‌کنیم که `poemIndex` همیشه یک اندیس معتبر در لیست `displayableBeyts` باشد.
                        // اگر به انتهای لیست برسیم، دوباره از اول شروع می‌کنیم.
                        poemIndex %= displayableBeyts.size
                        // شعر مورد نظر را از لیست فیلتر شده انتخاب می‌کنیم.
                        val selected = displayableBeyts[poemIndex]

                        // بهینه‌سازی: فقط در صورتی که شعر انتخاب شده با شعر قبلی متفاوت باشد، نوتیفیکیشن و ویجت را آپدیت می‌کنیم.
                        if (lastDisplayedPoemId != selected.id) {

                            Log.i(TAG, "Updating to poem ID: ${selected.id}")
                            // شناسه‌ی شعر جدید را در متغیر `lastDisplayedPoemId` ذخیره می‌کنیم.
                            lastDisplayedPoemId = selected.id

                            // بروزرسانی متغیرهای حافظه (برای استفاده سریع در سنسور)
                            currentM1 = selected.beyt1
                            currentM2 = selected.beyt2

                            // الف) ذخیره در SharedPreferences (برای WakeupActivity و پایداری)
                            updateSharedPreferences(selected)

                            //  شعر انتخاب شده را در SharedPreferences ذخیره می‌کنیم تا ویجت بتواند به آن دسترسی داشته باشد.
                            // گام ۱: اولویت اول با ذخیره‌سازی داده است.
                            // تا زمانی که داده جدید روی دیسک نوشته نشده، نباید سراغ آپدیت UI برویم.
                            // updateSharedPreferences(selected)

                            // گام ۲: درخواست آپدیت ویجت.
                            // چون ویجت کندتر است، دستور آن را زودتر از نوتیفیکیشن صادر می‌کنیم تا فاصله زمانی کمتر به چشم بیاید.
                            // PoemWidget().updateAll(applicationContext)

                            // گام ۱ (بهینه): همزمان‌سازی ویجت و نوتیفیکیشن
                            // ما از launch استفاده می‌کنیم تا آپدیت ویجت مانع نمایش سریع نوتیفیکیشن نشود
                            // اما چون هر دو تقریباً همزمان اجرا می‌شوند، کاربر تاخیری حس نمی‌کند.

                            // ۱. ابتدا ویجت را آپدیت می‌کنیم (بدون launch جداگانه برای تضمین ترتیب)
                            // چون متد suspend است، کد اینجا صبر می‌کند تا کار ویجت تمام شود
                            updateGlanceWidget(applicationContext, selected.beyt1, selected.beyt2)


                            // گام دو: نوتیفیکیشن پیش‌زمینه را با متن شعر جدید به‌روزرسانی می‌کنیم.
                            // این کار سریع انجام می‌شود، پس آن را در مرحله آخر انجام می‌دهیم.
                            // showNotification(selected.beyt1, selected.beyt2)
                            // ) آپدیت نوتیفیکیشن
                            // false یعنی: فقط متن را عوض کن، صفحه را روشن نکن (چون این آپدیتِ تایمری است)
                            val notification =
                                buildNotification(selected.beyt1, selected.beyt2, false)
                            val manager = getSystemService(NotificationManager::class.java)
                            manager.notify(NOTIFICATION_ID, notification)


                        } else {
                            Log.d(TAG, "Poem is same as before, skipping update.")
                        }

                        // اندیس را برای نمایش شعر بعدی در دفعه آینده، یک واحد افزایش می‌دهیم.
                        poemIndex++

                    } else {
                        currentM1 = "هیچ شعری یافت نشد"
                        currentM2 = "..."
                        Log.w(TAG, "No poems match the current time filter!")
                        // اگر در زمان فعلی هیچ شعر قابل نمایشی وجود نداشت، مقدار آخرین شعر نمایش داده شده را null می‌کنیم.
                        lastDisplayedPoemId = null
                        lastDisplayedPoemId = null
                        val notification = buildNotification(currentM1, currentM2, false)
                        val manager = getSystemService(NotificationManager::class.java)
                        manager.notify(NOTIFICATION_ID, notification)
                    }
                } catch (e: Exception) {
                    // در صورت بروز هرگونه خطا در حلقه (مثلاً خطای دیتابیس یا منطقی)، آن را در Logcat چاپ می‌کنیم تا بتوانیم آن را دیباگ کنیم.
                    // این کار از توقف ناگهانی سرویس جلوگیری می‌کند.
                    Log.e(TAG, "Error in service loop", e)
                }

                // پس از هر بار بررسی، کوروتین را به مدت مشخص شده (۲۰ ثانیه) متوقف می‌کنیم.
                // این کار برای جلوگیری از مصرف بیش از حد CPU و باتری ضروری است.
                delay(UPDATE_INTERVAL_MS)
            }
        }

        // این مقدار بازگشتی به سیستم‌عامل اندروید می‌گوید که اگر سیستم به دلیل کمبود منابع این سرویس را متوقف کرد،
        // پس از فراهم شدن منابع، سرویس را دوباره راه‌اندازی کند.
        return START_STICKY
    }

    /** private fun buildNotification(
    // title: String,
    m1: String,
    // content: String,
    m2: String,
    triggerFullScreen: Boolean
    ): Notification {

    // ساخت Intent برای باز کردن WakeupActivity
    val wakeupIntent = Intent(this, WakeupActivity::class.java).apply {
    // فلگ‌های مهم برای باز کردن یک اکتیویتی جدید از خارج از برنامه
    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }

    // ساخت PendingIntent
    val wakeupPendingIntent = PendingIntent.getActivity(
    this,
    0, // Request code
    wakeupIntent,
    // فلگ مهم برای اندرویدهای جدید که PendingIntent باید غیرقابل تغییر (Immutable) باشد
    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    if (triggerFullScreen) {
    // ۱. ساخت Intent برای باز کردن WakeupActivity
    val fullScreenIntent = Intent(this, WakeupActivity::class.java).apply {
    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }

    // ۱. ساخت Intent برای باز کردن WakeupActivity
    // val fullScreenIntent = Intent(this, WakeupActivity::class.java)
    // این فلگ‌ها باعث می‌شوند اکتیویتی اگر باز است، رفرش شود و تمیز بالا بیاید
    // fullScreenIntent.flags =
    //    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

    // ۲. تبدیل Intent به PendingIntent (چون نوتیفیکیشن باید آن را اجرا کند)
    val fullScreenPendingIntent = PendingIntent.getActivity(
    this,
    1, // Request Code
    fullScreenIntent,
    // FLAG_IMMUTABLE برای امنیت در اندروید ۱۲+ الزامی است
    // FLAG_UPDATE_CURRENT یعنی اگر اینتنت قبلی بود، اطلاعاتش را با این جدید آپدیت کن
    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // ۳. ساخت نوتیفیکیشن
    val builder = NotificationCompat.Builder(this, CHANNEL_ID)
    // .setContentTitle(title)
    .setContentTitle(m1)
    // .setContentText(content)
    .setContentText(m2)
    .setSmallIcon(R.drawable.ic_launcher_foreground)
    .setOngoing(true) // غیرقابل حذف توسط کاربر
    .setPriority(NotificationCompat.PRIORITY_HIGH) // اولویت بالا برای دیده شدن روی لاک‌اسکرین
    .setCategory(NotificationCompat.CATEGORY_ALARM) // رفتار مشابه آلارم ساعت

    // setOnlyAlertOnce: اگر true باشد، فقط متن آپدیت می‌شود و صدا نمی‌دهد.
    // ما می‌گوییم: اگر `triggerFullScreen` نیست (یعنی آپدیت معمولی)، صدا نده (!false = true).
    // اگر `triggerFullScreen` هست (یعنی Wakeup)، بگذار هشدار دهد (!true = false).
    // .setOnlyAlertOnce(!triggerFullScreen)

    .setOngoing(true) // برای سرویس پیش‌زمینه، نوتیفیکیشن باید دائمی باشد

    // ۴. منطق طلایی باز کردن صفحه (Full Screen Intent)
    /** if (triggerFullScreen) {
    // در اندروید ۱۰+ این خط باعث می‌شود اگر صفحه خاموش است، اکتیویتی `WakeupActivity` تمام صفحه باز شود.
    builder.setFullScreenIntent(fullScreenPendingIntent, true)
    // تنظیمات اضافه برای جلب توجه
    builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
    } else {
    // در حالت عادی نیازی به اینتنت تمام صفحه نداریم
    builder.setFullScreenIntent(null, false)
    } */

    return builder.build()
    } */

    // این کد را کپی و جایگزین متد buildNotification موجود در PoemForegroundService.kt کنید

    /**private fun buildNotification(
    m1: String,
    m2: String,
    triggerFullScreen: Boolean
    ): Notification {
    val builder = NotificationCompat.Builder(this, CHANNEL_ID)
    .setContentTitle(m1)
    .setContentText(m2)
    .setSmallIcon(R.drawable.ic_launcher_foreground) // آیکون را با آیکون مناسب جایگزین کنید
    .setPriority(NotificationCompat.PRIORITY_MAX)
    .setCategory(Notification.CATEGORY_ALARM)
    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // نمایش روی قفل
    .setVibrate(longArrayOf(0, 500, 200, 500)) // ویبره دستی روی بیلد
    .setOngoing(true) // برای سرویس پیش‌زمینه، نوتیفیکیشن باید دائمی باشد

    // منطق کلیدی برای باز کردن صفحه (Full Screen Intent)
    if (triggerFullScreen) {
    // ۱. ساخت Intent برای باز کردن WakeupActivity
    val fullScreenIntent = Intent(this, WakeupActivity::class.java).apply {
    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }

    // ۲. تبدیل Intent به PendingIntent (این بخش در کد شما کامنت شده بود)
    val fullScreenPendingIntent = PendingIntent.getActivity(
    this,
    1, // از یک Request Code غیر صفر استفاده کنید تا از تداخل جلوگیری شود
    fullScreenIntent,
    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // ۳. اتصال PendingIntent به نوتیفیکیشن
    // این دستور به سیستم عامل می‌گوید که این نوتیفیکیشن باید WakeupActivity را تمام‌صفحه باز کند
    builder.setFullScreenIntent(fullScreenPendingIntent, true)
    builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
    }

    return builder.build()
    }*/

    private fun buildNotification(
        m1: String,
        m2: String,
        triggerFullScreen: Boolean
    ): Notification {

        // ۱. ساخت Intent برای باز کردن MainActivity
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            // اضافه کردن همان کلیدی که در MainActivity برای باز کردن دیالوگ چک می‌کنیم
            putExtra("OPEN_ADD_POEM", true)
        }

        // ۲. تبدیل به PendingIntent (مخصوص کلیک روی نوتیفیکیشن)
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            2, // یک کد درخواست متفاوت از بقیه (مثلا ۲)
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // انتخاب کانال بر اساس نیاز
        val targetChannelId = if (triggerFullScreen) CHANNEL_ID_WAKEUP else CHANNEL_ID_SILENT

        val builder = NotificationCompat.Builder(this, targetChannelId)
            .setContentTitle(m1)
            .setContentText(m2)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setContentIntent(contentPendingIntent)

        if (triggerFullScreen) {
            // --- حالت بیدار باش (Wakeup) ---
            builder.setPriority(NotificationCompat.PRIORITY_MAX)
            builder.setCategory(NotificationCompat.CATEGORY_ALARM)
            builder.setVibrate(longArrayOf(0, 500, 200, 500))

            // تنظیم Intent برای روشن کردن صفحه
            val fullScreenIntent = Intent(this, WakeupActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val fullScreenPendingIntent = PendingIntent.getActivity(
                this,
                1,
                fullScreenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.setFullScreenIntent(fullScreenPendingIntent, true)
            builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)


        } else {
            // --- حالت آپدیت معمولی (Silent) ---
            // اولویت پایین برای جلوگیری از پاپ‌آپ شدن
            builder.setPriority(NotificationCompat.PRIORITY_LOW)
            // حذف ویبره و صدا
            builder.setVibrate(null)
            builder.setSound(null)
            // این خط باعث می‌شود حتی اگر اولویت بالا بود، فقط متن عوض شود و صدا ندهد
            builder.setOnlyAlertOnce(true)
        }

        return builder.build()
    }


    // =========================================================
    //               بخش منطق سنسور (Sensor Logic)
    // =========================================================

    /**
     * فعال‌سازی سنسور (معمولا وقتی صفحه خاموش می‌شود)
     */
    private fun registerSensor() {
        accelerometer?.let {
            // SENSOR_DELAY_NORMAL: تاخیر حدود ۲۰۰ میلی‌ثانیه.
            // این حالت کم‌مصرف است و برای تشخیص "برداشتن گوشی" کاملاً کافی است.
            // (نیازی به استفاده از GAME یا UI که باتری‌خور هستند نیست).
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    /**
     * غیرفعال‌سازی سنسور (معمولا وقتی صفحه روشن می‌شود)
     */
    private fun unregisterSensor() {
        sensorManager.unregisterListener(this)
    }

    /**
     * این متد هر بار که شتاب گوشی تغییر می‌کند فراخوانی می‌شود.
     */
    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        // دریافت مقادیر محورهای X, Y, Z
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        // ذخیره مقدار قبلی
        accelerationPreviousValue = accelerationCurrentValue

        // محاسبه بزرگی بردار شتاب (فرمول فیزیک: رادیکال(x^2 + y^2 + z^2))
        accelerationCurrentValue = sqrt((x * x + y * y + z * z).toDouble()).toFloat()

        // محاسبه میزان تغییر (Delta)
        val delta = accelerationCurrentValue - accelerationPreviousValue

        // محاسبه شتاب موثر (برای حذف نویزهای ریز)
        var acceleration = accelerationCurrentValue * delta

        // شرط اصلی تشخیص حرکت
        /** if (acceleration > SHAKE_THRESHOLD) {
        val currentTime = System.currentTimeMillis()

        Log.e("DEBUG_WAKEUP", ">>> Motion Detected! (Acceleration: $acceleration)")

        // بررسی مجوز Full Screen Intent (مخصوص اندروید ۱۴ به بالا)
        val manager = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= 34) {
        val canShow = manager.canUseFullScreenIntent()
        Log.e("DEBUG_WAKEUP", ">>> Permission Check: canUseFullScreenIntent = $canShow")

        if (!canShow) {
        Log.e("DEBUG_WAKEUP", ">>> ERROR: Android has BLOCKED waking up the screen!")
        Log.e("DEBUG_WAKEUP", ">>> SOLUTION: Go to Settings > Apps > Special App Access > Manage Full Screen Intents")
        }
        }

        // Debouncing: اگر از آخرین باری که سنسور عمل کرده کمتر از ۲ ثانیه گذشته، نادیده بگیر.
        // این باعث می‌شود با یک حرکت دست، ۱۰ بار صفحه باز نشود.
        if (currentTime - lastShakeTime > 2000) {
        lastShakeTime = currentTime

        Log.i(TAG, "Motion Detected! Triggering Wakeup...")

        // ۱. بیدار کردن CPU برای ۳ ثانیه
        // این اطمینان می‌دهد که سیستم عامل وقت دارد WakeupActivity را لود کند.
        Log.e("DEBUG_WAKEUP", ">>> Acquiring WakeLock...")
        wakeLock?.acquire(3000)

        // ۲. ساخت نوتیفیکیشن با فلگ `triggerFullScreen = true`
        // این دقیقاً همان کدی است که باعث روشن شدن صفحه و نمایش WakeupActivity می‌شود.
        Log.e("DEBUG_WAKEUP", ">>> Building & Sending Notification...")
        val notification = buildNotification(currentM1, currentM2, true)

        // ۳. ارسال نوتیفیکیشن به سیستم
        manager.notify(NOTIFICATION_ID, notification)

        // ۴. سنسور را موقتا قطع می‌کنیم.
        // چون صفحه قرار است روشن شود، `screenStateReceiver` به زودی صدا زده می‌شود،
        // اما قطع کردن دستی در اینجا کار را محکم‌کاری می‌کند.
        Log.e("DEBUG_WAKEUP", ">>> Notification Sent. Unregistering Sensor.")
        unregisterSensor()
        }
        }*/

        // شرط شتاب و زمان
        /** if (acceleration > SHAKE_THRESHOLD) {
        val currentTime = System.currentTimeMillis()

        // جلوگیری از تکرار سریع (Debouncing)
        if (currentTime - lastShakeTime > 2000) {
        // --- کد جدید برای دیباگ ---
        val notificationManager = getSystemService(NotificationManager::class.java)
        val canLaunch = if (Build.VERSION.SDK_INT >= 34) {
        notificationManager.canUseFullScreenIntent()
        } else {
        true // در اندروید ۱۳ و پایین‌تر همیشه true است
        }

        Log.e("DEBUG_WAKEUP", ">>> آیا اجازه روشن کردن صفحه را دارم؟ $canLaunch")

        if (!canLaunch) {
        Log.e("DEBUG_WAKEUP", ">>> مشکل همینجاست! باید مجوز را در تنظیمات Special App Access روشن کنید.")
        }
        // ---------------------------

        Log.i(TAG, "Motion Detected! Triggering Wakeup...")

        lastShakeTime = currentTime

        // ۱. بیدار کردن CPU
        wakeLock?.acquire(3000)

        // ۲. ساخت نوتیفیکیشن مخصوص بیدار کردن (triggerFullScreen = true)
        val notification = buildNotification(currentM1, currentM2, true)

        val manager = getSystemService(NotificationManager::class.java)

        // ۳. استفاده از یک ID متفاوت برای این نوتیفیکیشن
        // (اگر از همان ID سرویس استفاده کنید، ممکن است فقط متن آپدیت شود و صفحه روشن نشود)
        val WAKEUP_NOTIFICATION_ID = 9999
        manager.notify(WAKEUP_NOTIFICATION_ID, notification)

        // ۴. پاک کردن نوتیفیکیشن بعد از ۵ ثانیه
        // (چون صفحه روشن شده و دیگر نیازی به ماندن این نوتیفیکیشن در نوار اعلان نیست)
        /** Handler(Looper.getMainLooper()).postDelayed({
        try {
        manager.cancel(WAKEUP_NOTIFICATION_ID)
        } catch (e: Exception) {
        Log.e(TAG, "Error cancelling wakeup notification", e)
        }
        }, 5000) */

        // ۵. غیرفعال کردن موقت سنسور
        unregisterSensor()
        }*/
        if (acceleration > SHAKE_THRESHOLD) {
            val currentTime = System.currentTimeMillis()

            if (currentTime - lastShakeTime > 2000) {
                lastShakeTime = currentTime

                Log.i(TAG, "Motion Detected! Triggering Wakeup...")
                wakeLock?.acquire(3000)

                // ۳. روش دوگانه (Double-Trigger Strategy)

                // الف) ارسال نوتیفیکیشن Full Screen (روش استاندارد)
                val notification = buildNotification(currentM1, currentM2, true)
                // اضافه کردن فلگ‌ها برای اطمینان از پخش صدا و ویبره
                notification.defaults = Notification.DEFAULT_ALL

                val manager = getSystemService(NotificationManager::class.java)
                val WAKEUP_NOTIFICATION_ID = 9999
                manager.notify(WAKEUP_NOTIFICATION_ID, notification)

                // ب) باز کردن مستقیم اکتیویتی (روش تضمینی)
                // چون شما مجوز SYSTEM_ALERT_WINDOW دارید، این روش روی اندروید ۱۰+ کار می‌کند
                try {
                    val directIntent = Intent(this, WakeupActivity::class.java)
                    directIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(directIntent)
                    Log.d("DEBUG_WAKEUP", "Direct StartActivity called successfully.")
                } catch (e: Exception) {
                    Log.e("DEBUG_WAKEUP", "Failed to start activity directly", e)
                }

                unregisterSensor()

                // پاک کردن نوتیفیکیشن بعد از چند ثانیه
                Handler(Looper.getMainLooper()).postDelayed({
                    manager.cancel(WAKEUP_NOTIFICATION_ID)
                }, 5000)
            }
        }
    }

    // این متد برای تغییر دقت سنسور است که ما نیازی به آن نداریم.
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    /**
     * این تابع جادوی اصلی برای عملکرد حداکثری است.
     * به جای اینکه ویجت را مجبور کنیم برود و داده را بخواند، داده را در گلوی ویجت می‌ریزیم!
     */
    private suspend fun updateGlanceWidget(context: Context, m1: String, m2: String) {
        try {
            val manager = GlanceAppWidgetManager(context)
            val widget = PoemWidget()

            // دریافت تمام IDهای ویجت‌های فعال روی صفحه
            val glanceIds = manager.getGlanceIds(PoemWidget::class.java)

            if (glanceIds.isEmpty()) Log.w(TAG, "No active widgets found on home screen")

            // برای هر ویجت فعال:
            glanceIds.forEach { glanceId ->
                // ۱. وضعیت (State) آن ویجت را آپدیت می‌کنیم
                updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
                    prefs.toMutablePreferences().apply {
                        this[PoemWidget.KEY_M1] = m1
                        this[PoemWidget.KEY_M2] = m2
                    }
                }
                // ۲. دستور رندر مجدد می‌دهیم (حالا ویجت داده‌های جدید را دارد)
                widget.update(context, glanceId)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update widget", e)
        }
    }

    /**
     * این متد شعر انتخاب شده را در SharedPreferences ذخیره می‌کند.
     * SharedPreferences یک راه ساده برای ذخیره داده‌های کم‌حجم به صورت کلید-مقدار است.
     */
    private fun updateSharedPreferences(selectedBeyt: Beyt) {
        // یک نمونه از SharedPreferences با نام مشخص شده را دریافت می‌کنیم. حالت MODE_PRIVATE یعنی فقط همین برنامه به آن دسترسی دارد.
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        // برای ویرایش داده‌ها، یک Editor از SharedPreferences می‌گیریم.
        prefs.edit()
            // مصراع اول را با کلید KEY_LAST_M1 به صورت یک رشته ذخیره می‌کنیم.
            .putString(KEY_LAST_M1, selectedBeyt.beyt1)
            // مصراع دوم را با کلید KEY_LAST_M2 به صورت یک رشته ذخیره می‌کنیم.
            .putString(KEY_LAST_M2, selectedBeyt.beyt2)
            // تغییرات را اعمال می‌کنیم. apply() تغییرات را در پس‌زمینه و به صورت غیرهمزمان ذخیره می‌کند که کارایی بهتری دارد.
            // .apply()

            // تغییر مهم: استفاده از commit() به جای apply()
            // متد apply() غیرهمزمان است و ممکن است ویجت قبل از ذخیره شدن داده، آن را بخواند.
            // متد commit() همزمان (Synchronous) است و چون ما در ترد IO هستیم، استفاده از آن مشکلی ندارد
            // و تضمین می‌کند که داده حتماً روی دیسک نوشته شده است.
            .commit()


    }

    /**
     * منطق فیلتر کردن اشعار را به یک تابع جداگانه منتقل کردیم تا خوانایی متد onStartCommand بیشتر شود.
     * این تابع لیستی از تمام اشعار و زمان فعلی را گرفته و لیستی از اشعار قابل نمایش را برمی‌گرداند.
     */
    // این تابع لیستی از تمام اشعار و زمان فعلی را گرفته و لیستی از اشعار قابل نمایش را برمی‌گرداند.
    private fun filterDisplayableBeyts(
        allBeyts: List<Beyt>,
        now: PersianDate,
        workTime: WorkTime
    ): List<Beyt> {
        // زمان فعلی را به فرمت "HH:mm" (ساعت:دقیقه) تبدیل می‌کنیم.
        val currentTN = String.format("%02d:%02d", now.hour, now.minute)
        // تاریخ فعلی را به فرمت "yyyy/MM/dd" (سال/ماه/روز) تبدیل می‌کنیم.
        val currentDN = String.format("%04d/%02d/%02d", now.shYear, now.shMonth, now.shDay)

        // از تابع `filter` برای عبور دادن لیست اشعار از یک شرط استفاده می‌کنیم.
        return allBeyts.filter { beyt ->
            // *** تغییر جدید: فیلتر کردن شعرهای غیرفعال و حفظ شده در سرویس ***

            // ۱. اگر تیک "نمایش داده نشود" زده شده، در سرویس (نوتیفیکیشن/ویجت) نیاید
            if (beyt.sh == "0") return@filter false

            // ۲. اگر شعر حفظ شده است (kind == 5)، در سرویس نیاید
            if (beyt.kind == 5) return@filter false

            // بر اساس نوع (kind) هر بیت، شرط متناسب با آن را بررسی می‌کنیم.
            when (beyt.kind) {
                1 -> isTimeMatch(beyt, currentTN, workTime)
                2 -> isDayOfWeekMatch(beyt, now.dayOfWeek())
                3 -> isDayOfMonthMatch(beyt, currentDN)
                else -> isDateRangeMatch(beyt, currentDN, currentTN, workTime)
            }
        }
    }

    // توابع کمکی برای بررسی شرایط مختلف زمانی. جدا کردن این منطق‌ها به توابع کوچک، کد را تمیزتر و قابل فهم‌تر می‌کند.

    // این تابع بررسی می‌کند که آیا زمان فعلی (currentTN) بین زمان شروع و پایان (startT و endT) بیت قرار دارد یا خیر.
    private fun isTimeMatch(beyt: Beyt, currentTN: String, workTime: WorkTime) =
        workTime.compareTime(beyt.startT, currentTN) && workTime.compareTime(currentTN, beyt.endT)

    // این تابع بررسی می‌کند که آیا امروز روزی است که بیت باید نمایش داده شود.
    private fun isDayOfWeekMatch(beyt: Beyt, todayIndex: Int): Boolean {
        // weekDays رشته‌ای مثل "1001000" است که هر کاراکتر آن نماینده یک روز هفته است.
        return beyt.weekDays.length > todayIndex && beyt.weekDays[todayIndex] == '1'
    }

    // این تابع بررسی می‌کند که آیا روزِ ماهِ امروز با روزِ ماهِ تاریخ شروع بیت یکی است یا خیر. (برای مناسبت‌های ماهانه)
    private fun isDayOfMonthMatch(beyt: Beyt, currentDN: String): Boolean {
        // به عنوان مثال، اگر تاریخ شروع "1380/01/15" باشد، این تابع چک می‌کند آیا امروز هم روز ۱۵ ماه است یا نه.
        return beyt.startD.length >= 10 && beyt.startD.substring(8, 10) == currentDN.substring(
            8,
            10
        )
    }

    // این تابع بررسی می‌کند که آیا تاریخ و زمان فعلی، در یک بازه تاریخی و زمانی مشخص قرار دارد یا خیر.
    private fun isDateRangeMatch(
        beyt: Beyt,
        currentDN: String,
        currentTN: String,
        workTime: WorkTime
    ): Boolean {
        // ابتدا بررسی می‌کند که آیا تاریخ امروز بین تاریخ شروع و پایان بیت است.
        val isDateOk = workTime.checkDateBetween(currentDN, beyt.startD, beyt.endD)
        // اگر تاریخ درست بود، سپس بازه زمانی را نیز بررسی می‌کند.
        return isDateOk && isTimeMatch(beyt, currentTN, workTime)
    }


    /**
     * یک متد کمکی برای ساخت و نمایش (یا آپدیت) نوتیفیکیشن پیش‌زمینه.
     */
    /** private fun showNotification(title: String, content: String) {
    // با استفاده از NotificationCompat.Builder یک نوتیفیکیشن جدید می‌سازیم.
    val notification = NotificationCompat.Builder(this, CHANNEL_ID)
    .setContentTitle(title) // عنوان نوتیفیکیشن (مصراع اول)
    .setContentText(content) // متن اصلی نوتیفیکیشن (مصراع دوم)
    .setSmallIcon(R.drawable.ic_launcher_foreground) // آیکون کوچکی که در نوار وضعیت نمایش داده می‌شود.
    .setOngoing(true) // با true کردن این گزینه، نوتیفیکیشن "دائمی" می‌شود و کاربر نمی‌تواند آن را با کشیدن به طرفین ببندد.
    .setOnlyAlertOnce(true) // این خط مهم است: باعث می‌شود نوتیفیکیشن هر بار صدا ندهد و فقط متن آپدیت شود
    .build() // نوتیفیکیشن را می‌سازد.

    // این متد حیاتی، سرویس را به حالت پیش‌زمینه می‌برد و نوتیفیکیشن ساخته شده را نمایش می‌دهد.
    startForeground(NOTIFICATION_ID, notification)
    } */

    /**
     * این متد یک کانال نوتیفیکیشن ایجاد می‌کند. (این کار برای اندروید نسخه ۸ یا Oreo و بالاتر الزامی است).
     * کانال‌ها به کاربران اجازه می‌دهند تا تنظیمات نوتیفیکیشن‌های مختلف برنامه شما را به صورت دسته‌بندی شده کنترل کنند.
     */
    /** private fun createNotificationChannel() {
    // این شرط بررسی می‌کند که آیا نسخه اندروید دستگاه، ۸ یا بالاتر است.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    // یک شیء کانال با شناسه، نام و سطح اهمیت (Importance) مشخص می‌سازیم.
    // IMPORTANCE_LOW یعنی نوتیفیکیشن در نوار وضعیت نمایش داده می‌شود اما صدای هشدار ندارد.
    val channel = NotificationChannel(
    CHANNEL_ID,
    CHANNEL_NAME,
    // اصلاح مهم ۳: تغییر اهمیت به HIGH برای عملکرد صحیح Wakeup
    NotificationManager.IMPORTANCE_HIGH
    )
    // سرویس NotificationManager سیستم را برای مدیریت کانال دریافت می‌کنیم.
    val manager = getSystemService(NotificationManager::class.java)
    channel.enableVibration(true) // ویبره را روشن کنید تا سیستم بفهمد این یک هشدار جدی است
    // کانال ساخته شده را در سیستم ثبت می‌کنیم. این کار فقط یک بار باید انجام شود؛ دفعات بعدی سیستم آن را نادیده می‌گیرد.
    manager.createNotificationChannel(channel)
    }
    }*/

    /**private fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    val channel = NotificationChannel(
    CHANNEL_ID,
    CHANNEL_NAME,
    NotificationManager.IMPORTANCE_HIGH // یا IMPORTANCE_MAX
    ).apply {
    // ۲. افزودن تنظیمات حیاتی برای بیدار کردن گوشی
    description = "هشدار بیدار باش"
    enableVibration(true)
    vibrationPattern = longArrayOf(0, 500, 200, 500) // الگوی لرزش
    enableLights(true)
    setSound(
    android.provider.Settings.System.DEFAULT_NOTIFICATION_URI,
    android.media.AudioAttributes.Builder()
    .setUsage(android.media.AudioAttributes.USAGE_ALARM)
    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
    .build()
    )
    // این خط باعث می‌شود نوتیفیکیشن روی لاک‌اسکرین نمایش داده شود
    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
    }

    val manager = getSystemService(NotificationManager::class.java)
    manager.createNotificationChannel(channel)
    }
    }*/

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            // ۱. ساخت کانال ساکت (برای آپدیت‌های ۲۰ ثانیه‌ای)
            val silentChannel = NotificationChannel(
                CHANNEL_ID_SILENT,
                "Poem Service (Silent)",
                NotificationManager.IMPORTANCE_LOW // اهمیت پایین: بدون صدا، بدون پاپ‌آپ
            ).apply {
                description = "نمایش دائمی شعر در پس‌زمینه"
                enableVibration(false)
                enableLights(false)
                setSound(null, null) // حذف کامل صدا
            }
            manager.createNotificationChannel(silentChannel)

            // ۲. ساخت کانال بیدارباش (برای تکان دادن گوشی)
            val wakeupChannel = NotificationChannel(
                CHANNEL_ID_WAKEUP,
                "Poem Wakeup Alert",
                NotificationManager.IMPORTANCE_HIGH // اهمیت بالا: برای روشن کردن صفحه
            ).apply {
                description = "هشدار بیدار باش"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                enableLights(true)
                // صدای آلارم
                setSound(
                    android.provider.Settings.System.DEFAULT_NOTIFICATION_URI,
                    android.media.AudioAttributes.Builder()
                        .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            manager.createNotificationChannel(wakeupChannel)
        }
    }

    /**
     * این متد زمانی فراخوانی می‌شود که سرویس در حال از بین رفتن است (مثلاً با دستور stopService یا توسط سیستم).
     */
    override fun onDestroy() {
        super.onDestroy() // فراخوانی متد کلاس پدر برای انجام کارهای پاک‌سازی پیش‌فرض.
        // بسیار مهم: تمام کوروتین‌های در حال اجرا در `serviceScope` را لغو می‌کنیم.
        // این کار از نشت حافظه (Memory Leak) و اجرای کارهای پس‌زمینه پس از توقف سرویس، جلوگیری می‌کند.
// پاکسازی تمام منابع برای جلوگیری از نشت حافظه
        serviceScope.cancel()

        // غیرفعال کردن رسیور و سنسور
        try {
            unregisterReceiver(screenStateReceiver)
            unregisterSensor()
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering receivers", e)
        }

        // آزاد کردن قفل بیداری اگر هنوز فعال است
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
    }

    /**
     * این متد برای "سرویس‌های متصل" (Bound Services) استفاده می‌شود که به یک کامپوننت دیگر متصل شده و با آن تعامل دارند.
     * چون سرویس ما از این نوع نیست و به صورت مستقل کار می‌کند (Started Service)، مقدار null را برمی‌گردانیم.
     */
    override fun onBind(intent: Intent?): IBinder? = null
}
