package edu.cit.becera.lrbms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LrbmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(LrbmsApplication.class, args);
    }

}
