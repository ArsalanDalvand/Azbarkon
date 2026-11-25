package com.arsalandalvand.azbarkon.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PersianTealDark,
    onPrimary = DeepCharcoal,
    secondary = GoldAccentDark, // طلایی برای المان‌های گرافیکی باقی می‌ماند
    onSecondary = DeepCharcoal,
    background = DeepCharcoal,
    surface = DarkSurface,
    onBackground = TextWhite,
    onSurface = TextWhite,
    // تغییر مهم: رنگ متن ثانویه (توضیحات)
    onSurfaceVariant = TextTaupeDark,
    // رنگ خطوط جداکننده و حاشیه‌های کمرنگ
    outline = TextTaupeDark.copy(alpha = 0.5f)
)

private val LightColorScheme = lightColorScheme(
    primary = PersianTealLight,
    onPrimary = LightSurface,
    secondary = GoldAccentLight, // طلایی برای المان‌های گرافیکی باقی می‌ماند
    onSecondary = LightSurface,
    background = PaperWhite,
    surface = LightSurface,
    onBackground = TextBlack,
    onSurface = TextBlack,
    // تغییر مهم: رنگ متن ثانویه (توضیحات)
    onSurfaceVariant = TextTaupeLight,
    // رنگ خطوط جداکننده و حاشیه‌های کمرنگ
    outline = TextTaupeLight.copy(alpha = 0.5f)
)

@Composable
fun AzbarkonTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // داینامیک کالر را خاموش می‌کنیم تا تم اختصاصی خودمان اعمال شود
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb() // هماهنگ با پس‌زمینه
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}