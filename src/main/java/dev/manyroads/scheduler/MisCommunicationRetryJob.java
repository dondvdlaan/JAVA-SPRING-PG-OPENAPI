package dev.manyroads.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.manyroads.decomreception.exception.InternalTechnicalException;
import dev.manyroads.miscommunication.exception.MiscommunicationNotFoundException;
import dev.manyroads.model.entity.MisCommunication;
import dev.manyroads.model.repository.MiscommunicationRepository;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
@Slf4j
public class MisCommunicationRetryJob implements Job {

    private final MiscommunicationRepository miscommunicationRepository;
    private final RestTemplate retryRestTemplate;
    private final SchedulerService schedulerService;
    @Value("${misCommunicationMaxRetries}")
    Integer misCommunicationMaxRetries;

    public MisCommunicationRetryJob(MiscommunicationRepository miscommunicationRepository,
                                    @Qualifier("retryRestTemplate") RestTemplate retryRestTemplate,
                                    SchedulerService schedulerService) {
        this.miscommunicationRepository = miscommunicationRepository;
        this.retryRestTemplate = retryRestTemplate;
        this.schedulerService = schedulerService;
    }

    @Override
    @Transactional
    public void execute(JobExecutionContext jobExecutionContext) {
        int retries;
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
        retries = misCommunication.getRetries();
        retries++;
        misCommunication.setRetries(retries);
        miscommunicationRepository.save(misCommunication);
        log.info("MisCommunicationRetryJob: current retries {}", retries);

        if (retryResponse.getStatusCode().is2xxSuccessful()) {
            log.info("MisCommunicationRetryJob: retryResponse.getStatusCode() {}", retryResponse.getStatusCode());
            misCommunication.setRetrySuccesful(true);
            miscommunicationRepository.save(misCommunication);
            return;
        }
        if (retryResponse.getStatusCode().is5xxServerError()) {
            log.info("MisCommunicationRetryJob: retryResponse.getStatusCode() {}", retryResponse.getStatusCode());
            if (retries >= misCommunicationMaxRetries) {
                maximumRetriesReached(retries, misCommunication);
                return;
            }
            schedulerService.rescheduleJobMiscommunicationRetry(jobExecutionContext.getTrigger(), retries, misCommID);
            return;
        }
        if (retryResponse.getStatusCode().is4xxClientError()) {
            log.info("MisCommunicationRetryJob: retryResponse.getStatusCode() {}", retryResponse.getStatusCode());
            misCommunication.setRetrySuccesful(false);
            miscommunicationRepository.save(misCommunication);
        }
    }

    // Sub methods
    private void maximumRetriesReached(int retries, MisCommunication misCommunication) {
        log.info("MisCommunicationRetryJob: maximumRetriesReached-> maximum retries: {} reached: {}", misCommunicationMaxRetries, retries);
        misCommunication.setRetries(retries);
        misCommunication.setRetrySuccesful(false);
        miscommunicationRepository.save(misCommunication);
    }

    private ResponseEntity<?> retryCommunication(String url, String httpMethod, byte[] body, String headersAsJson) {
        HttpHeaders headers = new HttpHeaders();
        headers = convertJsonToHeaders(headersAsJson);
        HttpEntity<?> requestEntity = new HttpEntity<>(body, headers);
        try {
            return retryRestTemplate.exchange(url, HttpMethod.valueOf(httpMethod), requestEntity, Void.class);
        } catch (ResourceAccessException ex) {
            throw new InternalTechnicalException(ex.getMessage());
        }
    }

    private HttpHeaders convertJsonToHeaders(String headersAsJson) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(headersAsJson, HttpHeaders.class);
        } catch (JsonProcessingException e) {
            throw new InternalTechnicalException(e.getMessage());
        }
    }
}
