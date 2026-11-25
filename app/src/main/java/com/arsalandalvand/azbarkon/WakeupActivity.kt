package com.arsalandalvand.azbarkon
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arsalandalvand.azbarkon.ui.theme.AzbarkonTheme
import com.arsalandalvand.azbarkon.ui.theme.GoldAccentDark
import com.arsalandalvand.azbarkon.ui.theme.PersianTealDark
import kotlinx.coroutines.delay

class WakeupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        turnScreenOnAndKeyguard()
        super.onCreate(savedInstanceState)

        // دریافت شعر از حافظه
        val prefs = getSharedPreferences("poem_prefs", Context.MODE_PRIVATE)
        val m1 = prefs.getString("last_m1", "در حال بارگذاری...") ?: "..."
        val m2 = prefs.getString("last_m2", "...") ?: "..."

        setContent {
            // استفاده از تم تاریک اجباری برای زیبایی و مصرف کمتر باتری در شب
            AzbarkonTheme(darkTheme = true) {
                WakeupScreenContent(m1 = m1, m2 = m2)
            }
        }

        // تلاش مجدد برای روشن کردن صفحه (برای اطمینان)
        Handler(Looper.getMainLooper()).postDelayed({
            turnScreenOnAndKeyguard()
        }, 500)
    }

    private fun turnScreenOnAndKeyguard() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}

@Composable
fun WakeupScreenContent(m1: String, m2: String) {
    // انیمیشن محو برای ورود نرم (Fade In)
    val alphaAnim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        alphaAnim.animateTo(1f, animationSpec = tween(1500))
    }

    // طراحی پس‌زمینه: یک گرادینت بسیار ملایم از مشکی به سرمه‌ای تیره (Persian Teal خیلی تیره)
    // این کار عمق می‌دهد و از حالت تخت (Flat) خارج می‌کند.
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF000000), // سیاه مطلق در بالا
            Color(0xFF051311)  // یک هاله بسیار خفیف سبز/آبی در پایین
        )
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Transparent // رنگ را در Box اعمال می‌کنیم
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = backgroundBrush)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            // کادر تزئینی دور شعر (قاب شعر)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(alphaAnim.value) // اعمال انیمیشن
                    .border(
                        width = 1.dp,
                        color = GoldAccentDark.copy(alpha = 0.3f), // قاب طلایی کمرنگ
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(32.dp), // فاصله متن از قاب
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                // آیکون یا لوگوی کوچک در بالا (اختیاری - حس معنوی/هنری)
                // اگر وکتور "بته جقه" یا گل دارید اینجا عالی می‌شود. فعلاً یک آیکون ساده می‌گذاریم.
                // Icon(painter = painterResource(id = R.drawable.ic_flower), tint = GoldAccentDark, ...)

                // مصراع اول با فونت نستعلیق (headlineLarge از فایل Type.kt)
                Text(
                    text = m1,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                // جداکننده گرافیکی بین دو مصراع
                Spacer(modifier = Modifier.height(16.dp))
                Divider(
                    color = GoldAccentDark.copy(alpha = 0.5f),
                    modifier = Modifier.width(40.dp),
                    thickness = 2.dp
                )
                Spacer(modifier = Modifier.height(16.dp))

                // مصراع دوم
                Text(
                    text = m2,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }
            // فوتر (پایین صفحه)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
                    .alpha(0.6f), // کمی کمرنگ‌تر
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Azbar | Arsalan Dalvand",
                    style = MaterialTheme.typography.labelLarge, // فونت وزیرمتن
                    color = PersianTealDark
                )
            }
        }
    }
}