package dev.johnoreilly.common.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.johnoreilly.common.model.*
import dev.johnoreilly.common.viewmodel.VoiceViewModel

@Composable
fun VoiceScreen(
    viewModel: VoiceViewModel,
    modifier: Modifier = Modifier
) {
    val voiceState by viewModel.voiceState.collectAsStateWithLifecycle()
    val microphonePermission by viewModel.microphonePermission.collectAsStateWithLifecycle()
    val lastRecording by viewModel.lastRecording.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val recordingList by viewModel.recordingList.collectAsStateWithLifecycle()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        Text(
            text = "Voice Recording",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        // Status Card
        VoiceStatusCard(
            voiceState = voiceState,
            microphonePermission = microphonePermission
        )
        
        // Controls
        VoiceControlsCard(
            voiceState = voiceState,
            microphonePermission = microphonePermission,
            onStartListening = { 
                // This will be handled by the ViewModel
            },
            onStopListening = { 
                // This will be handled by the ViewModel
            },
            onRequestPermission = { 
                // This will be handled by the ViewModel
            }
        )
        
        // Playback Controls
        if (lastRecording != null) {
            PlaybackControlsCard(
                isPlaying = isPlaying,
                onPlay = { 
                    // This will be handled by the ViewModel
                },
                onPause = { 
                    // This will be handled by the ViewModel
                },
                onStop = { 
                    // This will be handled by the ViewModel
                },
                onDelete = { 
                    // This will be handled by the ViewModel
                }
            )
        }
        
        // Recordings List
        if (recordingList.isNotEmpty()) {
            RecordingsListCard(
                recordings = recordingList,
                onPlayRecording = { filePath -> 
                    // This would need to be implemented in the ViewModel
                }
            )
        }
    }
}

@Composable
private fun VoiceStatusCard(
    voiceState: VoiceState,
    microphonePermission: MicrophonePermission,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (voiceState) {
                is VoiceState.Error -> MaterialTheme.colorScheme.errorContainer
                is VoiceState.Listening -> MaterialTheme.colorScheme.primaryContainer
                is VoiceState.Recording -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = when (voiceState) {
                    is VoiceState.Idle -> "Ready to start"
                    is VoiceState.Initializing -> "Initializing..."
                    is VoiceState.Listening -> "Listening for voice..."
                    is VoiceState.Recording -> "Recording... (${voiceState.duration}ms)"
                    is VoiceState.Processing -> "Processing recording..."
                    is VoiceState.Ready -> "Ready - Recording available"
                    is VoiceState.Playing -> "Playing audio..."
                    is VoiceState.Error -> "Error: ${voiceState.message}"
                },
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "Permission: ${microphonePermission::class.simpleName}",
                style = MaterialTheme.typography.bodySmall,
                color = when (microphonePermission) {
                    is MicrophonePermission.Granted -> Color.Green
                    is MicrophonePermission.Denied -> Color.Red
                    else -> Color.Gray
                }
            )
        }
    }
}

@Composable
private fun VoiceControlsCard(
    voiceState: VoiceState,
    microphonePermission: MicrophonePermission,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Controls",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (microphonePermission) {
                    is MicrophonePermission.NotRequested, is MicrophonePermission.Denied -> {
                        Button(
                            onClick = onRequestPermission,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Request Permission")
                        }
                    }
                    is MicrophonePermission.Granted -> {
                        when (voiceState) {
                            is VoiceState.Idle, is VoiceState.Ready -> {
                                Button(
                                    onClick = onStartListening,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Start Listening")
                                }
                            }
                            is VoiceState.Listening, is VoiceState.Recording -> {
                                Button(
                                    onClick = onStopListening,
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Text("Stop Listening")
                                }
                            }
                            else -> {
                                Button(
                                    onClick = { },
                                    modifier = Modifier.weight(1f),
                                    enabled = false
                                ) {
                                    Text("Processing...")
                                }
                            }
                        }
                    }
                    is MicrophonePermission.Restricted -> {
                        Text(
                            text = "Microphone access restricted",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaybackControlsCard(
    isPlaying: Boolean,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Playback",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isPlaying) {
                    Button(
                        onClick = onPause,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Pause")
                    }
                    Button(
                        onClick = onStop,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Stop")
                    }
                } else {
                    Button(
                        onClick = onPlay,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Play Last Recording")
                    }
                }
                
                Button(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            }
        }
    }
}

@Composable
private fun RecordingsListCard(
    recordings: List<AudioFile>,
    onPlayRecording: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Recordings (${recordings.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(recordings) { recording ->
                    RecordingItem(
                        recording = recording,
                        onPlay = { onPlayRecording(recording.filePath) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RecordingItem(
    recording: AudioFile,
    onPlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = recording.id,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Duration: ${recording.duration}ms",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Button(
                onClick = onPlay,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Play")
            }
        }
    }
}
