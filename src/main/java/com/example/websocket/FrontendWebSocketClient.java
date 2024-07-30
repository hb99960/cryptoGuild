package com.example.websocket;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@EnableWebSocket
public class FrontendWebSocketClient extends TextWebSocketHandler {

    private InfluxDBClient influxDBClient;
    private InfluxDbConfig influxDbConfig;
    private WriteApiBlocking writeApi;
    private WebSocketSession webSocketSession;
    private final ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();

    @Autowired
    public FrontendWebSocketClient(InfluxDbConfig influxDbConfig){
        System.out.println("Front-end initialization");
        this.influxDbConfig = influxDbConfig;
        influxDBClient = InfluxDBClientFactory.create(influxDbConfig.getUrl(), influxDbConfig.getToken().toCharArray(), influxDbConfig.getOrg(), influxDbConfig.getBucket());
        this.writeApi = influxDBClient.getWriteApiBlocking();
        taskScheduler.initialize();
    }


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("Connected to Front end WebSocket server");
        super.afterConnectionEstablished(session);

        //send Data stream
        startDataStream(session);
    }

    private void startDataStream(WebSocketSession session) {
        taskScheduler.schedule(() -> {
            if (session.isOpen()) {
                sendData(session);
            }
        }, new PeriodicTrigger(1, TimeUnit.SECONDS));  // Sends data every second
    }

    private void sendData(WebSocketSession session) {
//        Your existing sendData logic to query and send data
//        Define Flux query
        String fluxQuery = "from(bucket: \"my-bucket\")"
                + " |> range(start: -1h)"  // You can adjust the time range
                + " |> filter(fn: (r) => r[\"_measurement\"] == \"price\")"
                + " |> filter(fn: (r) => r[\"_field\"] == \"price\")"
                + " |> filter(fn: (r) => r[\"tag\"] == \"BTC\")"
                + " |> aggregateWindow(every: 1m, fn: mean, createEmpty: false)"
                + " |> yield(name: \"mean\")";

        // Execute Flux query
        QueryApi queryApi = influxDBClient.getQueryApi();
        List<FluxTable> tables = queryApi.query(fluxQuery);


        // Extract and format data
        if (tables == null || tables.isEmpty()) {
            // Handle no data case
            return;
        }
        StringBuilder dataBuilder = new StringBuilder();
        for (FluxTable table : tables) {
            System.out.println("in StringBuilder");
            for (FluxRecord record : table.getRecords()) {
                dataBuilder.append(record.getValueByKey("_time")).append(": ");
                dataBuilder.append(record.getValueByKey("_value")).append("\n");
            }
        }

        // Send data to the WebSocket client
        try {
            System.out.println("Sending Data : " + dataBuilder.toString());
            session.sendMessage(new TextMessage(dataBuilder.toString()));
        } catch (Exception e) {
            // Handle exception
            e.printStackTrace();
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        super.handleTransportError(session, exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
    }
}
