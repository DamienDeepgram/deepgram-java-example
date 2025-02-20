Based on the nodejs sdk

Write a java real time audio streaming with websockets and type the websocket messages in a simple standalone java class and library file.

Connection Pooling Example:
- Open N connections to Deepgram but do not send any audio and only send Keep Alive messages to keep the websocket open.
- When a new connection is needed get it from the connection pool.
- When a connection is no longer needed close it and remove it from the connection pool.
- If the connection is closed it should be removed from the connection pool.
- If the connection is not used for a while it should be removed from the connection pool after 60mins.

The ConnectionPoolStreamingTedst.java should process the bueller.wav file multiple times and log the time to first transcript latency measurement.

Also add the time to first transcript latency measurement for the AudioStreamingTest.java file.
