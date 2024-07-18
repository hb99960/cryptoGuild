package com.example.websocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class MyWebSocketClient implements WebSocketClient {

    public MyWebSocketClient(URI serverUri, Map<String, String> headers) {
        super();
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("Opened connection");
        // Send initial message if needed
        String initialMessage = "{\"e\":\"get_candles\",\"oid\":\"initial_request\",\"ok\":\"ok\",\"data\":{\"pair\":\"BTC-USD\",\"fromISO\":1721211290591,\"limit\":1,\"dataType\":\"bestAsk\",\"resolution\":\"1h\"}}";
        send(initialMessage);
    }

    @Override
    public void onMessage(String message) {
        System.out.println("Received message: " + message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Closed connection with exit code " + code + " additional info: " + reason);
    }

    @Override
    public void onError(Exception ex) {
        System.err.println("An error occurred:" + ex.getMessage());
    }

    @Override
    public CompletableFuture<WebSocketSession> execute(WebSocketHandler webSocketHandler, String uriTemplate, Object... uriVariables) {
        return null;
    }

    @Override
    public CompletableFuture<WebSocketSession> execute(WebSocketHandler webSocketHandler, WebSocketHttpHeaders headers, URI uri) {
        return null;
    }
}
