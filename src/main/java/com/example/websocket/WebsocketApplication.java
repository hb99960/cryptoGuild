package com.example.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class WebsocketApplication implements CommandLineRunner {

	@Autowired
	InfluxDbService influxDbService;
	public static void main(String[] args) {
		SpringApplication.run(WebsocketApplication.class, args);
	}

	@Override
	public void run(String... args) {
		//influxDbService.writeData("temperature", "location", 23.5);
	}



}
