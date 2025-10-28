package dev.johnoreilly.common.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.johnoreilly.common.model.AudioFile
import dev.johnoreilly.common.model.MicrophonePermission
import dev.johnoreilly.common.model.VoiceState
import dev.johnoreilly.common.viewmodel.VoiceViewModel
import org.koin.compose.koinInject
import kotlinx.coroutines.launch

@Composable
fun VoiceScreenAndroid(viewModel: VoiceViewModel = koinInject()) {
    val voiceState by viewModel.voiceState.collectAsState()
    val microphonePermission by viewModel.microphonePermission.collectAsState()
    val lastRecording by viewModel.lastRecording.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val recordingList by viewModel.recordingList.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
            // Header
            Text(
                text = "Voice Recorder",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            // Status Card
            StatusCard(
                voiceState = voiceState,
                microphonePermission = microphonePermission
            )

            // Controls
            VoiceControlsCard(
                voiceState = voiceState,
                microphonePermission = microphonePermission,
                onStartListening = {
                    coroutineScope.launch { viewModel.startListening() }
                },
                onStopListening = {
                    coroutineScope.launch { viewModel.stopListening() }
                },
                onRequestPermission = {
                    coroutineScope.launch { viewModel.requestMicrophonePermission() }
                }
            )

            // Recordings List
            if (recordingList.isNotEmpty()) {
                RecordingsList(
                    recordings = recordingList,
                    currentlyPlayingId = viewModel.currentlyPlayingId.collectAsState().value,
                    onPlayRecording = { recordingId ->
                        coroutineScope.launch { viewModel.playRecording(recordingId) }
                    },
                    onStopRecording = { recordingId ->
                        coroutineScope.launch { viewModel.stopRecording(recordingId) }
                    }
                )
            }
        }
    }

@Composable
fun VoiceControlsCard(
    voiceState: VoiceState,
    microphonePermission: MicrophonePermission,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onRequestPermission: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Filled.PlayArrow,
                    contentDescription = "Controls",
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Controles",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (microphonePermission is MicrophonePermission.NotRequested || microphonePermission is MicrophonePermission.Denied) {
                Button(
                    onClick = onRequestPermission,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = "Request Permission")
                    Spacer(Modifier.width(8.dp))
                    Text("Solicitar Permiso")
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                                Button(
                                    onClick = onStartListening,
                                    enabled = voiceState is VoiceState.Idle || voiceState is VoiceState.Ready || voiceState is VoiceState.Error,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (voiceState is VoiceState.Idle || voiceState is VoiceState.Ready || voiceState is VoiceState.Error) 
                                            MaterialTheme.colorScheme.primary 
                                        else 
                                            MaterialTheme.colorScheme.outline
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Filled.PlayArrow, contentDescription = "Start Listening")
                                    Spacer(Modifier.width(8.dp))
                                    Text("Iniciar Detección")
                                }
                                Button(
                                    onClick = onStopListening,
                                    enabled = voiceState is VoiceState.Listening || voiceState is VoiceState.Recording,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (voiceState is VoiceState.Listening || voiceState is VoiceState.Recording) 
                                            MaterialTheme.colorScheme.error 
                                        else 
                                            MaterialTheme.colorScheme.outline
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Filled.Close, contentDescription = "Stop Listening")
                                    Spacer(Modifier.width(8.dp))
                                    Text("Detener Detección")
                                }
                }
            }
        }
    }
}

@Composable
fun PlaybackControlsCard(
    isPlaying: Boolean,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Reproducción", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onPlay,
                    enabled = !isPlaying,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (!isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                ) {
                    Icon(
                        Icons.Filled.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                IconButton(
                    onClick = onPause,
                    enabled = isPlaying,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (isPlaying) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline
                        )
                ) {
                    Icon(
                        Icons.Filled.PlayArrow,
                        contentDescription = "Pause",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                IconButton(
                    onClick = onStop,
                    enabled = isPlaying,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (isPlaying) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                        )
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Stop",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.error)
                ) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun RecordingsList(
    recordings: List<AudioFile>, 
    currentlyPlayingId: String?,
    onPlayRecording: (String) -> Unit,
    onStopRecording: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Grabaciones", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Column {
                recordings.forEach { recording ->
                    RecordingItem(
                        recording = recording, 
                        isPlaying = currentlyPlayingId == recording.id,
                        onPlayRecording = onPlayRecording,
                        onStopRecording = onStopRecording
                    )
                }
            }
        }
    }
}

@Composable
fun RecordingItem(
    recording: AudioFile, 
    isPlaying: Boolean,
    onPlayRecording: (String) -> Unit,
    onStopRecording: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("Grabación: ${recording.id}", style = MaterialTheme.typography.bodyMedium)
            Text("Duración: ${recording.duration / 1000}s", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        Button(
            onClick = { 
                if (isPlaying) {
                    onStopRecording(recording.id)
                } else {
                    onPlayRecording(recording.id)
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isPlaying) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                if (isPlaying) Icons.Filled.Close else Icons.Filled.PlayArrow, 
                contentDescription = if (isPlaying) "Stop Recording" else "Play Recording"
            )
        }
    }
    HorizontalDivider()
}

@Composable
fun StatusCard(voiceState: VoiceState, microphonePermission: MicrophonePermission) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Estado Actual",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(statusColor(voiceState))
                )
                Text(
                    text = statusText(voiceState),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Text(
                text = "Micrófono: ${microphonePermissionText(microphonePermission)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun statusColor(voiceState: VoiceState): Color {
    return when (voiceState) {
        is VoiceState.Idle, is VoiceState.Ready -> Color.Green
        is VoiceState.Listening, is VoiceState.Recording -> Color(0xFFFF9500) // Orange
        is VoiceState.Error -> Color.Red
        else -> Color.Gray
    }
}

private fun statusText(voiceState: VoiceState): String {
    return when (voiceState) {
        is VoiceState.Idle -> "Inactivo"
        is VoiceState.Listening -> "Escuchando..."
        is VoiceState.Recording -> "Grabando..."
        is VoiceState.Ready -> "Listo - Grabación disponible"
        is VoiceState.Error -> "Error"
        else -> "Desconocido"
    }
}

private fun microphonePermissionText(microphonePermission: MicrophonePermission): String {
    return when (microphonePermission) {
        is MicrophonePermission.NotRequested -> "No solicitado"
        is MicrophonePermission.Granted -> "Permitido"
        is MicrophonePermission.Denied -> "Denegado"
        is MicrophonePermission.Restricted -> "Restringido"
    }
}
