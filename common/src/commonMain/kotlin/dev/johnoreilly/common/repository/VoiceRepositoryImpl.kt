package dev.johnoreilly.common.repository

import dev.johnoreilly.common.model.AudioFile
import dev.johnoreilly.common.model.RecordingMetadata
import dev.johnoreilly.common.model.VADSettings
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class VoiceRepositoryImpl : VoiceRepository {
    
    private val recordings = mutableMapOf<String, AudioFile>()
    private val metadata = mutableMapOf<String, RecordingMetadata>()
    private val mutex = Mutex()
    private val json = Json { isLenient = true; ignoreUnknownKeys = true }
    
    // In a real implementation, these would be actual file paths
    private val recordingsDirectory = "VoiceRecordings/recordings"
    private val metadataDirectory = "VoiceRecordings/metadata"
    
    override suspend fun saveRecording(audioFile: AudioFile) = mutex.withLock {
        recordings[audioFile.id] = audioFile
        
        // Save metadata
        val recordingMetadata = RecordingMetadata(
            id = audioFile.id,
            timestamp = audioFile.timestamp,
            duration = audioFile.duration.toDouble() / 1000.0, // Convert to seconds
            fileSize = audioFile.fileSize,
            sampleRate = audioFile.sampleRate,
            channels = audioFile.channels,
            format = "WAV",
            vadSettings = VADSettings(
                silenceThreshold = 0.3f,
                voiceThreshold = 0.7f,
                silenceTimeout = 2000L,
                minRecordingDuration = 500L,
                maxRecordingDuration = 30000L
            )
        )
        
        metadata[audioFile.id] = recordingMetadata
    }
    
    override suspend fun getAllRecordings(): List<AudioFile> = mutex.withLock {
        recordings.values.sortedByDescending { it.timestamp }
    }
    
    override suspend fun getRecording(id: String): AudioFile? = mutex.withLock {
        recordings[id]
    }
    
    override suspend fun deleteRecording(filePath: String) = mutex.withLock {
        val recordingToDelete = recordings.values.find { it.filePath == filePath }
        recordingToDelete?.let { recording ->
            recordings.remove(recording.id)
            metadata.remove(recording.id)
        }
        Unit
    }
    
    override suspend fun deleteAllRecordings() = mutex.withLock {
        recordings.clear()
        metadata.clear()
    }
    
    override suspend fun getRecordingMetadata(id: String): RecordingMetadata? = mutex.withLock {
        metadata[id]
    }
    
    override suspend fun saveRecordingMetadata(recordingMetadata: RecordingMetadata) = mutex.withLock {
        metadata[recordingMetadata.id] = recordingMetadata
    }
}
