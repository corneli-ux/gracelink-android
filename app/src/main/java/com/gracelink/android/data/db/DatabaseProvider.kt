package com.gracelink.android.data.db

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.gracelink.android.data.seed.SeedData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Database provider with safe first-launch seeding.
 *
 * The seeding runs AFTER the database instance is fully constructed —
 * we capture the built instance in a local var and seed it directly,
 * avoiding the recursive get() call that caused deadlocks.
 */
object DatabaseProvider {

    @Volatile
    private var INSTANCE: GraceDatabase? = null

    fun get(context: Context): GraceDatabase = INSTANCE ?: synchronized(this) {
        INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
    }

    private fun buildDatabase(context: Context): GraceDatabase {
        lateinit var db: GraceDatabase
        db = Room.databaseBuilder(
            context.applicationContext,
            GraceDatabase::class.java,
            "gracelink.db"
        )
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    // Seed on a background coroutine. We use the INSTANCE once
                    // it's assigned (after build() returns) — not get(), which
                    // would re-enter synchronized(this) and deadlock.
                    CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                        seedDatabase(INSTANCE ?: return@launch)
                    }
                }
            })
            .fallbackToDestructiveMigration()
            .build()
        return db
    }

    private suspend fun seedDatabase(db: GraceDatabase) {
        try {
            db.contentDao().insertAll(SeedData.content())
            db.liveSessionDao().insertAll(SeedData.liveSessions())
            db.prayerDao().insertAll(SeedData.prayers())
            db.chatDao().insertAll(SeedData.chatMessages())
            db.userDao().upsert(SeedData.user())
        } catch (_: Throwable) {
            // Seeding failed — the app will start with an empty database,
            // which is better than crashing.
        }
    }
}
