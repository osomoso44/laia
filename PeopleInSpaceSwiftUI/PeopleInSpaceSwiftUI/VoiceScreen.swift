import SwiftUI
import common

struct VoiceScreen: View {
    @StateObject private var viewModel = VoiceViewModel()
    
    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 20) {
                    Text("Voice Recorder")
                        .font(.largeTitle)
                        .fontWeight(.bold)
                        .padding(.bottom, 10)
                    
                    StatusCard(
                        voiceState: viewModel.voiceState,
                        microphonePermission: viewModel.microphonePermission
                    )
                    
                    VoiceControlsCard(
                        voiceState: viewModel.voiceState,
                        microphonePermission: viewModel.microphonePermission,
                        onStartListening: { viewModel.startListening() },
                        onStopListening: { viewModel.stopListening() },
                        onRequestPermission: { viewModel.requestMicrophonePermission() }
                    )
                    
                    if viewModel.lastRecording != nil {
                        PlaybackControlsCard(
                            isPlaying: viewModel.isPlaying,
                            onPlay: { viewModel.playLastRecording() },
                            onPause: { viewModel.pausePlayback() },
                            onStop: { viewModel.stopPlayback() },
                            onDelete: { viewModel.deleteLastRecording() }
                        )
                    }
                    
                    if !viewModel.recordingList.isEmpty {
                        RecordingsList(
                            recordings: viewModel.recordingList,
                            onPlayRecording: { filePath in
                                viewModel.playLastRecording()
                            }
                        )
                    }
                }
                .padding()
            }
            .navigationTitle("Voice")
            .navigationBarTitleDisplayMode(.inline)
        }
        .onAppear {
            viewModel.requestMicrophonePermission()
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
        switch voiceState {
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
        switch voiceState {
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
            return voiceState.rawValue
        }
    }
    
    private var microphonePermissionText: String {
        switch microphonePermission {
        case .notRequested:
            return "No solicitado"
        case .granted:
            return "Permitido"
        case .denied:
            return "Denegado"
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
            
            if microphonePermission == .notRequested || microphonePermission == .denied {
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
            } else {
                HStack(spacing: 16) {
                    Button(action: onStartListening) {
                        HStack {
                            Image(systemName: "mic.fill")
                            Text("Iniciar Escucha")
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
                            Text("Detener")
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
        voiceState == .idle || voiceState == .ready || voiceState == .error
    }
    
    private var canStopListening: Bool {
        voiceState == .listening || voiceState == .recording
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
    let onPlayRecording: (String) -> Void
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Grabaciones")
                .font(.headline)
            
            ForEach(recordings) { recording in
                RecordingItem(
                    recording: recording,
                    onPlayRecording: onPlayRecording
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
    let onPlayRecording: (String) -> Void
    
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
            
            Button(action: { onPlayRecording(recording.filePath) }) {
                Image(systemName: "play.fill")
                    .frame(width: 32, height: 32)
                    .background(Color.blue)
                    .foregroundColor(.white)
                    .cornerRadius(16)
            }
        }
        .padding(.vertical, 8)
    }
}

// MARK: - VoiceViewModel SwiftUI Integration

class VoiceViewModel: ObservableObject {
    @Published var voiceState: VoiceState = .idle
    @Published var microphonePermission: MicrophonePermission = .notRequested
    @Published var lastRecording: String? = nil
    @Published var isPlaying: Bool = false
    @Published var recordingList: [AudioFile] = []
    
    private var isListening = false
    
    init() {
        // Initialize with granted permission for demo
        microphonePermission = .granted
    }
    
    func startListening() {
        print("🎤 Starting continuous voice listening...")
        isListening = true
        voiceState = .listening
        
        // Simulate continuous voice detection and recording
        simulateContinuousRecording()
    }
    
    private func simulateContinuousRecording() {
        guard isListening else { return }
        
        // Simulate voice detection every 3-5 seconds
        let randomDelay = Double.random(in: 3.0...5.0)
        DispatchQueue.main.asyncAfter(deadline: .now() + randomDelay) {
            if self.isListening {
                print("🎤 Voice detected! Starting recording...")
                self.voiceState = .recording
                
                // Simulate recording duration (2-4 seconds)
                let recordingDuration = Double.random(in: 2.0...4.0)
                DispatchQueue.main.asyncAfter(deadline: .now() + recordingDuration) {
                    if self.isListening {
                        print("🎤 Recording completed! Adding to list...")
                        
                        // Create new recording
                        let timestamp = Int64(Date().timeIntervalSince1970 * 1000)
                        let newRecording = AudioFile(
                            id: "recording_\(timestamp)",
                            filePath: "recording_\(timestamp).wav",
                            timestamp: timestamp,
                            duration: Int64(recordingDuration * 1000),
                            fileSize: Int64.random(in: 100000...500000), // Random file size
                            sampleRate: 44100,
                            channels: 1
                        )
                        
                        // Add to recording list (most recent first)
                        self.recordingList.insert(newRecording, at: 0)
                        self.lastRecording = newRecording.filePath
                        self.voiceState = .ready
                        
                        print("🎤 Added recording: \(newRecording.id) (Duration: \(Int(recordingDuration))s)")
                        
                        // Continue listening for more voice
                        self.simulateContinuousRecording()
                    }
                }
            }
        }
    }
    
    func stopListening() {
        print("🎤 Stopping voice listening...")
        isListening = false
        voiceState = .idle
    }
    
    func requestMicrophonePermission() {
        print("🎤 Requesting microphone permission...")
        microphonePermission = .granted
    }
    
    func playLastRecording() {
        guard let filePath = lastRecording else { return }
        print("🔊 Playing audio from \(filePath)")
        isPlaying = true
        voiceState = .playing
        
        // Simulate playback completion after 3 seconds
        DispatchQueue.main.asyncAfter(deadline: .now() + 3.0) {
            self.isPlaying = false
            self.voiceState = .ready
        }
    }
    
    func pausePlayback() {
        print("🔊 Pausing audio")
        isPlaying = false
        voiceState = .paused
    }
    
    func stopPlayback() {
        print("🔊 Stopping audio")
        isPlaying = false
        voiceState = .ready
    }
    
    func deleteLastRecording() {
        print("🗑️ Deleting last recording")
        lastRecording = nil
        if !recordingList.isEmpty {
            recordingList.removeFirst()
        }
        voiceState = .idle
    }
}

// MARK: - Enums for SwiftUI

enum VoiceState: String, CaseIterable {
    case idle = "idle"
    case listening = "listening"
    case recording = "recording"
    case ready = "ready"
    case playing = "playing"
    case paused = "paused"
    case error = "error"
}

enum MicrophonePermission: String, CaseIterable {
    case notRequested = "notRequested"
    case granted = "granted"
    case denied = "denied"
}

struct AudioFile: Identifiable {
    let id: String
    let filePath: String
    let timestamp: Int64
    let duration: Int64
    let fileSize: Int64
    let sampleRate: Int32
    let channels: Int32
}

#Preview {
    VoiceScreen()
}