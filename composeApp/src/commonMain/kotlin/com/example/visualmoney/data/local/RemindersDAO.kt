package com.example.visualmoney.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Dao
interface InvestmentReminderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(reminder: InvestmentReminderEntity)

    @Query("SELECT * FROM investment_reminders WHERE investmentId = :investmentId ORDER BY dueDateEpochMs ASC")
    fun observeForInvestment(investmentId: String): Flow<List<InvestmentReminderEntity>>

    @Query(
        """
        SELECT * FROM investment_reminders
        WHERE dueDateEpochMs BETWEEN :fromEpochMs AND :toEpochMs
        ORDER BY dueDateEpochMs ASC
        """
    )
    fun observeBetween(fromEpochMs: Long, toEpochMs: Long): Flow<List<InvestmentReminderEntity>>

    @Query("UPDATE investment_reminders SET isDone = :done WHERE id = :id")
    suspend fun setDone(id: String, done: Boolean)

    @Query("DELETE FROM investment_reminders WHERE id = :id")
    suspend fun delete(id: String)
}