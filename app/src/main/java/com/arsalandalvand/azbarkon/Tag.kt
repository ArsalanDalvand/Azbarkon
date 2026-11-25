package com.arsalandalvand.azbarkon

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tags",
    foreignKeys = [
        ForeignKey(
            entity = Tag::class,
            parentColumns = ["id"],
            childColumns = ["parentId"],
            onDelete = ForeignKey.CASCADE // اگر پدر حذف شد، فرزندان هم حذف شوند
        )
    ],
    indices = [Index(value = ["parentId"])]
)
data class Tag(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val parentId: Long? = null // اگر null باشد یعنی دسته اصلی است (مثل "شاعر")
)