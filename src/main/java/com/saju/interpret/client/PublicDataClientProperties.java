package com.saju.interpret.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "client.public-data")
public record PublicDataClientProperties(
    String baseUrl,
    String serviceKey,
    Endpoints endpoints
) {

    public record Endpoints(
        String lunarCalendar
    ) {

    }
}