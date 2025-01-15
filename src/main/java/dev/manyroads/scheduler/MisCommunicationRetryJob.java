package dev.manyroads.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.manyroads.decomreception.exception.InternalException;
import dev.manyroads.miscommunication.exception.MiscommunicationNotFoundException;
import dev.manyroads.model.entity.MisCommunication;
import dev.manyroads.model.repository.MiscommunicationRepository;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class MisCommunicationRetryJob implements Job {

    private final MiscommunicationRepository miscommunicationRepository;
    private final RestTemplate retryRestTemplate;

    public MisCommunicationRetryJob(MiscommunicationRepository miscommunicationRepository,
                                    @Qualifier("retryRestTemplate") RestTemplate retryRestTemplate) {
        this.miscommunicationRepository = miscommunicationRepository;
        this.retryRestTemplate = retryRestTemplate;
    }

    @Override
    @Transactional
    public void execute(JobExecutionContext jobExecutionContext) {
        String misCommID = jobExecutionContext.getJobDetail().getJobDataMap().getString("misCommID");
        log.info(String.format("MisCommunicationRetryJob: executing job for retry %s", misCommID));
        MisCommunication misCommunication = miscommunicationRepository.findById(UUID.fromString(misCommID)).orElse(null);
        if (misCommunication == null) throw new MiscommunicationNotFoundException(UUID.fromString(misCommID));

        var retryResponse = retryCommunication(
                misCommunication.getRequestURI(),
                misCommunication.getHttpMethod(),
                misCommunication.getMessageBody(),
                misCommunication.getHeadersAsJson()
        );
        if (retryResponse.getStatusCode().is4xxClientError())
            log.info(String.format("MisCommunicationRetryJob: retryResponse.getStatusCode() %s", retryResponse.getStatusCode()));
        if (retryResponse.getStatusCode().is5xxServerError())
            log.info(String.format("MisCommunicationRetryJob: retryResponse.getStatusCode() %s", retryResponse.getStatusCode()));
        if (retryResponse.getStatusCode().is2xxSuccessful())
            log.info(String.format("MisCommunicationRetryJob: retryResponse.getStatusCode() %s", retryResponse.getStatusCode()));
    }

    private ResponseEntity<?> retryCommunication(String url, String httpMethod, byte[] body, String headersAsJson) {
        HttpHeaders headers = new HttpHeaders();
        headers = convertJsonToHeaders(headersAsJson);
        HttpEntity<?> requestEntity = new HttpEntity<>(body, headers);
        try {
            return retryRestTemplate.exchange(url, HttpMethod.valueOf(httpMethod), requestEntity, Void.class);
        } catch (ResourceAccessException ex) {
            throw new InternalException(ex.getMessage());
        }
    }

    private HttpHeaders convertJsonToHeaders(String headersAsJson) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(headersAsJson, HttpHeaders.class);
        } catch (JsonProcessingException e) {
            throw new InternalException(e.getMessage());
        }
    }
}
