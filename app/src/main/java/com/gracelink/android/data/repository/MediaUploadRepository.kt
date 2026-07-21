package com.gracelink.android.data.repository

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.upload
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Uploads a local file (picked via Storage Access Framework, or recorded
 * on-device) to Supabase Storage and returns a public URL.
 *
 * Was Firebase Storage -- moved here because Firebase now requires the
 * paid Blaze plan for any Cloud Storage usage at all, even the free-tier
 * default bucket, which was the actual cause of every upload failing
 * with "Object does not exist at location" regardless of what this code
 * did. Firebase Auth and Firestore are unaffected by that and stay as
 * they are; only file storage moved.
 *
 * Public method signatures (uploadContentUri/uploadLocalFile) are
 * unchanged on purpose, so every existing caller -- profile photos,
 * church photos, podcast covers/episodes -- needed zero changes.
 */
@Singleton
class MediaUploadRepository @Inject constructor(
    private val supabase: SupabaseClient,
    @ApplicationContext private val context: Context,
) {
    private val bucket = supabase.storage.from("media")

    /** Uploads a file the user picked (e.g. via ACTION_OPEN_DOCUMENT) and returns its public URL. */
    suspend fun uploadContentUri(uri: Uri, storagePath: String): String {
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IllegalStateException("Couldn't read the selected file")
        bucket.upload(storagePath, bytes) { upsert = true }
        return bucket.publicUrl(storagePath)
    }

    /** Uploads a local file path (e.g. a MediaRecorder output file) and returns its public URL. */
    suspend fun uploadLocalFile(localPath: String, storagePath: String): String {
        bucket.upload(storagePath, File(localPath)) { upsert = true }
        return bucket.publicUrl(storagePath)
    }
}
