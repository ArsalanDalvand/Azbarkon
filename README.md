# ازبَر (Azbarkon)

اپلیکیشن اندرویدی متن‌باز به زبان **کاتلین** و **Jetpack Compose** برای مرور، دسته‌بندی و از بر کردن ابیات و اشعار فارسی، همراه با ویجت صفحهٔ اصلی، سرویس یادآوری و بخش نوشتار.

---

## امکانات اصلی

- **خانه (`Home`):** نمای اصلی برنامه برای دسترسی سریع و مرور ابیات.
- **گنجینه (`All Poems`):** نگهداری، جستجو و مدیریت ابیات و اشعار در پایگاه دادهٔ محلی (`Room` + `poem_database.db`).
- **دسته‌ها (`Tags`):** ساخت و مدیریت برچسب‌ها برای دسته‌بندی اشعار و یادداشت‌ها.
- **نوشتار (`Notes`):** ثبت یادداشت‌های شخصی و اتصال آن‌ها به دسته‌ها.
- **پیشنهاد (`Suggestions`):** پیشنهاد ابیات برای مطالعه و حفظ.
- **ویجت صفحهٔ اصلی:** نمایش بیت روی صفحهٔ اصلی گوشی با استفاده از **Jetpack Glance**.
- **نمایش خودکار شعر هنگام برداشتن گوشی (Smart Wakeup):** تشخیص حرکت و برداشتن گوشی با سنسور شتاب‌سنج (`Accelerometer`) در زمان خاموش بودن صفحه و روشن کردن خودکار صفحه روی لاک‌اسکرین (`WakeupActivity` + `Full Screen Intent`) برای نمایش بیت با فونت نستعلیق.
- **سرویس پس‌زمینه و صرفه‌جویی باتری:** ثبت و شنود سنسور فقط هنگام خاموش بودن صفحه (`ACTION_SCREEN_OFF` / `ACTION_SCREEN_ON`) همراه با پشتیبانی از شروع خودکار پس از روشن شدن دستگاه (`BootReceiver`).
- **پشتیبانی کامل از فارسی:** رابط کاربری راست‌به‌چپ (RTL)، تاریخ شمسی (`PersianDate`) و فونت‌های `Vazirmatn` و `IranNastaliq`.

---

## فناوری‌ها و کتابخانه‌ها

| بخش | فناوری / نسخه |
| --- | --- |
| زبان برنامه‌نویسی | Kotlin `2.0.21` |
| رابط کاربری | Jetpack Compose + Material 3 |
| ویجت | Jetpack Glance `1.1.0` |
| پایگاه داده | Room `2.6.1` (KSP) |
| تاریخ شمسی | `com.github.samanzamani:PersianDate:1.7.1` |
| حداقل نسخهٔ اندروید | Android 7.0 (`minSdk = 24`) |
| نسخهٔ هدف و کامپایل | Android 15 (`compileSdk = 35`, `targetSdk = 35`) |

---

## ساختار کلی پروژه

```text
app/src/main/
├── assets/
│   ├── database/poem_database.db   # پایگاه دادهٔ اولیهٔ اشعار
│   └── sady1.txt
├── java/com/arsalandalvand/azbarkon/
│   ├── MainActivity.kt             # نقطهٔ ورود برنامه
│   ├── Navigation.kt               # ناوبری پایین صفحه
│   ├── MainScreen.kt               # صفحهٔ خانه
│   ├── MyPoemsScreen.kt            # گنجینهٔ اشعار
│   ├── TagManagerScreen.kt         # مدیریت دسته‌ها
│   ├── NoteListScreen.kt           # لیست نوشتارها
│   ├── NoteDetailScreen.kt         # جزئیات و ویرایش نوشتار
│   ├── SuggestionScreen.kt         # صفحهٔ پیشنهاد
│   ├── SettingsScreen.kt           # تنظیمات
│   ├── AppDatabase.kt              # تعریف دیتابیس Room
│   ├── PoemWidget.kt               # ویجت Jetpack Glance
│   ├── PoemForegroundService.kt    # سرویس پس‌زمینه و تشخیص برداشتن گوشی
│   ├── WakeupActivity.kt           # نمایش خودکار شعر روی لاک‌اسکرین
│   └── ui/theme/                   # تم، رنگ‌ها و تایپوگرافی
└── res/
    └── font/                       # فونت‌های وزیرمتن و ایران‌نستعلیق
```

---

## ساخت و اجرا (Build)

۱. مخزن را کلون کنید:

```bash
git clone https://github.com/ArsalanDalvand/Azbarkon.git
cd Azbarkon
```

۲. پروژه را در **Android Studio** باز کنید یا از طریق ترمینال بیلد بگیرید:

```bash
./gradlew assembleDebug
```

---

## مجوز (License)

این پروژه تحت مجوز **GNU General Public License v3.0 (GPL-3.0)** منتشر شده است. برای جزئیات بیشتر فایل [LICENSE](LICENSE) را مطالعه کنید.
