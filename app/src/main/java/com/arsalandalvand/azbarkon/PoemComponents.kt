package com.arsalandalvand.azbarkon

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arsalandalvand.azbarkon.ui.theme.VazirmatnFontFamily

// تعریف رنگ طلایی کلاسیک (مناسب برای Dark Academia)
val GoldColor = Color(0xFFC5A059)

// ------------------------------------------------------------------
// *** کارت شعر با استایل ویژه برای حفظ شده‌ها ***
// ------------------------------------------------------------------
@Composable
fun PoemCard(
    beyt: Beyt,
    isQuizMode: Boolean,
    allTags: List<Tag>,
    loadTags: suspend (Int) -> List<Long>,
    onClick: () -> Unit,
    onMemorizeClick: () -> Unit
) {
    // وضعیت نمایش مصرع دوم در حالت کوییز
    var isRevealed by remember { mutableStateOf(false) }
    LaunchedEffect(isQuizMode) { if (!isQuizMode) isRevealed = true else isRevealed = false }

    // لود کردن تگ‌ها
    var poemTagPaths by remember { mutableStateOf<List<String>>(emptyList()) }
    LaunchedEffect(beyt.id, allTags) {
        if (beyt.id != null) {
            val tagIds = loadTags(beyt.id)
            val tags = allTags.filter { tagIds.contains(it.id) }
            poemTagPaths = tags.map { tag -> getFullTagPath(tag, allTags) }
        }
    }

    // --- تشخیص وضعیت‌ها ---
    val isDisabled = beyt.sh == "0"
    val isMemorized = beyt.kind == 5

    // --- تنظیمات رنگ و استایل ---
    val containerColor = if (isDisabled) {
        MaterialTheme.colorScheme.surfaceVariant // خاکستری کامل برای غیرفعال
    } else {
        MaterialTheme.colorScheme.surface
    }

    val contentAlpha = if (isDisabled) 0.5f else 1f

    // رنگ نوار سمت راست
    val indicatorColor = when {
        isDisabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        isMemorized -> GoldColor // طلایی برای حفظ شده
        beyt.kind == 1 -> MaterialTheme.colorScheme.primary
        beyt.kind == 2 -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.tertiary
    }

    // تنظیم حاشیه (Border)
    val cardBorder = when {
        isQuizMode && !isRevealed -> BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        isMemorized && !isDisabled -> BorderStroke(1.dp, GoldColor.copy(alpha = 0.6f)) // حاشیه طلایی برای حفظ شده‌ها
        else -> null
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDisabled) 0.dp else 4.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = cardBorder
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // نوار رنگی سمت راست
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .width(6.dp)
                    .background(indicatorColor)
            )

            // محتوای اصلی کارت
            Column(
                modifier = Modifier
                    .padding(start = 16.dp, end = 24.dp, top = 20.dp, bottom = 16.dp)
                    .fillMaxWidth()
                    .alpha(contentAlpha)
            ) {
                // هدر: آیکون وضعیت (غیرفعال یا حفظ شده)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isDisabled) {
                        Icon(
                            imageVector = Icons.Outlined.VisibilityOff,
                            contentDescription = "غیرفعال",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("نمایش غیرفعال", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else if (isMemorized) {
                        // آیکون کاپ یا ستاره برای حفظ شده‌ها
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "حفظ شده",
                            tint = GoldColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("تبریک! حفظ شده", style = MaterialTheme.typography.labelSmall, color = GoldColor, fontFamily = VazirmatnFontFamily)
                    }
                }

                // --- بخش تگ‌ها ---
                if (poemTagPaths.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        items(poemTagPaths) { path ->
                            PoemTagChip(text = path)
                        }
                    }
                }

                // --- بدنه شعر ---
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AutoResizedText(
                        text = beyt.beyt1,
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp, end = 16.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp)
                            .then(if (isQuizMode) Modifier.clickable { isRevealed = true } else Modifier)
                    ) {
                        AutoResizedText(
                            text = beyt.beyt2,
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth().alpha(if (isQuizMode && !isRevealed) 0f else 1f)
                        )

                        if (isQuizMode && !isRevealed) {
                            Text(
                                text = "برای نمایش ضربه بزنید",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontFamily = VazirmatnFontFamily,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // --- فوتر کارت ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // قسمت زمان / وضعیت
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if(isMemorized) GoldColor else MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = getFormattedTimeRange(beyt),
                            style = MaterialTheme.typography.labelMedium,
                            fontFamily = VazirmatnFontFamily,
                            color = if(isMemorized) GoldColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // دکمه حفظ کردم (فقط اگر هنوز حفظ نشده و فعال است نمایش داده شود)
                    if (!isDisabled && !isMemorized) {
                        Surface(
                            onClick = onMemorizeClick,
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(4.dp))
                                Text("حفظ کردم", style = MaterialTheme.typography.labelSmall, fontFamily = VazirmatnFontFamily)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------
// *** سایر بخش‌ها (تغییرات جزئی در متن) ***
// ------------------------------------------------------------------

fun getFullTagPath(tag: Tag, allTags: List<Tag>): String {
    val parent = allTags.find { it.id == tag.parentId }
    return if (parent != null) {
        "${getFullTagPath(parent, allTags)} / ${tag.title}"
    } else {
        tag.title
    }
}

fun getFormattedTimeRange(beyt: Beyt): String {
    // اگر حفظ شده است، متن مخصوص برگردان
    if (beyt.kind == 5) return "به حافظه سپرده شد"

    val timeRange = "${beyt.startT} تا ${beyt.endT}".toPersianDigits()
    return when (beyt.kind) {
        1 -> "روزانه ($timeRange)"
        2 -> "هفتگی"
        3 -> "ماهانه (روز ${beyt.startD.takeLast(2)})"
        4 -> "بازه زمانی ($timeRange)"
        else -> "سفارشی"
    }
}

@Composable
fun SwipeToDeleteContainer(item: Beyt, onDelete: () -> Unit, content: @Composable () -> Unit) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) {
                onDelete(); true
            } else false
        }
    )
    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        SwipeToDismissBox(
            state = dismissState,
            backgroundContent = {
                val color = Color.Red.copy(alpha = 0.8f)
                Box(
                    Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)).background(color).padding(horizontal = 20.dp),
                    contentAlignment = Alignment.CenterStart
                ) { Icon(Icons.Default.Delete, "حذف", tint = Color.White) }
            },
            content = { content() }
        )
    }
}

@Composable
fun PoemTagChip(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
        shape = RoundedCornerShape(6.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontFamily = VazirmatnFontFamily,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun AutoResizedText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = style.color,
    textAlign: TextAlign = TextAlign.Start,
    minFontSize: Float = 14f
) {
    var resizedStyle by remember { mutableStateOf(style) }
    LaunchedEffect(text) { resizedStyle = style }
    Text(
        text = text,
        color = color,
        textAlign = textAlign,
        softWrap = false,
        style = resizedStyle,
        onTextLayout = { result ->
            if (result.didOverflowWidth && resizedStyle.fontSize.value > minFontSize) {
                val newSize = resizedStyle.fontSize.value * 0.9f
                resizedStyle = resizedStyle.copy(fontSize = newSize.sp)
            }
        },
        modifier = modifier
    )
}