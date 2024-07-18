package com.example.websocket;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class WebsocketApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(WebsocketApplication.class, args);
	}

	@Override
	public void run(String... args) {
		try {
			// Replace with your WebSocket API URL and API key
			String apiUrl = "wss://trade.cex.io/api/spot/ws-public";
			String apiKey = "your_api_key_here";

			// Set headers
			Map<String, String> headers = new HashMap<>();
			headers.put("Authorization", "Bearer " + apiKey);

			// Create and connect the WebSocket client
			MyWebSocketClient client = new MyWebSocketClient(new URI(apiUrl), headers);
			client.connectBlocking();

			// Keep the connection alive
			while (client.isOpen()) {
				// Send ping message to keep the connection alive
				client.send("{\"e\":\"ping\"}");
				Thread.sleep(9000);  // Send a ping message every 9 seconds
			}

		} catch (URISyntaxException | InterruptedException e) {
			e.printStackTrace();
		}
	}
}
