package dev.johnoreilly.common.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.johnoreilly.common.model.*
import dev.johnoreilly.common.service.AudioPlayerService
import dev.johnoreilly.common.service.VoiceNativeService
import dev.johnoreilly.common.repository.VoiceRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class VoiceViewModel : ViewModel(), KoinComponent {
    
    private val voiceNativeService: VoiceNativeService by inject()
    private val audioPlayerService: AudioPlayerService by inject()
    private val voiceRepository: VoiceRepository by inject()
    
    private val _voiceState = MutableStateFlow<VoiceState>(VoiceState.Idle)
    val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()
    
    private val _microphonePermission = MutableStateFlow<MicrophonePermission>(MicrophonePermission.NotRequested)
    val microphonePermission: StateFlow<MicrophonePermission> = _microphonePermission.asStateFlow()
    
    private val _lastRecording = MutableStateFlow<String?>(null)
    val lastRecording: StateFlow<String?> = _lastRecording.asStateFlow()
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _recordingList = MutableStateFlow<List<AudioFile>>(emptyList())
    val recordingList: StateFlow<List<AudioFile>> = _recordingList.asStateFlow()
    
    init {
        setupEventHandling()
        loadRecordings()
    }
    
    private fun setupEventHandling() {
        voiceNativeService.setEventCallback { event ->
            when (event) {
                is VoiceEvent.VoiceDetected -> {
                    _voiceState.value = VoiceState.Recording(event.timestamp)
                }
                is VoiceEvent.RecordingStarted -> {
                    _voiceState.value = VoiceState.Recording(event.timestamp)
                }
                is VoiceEvent.RecordingCompleted -> {
                    _lastRecording.value = event.filePath
                    _voiceState.value = VoiceState.Processing(event.filePath)
                    
                    viewModelScope.launch {
                        try {
                            val audioFile = AudioFile(
                                id = generateRecordingId(),
                                filePath = event.filePath,
                                timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds(),
                                duration = event.duration,
                                fileSize = 0L, // Will be updated by repository
                                sampleRate = 44100,
                                channels = 1
                            )
                            voiceRepository.saveRecording(audioFile)
                            _voiceState.value = VoiceState.Ready(event.filePath)
                            loadRecordings()
                        } catch (e: Exception) {
                            _voiceState.value = VoiceState.Error("Failed to save recording: ${e.message}")
                        }
                    }
                }
                is VoiceEvent.ErrorOccurred -> {
                    _voiceState.value = VoiceState.Error(event.error)
                }
                is VoiceEvent.PermissionChanged -> {
                    _microphonePermission.value = if (event.granted) {
                        MicrophonePermission.Granted
                    } else {
                        MicrophonePermission.Denied
                    }
                }
            }
        }
        
        audioPlayerService.isPlaying.onEach { playing ->
            _isPlaying.value = playing
        }.launchIn(viewModelScope)
    }
    
    private fun loadRecordings() {
        viewModelScope.launch {
            try {
                val recordings = voiceRepository.getAllRecordings()
                _recordingList.value = recordings
            } catch (e: Exception) {
                // Handle error silently for now
            }
        }
    }
    
    private fun generateRecordingId(): String {
        return "recording_${kotlinx.datetime.Clock.System.now().toEpochMilliseconds()}"
    }
    
    suspend fun startListening() {
        if (_microphonePermission.value != MicrophonePermission.Granted) {
            _voiceState.value = VoiceState.Error("Microphone permission not granted")
            return
        }
        
        _voiceState.value = VoiceState.Initializing
        try {
            voiceNativeService.startListening()
            _voiceState.value = VoiceState.Listening
        } catch (e: Exception) {
            _voiceState.value = VoiceState.Error("Failed to start listening: ${e.message}")
        }
    }
    
    suspend fun stopListening() {
        try {
            voiceNativeService.stopListening()
            _voiceState.value = VoiceState.Idle
        } catch (e: Exception) {
            _voiceState.value = VoiceState.Error("Failed to stop listening: ${e.message}")
        }
    }
    
    suspend fun requestMicrophonePermission() {
        try {
            val granted = voiceNativeService.requestMicrophonePermission()
            _microphonePermission.value = if (granted) {
                MicrophonePermission.Granted
            } else {
                MicrophonePermission.Denied
            }
        } catch (e: Exception) {
            _microphonePermission.value = MicrophonePermission.Denied
        }
    }
    
    suspend fun playLastRecording() {
        val recording = _lastRecording.value ?: return
        
        try {
            audioPlayerService.playAudio(recording)
        } catch (e: Exception) {
            _voiceState.value = VoiceState.Error("Failed to play recording: ${e.message}")
        }
    }
    
    suspend fun pausePlayback() {
        try {
            audioPlayerService.pauseAudio()
        } catch (e: Exception) {
            _voiceState.value = VoiceState.Error("Failed to pause playback: ${e.message}")
        }
    }
    
    suspend fun stopPlayback() {
        try {
            audioPlayerService.stopAudio()
        } catch (e: Exception) {
            _voiceState.value = VoiceState.Error("Failed to stop playback: ${e.message}")
        }
    }
    
    suspend fun deleteLastRecording() {
        val recording = _lastRecording.value ?: return
        
        try {
            voiceRepository.deleteRecording(recording)
            _lastRecording.value = null
            _voiceState.value = VoiceState.Idle
            loadRecordings()
        } catch (e: Exception) {
            _voiceState.value = VoiceState.Error("Failed to delete recording: ${e.message}")
        }
    }
    
    fun clearError() {
        if (_voiceState.value is VoiceState.Error) {
            _voiceState.value = VoiceState.Idle
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            voiceNativeService.stopListening()
            audioPlayerService.stopAudio()
        }
    }
}
