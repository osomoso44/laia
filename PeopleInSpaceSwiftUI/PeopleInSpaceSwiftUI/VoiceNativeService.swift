import Foundation
import common

class VoiceNativeServiceIOS: NSObject, VoiceNativeServiceProtocol {
    
    private var eventCallback: ((VoiceEvent) -> Void)?
    private var isListening = false
    private var isPlaying = false
    
    func setEventCallback(_ callback: @escaping (VoiceEvent) -> Void) {
        self.eventCallback = callback
    }
    
    func startListening() async throws {
        print("🎤 Starting voice listening...")
        isListening = true
        
        // Simulate permission granted
        eventCallback?(VoiceEvent.PermissionChanged(granted: true))
        
        // Simulate voice detection after 2 seconds
        DispatchQueue.main.asyncAfter(deadline: .now() + 2.0) {
            if self.isListening {
                print("🎤 Voice detected!")
                self.eventCallback?(VoiceEvent.VoiceDetected(timestamp: Int64(Date().timeIntervalSince1970 * 1000)))
                
                // Simulate recording completion after 3 seconds
                DispatchQueue.main.asyncAfter(deadline: .now() + 3.0) {
                    if self.isListening {
                        print("🎤 Recording completed!")
                        let filePath = "/Documents/VoiceRecordings/recording_\(Int64(Date().timeIntervalSince1970 * 1000)).wav"
                        self.eventCallback?(VoiceEvent.RecordingCompleted(
                            filePath: filePath,
                            duration: 3000
                        ))
                    }
                }
            }
        }
    }
    
    func stopListening() async {
        print("🎤 Stopping voice listening...")
        isListening = false
    }
    
    func playAudio(filePath: String) async throws {
        print("🔊 Playing audio: \(filePath)")
        isPlaying = true
        
        // Simulate playback for 3 seconds
        DispatchQueue.main.asyncAfter(deadline: .now() + 3.0) {
            self.isPlaying = false
        }
    }
    
    func pauseAudio() async throws {
        print("⏸️ Pausing audio")
        isPlaying = false
    }
    
    func stopAudio() async throws {
        print("⏹️ Stopping audio")
        isPlaying = false
    }
    
    func requestMicrophonePermission() async -> Bool {
        print("🔐 Requesting microphone permission...")
        
        // Simulate permission request
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) {
            self.eventCallback?(VoiceEvent.PermissionChanged(granted: true))
        }
        
        return true
    }
}

// MARK: - Protocol for communication with KMM

protocol VoiceNativeServiceProtocol {
    func setEventCallback(_ callback: @escaping (VoiceEvent) -> Void)
    func startListening() async throws
    func stopListening() async
    func playAudio(filePath: String) async throws
    func pauseAudio() async throws
    func stopAudio() async throws
    func requestMicrophonePermission() async -> Bool
}
