package com.arsalandalvand.azbarkon

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,       // نام شاعر
    val description: String, // متن شعر
    val date: String         // تاریخ ثبت (شمسی)
)