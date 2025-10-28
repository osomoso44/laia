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
    
    private val _currentlyPlayingId = MutableStateFlow<String?>(null)
    val currentlyPlayingId: StateFlow<String?> = _currentlyPlayingId.asStateFlow()
    
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
                    // Change to recording state when voice is detected
                    _voiceState.value = VoiceState.Recording(0L)
                    println("🎤 Voice detected - changing to recording state")
                }
                is VoiceEvent.RecordingStarted -> {
                    // Update recording state with duration
                    _voiceState.value = VoiceState.Recording(0L)
                    println("🎤 Recording started")
                }
                is VoiceEvent.RecordingCompleted -> {
                    _lastRecording.value = event.filePath
                    println("🎤 Recording completed - returning to listening state")
                    // Return to listening state after recording is completed
                    _voiceState.value = VoiceState.Listening
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
                            println("🎤 Saved recording: ${audioFile.id}")
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
        
        // TODO: Implement isPlaying observation when AudioPlayerService supports it
    }
    
    private fun loadRecordings() {
        viewModelScope.launch {
            try {
                val recordings = voiceRepository.getAllRecordings()
                println("🎤 Loaded ${recordings.size} recordings")
                _recordingList.value = recordings
                
                // Update lastRecording and state based on recordings
                if (recordings.isNotEmpty()) {
                    _lastRecording.value = recordings.first().filePath
                    println("🎤 Last recording: ${recordings.first().filePath}")
                    // Only update state if we're not currently listening or recording
                    if (_voiceState.value !is VoiceState.Listening && _voiceState.value !is VoiceState.Recording) {
                        _voiceState.value = VoiceState.Ready(recordings.first().filePath)
                    }
                } else {
                    _lastRecording.value = null
                    println("🎤 No recordings found")
                    // Only update state if we're not currently listening or recording
                    if (_voiceState.value !is VoiceState.Listening && _voiceState.value !is VoiceState.Recording) {
                        _voiceState.value = VoiceState.Idle
                    }
                }
            } catch (e: Exception) {
                println("🎤 Error loading recordings: ${e.message}")
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
    
    suspend fun playRecording(recordingId: String) {
        val recording = _recordingList.value.find { it.id == recordingId } ?: return
        
        try {
            _isPlaying.value = true
            _currentlyPlayingId.value = recordingId
            audioPlayerService.playAudio(recording.filePath)
            _isPlaying.value = false
            _currentlyPlayingId.value = null
        } catch (e: Exception) {
            _isPlaying.value = false
            _currentlyPlayingId.value = null
            _voiceState.value = VoiceState.Error("Failed to play recording: ${e.message}")
        }
    }
    
    suspend fun stopRecording(recordingId: String) {
        if (_currentlyPlayingId.value == recordingId) {
            try {
                audioPlayerService.stopAudio()
                _isPlaying.value = false
                _currentlyPlayingId.value = null
            } catch (e: Exception) {
                _voiceState.value = VoiceState.Error("Failed to stop recording: ${e.message}")
            }
        }
    }
    
    suspend fun playLastRecording() {
        val recording = _lastRecording.value ?: return
        
        try {
            _isPlaying.value = true
            audioPlayerService.playAudio(recording)
            _isPlaying.value = false
        } catch (e: Exception) {
            _isPlaying.value = false
            _voiceState.value = VoiceState.Error("Failed to play recording: ${e.message}")
        }
    }
    
    suspend fun pausePlayback() {
        try {
            audioPlayerService.pauseAudio()
            _isPlaying.value = false
        } catch (e: Exception) {
            _voiceState.value = VoiceState.Error("Failed to pause playback: ${e.message}")
        }
    }
    
    suspend fun stopPlayback() {
        try {
            audioPlayerService.stopAudio()
            _isPlaying.value = false
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
