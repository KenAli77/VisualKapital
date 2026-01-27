package com.example.visualmoney.data.local

import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.visualmoney.VisualMoneyApplication

actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val appContext = VisualMoneyApplication.context.applicationContext
    val dbFile = appContext.getDatabasePath("visual_money.db")
    return Room.databaseBuilder(appContext,dbFile.absolutePath)
}