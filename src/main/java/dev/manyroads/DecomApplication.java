package dev.manyroads;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@SpringBootApplication
@EnableFeignClients
@EnableScheduling
@Slf4j
public class DecomApplication {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(DecomApplication.class, args);
        List<String> restTemplateBeans = List.of(context.getBeanNamesForType(RestTemplate.class));
        restTemplateBeans.forEach(System.out::println);
    }


}
