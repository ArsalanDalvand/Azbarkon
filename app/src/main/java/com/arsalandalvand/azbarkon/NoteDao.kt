package com.arsalandalvand.azbarkon

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    // تغییر: برای دریافت همزمان یادداشت و تگ‌هایش باید از Transaction استفاده شود
    // همچنین خروجی به NoteWithTags تغییر کرد تا لیست تگ‌ها را هم داشته باشیم
    @Transaction
    @Query("SELECT * FROM notes ORDER BY id DESC")
    fun getNotesWithTags(): Flow<List<NoteWithTags>>

    // دقیقاً مشابه متد قبلی شما با حفظ @JvmSuppressWildcards و خروجی Long
    @Insert
    @JvmSuppressWildcards
    suspend fun insertNoteItem(note: Note): Long

    // دقیقاً مشابه متد قبلی شما با حفظ @JvmSuppressWildcards و خروجی Int
    @Update
    @JvmSuppressWildcards
    suspend fun updateNoteItem(note: Note): Int

    // دقیقاً مشابه متد قبلی شما با حفظ @JvmSuppressWildcards و خروجی Int
    @Delete
    @JvmSuppressWildcards
    suspend fun deleteNoteItem(note: Note): Int

    // --- بخش‌های جدید برای مدیریت تگ‌ها (با رعایت استایل شما) ---

    // اضافه کردن رابطه بین یادداشت و تگ
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    @JvmSuppressWildcards
    suspend fun insertNoteTagCrossRef(crossRef: NoteTagCrossRef): Long

    // حذف تمام تگ‌های یک یادداشت (برای زمان ویرایش که می‌خواهیم تگ‌ها را بازنشانی کنیم)
    @Query("DELETE FROM note_tags WHERE noteId = :noteId")
    @JvmSuppressWildcards
    suspend fun clearNoteTags(noteId: Int): Int
}