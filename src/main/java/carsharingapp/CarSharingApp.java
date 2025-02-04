package carsharingapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class CarSharingApp {

    public static void main(String[] args) {
        SpringApplication.run(CarSharingApp.class, args);
    }
}
