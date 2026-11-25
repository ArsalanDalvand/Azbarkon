package com.arsalandalvand.azbarkon

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "beyts")
data class Beyt(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val beyt1: String,
    val beyt2: String,
    val startT: String,
    val endT: String,
    val startD: String,
    val endD: String,
    val weekDays: String,
    val repeat: String,
    val sh: String,
    val kind: Int // این فیلد برای تفکیک انواع شعر (مثل روزانه/هفتگی) لازم است
)