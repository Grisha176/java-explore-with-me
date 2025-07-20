package ru.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"ru.practicum", "ru.practicum.client","ru.practicum.dto"})
public class EwmService {
    public static void main(String[] args) {
        SpringApplication.run(EwmService.class, args);
    }
}