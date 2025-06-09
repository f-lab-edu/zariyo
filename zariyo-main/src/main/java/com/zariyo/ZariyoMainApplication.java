package com.zariyo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ZariyoMainApplication {
    public static void main(String[] args) {
        SpringApplication.run(ZariyoMainApplication.class, args);
    }
}
