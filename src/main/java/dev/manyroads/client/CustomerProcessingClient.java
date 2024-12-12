package dev.manyroads.client;

import dev.manyroads.decomreception.exception.InternalException;
import dev.manyroads.model.entity.Charge;
import dev.manyroads.model.messages.CustomerProcessingClientMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class CustomerProcessingClient extends RESTConnector {

    @Autowired
    public CustomerProcessingClient(RestTemplate restTemplate) {
        super(restTemplate);
    }
    private final static String DCM_ROLE = "DCM-SUPER";
    private final static String CUSTOMER_PROCESSING_URL = "http://localhost:7090/v1/process_charge";

    public boolean sendMessageToCustomerProcessing(CustomerProcessingClientMessage customerProcessingClientMessage) {

        ResponseEntity<?> response= null;
        try {
            response = sendMessage(customerProcessingClientMessage, DCM_ROLE, CUSTOMER_PROCESSING_URL, HttpMethod.POST);
        } catch (Exception e) {
            log.info("response: {}", response);
            throw new InternalException(String.format("sendMessageToCustomerProcessing: %s", e.getMessage()));
        }

        return response.getStatusCode().value()==200;
    }

}
