package dev.johnoreilly.common.repository

import dev.johnoreilly.common.model.AudioFile
import dev.johnoreilly.common.model.RecordingMetadata

interface VoiceRepository {
    suspend fun saveRecording(audioFile: AudioFile)
    suspend fun getAllRecordings(): List<AudioFile>
    suspend fun getRecording(id: String): AudioFile?
    suspend fun deleteRecording(filePath: String)
    suspend fun deleteAllRecordings()
    suspend fun getRecordingMetadata(id: String): RecordingMetadata?
    suspend fun saveRecordingMetadata(metadata: RecordingMetadata)
}
