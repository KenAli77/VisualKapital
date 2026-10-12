package com.visualmoney.app

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.SQLiteDriver
import androidx.sqlite.driver.AndroidSQLiteDriver
import com.visualmoney.app.VisualMoneyApplication

actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val appContext = VisualMoneyApplication.context.applicationContext
    val dbFile = appContext.getDatabasePath("visual_money.db")
    return Room.databaseBuilder(appContext,dbFile.absolutePath)
}

actual fun getSQLDriver(): SQLiteDriver {
    return AndroidSQLiteDriver()
}