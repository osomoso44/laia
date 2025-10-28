import SwiftUI
import common

struct VoiceScreen: View {
    @State var viewModel = VoiceViewModel()

    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 20) {
                    Text("Voice Recorder")
                        .font(.largeTitle)
                        .fontWeight(.bold)
                        .padding(.bottom, 10)

                    Observing(viewModel.voiceState) { voiceState in
                        Observing(viewModel.microphonePermission) { microphonePermission in
                            StatusCard(
                                voiceState: voiceState,
                                microphonePermission: microphonePermission
                            )
                        }
                    }

                    Observing(viewModel.voiceState) { voiceState in
                        Observing(viewModel.microphonePermission) { microphonePermission in
                            VoiceControlsCard(
                                voiceState: voiceState,
                                microphonePermission: microphonePermission,
                                onStartListening: { Task { try? await viewModel.startListening() } },
                                onStopListening: { Task { try? await viewModel.stopListening() } },
                                onRequestPermission: { Task { try? await viewModel.requestMicrophonePermission() } }
                            )
                        }
                    }


                    Observing(viewModel.recordingList) { recordingList in
                        if !recordingList.isEmpty {
                            Observing(viewModel.currentlyPlayingId) { currentlyPlayingId in
                                RecordingsList(
                                    recordings: recordingList,
                                    currentlyPlayingId: currentlyPlayingId,
                                    onPlayRecording: { recordingId in
                                        Task { try? await viewModel.playRecording(recordingId: recordingId) }
                                    },
                                    onStopRecording: { recordingId in
                                        Task { try? await viewModel.stopRecording(recordingId: recordingId) }
                                    }
                                )
                            }
                        } else {
                            Text("No hay grabaciones disponibles")
                                .foregroundColor(.secondary)
                                .padding()
                        }
                    }
                }
                .padding()
            }
            .navigationTitle("Voice")
            .navigationBarTitleDisplayMode(.inline)
        }
        .onAppear {
            Task { try? await viewModel.requestMicrophonePermission() }
        }
    }
}

// MARK: - Status Card

struct StatusCard: View {
    let voiceState: VoiceState
    let microphonePermission: MicrophonePermission
    
    var body: some View {
        VStack(spacing: 12) {
            Text("Estado Actual")
                .font(.headline)
            
            HStack {
                Circle()
                    .fill(statusColor)
                    .frame(width: 12, height: 12)
                
                Text(statusText)
                    .font(.subheadline)
            }
            
            Text("Micrófono: \(microphonePermissionText)")
                .font(.caption)
                .foregroundColor(.secondary)
        }
        .padding()
        .background(Color(.systemGray6))
        .cornerRadius(12)
    }
    
    private var statusColor: Color {
        switch onEnum(of: voiceState) {
        case .idle, .ready:
            return .green
        case .listening, .recording:
            return .orange
        case .error:
            return .red
        default:
            return .gray
        }
    }
    
    private var statusText: String {
        switch onEnum(of: voiceState) {
        case .idle:
            return "Inactivo"
        case .listening:
            return "Escuchando..."
        case .recording:
            return "Grabando..."
        case .ready:
            return "Listo - Grabación disponible"
        case .error:
            return "Error"
        default:
            return "Desconocido"
        }
    }
    
    private var microphonePermissionText: String {
        switch onEnum(of: microphonePermission) {
        case .notRequested:
            return "No solicitado"
        case .granted:
            return "Permitido"
        case .denied:
            return "Denegado"
        case .restricted:
            return "Restringido"
        }
    }
}

// MARK: - Voice Controls Card

struct VoiceControlsCard: View {
    let voiceState: VoiceState
    let microphonePermission: MicrophonePermission
    let onStartListening: () -> Void
    let onStopListening: () -> Void
    let onRequestPermission: () -> Void
    
    var body: some View {
        VStack(spacing: 16) {
            Text("Controles")
                .font(.headline)
            
            switch onEnum(of: microphonePermission) {
            case .notRequested, .denied:
                Button(action: onRequestPermission) {
                    HStack {
                        Image(systemName: "mic.fill")
                        Text("Solicitar Permiso")
                    }
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(Color.blue)
                    .foregroundColor(.white)
                    .cornerRadius(8)
                }
            case .granted, .restricted:
                HStack(spacing: 16) {
                    Button(action: onStartListening) {
                        HStack {
                            Image(systemName: "play.fill")
                            Text("Iniciar Detección")
                        }
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(canStartListening ? Color.green : Color.gray)
                        .foregroundColor(.white)
                        .cornerRadius(8)
                    }
                    .disabled(!canStartListening)
                    
                    Button(action: onStopListening) {
                        HStack {
                            Image(systemName: "stop.fill")
                            Text("Detener Detección")
                        }
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(canStopListening ? Color.red : Color.gray)
                        .foregroundColor(.white)
                        .cornerRadius(8)
                    }
                    .disabled(!canStopListening)
                }
            }
        }
        .padding()
        .background(Color(.systemGray6))
        .cornerRadius(12)
    }
    
