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
 * Uses the legacy JWT anon key rather than the newer sb_publishable_
 * format. Confirmed by direct testing (SET LOCAL ROLE anon; INSERT ...)
 * that the RLS policy itself is correct and permits this exact insert --
 * but real client requests using the sb_publishable_ key still failed
 * with "new row violates row-level security policy" on Storage uploads
 * specifically (not on regular database queries). This matches a
 * documented, open report of the same exact symptom: the Storage
 * microservice not evaluating sb_publishable_ requests with the same
 * auth context that PostgREST/direct SQL does. The legacy anon key is
 * explicitly still supported and carries identical permissions, so it
 * sidesteps that specific gap.
 *
 * Both keys are equally safe to ship in the app binary -- this is the
 * client-side key type by design, meant to be paired with RLS (which
 * this bucket has). The RLS policies are intentionally permissive
 * (bucket-scoped, not per-user) since this app authenticates with
 * Firebase, not Supabase Auth, so there's no Supabase user session to
 * scope policies to. That's a reasonable MVP starting point, not a
 * long-term security model -- tightening it later (e.g. exchanging
 * Firebase ID tokens for scoped Supabase access) is a real follow-up,
 * not required to ship working uploads today.
 */
@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    private const val SUPABASE_URL = "https://pywtyijdwvlbzdlagbvo.supabase.co"
    private const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InB5d3R5aWpkd3ZsYnpkbGFnYnZvIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODQ2NTA4MzEsImV4cCI6MjEwMDIyNjgzMX0.njQojax9G7au4AcUIIR2lALa8EGCyYnOiaxxgv2UigQ"

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_ANON_KEY,
    ) {
        install(Storage)
    }
}
