package com.saju.interpret;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class SajuInterpretApplication {

    public static void main(String[] args) {
        SpringApplication.run(SajuInterpretApplication.class, args);
    }
}
