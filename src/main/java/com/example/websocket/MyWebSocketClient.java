package com.example.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Component
public class MyWebSocketClient extends TextWebSocketHandler {

    private final WebSocketClient webSocketClient;

    public MyWebSocketClient(WebSocketClient webSocketClient) {
        this.webSocketClient = webSocketClient;
    }

    @PostConstruct
    public void connect() {
        try {
            // Replace with your WebSocket API URL and API key
            String apiUrl = "wss://trade.cex.io/api/spot/ws-public";
            String apiKey = "Cb01cd51784b2b423684a982284b9372e63c8375f6982861bad84c522360c230";

            // Set headers
            WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
            headers.add("Authorization", "Bearer " + apiKey);
            webSocketClient.doHandshake(this, headers, URI.create(apiUrl)).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("Connected to WebSocket server");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        System.out.println("Received message: " + message.getPayload());

        if (message.getPayload().contains("ping")) {
            session.sendMessage(new TextMessage("{\n" +
                    "  \"e\": \"pong\"\n" +
                    "}"));
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.err.println("Transport error: " + exception.getMessage());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println("Connection closed: " + status);
    }
}
