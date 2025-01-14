package dev.manyroads.config;

import dev.manyroads.miscommunication.RESTInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class SpringConfig {

    final private RESTInterceptor restInterceptor;

    public SpringConfig(RESTInterceptor restInterceptor) {
        this.restInterceptor = restInterceptor;
    }

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        interceptors.add(restInterceptor);
        restTemplate.setInterceptors(interceptors);
        return restTemplate;
    }
}
