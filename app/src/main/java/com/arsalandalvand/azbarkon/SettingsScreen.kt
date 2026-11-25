package com.arsalandalvand.azbarkon

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.outlined.ModeNight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onThemeChanged: (Boolean) -> Unit, // وقتی تم عوض شد، به MainActivity خبر بده
    onNotificationChanged: (Boolean) -> Unit // وقتی وضعیت سرویس عوض شد
) {
    val context = LocalContext.current
    val settings = remember { SettingsManager(context) }

    // وضعیت‌های محلی برای نمایش سوئیچ‌ها
    var isDark by remember { mutableStateOf(settings.isDarkMode) }
    var isNotif by remember { mutableStateOf(settings.isNotificationEnabled) }
    var isSuggestion by remember { mutableStateOf(settings.showRandomSuggestion) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تنظیمات برنامه") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "بازگشت")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {

            // ۱. تنظیمات حالت شب
            SettingsItem(
                title = "حالت شب",
                icon = Icons.Outlined.ModeNight,
                checked = isDark,
                onCheckedChange = {
                    isDark = it
                    settings.isDarkMode = it
                    onThemeChanged(it) // اعمال آنی تم
                }
            )

            // ۲. تنظیمات نوتیفیکیشن
            SettingsItem(
                title = "نمایش بیت در اعلان‌ها",
                icon = Icons.Default.Notifications,
                checked = isNotif,
                onCheckedChange = {
                    isNotif = it
                    settings.isNotificationEnabled = it
                    onNotificationChanged(it) // خاموش/روشن کردن سرویس
                }
            )

            // ۳. پیشنهادات روزانه (فعلاً فقط ذخیره می‌شود)
            SettingsItem(
                title = "هر روز یک بیت پیشنهاد کن",
                icon = Icons.Default.Lightbulb,
                checked = isSuggestion,
                onCheckedChange = {
                    isSuggestion = it
                    settings.showRandomSuggestion = it
                }
            )

            // --- بخش جدید: امضای ارسلان دالوند ---
            Spacer(modifier = Modifier.weight(1f)) // ایجاد فاصله تا پایین صفحه

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "توسعه‌یافته توسط",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "ارسلان دالوند",
                    // استفاده از استایل نستعلیقی که در Type.kt تعریف کردی
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun SettingsItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(16.dp))
                Text(title, style = MaterialTheme.typography.bodyLarge)
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}