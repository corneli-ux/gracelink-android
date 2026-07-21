package com.gracelink.android.player

import android.content.Context
import android.media.MediaRecorder
import android.media.audiofx.AcousticEchoCanceler
import android.media.audiofx.AutomaticGainControl
import android.media.audiofx.NoiseSuppressor
import android.os.Build
import java.io.File

/**
 * Records a voice note/podcast take to a local file using MediaRecorder
 * (AAC/M4A). One recorder instance is meant for one recording at a time --
 * create a fresh instance (or call stop() then start() again) rather than
 * reusing across unrelated recordings.
 *
 * Applies real Android platform audio effects (NoiseSuppressor,
 * AcousticEchoCanceler, AutomaticGainControl) where the device actually
 * supports them -- this is a genuine, if modest, noise-reduction step
 * during recording, not a marketing claim. It is NOT the same thing as
 * ML-based post-processing denoising (RNNoise-style), which would need
 * either a bundled ML model or an FFmpeg-class DSP pipeline -- a much
 * larger, separate undertaking that isn't pretended to be covered here.
 */
class VoiceRecorder(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var noiseSuppressor: NoiseSuppressor? = null
    private var echoCanceler: AcousticEchoCanceler? = null
    private var gainControl: AutomaticGainControl? = null

    /** Starts recording to a new temp file in the app's cache dir. Returns that file's path. */
    fun start(): String {
        val file = File(context.cacheDir, "voice_${System.currentTimeMillis()}.m4a")
        outputFile = file

        @Suppress("DEPRECATION")
        val mr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(context) else MediaRecorder()

        mr.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(96_000)
            setAudioSamplingRate(44_100)
            setOutputFile(file.absolutePath)
            prepare()
        }
        applyAudioEffects(mr.audioSessionId)
        mr.start()
        recorder = mr
        return file.absolutePath
    }

    /** Enables whichever platform noise-reduction effects this specific
     * device actually supports. Every one of these is optional and
     * device-dependent -- absence of support is normal, not an error. */
    private fun applyAudioEffects(audioSessionId: Int) {
        try {
            if (NoiseSuppressor.isAvailable()) {
                noiseSuppressor = NoiseSuppressor.create(audioSessionId)?.apply { enabled = true }
            }
        } catch (_: Exception) { /* device doesn't support it -- fine, recording still works */ }
        try {
            if (AcousticEchoCanceler.isAvailable()) {
                echoCanceler = AcousticEchoCanceler.create(audioSessionId)?.apply { enabled = true }
            }
        } catch (_: Exception) { }
        try {
            if (AutomaticGainControl.isAvailable()) {
                gainControl = AutomaticGainControl.create(audioSessionId)?.apply { enabled = true }
            }
        } catch (_: Exception) { }
    }

    private fun releaseAudioEffects() {
        try { noiseSuppressor?.release() } catch (_: Exception) { }
        try { echoCanceler?.release() } catch (_: Exception) { }
        try { gainControl?.release() } catch (_: Exception) { }
        noiseSuppressor = null; echoCanceler = null; gainControl = null
    }

    /** Stops recording and releases the recorder. Returns the recorded file path, or null if nothing was recorded. */
    fun stop(): String? {
        val path = outputFile?.absolutePath
        try {
            recorder?.apply {
                stop()
                release()
            }
        } catch (_: Exception) {
            // If stop() is called too soon after start(), MediaRecorder can throw --
            // treat as "no usable recording" rather than crashing the caller.
            return null
        } finally {
            recorder = null
            releaseAudioEffects()
        }
        return path
    }

    /** Discards any in-progress recording without saving. */
    fun cancel() {
        try {
            recorder?.apply {
                reset()
                release()
            }
        } catch (_: Exception) {
        } finally {
            recorder = null
            releaseAudioEffects()
            outputFile?.delete()
            outputFile = null
        }
    }
}
