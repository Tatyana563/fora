package com.example.fora;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ForaApplication {

    public static void main(String[] args) {
        SpringApplication.run(ForaApplication.class, args);
    }

}
