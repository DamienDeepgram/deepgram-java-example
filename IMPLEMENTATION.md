Example output:
```
Found input device: default [default]
Found input device: D6000 [plughw:0,0]
Found input device: Headset [plughw:1,0]
Found input device: Camera [plughw:2,0]
Found input device: sofhdadsp [plughw:3,0]
Found input device: sofhdadsp [plughw:3,7]

Available audio input devices:
  1. default [default]
  2. D6000 [plughw:0,0]
  3. Headset [plughw:1,0]
  4. Camera [plughw:2,0]
  5. sofhdadsp [plughw:3,0]
  6. sofhdadsp [plughw:3,7]

Select device number (1-6): 5

Audio device configuration:
  Device: sofhdadsp [plughw:3,0]
  Format: 16000.0 Hz, 16-bit PCM_SIGNED, 1 channel(s)
  Frame size: 2 bytes
  Frame rate: 16000.0 fps

Deepgram configuration:
  Model: nova-2
  Language: en-US
  Interim results: enabled

Connecting to Deepgram...
Connected to Deepgram
Streaming from microphone. Press Ctrl+C to stop.
    [Interim] hello
  [Speech Final] hello
  [Speech Final] this is
    [Interim] hello
  [Speech Final] hello
    [Interim] all these be for

Shutting down...
Shutdown complete

### Latest Updates
- Fixed audio file path in AudioStreamingTest to use correct test resources location
- Added AudioStreamOptions to all example classes for consistency
- Improved error handling and logging across all examples
- Standardized connection parameters and timeouts
- Added performance metrics tracking to streaming examples
- Added graceful shutdown handling in MicrophoneStreamingTest
  - Sends CloseStream control message
  - Waits for pending messages
  - Properly closes WebSocket connection
  - Gracefully shuts down scheduler
- Updated ControlMessage to use enum types with string values
  - Added ControlType enum with string mappings
  - Updated control message serialization
  - Improved type safety and readability

### Next Steps 