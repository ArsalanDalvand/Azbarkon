package com.arsalandalvand.azbarkon

import android.content.Context
import android.content.SharedPreferences

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    // تنظیمات حالت شب
    var isDarkMode: Boolean
        get() = prefs.getBoolean("dark_mode", false)
        set(value) = prefs.edit().putBoolean("dark_mode", value).apply()

    // تنظیمات نمایش پیشنهادات
    var showRandomSuggestion: Boolean
        get() = prefs.getBoolean("show_suggestion", false)
        set(value) = prefs.edit().putBoolean("show_suggestion", value).apply()

    // تنظیمات فعال بودن نوتیفیکیشن
    var isNotificationEnabled: Boolean
        get() = prefs.getBoolean("notification_enabled", true)
        set(value) = prefs.edit().putBoolean("notification_enabled", value).apply()
}
