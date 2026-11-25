package com.arsalandalvand.azbarkon

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "beyt_tags",
    primaryKeys = ["beytId", "tagId"],
    foreignKeys = [
        ForeignKey(entity = Beyt::class, parentColumns = ["id"], childColumns = ["beytId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Tag::class, parentColumns = ["id"], childColumns = ["tagId"], onDelete = ForeignKey.CASCADE)
    ]
)
data class BeytTagCrossRef(
    val beytId: Int,
    val tagId: Long
)