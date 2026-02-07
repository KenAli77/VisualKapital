package com.example.visualmoney.data.repository

import com.example.visualmoney.data.local.InvestmentReminderDao
import com.example.visualmoney.data.local.InvestmentReminderEntity
import kotlinx.coroutines.flow.Flow


interface InvestmentReminderRepository {
    suspend fun upsert(reminder: InvestmentReminderEntity)

    fun observeForInvestment(symbol: String): Flow<List<InvestmentReminderEntity>>

    fun observeAll(): Flow<List<InvestmentReminderEntity>>

    suspend fun setDone(id: String, done: Boolean)

    suspend fun delete(id: String)
}


class InvestmentReminderRepositoryImpl(
    private val dao: InvestmentReminderDao
) : InvestmentReminderRepository {

    override suspend fun upsert(reminder: InvestmentReminderEntity) {
        dao.upsert(reminder)
    }

    override fun observeForInvestment(symbol: String): Flow<List<InvestmentReminderEntity>> {
        return dao.observeForInvestment(symbol)
    }

    override fun observeAll(): Flow<List<InvestmentReminderEntity>> {
        return dao.observeBetween()
    }

    override suspend fun setDone(id: String, done: Boolean) {
        dao.setDone(id, done)
    }

    override suspend fun delete(id: String) {
        dao.delete(id)
    }
}