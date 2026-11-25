plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    // نکته: اگر این پلاگین در libs تعریف نشده، باید نسخه آن را اینجا دستی وارد کنید
    // مثلا: id("com.google.devtools.ksp") version "2.0.0-1.0.21"
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.arsalandalvand.azbarkon"
    // اصلاح ۱: استفاده از نسخه پایدار به جای نسخه پریویو (36)
    compileSdk = 35

    defaultConfig {
        applicationId = "com.arsalandalvand.azbarkon"
        minSdk = 24
        // اصلاح ۲: تارگت باید عدد صحیح باشد
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            // فعال کردن فشرده‌سازی کد (R8) برای امنیت و کاهش حجم
            isMinifyEnabled = false
            // حذف منابع استفاده نشده
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // اصلاح ۳: رفع خطاهای سینتکس کاتلین در این بخش
    splits {
        abi {
            isEnable = true
            reset()
            include("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
            isUniversalApk = true // این خط باعث ساخت تک فایل برای همه گوشی‌ها می‌شود
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }


    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // نکته: اگر از BOM استفاده می‌کنید نسخه ندهید، اگر نه، باید نسخه مشخص باشد
    implementation("androidx.compose.material:material-icons-extended")

    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.ui.text)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Room Database
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // Jetpack Glance
    implementation("androidx.glance:glance:1.1.0")
    implementation("androidx.glance:glance-appwidget:1.1.0")
    implementation("androidx.glance:glance-material:1.1.0")

    // کتابخانه تاریخ شمسی
    implementation("com.github.samanzamani:PersianDate:1.7.1")

    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
}