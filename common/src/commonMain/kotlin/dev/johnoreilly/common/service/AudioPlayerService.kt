package dev.johnoreilly.common.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface AudioPlayerService {
    suspend fun playAudio(filePath: String): Result<Unit>
    suspend fun pauseAudio(): Result<Unit>
    suspend fun stopAudio(): Result<Unit>
    suspend fun getPlaybackPosition(): Long
    suspend fun getDuration(): Long
    val isPlaying: StateFlow<Boolean>
    val playbackPosition: StateFlow<Long>
}
