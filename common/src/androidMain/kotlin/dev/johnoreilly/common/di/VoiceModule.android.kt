package dev.johnoreilly.common.di

import android.content.Context
import dev.johnoreilly.common.service.AudioPlayerService
import dev.johnoreilly.common.service.VoiceNativeService
import org.koin.core.scope.Scope
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
actual class VoiceNativeModule actual constructor() {

    @Single
    actual fun voiceNativeService(): VoiceNativeService {
        val service = VoiceNativeService()
        // TODO: Initialize with context when needed
        return service
    }

    @Single
    actual fun audioPlayerService(): AudioPlayerService {
        val service = AudioPlayerService()
        // TODO: Initialize with context when needed
        return service
    }
}
