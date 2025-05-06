package com.zariyo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class ZariyoMainApplication {
    public static void main(String[] args) {
        SpringApplication.run(ZariyoMainApplication.class, args);
    }
}