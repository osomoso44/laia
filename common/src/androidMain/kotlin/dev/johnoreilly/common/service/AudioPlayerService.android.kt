package dev.johnoreilly.common.service

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

actual class AudioPlayerService : MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
    private var mediaPlayer: MediaPlayer? = null
    private var completionCallback: ((String) -> Unit)? = null
    private var currentPlayingFilePath: String? = null
    private val _isPlaying = MutableStateFlow(false)
    private var context: Context? = null

    // actual val isPlaying: StateFlow<Boolean> = _isPlaying

    fun initialize(context: Context) {
        this.context = context
    }

    actual fun setCompletionCallback(callback: (String) -> Unit) {
        this.completionCallback = callback
    }

    actual suspend fun playAudio(filePath: String) {
        println("🔊 Android Native Service: Playing audio from $filePath")
        currentPlayingFilePath = filePath
        
        try {
            // Release previous player if exists
            mediaPlayer?.release()
            
            mediaPlayer = MediaPlayer().apply {
                setOnCompletionListener(this@AudioPlayerService)
                setOnErrorListener(this@AudioPlayerService)
                
                // Try to set data source from file path
                try {
                    setDataSource(filePath)
                    prepare()
                    start()
                    _isPlaying.value = true
                } catch (e: Exception) {
                    println("Error setting data source: ${e.message}")
                    // For simulation, just delay and complete
                    GlobalScope.launch {
                        delay(3000)
                        onCompletion(mediaPlayer)
                    }
                }
            }
        } catch (e: Exception) {
            println("Error creating media player: ${e.message}")
            throw Exception("Failed to create media player: ${e.message}")
        }
    }

    actual suspend fun pauseAudio() {
        println("🔊 Android Native Service: Pausing audio")
        mediaPlayer?.pause()
        _isPlaying.value = false
    }

    actual suspend fun stopAudio() {
        println("🔊 Android Native Service: Stopping audio")
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        currentPlayingFilePath = null
        _isPlaying.value = false
    }

    override fun onCompletion(mp: MediaPlayer?) {
        println("🔊 Android Native Service: Audio playback finished")
        currentPlayingFilePath?.let {
            completionCallback?.invoke(it)
        }
        mediaPlayer?.release()
        mediaPlayer = null
        currentPlayingFilePath = null
        _isPlaying.value = false
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        println("🔊 Android Native Service: Audio error: what=$what, extra=$extra")
        currentPlayingFilePath?.let {
            completionCallback?.invoke(it) // Indicate completion even on error
        }
        mediaPlayer?.release()
        mediaPlayer = null
        currentPlayingFilePath = null
        _isPlaying.value = false
        return true
    }
}
