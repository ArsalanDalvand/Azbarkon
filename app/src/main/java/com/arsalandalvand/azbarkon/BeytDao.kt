package com.arsalandalvand.azbarkon

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.*

@Dao
interface BeytDao {
    @Query("SELECT * FROM beyts")
    fun getAllBeyts(): List<Beyt>

    @Insert
    fun insertBeyt(beyt: Beyt): Long

    @Query("DELETE FROM beyts WHERE id = :beytId")
    fun deleteById(beytId: Int): Int

    @Update
    fun updateBeyt(beyt: Beyt): Int

    @Delete
    fun deleteBeyt(beyt: Beyt): Int
}
