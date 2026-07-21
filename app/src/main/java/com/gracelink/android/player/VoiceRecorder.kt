package com.gracelink.android.player

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File

/**
 * Records a voice note/podcast take to a local file using MediaRecorder
 * (AAC/M4A). One recorder instance is meant for one recording at a time --
 * create a fresh instance (or call stop() then start() again) rather than
 * reusing across unrelated recordings.
 *
 * NOISE/ECHO/GAIN HANDLING, corrected after getting this wrong once: the
 * android.media.audiofx effects (NoiseSuppressor, AcousticEchoCanceler,
 * AutomaticGainControl) attach to an AudioRecord session id via
 * AudioRecord.getAudioSessionId() -- MediaRecorder has no equivalent public
 * method to get a session id to attach them to, so those effects genuinely
 * cannot be wired onto a MediaRecorder-based recorder this way (an earlier
 * version of this file tried to and didn't compile). The real, documented
 * way to get this benefit while still using MediaRecorder is the audio
 * SOURCE itself: VOICE_COMMUNICATION (rather than plain MIC) is specifically
 * documented to "take advantage of echo cancellation or automatic gain
 * control if available" on the device, routing through the same processing
 * path phone calls use. It's not user-controllable per-effect and not
 * every device applies it, but it's genuine platform-level processing
 * during recording, not a fake toggle.
 */
class VoiceRecorder(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null

    /** Starts recording to a new temp file in the app's cache dir. Returns that file's path. */
    fun start(): String {
        val file = File(context.cacheDir, "voice_${System.currentTimeMillis()}.m4a")
        outputFile = file

        @Suppress("DEPRECATION")
        val mr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(context) else MediaRecorder()

        mr.apply {
            setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(96_000)
            setAudioSamplingRate(44_100)
            setOutputFile(file.absolutePath)
            prepare()
            start()
        }
        recorder = mr
        return file.absolutePath
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
            outputFile?.delete()
            outputFile = null
        }
    }
}
