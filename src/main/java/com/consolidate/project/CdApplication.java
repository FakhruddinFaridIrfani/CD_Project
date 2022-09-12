package com.consolidate.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CdApplication {

    public static void main(String[] args) {
        SpringApplication.run(CdApplication.class, args);
    }

}
