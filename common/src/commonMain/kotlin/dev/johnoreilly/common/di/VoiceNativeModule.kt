package dev.johnoreilly.common.di

import dev.johnoreilly.common.service.AudioPlayerService
import dev.johnoreilly.common.service.VoiceNativeService
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
expect class VoiceNativeModule() {
    
    @Single
    fun voiceNativeService(): VoiceNativeService
    
    @Single
    fun audioPlayerService(): AudioPlayerService
}
