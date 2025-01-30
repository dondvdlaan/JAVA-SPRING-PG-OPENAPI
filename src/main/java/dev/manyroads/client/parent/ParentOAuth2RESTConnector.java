package dev.manyroads.client.parent;

import dev.manyroads.model.OAuth2ResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@Slf4j
public abstract class ParentOAuth2RESTConnector {

    protected final RestTemplate restTemplate;

    public ParentOAuth2RESTConnector(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<OAuth2ResponseDTO> sendMessage(Object messageBody, HttpHeaders headers, String url, HttpMethod method) {

        HttpEntity<?> requestEntity = new HttpEntity<>(messageBody, headers);
        log.info("OAuth2RESTConnector: sendMessage url: {}", url);
        var res = restTemplate.exchange(url, method, requestEntity, OAuth2ResponseDTO.class);
        log.info("OAuth2RESTConnector: res-> " + res);
        return res;
    }


}
