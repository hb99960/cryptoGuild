package com.example.websocket;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix="influxdb")
public class InfluxDbConfig {

    private String url;


    private String token;


    private String org;


    private String bucket;

    // Getters for the fields
    public String getUrl() {
        return url;
    }

    public String getToken() {
        return token;
    }

    public String getOrg() {
        return org;
    }

    public String getBucket() {
        return bucket;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setOrg(String org) {
        this.org = org;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }
}
