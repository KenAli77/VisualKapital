package com.example.visualmoney.data.local


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ManualInvestmentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: ManualInvestmentEntity)

    @Query("SELECT * FROM manual_investments WHERE id = :id LIMIT 1")
    suspend fun get(id: String): ManualInvestmentEntity?

    @Query("SELECT * FROM manual_investments ORDER BY updatedAtEpochMs DESC")
    fun observeAll(): Flow<List<ManualInvestmentEntity>>

    @Query("DELETE FROM manual_investments WHERE id = :id")
    suspend fun delete(id: String)
}