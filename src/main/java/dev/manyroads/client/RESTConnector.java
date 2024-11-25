package dev.manyroads.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
@Slf4j
public abstract class RESTConnector {

    RestTemplate restTemplate;

    public ResponseEntity<?> sendMessage(Object messageBody, String userRole, String url, HttpMethod method) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("x-user-role", userRole);
        HttpEntity<?> requestEntity = new HttpEntity<>(messageBody, headers);
        return restTemplate.exchange(url, method, requestEntity, Void.class);
    }


}
