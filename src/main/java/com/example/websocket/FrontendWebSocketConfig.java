package com.example.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class FrontendWebSocketConfig implements WebSocketConfigurer {
    private final FrontendWebSocketClient frontendWebSocketClient;

    @Autowired
    public FrontendWebSocketConfig(FrontendWebSocketClient frontendWebSocketClient) {
        this.frontendWebSocketClient = frontendWebSocketClient;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(frontendWebSocketClient, "/data")
                .setAllowedOrigins("*");  // Adjust as needed for CORS
    }

//        registry.addHandler(new WebSocketHandler() {
//            @Override
//            public void afterConnectionEstablished(WebSocketSession session) throws Exception {
//                System.out.println("Config : AfterConnection Established");
//            }
//
//            @Override
//            public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
//
//            }
//
//            @Override
//            public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
//
//            }
//
//            @Override
//            public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
//                System.out.println("Config : AfterConnection Closed");
//            }
//
//            @Override
//            public boolean supportsPartialMessages() {
//                return false;
//            }
//        }, "/data").setAllowedOrigins("*");

}
