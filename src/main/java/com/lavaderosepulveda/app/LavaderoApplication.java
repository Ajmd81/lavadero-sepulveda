package com.lavaderosepulveda.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LavaderoApplication {

    public static void main(String[] args) {
        SpringApplication.run(LavaderoApplication.class, args);
    }
}