package dev.manyroads.client;

import dev.manyroads.decomreception.exception.InternalException;
import dev.manyroads.model.entity.Matter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@AllArgsConstructor
@Slf4j
public class ParentMicroserviceClient extends RESTConnector {

    @Autowired
    public ParentMicroserviceClient(RestTemplate restTemplate) {
        super(restTemplate);
    }

    private final static String DCM_ROLE = "DCM-SUPER";

    public boolean requestParentMicroserviceToacticateTermination(Matter matter) {

        String CUSTOMER_PROCESSING_URL = "http://localhost:7090" + matter.getTerminationCallBackUrl();
        System.out.println("CUSTOMER_PROCESSING_URL: " + CUSTOMER_PROCESSING_URL);
        ResponseEntity<?> response = null;
        try {
            response = sendMessage(matter.convertToMatterMessage(), DCM_ROLE, CUSTOMER_PROCESSING_URL, HttpMethod.POST);
        } catch (Exception e) {
            log.info("response: {}", response);
            throw new InternalException(String.format("sendMessageToParentMicroservice: %s", e.getMessage()));
        }

        return response.getStatusCode().value() == 200;
    }
}
