package com.gracelink.android.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt root module.
 *
 * Repositories are @Singleton-injected via constructor @Inject (annotated on
 * the class itself) — no @Provides needed for them.
 *
 * Note: we do NOT @Provides @ApplicationContext Context here because Hilt
 * already provides it built-in. Providing it ourselves would create a
 * duplicate binding that can crash at runtime.
 *
 * Add Retrofit/OkHttp/Firebase/ExoPlayer providers here once those backends
 * are wired in.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule
