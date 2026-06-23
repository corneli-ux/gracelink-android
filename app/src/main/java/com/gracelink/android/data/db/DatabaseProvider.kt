package com.gracelink.android.data.db

import android.content.Context
import androidx.room.Room
import com.gracelink.android.data.seed.SeedData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object DatabaseProvider {

    @Volatile
    private var INSTANCE: GraceDatabase? = null

    fun get(context: Context): GraceDatabase = INSTANCE ?: synchronized(this) {
        INSTANCE ?: Room.databaseBuilder(
            context.applicationContext,
            GraceDatabase::class.java,
            "gracelink.db"
        )
            .addCallback(object : androidx.room.RoomDatabase.Callback() {
                override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                    super.onCreate(db)
                    // Populate the database with seed data on first creation.
                    CoroutineScope(Dispatchers.IO).launch {
                        val dao = get(context)
                        dao.contentDao().insertAll(SeedData.content())
                        dao.liveSessionDao().insertAll(SeedData.liveSessions())
                        dao.prayerDao().insertAll(SeedData.prayers())
                        dao.chatDao().insertAll(SeedData.chatMessages())
                        dao.userDao().upsert(SeedData.user())
                    }
                }
            })
            .fallbackToDestructiveMigration()
            .build()
            .also { INSTANCE = it }
    }
}
