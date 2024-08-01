package com.example.websocket;

import com.example.websocket.Repository.InfluxDbService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


@Component
public class BitmexWebSocketClient extends TextWebSocketHandler {

    StandardWebSocketClient standardWebSocketClient;
    private WebSocketSession session;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final InfluxDbService influxDbService;

    // API key and secret
    private static final String API_KEY = "D7dIIstv7H-PFJ9l8kl6K-U8";
    private static final String API_SECRET = "0ogPFtdJd1VGlfvO0zSXEFVZ9ZEomAZJ65anVCgghuAW4BGE";
    private long startTime;


    public BitmexWebSocketClient(StandardWebSocketClient webSocketClient, InfluxDbService influxDbService) {
        this.standardWebSocketClient = webSocketClient;
        this.influxDbService = influxDbService;
    }

    @PostConstruct
    public void connect() {
        try {

            String apiUrl = "wss://ws.bitmex.com/realtime";
            String expires = String.valueOf(Instant.now().getEpochSecond() + 60); // 60 seconds expiration
            String signature = generateSignature("GET", "/realtime", expires, "");

            URI uri = new URI(apiUrl + "?api-key=" + API_KEY + "&api-signature=" + signature + "&api-expires=" + expires);

            standardWebSocketClient.doHandshake(this, new WebSocketHttpHeaders(), uri).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String generateSignature(String verb, String path, String expires, String data) throws Exception {
        String message = verb + path + expires + data;
        Mac sha256Hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(API_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256Hmac.init(secretKey);

        byte[] hash = sha256Hmac.doFinal(message.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hash);

    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
//        System.out.println("Connected to WebSocket server");
        this.session = session;
        // Subscribe to a topic
        sendSubscriptionMessage("subscribe", "trade:XBTUSD");
        scheduler.scheduleAtFixedRate(this::sendPingMessage, 0, 5, TimeUnit.MINUTES);
    }

    private void sendSubscriptionMessage(String action, String topic) {
        if (session != null && session.isOpen()) {
            String message = String.format("{\"op\": \"%s\", \"args\": [\"%s\"]}", action, topic);
            startTime = System.currentTimeMillis();
            try {
                session.sendMessage(new TextMessage(message));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void sendPingMessage() {
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage("ping"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws JsonProcessingException {
        System.out.println("Received message Bitmex api: " + message.getPayload());
        long receiveTime = System.currentTimeMillis();
        long latency = receiveTime - startTime;
        //System.out.println("Latency = " + latency);
        // Handle incoming messages

//        System.out.println("Received message Bitmex api: " + message.getPayload());
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(message.getPayload());
            JsonNode tableName = rootNode.path("table");
            JsonNode action = rootNode.path("action");
            int dataSize = rootNode.path("data").size();
            System.out.println("Size = " + dataSize);
            for(int i=0; i<dataSize; i++){
                JsonNode dataNode = rootNode.path("data").get(i);

                String timestamp = dataNode.path("timestamp").asText();
                String symbol = dataNode.path("symbol").asText();
                String side = dataNode.path("side").asText();
                int size = dataNode.path("size").asInt();
                double price = dataNode.path("price").asDouble();
                String tickDirection = dataNode.path("tickDirection").asText();
                double grossValue = dataNode.path("grossValue").asDouble();
                double homeNotional = dataNode.path("homeNotional").asDouble();
                double foreignNotional = dataNode.path("foreignNotional").asDouble();
                String trdType = dataNode.path("trdType").asText();
                System.out.println("Price: " + price + " ");
                System.out.println("Timestamp: " + timestamp);
                influxDbService.writeTradeData(timestamp, symbol, side, size, price, tickDirection, grossValue, homeNotional, foreignNotional, trdType);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        System.err.println("Transport error: " + exception.getMessage());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        System.out.println("Connection closed: " + status);
        // Optionally, attempt to reconnect here
    }
}


//        XBTUSD: Bitcoin to US Dollar
//        ETHUSD: Ethereum to US Dollar
//        XRPUSD: Ripple to US Dollar
//        LTCUSD: Litecoin to US Dollar
//        BCHUSD: Bitcoin Cash to US Dollar

// Schedule periodic heartbeat to keep connection alive
//        {
//  "op": "subscribe",
//  "args": [
//    "funding:XBTUSD",
//    "instrument:XBTUSD",
//    "insurance:XBTUSD",
//    "liquidation:XBTUSD",
//    "orderBookL2_25:XBTUSD",
//    "quote:XBTUSD",
//    "quoteBin1m:XBTUSD",
//    "quoteBin5m:XBTUSD",
//    "quoteBin1h:XBTUSD",
//    "quoteBin1d:XBTUSD",
//    "settlement:XBTUSD",
//    "trade:XBTUSD",
//    "tradeBin1m:XBTUSD",
//    "tradeBin5m:XBTUSD",
//    "tradeBin1h:XBTUSD",
//    "tradeBin1d:XBTUSD",
//    "funding:ETHUSD",
//    "instrument:ETHUSD",
//    "insurance:ETHUSD",
//    "liquidation:ETHUSD",
//    "orderBookL2_25:ETHUSD",
//    "quote:ETHUSD",
//    "quoteBin1m:ETHUSD",
//    "quoteBin5m:ETHUSD",
//    "quoteBin1h:ETHUSD",
//    "quoteBin1d:ETHUSD",
//    "settlement:ETHUSD",
//    "trade:ETHUSD",
//    "tradeBin1m:ETHUSD",
//    "tradeBin5m:ETHUSD",
//    "tradeBin1h:ETHUSD",
//    "tradeBin1d:ETHUSD"
//  ]
//}