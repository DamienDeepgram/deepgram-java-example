package ai.deepgram.sdk.websocket;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * A mock WebSocket server for testing the Deepgram WebSocket client.
 */
public class MockWebSocketServer extends WebSocketServer {
    private static final Logger logger = LoggerFactory.getLogger(MockWebSocketServer.class);
    private final CountDownLatch connectionLatch;
    private final CountDownLatch startLatch;
    private Consumer<WebSocket> onConnectHandler;
    private Consumer<WebSocket> onCloseHandler;
    private Consumer<String> onMessageHandler;
    private volatile boolean isShuttingDown;

    public MockWebSocketServer(int port) {
        super(new InetSocketAddress(port));
        this.connectionLatch = new CountDownLatch(1);
        this.startLatch = new CountDownLatch(1);
        this.isShuttingDown = false;
        setReuseAddr(true);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        if (!isShuttingDown) {
            logger.debug("Client connected");
            connectionLatch.countDown();
            if (onConnectHandler != null) {
                onConnectHandler.accept(conn);
            }
        }
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        if (onCloseHandler != null && !isShuttingDown) {
            logger.debug("Client disconnected: {} - {}", code, reason);
            onCloseHandler.accept(conn);
        }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        if (onMessageHandler != null && !isShuttingDown) {
            logger.debug("Received message: {}", message);
            onMessageHandler.accept(message);
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        if (!isShuttingDown) {
            logger.error("WebSocket server error", ex);
        }
    }

    @Override
    public void onStart() {
        logger.info("Mock WebSocket server started on port {}", getPort());
        startLatch.countDown();
    }

    public void setOnConnect(Consumer<WebSocket> handler) {
        this.onConnectHandler = handler;
    }

    public void setOnClose(Consumer<WebSocket> handler) {
        this.onCloseHandler = handler;
    }

    public void setOnMessage(Consumer<String> handler) {
        this.onMessageHandler = handler;
    }

    public boolean awaitConnection(long timeout, TimeUnit unit) throws InterruptedException {
        return connectionLatch.await(timeout, unit);
    }

    public boolean awaitStart(long timeout, TimeUnit unit) throws InterruptedException {
        return startLatch.await(timeout, unit);
    }

    public void closeAllConnections() {
        closeAllConnectionsWithCode(1000, "Normal closure");
    }

    public void closeAllConnectionsWithCode(int code, String reason) {
        for (WebSocket conn : getConnections()) {
            conn.close(code, reason);
        }
    }

    @Override
    public void stop() throws InterruptedException {
        stop(1000);
    }

    @Override
    public void stop(int timeout) throws InterruptedException {
        isShuttingDown = true;
        try {
            closeAllConnections();
            super.stop(timeout);
        } finally {
            isShuttingDown = false;
        }
    }

    public void startAndWait() throws InterruptedException {
        start();
        if (!awaitStart(5, TimeUnit.SECONDS)) {
            throw new IllegalStateException("Server failed to start within timeout");
        }
    }
} 