package dev.manyroads.miscommunication;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.manyroads.decomreception.exception.InternalException;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class RESTInterceptor implements ClientHttpRequestInterceptor {

    private final ApplicationEventPublisher applicationEventPublisher;

    public RESTInterceptor(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public ClientHttpResponse intercept(@NotNull HttpRequest request, byte[] body, @NotNull ClientHttpRequestExecution execution) throws IOException {
        log.info("******************* intercept ************************");
        log.info("intercept request.getHeaders(): " + request.getHeaders());
        log.info("intercept request.getMethod(): " + request.getMethod());
        log.info("intercept request.getURI(): " + request.getURI());
        ClientHttpResponse response = null;
        try {
            response = execution.execute(request, body);
            log.info("intercept response.getStatusCode(): " + response.getStatusCode());
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.info("Preparing MisCommunicationEvent ");
                MisCommunicationEvent misCommunicationEvent = new MisCommunicationEvent
                        (this, request.getURI().toString(), request.getMethod().toString(), body, getHeadersAsJSON(request.getHeaders()));
                applicationEventPublisher.publishEvent(misCommunicationEvent);
            }
        } catch (IOException ex) {
            log.info("***********IOException: " + ex.getMessage());
            log.info("***********IOException: " + ex.getCause());
            throw new InternalException(ex.getMessage());
        }
        return new InterceptedResponse(HttpStatus.OK, "OK", body, request.getHeaders());
    }

    private String getHeadersAsJSON(HttpHeaders headers) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(headers);
        } catch (JsonProcessingException ex) {
            ex.getMessage();
        }
        return null;
    }
}
