package dev.johnoreilly.common.service

import dev.johnoreilly.common.model.VoiceCommand
import dev.johnoreilly.common.model.VoiceEvent
import kotlinx.coroutines.flow.StateFlow

expect class VoiceNativeService {
    suspend fun startListening()
    suspend fun stopListening()
    suspend fun playAudio(filePath: String)
    suspend fun pauseAudio()
    suspend fun stopAudio()
    suspend fun requestMicrophonePermission(): Boolean
    
    val isPlaying: StateFlow<Boolean>
    
    fun setEventCallback(callback: (VoiceEvent) -> Unit)
}
