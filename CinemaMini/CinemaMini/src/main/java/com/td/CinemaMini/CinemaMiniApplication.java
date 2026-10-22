package com.td.CinemaMini;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CinemaMiniApplication {

    public static void main(String[] args) {
        SpringApplication.run(CinemaMiniApplication.class, args);
    }
}
