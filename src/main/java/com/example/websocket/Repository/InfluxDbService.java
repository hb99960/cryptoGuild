package com.example.websocket.Repository;

import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.domain.WritePrecision;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.write.Point;


@Service
public class InfluxDbService {
    private final InfluxDbConfig influxDBConfig;
    private InfluxDBClient influxDBClient;
    private final WriteApiBlocking writeApi;

    @Autowired
    public InfluxDbService(InfluxDbConfig influxDBConfig) {
        this.influxDBConfig = influxDBConfig;
        influxDBClient = InfluxDBClientFactory.create(influxDBConfig.getUrl(), influxDBConfig.getToken().toCharArray());
        this.writeApi = influxDBClient.getWriteApiBlocking();

    }

    public void connectToInfluxDB() {
        String url = influxDBConfig.getUrl();
        String token = influxDBConfig.getToken();
        String org = influxDBConfig.getOrg();
        String bucket = influxDBConfig.getBucket();

        // Here you would use the configuration values to connect to InfluxDB
        System.out.println("Connecting to InfluxDB at " + url);
        // Example connection logic might go here
        // e.g., InfluxDBClient client = InfluxDBClientFactory.create(url, token.toCharArray(), org, bucket);
    }

    public void writeTradeData(String timeStamp, String symbol, String side, int size, double price, String tickDirection, double grossValue, double homeNotional, double foreignNotional, String trdType) {

        long timeStampMillis = convertToMillis(timeStamp);
        //System.out.println("Writing to Database. Measurement = price, Symbol = " + symbol + ", Price = " + price + ", Timestamp = " + timeStampMillis);
        Point point = Point.measurement("trade")
                .addTag("tag", symbol)
                .addField("side", side)
                .addField("tickDirection", tickDirection)
                .addField("trdType", trdType)
                .addField("size", size)
                .addField("price", price)
                .addField("grossValue", grossValue)
                .addField("homeNotional", homeNotional)
                .addField("foreignNotional", foreignNotional)
                .time(timeStampMillis, WritePrecision.MS);

        writeApi.writePoint(influxDBConfig.getBucket(), influxDBConfig.getOrg(), point);
    }

    public void close() {
        influxDBClient.close();
    }

    public static long convertToMillis(String timestamp) {
        // Define the format of your timestamp string
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

        // Parse the timestamp string to LocalDateTime
        LocalDateTime localDateTime = LocalDateTime.parse(timestamp, formatter);

        // Convert LocalDateTime to Instant and then to milliseconds
        Instant instant = localDateTime.toInstant(ZoneOffset.UTC);
        return instant.toEpochMilli();
    }

}