    private var canStartListening: Bool {
        switch onEnum(of: voiceState) {
        case .idle, .ready, .error:
            return true
        default:
            return false
        }
    }
    
    private var canStopListening: Bool {
        switch onEnum(of: voiceState) {
        case .listening, .recording:
            return true
        default:
            return false
        }
    }
}

// MARK: - Playback Controls Card

struct PlaybackControlsCard: View {
    let isPlaying: Bool
    let onPlay: () -> Void
    let onPause: () -> Void
    let onStop: () -> Void
    let onDelete: () -> Void
    
    var body: some View {
        VStack(spacing: 16) {
            Text("Reproducción")
                .font(.headline)
            
            HStack(spacing: 12) {
                Button(action: onPlay) {
                    Image(systemName: "play.fill")
                        .frame(width: 44, height: 44)
                        .background(Color.green)
                        .foregroundColor(.white)
                        .cornerRadius(22)
                }
                .disabled(isPlaying)
                
                Button(action: onPause) {
                    Image(systemName: "pause.fill")
                        .frame(width: 44, height: 44)
                        .background(Color.orange)
                        .foregroundColor(.white)
                        .cornerRadius(22)
                }
                .disabled(!isPlaying)
                
                Button(action: onStop) {
                    Image(systemName: "stop.fill")
                        .frame(width: 44, height: 44)
                        .background(Color.red)
                        .foregroundColor(.white)
                        .cornerRadius(22)
                }
                .disabled(!isPlaying)
                
                Button(action: onDelete) {
                    Image(systemName: "trash.fill")
                        .frame(width: 44, height: 44)
                        .background(Color.red)
                        .foregroundColor(.white)
                        .cornerRadius(22)
                }
            }
        }
        .padding()
        .background(Color(.systemGray6))
        .cornerRadius(12)
    }
}

// MARK: - Recordings List

struct RecordingsList: View {
    let recordings: [AudioFile]
    let currentlyPlayingId: String?
    let onPlayRecording: (String) -> Void
    let onStopRecording: (String) -> Void
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Grabaciones")
                .font(.headline)
            
            ForEach(recordings) { recording in
                RecordingItem(
                    recording: recording,
                    isPlaying: currentlyPlayingId == recording.id,
                    onPlayRecording: onPlayRecording,
                    onStopRecording: onStopRecording
                )
            }
        }
        .padding()
        .background(Color(.systemGray6))
        .cornerRadius(12)
    }
}

// MARK: - Recording Item

struct RecordingItem: View {
    let recording: AudioFile
    let isPlaying: Bool
    let onPlayRecording: (String) -> Void
    let onStopRecording: (String) -> Void
    
    var body: some View {
        HStack {
            VStack(alignment: .leading, spacing: 4) {
                Text("Grabación: \(recording.id)")
                    .font(.subheadline)
                    .fontWeight(.medium)
                
                Text("Duración: \(recording.duration / 1000)s")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
            
            Spacer()
            
            Button(action: { 
                if isPlaying {
                    onStopRecording(recording.id)
                } else {
                    onPlayRecording(recording.id)
                }
            }) {
                Image(systemName: isPlaying ? "stop.fill" : "play.fill")
                    .frame(width: 32, height: 32)
                    .background(isPlaying ? Color.red : Color.blue)
                    .foregroundColor(.white)
                    .cornerRadius(16)
            }
        }
        .padding(.vertical, 8)
    }
}

// MARK: - VoiceViewModel SwiftUI Integration

// Using the shared VoiceViewModel directly from Kotlin

// MARK: - Using shared types from Kotlin

// VoiceState, MicrophonePermission, and AudioFile are now used directly from the shared module

// Make AudioFile conform to Identifiable for SwiftUI
extension AudioFile: @retroactive Identifiable {
    // AudioFile already has an `id` property from the common module
}

#Preview {
    VoiceScreen()
}