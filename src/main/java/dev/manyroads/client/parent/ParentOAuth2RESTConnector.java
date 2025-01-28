package dev.manyroads.client.parent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;


//@AllArgsConstructor
//@RequiredArgsConstructor
@Slf4j
public abstract class ParentOAuth2RESTConnector {

    protected final RestTemplate restTemplate;
    @Value("${rest.user}")
    private String restUser;
    @Value("${rest.password}")
    private String restPassword;

    public ParentOAuth2RESTConnector(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<?> sendMessage(Object messageBody, String userRole, String url, HttpMethod method) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("x-user-role", userRole);
        headers.setBasicAuth(restUser, restPassword);
        HttpEntity<?> requestEntity = new HttpEntity<>(messageBody, headers);
        log.info("RESTConnector: sendMessage url: {}", url);
        return restTemplate.exchange(url, method, requestEntity, Void.class);
    }


}
