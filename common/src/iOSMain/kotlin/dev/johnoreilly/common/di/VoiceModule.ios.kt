package dev.johnoreilly.common.di

import dev.johnoreilly.common.service.AudioPlayerService
import dev.johnoreilly.common.service.VoiceNativeService
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
actual class VoiceNativeModule actual constructor() {
    
    @Single
    actual fun voiceNativeService(): VoiceNativeService = VoiceNativeService()

    @Single
    actual fun audioPlayerService(): AudioPlayerService = AudioPlayerService()
}
