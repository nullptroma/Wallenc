package com.github.nullptroma.wallenc.data.db

import android.content.Context
import androidx.room.Room
import com.github.nullptroma.wallenc.data.db.app.AppDb

class RoomFactory(private val context: Context) {
    fun buildAppDb(): AppDb {
        val room = Room.databaseBuilder(
            context, AppDb::class.java, "app-db"
        ).fallbackToDestructiveMigration().build()
        return room
    }
}