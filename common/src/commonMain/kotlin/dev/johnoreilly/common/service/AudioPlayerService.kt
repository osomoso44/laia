package dev.johnoreilly.common.service

import kotlinx.coroutines.flow.StateFlow

expect class AudioPlayerService {
    fun setCompletionCallback(callback: (String) -> Unit)
    suspend fun playAudio(filePath: String)
    suspend fun pauseAudio()
    suspend fun stopAudio()
}
