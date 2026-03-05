package com.saju.interpret.common.config;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.saju.interpret.client.PublicDataClientProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(PublicDataClientProperties.class)
public class PublicDataClientConfig {

    @Bean
    public XmlMapper xmlMapper() {
        return new XmlMapper();
    }

    @Bean
    public RestClient publicDataRestClient(PublicDataClientProperties properties, XmlMapper xmlMapper) {
        MappingJackson2XmlHttpMessageConverter xmlConverter = new MappingJackson2XmlHttpMessageConverter(xmlMapper);
        
        return RestClient.builder()
            .baseUrl(properties.baseUrl())
            .messageConverters(converters -> converters.add(xmlConverter))
            .build();
    }
}
