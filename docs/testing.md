# Testing Guide

## Overview
This document describes the testing approach for the Deepgram Java SDK, focusing on integration tests with the real Deepgram API.

## Test Categories

### Integration Tests
Our test suite focuses on real-world integration testing with the Deepgram API. This approach ensures that our SDK works correctly in production scenarios.

#### Connection Pool Streaming Test
The `ConnectionPoolStreamingTest` is our primary integration test that verifies:
- Multiple concurrent connections (default: 3 connections)
- Real-time audio streaming
- Connection pool management
- Performance metrics collection

##### Running the Test
```bash
# Using Maven profile
mvn compile exec:java -P pool

# Or directly
mvn test -Dtest=ConnectionPoolStreamingTest
```

##### Test Configuration
- Initial pool size: 3 connections
- Audio chunk size: 8820 bytes (~50ms of audio at 44.1kHz stereo)
- Start delay between streams: 1 second
- Total streams to process: 6
- Close wait time: 500ms
- Test timeout: 120 seconds

##### Performance Metrics
The test measures and reports:
- First message latency (typically 1.1-1.3 seconds)
- First transcript latency (typically 6.0-6.1 seconds)
- Transcription accuracy (>98% confidence)
- Connection pool utilization
- Connection lifecycle events

##### Example Output
```
[Conn 1] First message received after 1353 ms
[Conn 2] First message received after 1262 ms
[Conn 3] First message received after 1160 ms
[Conn 1] First transcript with content received after 6097 ms
[Conn 2] First transcript with content received after 6132 ms
[Conn 3] First transcript with content received after 6078 ms
```

##### Test Flow
1. Initializes connection pool with configuration
2. Starts multiple audio streams with staggered timing
3. Processes audio file through each connection
4. Collects and verifies transcription results
5. Monitors connection states and performance
6. Validates clean connection termination

##### Validation Points
- Connection establishment
- Audio streaming functionality
- Transcription accuracy
- Connection pool management
- Resource cleanup
- Error handling
- Performance metrics

### Other Tests
- **Audio Stream Options Test**: Validates configuration options
- **Pool Config Test**: Verifies pool configuration validation
- **Message Handling Tests**: Ensures proper message processing
- **Metrics Collection Test**: Validates metrics gathering

## Test Coverage
Current test suite includes:
- Connection pool functionality
- Pool configuration validation
- Pool metrics collection
- Audio stream options
- Message handling and validation
- Real-time transcription
- Error handling and recovery
- Concurrent streaming

## Running Tests
```bash
# Run all tests
mvn clean test

# Run specific test category
mvn test -Dtest=*StreamingTest

# Run with debug logging
mvn test -Dlogback.configurationFile=logback-debug.xml
```

## Test Results
Latest test metrics:
- 97 tests passing
- First message latency: 1.1-1.3 seconds
- First transcript latency: 6.0-6.1 seconds
- Transcription accuracy: >98% confidence
- Build time: ~38 seconds 