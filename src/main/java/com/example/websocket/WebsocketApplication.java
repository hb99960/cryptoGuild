package com.example.websocket;

import com.example.websocket.Repository.InfluxDbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
