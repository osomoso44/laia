package dev.johnoreilly.common.service

import kotlinx.coroutines.delay

actual class AudioPlayerService {
    private var completionCallback: ((String) -> Unit)? = null
    private var currentPlayingFilePath: String? = null

    actual fun setCompletionCallback(callback: (String) -> Unit) {
        this.completionCallback = callback
    }

    actual suspend fun playAudio(filePath: String) {
        println("🔊 iOS Native Service: Playing audio from $filePath")
        currentPlayingFilePath = filePath
        // TODO: Implement actual iOS audio playback using AVAudioPlayer
        // For now, simulate playback completion after 3 seconds
        delay(3000)
        currentPlayingFilePath?.let {
            completionCallback?.invoke(it)
        }
        currentPlayingFilePath = null
    }

    actual suspend fun pauseAudio() {
        println("🔊 iOS Native Service: Pausing audio")
        // TODO: Implement actual pause
    }

    actual suspend fun stopAudio() {
        println("🔊 iOS Native Service: Stopping audio")
        currentPlayingFilePath?.let {
            completionCallback?.invoke(it)
        }
        currentPlayingFilePath = null
    }
}
