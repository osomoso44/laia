package dev.johnoreilly.common.di

import dev.johnoreilly.common.repository.VoiceRepository
import dev.johnoreilly.common.repository.VoiceRepositoryImpl
import dev.johnoreilly.common.service.AudioPlayerService
import dev.johnoreilly.common.service.VoiceNativeService
import dev.johnoreilly.common.viewmodel.VoiceViewModel
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class VoiceModule {
    
    @Single
    fun voiceRepository(): VoiceRepository = VoiceRepositoryImpl()
    
    @Factory
    fun voiceViewModel(
        voiceNativeService: VoiceNativeService,
        audioPlayerService: AudioPlayerService,
        voiceRepository: VoiceRepository
    ): VoiceViewModel = VoiceViewModel()
}
