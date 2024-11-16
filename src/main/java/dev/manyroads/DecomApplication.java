package dev.manyroads;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class DecomApplication {

    public static void main(String[] args) {
        SpringApplication.run(DecomApplication.class, args);
    }

}
