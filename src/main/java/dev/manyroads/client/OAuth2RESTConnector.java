package dev.manyroads.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * Dedicated class for clients who require OAuth2 authorization
 */
//@AllArgsConstructor
@RequiredArgsConstructor
@Slf4j
public abstract class OAuth2RESTConnector {

    protected final RestTemplate restTemplate;
    @Value("${OAuth2rest.user}")
    private String oAuth2RestUser;
    @Value("${OAuth2rest.password}")
    private String oAuth2RestPassword;
    private final String AUTHORIZATION_CODE = "Authorization Code";

    public ResponseEntity<?> sendMessage(Object messageBody, String userRole, String url, HttpMethod method) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Grant Type", AUTHORIZATION_CODE);
        headers.add("x-user-role", userRole);
        headers.setBasicAuth(oAuth2RestUser, oAuth2RestPassword);
        HttpEntity<?> requestEntity = new HttpEntity<>(messageBody, headers);
        log.info("OAuth2RESTConnector: sendMessage url: {}", url);
        return restTemplate.exchange(url, method, requestEntity, Void.class);
    }
}
