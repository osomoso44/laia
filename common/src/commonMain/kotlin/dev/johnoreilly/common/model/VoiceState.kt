package dev.johnoreilly.common.model

sealed class VoiceState {
    object Idle : VoiceState()
    object Listening : VoiceState()
    data class Recording(val duration: Long) : VoiceState()
    data class Ready(val lastRecording: String?) : VoiceState()
    data class Playing(val filePath: String, val position: Long) : VoiceState()
    data class Error(val message: String) : VoiceState()
}

sealed class MicrophonePermission {
    object NotRequested : MicrophonePermission()
    object Granted : MicrophonePermission()
    object Denied : MicrophonePermission()
    object Restricted : MicrophonePermission()
}

sealed class VoiceEvent {
    data class VoiceDetected(val timestamp: Long) : VoiceEvent()
    data class RecordingStarted(val timestamp: Long) : VoiceEvent()
    data class RecordingCompleted(val filePath: String, val duration: Long) : VoiceEvent()
    data class ErrorOccurred(val error: String) : VoiceEvent()
    data class PermissionChanged(val granted: Boolean) : VoiceEvent()
}

sealed class VoiceCommand {
    object StartListening : VoiceCommand()
    object StopListening : VoiceCommand()
    object StartRecording : VoiceCommand()
    object StopRecording : VoiceCommand()
    data class PlayAudio(val filePath: String) : VoiceCommand()
    object PauseAudio : VoiceCommand()
    object StopAudio : VoiceCommand()
}

data class AudioFile(
    val id: String,
    val filePath: String,
    val timestamp: Long,
    val duration: Long,
    val fileSize: Long,
    val sampleRate: Int,
    val channels: Int
)

data class RecordingMetadata(
    val id: String,
    val timestamp: Long,
    val duration: Double,
    val fileSize: Long,
    val sampleRate: Int,
    val channels: Int,
    val format: String,
    val vadSettings: VADSettings
)

data class VADSettings(
    val silenceThreshold: Float,
    val voiceThreshold: Float,
    val silenceTimeout: Long,
    val minRecordingDuration: Long,
    val maxRecordingDuration: Long
)
