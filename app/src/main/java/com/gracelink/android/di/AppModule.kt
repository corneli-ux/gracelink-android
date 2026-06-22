package com.gracelink.android.di

import android.content.Context
import com.gracelink.android.data.repository.ContentRepository
import com.gracelink.android.data.repository.LiveSessionRepository
import com.gracelink.android.data.repository.PrayerRepository
import com.gracelink.android.data.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt root module. Repositories are @Singleton-injected (annotated on the
 * class itself) — this module is reserved for things that need a Context
 * (ExoPlayer factory, OkHttp client, etc.) and will grow as we wire real
 * backends.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideApplicationContext(@ApplicationContext ctx: Context): Context = ctx

    // Repositories are injected via constructor @Inject — no @Provides needed.
    // Add Retrofit/OkHttp/Firebase providers here once those backends are live.
}
