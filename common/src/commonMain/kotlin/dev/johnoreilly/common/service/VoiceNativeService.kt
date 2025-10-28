package dev.johnoreilly.common.service

import dev.johnoreilly.common.model.VoiceCommand
import dev.johnoreilly.common.model.VoiceEvent

expect class VoiceNativeService {
    suspend fun startListening()
    suspend fun stopListening()
    suspend fun playAudio(filePath: String)
    suspend fun pauseAudio()
    suspend fun stopAudio()
    suspend fun requestMicrophonePermission(): Boolean
    
    fun setEventCallback(callback: (VoiceEvent) -> Unit)
}
