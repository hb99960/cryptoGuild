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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

@Component
public class MyWebSocketClient extends TextWebSocketHandler {

    private final WebSocketClient webSocketClient;

    public MyWebSocketClient(WebSocketClient webSocketClient) {
        this.webSocketClient = webSocketClient;
    }



    public void connect() {
        try {
            // Replace with your WebSocket API URL and API key
            String apiUrl = "wss://trade.cex.io/api/spot/ws-public";

            //private API
//          String apiUrl = "wss://trade.cex.io/api/spot/ws";
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

        long fromISO = getTime();

        String initialMessage = String.format(
                "{\"e\":\"get_candles\",\"oid\":\"initial_request\",\"ok\":\"ok\",\"data\":{\"pair\":\"BTC-USD\",\"fromISO\":%d,\"limit\":10,\"dataType\":\"bestAsk\",\"resolution\":\"1h\"}}",
                fromISO
        );

//        String initialMessage = "{\"e\":\"get_candles\",\"oid\":\"initial_request\",\"ok\":\"ok\",\"data\":{\"pair\":\"BTC-USD\",\"fromISO\":1789615367123,\"limit\":10,\"dataType\":\"bestAsk\",\"resolution\":\"1h\"}}";
        session.sendMessage(new TextMessage(initialMessage));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        System.out.println("Received message: " + message.getPayload());

//        if (message.getPayload().contains("ping")) {
//
//            String initialMessage = "{\"e\":\"get_candles\",\"oid\":\"initial_request\",\"ok\":\"ok\",\"data\":{\"pair\":\"BTC-USD\",\"fromISO\":1689615367123,\"limit\":10,\"dataType\":\"bestAsk\",\"resolution\":\"1h\"}}";
//            session.sendMessage(new TextMessage(initialMessage));
////            session.sendMessage(new TextMessage("{\n" +
////                    "  \"e\": \"pong\"\n" +
////                    "}"));
//        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.err.println("Transport error: " + exception.getMessage());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println("Connection closed: " + status);
    }

    public long getTime(){

//      String dateString = "2024-07-19T12:00:00Z";

        String dateString = getCurrentDateInISO8601();

        // Create a SimpleDateFormat instance for parsing
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC")); // Set timezone to UTC
        long timestampInMilliseconds = 0;
        try {
            // Parse the date string into a Date object
            Date date = sdf.parse(dateString);

            // Get the time in milliseconds
            timestampInMilliseconds = date.getTime();
            System.out.println("Timestamp in Milliseconds: " + timestampInMilliseconds);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return timestampInMilliseconds;
    }

    public static String getCurrentDateInISO8601() {
        // Create a SimpleDateFormat instance for ISO 8601 format
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC")); // Set timezone to UTC

        // Get the current date
        Date now = new Date();

        // Format the current date and time
        return sdf.format(now);
    }


}