package com.arsalandalvand.azbarkon.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import com.arsalandalvand.azbarkon.R

// تعریف خانواده فونت وزیرمتن
val VazirmatnFontFamily = FontFamily(
    Font(R.font.vazirmatn_regular, FontWeight.Normal),
    Font(R.font.vazirmatn_bold, FontWeight.Bold)
)

// تعریف خانواده فونت نستعلیق
val NastaliqFontFamily = FontFamily(
    Font(R.font.iran_nastaliq, FontWeight.Normal)
)

val Typography = Typography(
    // برای متون بدنه اصلی (مثل توضیحات، لیست‌ها)
    bodyLarge = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    // برای متون ریزتر
    bodyMedium = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    // استایل اختصاصی برای نمایش شعر (بزرگ و نستعلیق)
// در فایل Type.kt اصلاح کنید:
// تغییرات اصلی اینجاست:
    headlineLarge = TextStyle(
        fontFamily = NastaliqFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        // ارتفاع خط را کمی بیشتر می‌کنیم تا تداخل کم شود
        lineHeight = 64.sp,
        letterSpacing = 0.sp,
        // این بخش جدید است: جلوگیری از برش خوردن متن
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.None
        )
    ),

    // برای headlineMedium هم همین تنظیمات را اعمال می‌کنیم
    headlineMedium = TextStyle(
        fontFamily = NastaliqFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 50.sp,
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.None
        )
    ),
    // برای تیترهای UI (مثل "خانه"، "تنظیمات")
    titleLarge = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
    ),
    // برای دکمه‌ها و برچسب‌ها
    labelLarge = TextStyle(
        fontFamily = VazirmatnFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    )
)