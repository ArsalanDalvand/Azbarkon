// این بسته (package) شناسه‌ی منحصر به فرد کد شما در پروژه است.
package com.arsalandalvand.azbarkon

// وارد کردن کلاس‌ها و توابع مورد نیاز از کتابخانه‌های اندروید و Jetpack Compose.
// این بخش به کد شما اجازه می‌دهد تا از قابلیت‌های از پیش ساخته شده استفاده کند.

import android.content.Context // برای دسترسی به اطلاعات و سرویس‌های کلی برنامه مانند SharedPreferences.
import androidx.compose.runtime.Composable // این انوتیشن توابع را به عنوان کامپوننت‌های UI تعریف می‌کند که می‌توانند در صفحه نمایش داده شوند.
import androidx.compose.ui.graphics.Color // برای کار با رنگ‌ها در UI.
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.compose.ui.unit.dp // برای تعریف اندازه‌ها با واحد "Density-independent Pixels" که به تراکم پیکسلی صفحه وابسته نیست.
import androidx.glance.GlanceId // شناسه‌ی منحصر به فرد برای هر نمونه از ویجت شما روی صفحه اصلی.
import androidx.glance.GlanceModifier // برای اعمال تغییراتی مانند پس‌زمینه، فاصله و اندازه به کامپوننت‌های Glance.
import androidx.glance.GlanceTheme // برای اعمال یک تم طراحی یکپارچه (مانند رنگ‌ها و فونت‌ها) به ویجت.
import androidx.glance.appwidget.GlanceAppWidget // کلاس پایه‌ای که باید برای ساخت ویجت‌های مبتنی بر Glance از آن ارث‌بری کنید.
import androidx.glance.appwidget.provideContent // تابعی که محتوای اصلی و قابل نمایش ویجت را تعریف می‌کند.
import androidx.glance.background // برای تنظیم رنگ یا تصویر پس‌زمینه یک کامپوننت.
import androidx.glance.currentState
import androidx.glance.layout.Alignment // برای تعیین نحوه چینش عناصر در یک ردیف یا ستون (مثلاً وسط‌چین، چپ‌چین و...).
import androidx.glance.layout.Column // یک کامپوننت برای چیدن عناصر فرزند به صورت عمودی (زیر هم).
import androidx.glance.layout.Spacer // برای ایجاد یک فضای خالی بین عناصر.
import androidx.glance.layout.fillMaxSize // یک اصلاح‌گر (modifier) که به کامپوننت می‌گوید تمام فضای والد خود را پر کند.
import androidx.glance.layout.height // برای تنظیم ارتفاع یک کامپوننت.
import androidx.glance.layout.padding // برای ایجاد فاصله از لبه‌های داخلی یک کامپوننت.
import androidx.glance.text.Text // کامپوننتی برای نمایش متن.
import androidx.glance.text.TextStyle // برای استایل‌دهی به متن (مانند رنگ، اندازه فونت و...).
import androidx.glance.unit.ColorProvider // یک ارائه‌دهنده رنگ که با تم‌های مختلف Glance سازگار است.
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.action.actionParametersOf
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionStartActivity

/**
 * این کلاس ویجت اصلی برنامه را تعریف می‌کند که یک بیت شعر را نمایش می‌دهد.
 * این کلاس از GlanceAppWidget ارث‌بری می‌کند تا بتواند با سیستم ویجت‌های اندروید کار کند.
 */
