package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE isCancelled = 0 ORDER BY targetTimeMillis ASC")
    fun getActiveReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders ORDER BY targetTimeMillis DESC")
    fun getAllReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE id = :id LIMIT 1")
    suspend fun getReminderById(id: Long): ReminderEntity?

    @Query("SELECT * FROM reminders WHERE title LIKE '%' || :title || '%' AND isCancelled = 0 ORDER BY targetTimeMillis ASC LIMIT 1")
    suspend fun findFirstActiveByTitle(title: String): ReminderEntity?

    @Query("SELECT * FROM reminders WHERE targetTimeMillis > :now AND isCancelled = 0 AND isTriggered = 0")
    suspend fun getPendingUpcomingReminders(now: Long = System.currentTimeMillis()): List<ReminderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity): Long

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Query("UPDATE reminders SET isCancelled = 1 WHERE id = :id")
    suspend fun cancelReminder(id: Long): Int

    @Query("UPDATE reminders SET isTriggered = 1 WHERE id = :id")
    suspend fun markTriggered(id: Long)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteReminderById(id: Long): Int

    @Query("DELETE FROM reminders")
    suspend fun deleteAllReminders()
}
