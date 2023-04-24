package com.helenaramyan.cardauth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CardAuthSimulatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(CardAuthSimulatorApplication.class, args);
    }
}