/** class PoemWidget : GlanceAppWidget() {

/**
 * این متد اصلی‌ترین بخش ویجت است و محتوای آن را تولید می‌کند.
 * این یک تابع معلق (suspend) است، یعنی می‌تواند عملیات طولانی‌مدت را بدون مسدود کردن ترد اصلی انجام دهد.
 * @param context زمینه (Context) برنامه که برای دسترسی به منابع استفاده می‌شود.
 * @param id شناسه‌ی منحصر به فرد این نمونه از ویجت.
*/
override suspend fun provideGlance(context: Context, id: GlanceId) {

// provideContent یک بلاک Composable برای تعریف UI ویجت فراهم می‌کند.
provideContent {
// در این بخش، داده‌های مورد نیاز برای نمایش در ویجت را از حافظه می‌خوانیم.
// SharedPreferences یک راه ساده برای ذخیره داده‌های کلید-مقدار (key-value) است.
val prefs = context.getSharedPreferences("poem_prefs", Context.MODE_PRIVATE)

// خواندن مصرع اول از حافظه.
// اگر مقداری با کلید "last_m1" وجود نداشته باشد، متن "در انتظار شعر..." به عنوان مقدار پیش‌فرض استفاده می‌شود.
// عملگر "?: """ تضمین می‌کند که اگر نتیجه null بود، یک رشته خالی برگردانده شود.
val m1 = prefs.getString("last_m1", "در انتظار شعر...") ?: ""

// خواندن مصرع دوم از حافظه.
// اگر مقداری با کلید "last_m2" وجود نداشته باشد، یک رشته خالی برگردانده می‌شود.
val m2 = prefs.getString("last_m2", "") ?: ""

// اعمال تم طراحی Glance به کل محتوای ویجت.
GlanceTheme {
// فراخوانی تابع Composable که مسئولیت طراحی و نمایش بیت شعر را بر عهده دارد.
PoemWidgetContent(m1, m2)
}
}
} */

class PoemWidget : GlanceAppWidget() {

    // تعریف کلیدها برای استفاده در State منیجر Glance
    companion object {
        val KEY_M1 = stringPreferencesKey("last_m1")
        val KEY_M2 = stringPreferencesKey("last_m2")

        // ** اضافه شده: کلید برای تشخیص کلیک روی ویجت **
        val KEY_OPEN_ADD = ActionParameters.Key<Boolean>("OPEN_ADD_POEM")
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            // دریافت داده‌ها مستقیماً از State داخلی Glance
            // این داده‌ها توسط سرویس تزریق شده‌اند و آماده مصرف هستند
            val prefs = currentState<Preferences>()

            val m1 = prefs[KEY_M1] ?: "در انتظار شعر..."
            val m2 = prefs[KEY_M2] ?: ""

            GlanceTheme {
                PoemWidgetContent(m1, m2)
            }
        }
    }



    /**
     * این یک تابع Composable خصوصی است که ساختار و ظاهر ویجت را تعریف می‌کند.
     * @param m1 متن مصرع اول برای نمایش.
     * @param m2 متن مصرع دوم برای نمایش.
     */
    @Composable
    private fun PoemWidgetContent(m1: String, m2: String) {
        // از کامپوننت Column برای چیدن عناصر به صورت عمودی (مصرع اول بالای مصرع دوم) استفاده می‌کنیم.
        Column(
            // `modifier` برای اعمال مجموعه‌ای از تغییرات ظاهری و رفتاری به کامپوننت استفاده می‌شود.
            modifier = GlanceModifier
                .fillMaxSize() // کل فضای موجود در ویجت را اشغال می‌کند.
                .padding(8.dp) // ۸ واحد dp فاصله از تمام لبه‌های ویجت ایجاد می‌کند.
                // یک پس‌زمینه سفید با ۹۰٪ شفافیت تنظیم می‌کند تا روی والپیپرهای مختلف خوانایی بهتری داشته باشد.
                .background(Color.White.copy(alpha = 0.9f))
                .clickable(
                    actionStartActivity<MainActivity>(
                        MainActivity::class.java, // کلاس را اینجا پاس دهید
                        actionParametersOf(KEY_OPEN_ADD to true)
                    )
                ),
            verticalAlignment = Alignment.CenterVertically, // محتوای ستون را در مرکز محور عمودی چینش می‌کند.
            horizontalAlignment = Alignment.CenterHorizontally // محتوای ستون را در مرکز محور افقی چینش می‌کند.
        ) {
            // نمایش متن مصرع اول.
            Text(
                text = m1,
                style = TextStyle(color = ColorProvider(Color.Black)) // استایل متن: رنگ سیاه.
            )
            // یک فضای خالی عمودی به ارتفاع ۴dp برای ایجاد فاصله بین دو مصرع.
            Spacer(modifier = GlanceModifier.height(4.dp))
            // نمایش متن مصرع دوم.
            Text(
                text = m2,
                style = TextStyle(color = ColorProvider(Color.DarkGray)) // استایل متن: رنگ خاکستری تیره برای تمایز.
            )
        }
    }
}
