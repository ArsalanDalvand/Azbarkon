package com.arsalandalvand.azbarkon

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // بررسی تنظیمات: اگر کاربر سرویس را خاموش نکرده باشد، آن را اجرا کن
            val settings = SettingsManager(context)
            if (settings.isNotificationEnabled) {
                val serviceIntent = Intent(context, PoemForegroundService::class.java)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            }
        }
    }
}