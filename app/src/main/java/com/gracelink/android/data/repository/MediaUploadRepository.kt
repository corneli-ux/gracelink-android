package com.gracelink.android.data.repository

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Uploads a local file (picked via Storage Access Framework, or recorded
 * on-device) to Firebase Storage and returns a public download URL.
 * Real end-to-end upload -- the Firebase Storage dependency and
 * google-services.json were already part of this project, just unused
 * for media until now.
 */
@Singleton
class MediaUploadRepository @Inject constructor() {

    private val storage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }

    /** Uploads a file the user picked (e.g. via ACTION_OPEN_DOCUMENT) and returns its download URL. */
    suspend fun uploadContentUri(uri: Uri, storagePath: String): String {
        val ref = storage.reference.child(storagePath)
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }

    /** Uploads a local file path (e.g. a MediaRecorder output file) and returns its download URL. */
    suspend fun uploadLocalFile(localPath: String, storagePath: String): String {
        val ref = storage.reference.child(storagePath)
        ref.putFile(Uri.fromFile(java.io.File(localPath))).await()
        return ref.downloadUrl.await().toString()
    }
}
