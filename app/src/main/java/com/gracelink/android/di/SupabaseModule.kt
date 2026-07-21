package com.gracelink.android.di

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Storage-only Supabase client -- Auth and the database both stay on
 * Firebase (Firestore/Firebase Auth), since neither of those is broken.
 * Only file uploads moved here, since Firebase Storage now requires the
 * paid Blaze plan for any usage at all, even the free-tier default bucket.
 *
 * The publishable key embedded here is safe to ship in the app binary --
 * it's the client-side key Supabase's own docs say is meant for exactly
 * this (see "New API Keys" migration guide). It is intentionally paired
 * with permissive RLS policies on the `media` bucket rather than
 * per-user-scoped ones, since this app authenticates with Firebase, not
 * Supabase Auth, so there's no Supabase user session to scope policies
 * to. That's a reasonable MVP starting point, not a long-term security
 * model -- tightening it later (e.g. exchanging Firebase ID tokens for
 * scoped Supabase access) is a real follow-up, not required to ship
 * working uploads today.
 */
@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    private const val SUPABASE_URL = "https://pywtyijdwvlbzdlagbvo.supabase.co"
    private const val SUPABASE_PUBLISHABLE_KEY = "sb_publishable_Oxf7mjfcP5c6yYwr0HbVXQ_hUb0cVF5"

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_PUBLISHABLE_KEY,
    ) {
        install(Storage)
    }
}
