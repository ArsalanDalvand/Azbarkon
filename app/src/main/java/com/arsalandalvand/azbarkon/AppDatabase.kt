package com.arsalandalvand.azbarkon // تعریف بسته (package) که این فایل در آن قرار دارد

import android.content.Context // وارد کردن کلاس Context برای دسترسی به اطلاعات برنامه
import androidx.room.Database // وارد کردن انوتیشن Database برای تعریف پایگاه داده
import androidx.room.Room // وارد کردن کلاس Room برای ساختن نمونه از پایگاه داده
import androidx.room.RoomDatabase // وارد کردن کلاس پایه RoomDatabase

// تعریف پایگاه داده با انوتیشن @Database
// entities: لیستی از کلاس‌هایی که به عنوان جدول در پایگاه داده استفاده می‌شوند
// تغییر مهم: NoteTagCrossRef به لیست زیر اضافه شد تا جدول رابط یادداشت و تگ ساخته شود
@Database(
    entities = [
        Beyt::class,
        Note::class,
        Tag::class,
        BeytTagCrossRef::class,
        NoteTagCrossRef::class // اضافه شدن کلاس رابط جدید
    ],
    version = 4, // نسخه پایگاه داده از 3 به 4 تغییر کرد (چون جدول جدید اضافه شده)
    exportSchema = false
)
abstract class AppDatabase :
    RoomDatabase() { // تعریف یک کلاس abstract که از RoomDatabase ارث‌بری می‌کند

    // تعریف یک متد abstract برای دسترسی به BeytDao
    // Room این متد را به صورت خودکار پیاده‌سازی می‌کند
    abstract fun beytDao(): BeytDao

    // تعریف یک متد abstract برای دسترسی به NoteDao
    // Room این متد را به صورت خودکار پیاده‌سازی می‌کند
    abstract fun noteDao(): NoteDao

    // تغییر 2: اضافه کردن دسترسی به TagDao
    abstract fun tagDao(): TagDao

    // Companion object برای تعریف متدها و متغیرهای استاتیک
    companion object {
        // @Volatile: تضمین می‌کند که مقدار INSTANCE همیشه از حافظه اصلی خوانده شود
        // و در همه thread ها یکسان باشد
        @Volatile
        private var INSTANCE: AppDatabase? =
            null // یک نمونه (instance) از AppDatabase برای پیاده‌سازی الگوی Singleton

        // متدی برای گرفتن نمونه از پایگاه داده
        fun getDatabase(context: Context): AppDatabase {
            // اگر INSTANCE قبلاً ساخته شده باشد، همان را برمی‌گرداند
            // در غیر این صورت، یک نمونه جدید می‌سازد
            return INSTANCE
                ?: synchronized(this) { // بلوک synchronized برای جلوگیری از ساخته شدن همزمان چند نمونه در thread های مختلف
                    // ساخت نمونه جدید از پایگاه داده با استفاده از Room.databaseBuilder
                    val instance = Room.databaseBuilder(
                        context.applicationContext, // Context برنامه
                        AppDatabase::class.java, // کلاس پایگاه داده
                        "poem_database" // نام فایلی که در حافظه دستگاه ذخیره می‌شود
                    )
                        // در صورت تغییر نسخه دیتابیس، داده‌های قبلی پاک می‌شوند و دیتابیس از نو ساخته می‌شود
                        // این برای مرحله توسعه مناسب است تا با تغییر ورژن به ارور نخورید
                        .fallbackToDestructiveMigration()
                        .build() // ساخت نهایی پایگاه داده
                    INSTANCE = instance // ذخیره نمونه ساخته شده در متغیر INSTANCE
                    instance // برگرداندن نمونه ساخته شده
                }
        }
    }
}