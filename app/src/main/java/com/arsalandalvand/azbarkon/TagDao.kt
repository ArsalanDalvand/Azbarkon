package com.arsalandalvand.azbarkon

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Query("SELECT * FROM tags")
    fun getAllTags(): Flow<List<Tag>> // استفاده از Flow برای آپدیت خودکار

    @Insert
    @JvmSuppressWildcards // جلوگیری از ایجاد Wildcard در کدهای جاوا
    suspend fun insertTag(tag: Tag): Long

    @Delete
    @JvmSuppressWildcards
    suspend fun deleteTag(tag: Tag): Int

    // اضافه کردن رابطه تگ و شعر
    @Insert
    @JvmSuppressWildcards
    suspend fun insertBeytTagCrossRef(crossRef: BeytTagCrossRef): Long

    // حذف تمام تگ‌های یک شعر (برای موقع ویرایش که می‌خواهیم تگ‌ها را بازنشانی کنیم)
    @Query("DELETE FROM beyt_tags WHERE beytId = :beytId")
    @JvmSuppressWildcards
    suspend fun clearTagsForBeyt(beytId: Int): Int

    // گرفتن لیست آی‌دی تگ‌های یک شعر خاص
    @Query("SELECT tagId FROM beyt_tags WHERE beytId = :beytId")
    @JvmSuppressWildcards
    suspend fun getTagIdsForBeyt(beytId: Int): List<Long>
}