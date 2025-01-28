package dev.manyroads.client.parent;

import dev.manyroads.model.entity.Matter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class ParentMicroserviceClient extends ParentOAuth2RESTConnector {

    public ParentMicroserviceClient(RestTemplate oAUth2restTemplate) {
        super(oAUth2restTemplate);
    }

    private final static String DCM_ROLE = "DCM-SUPER";

    public boolean requestParentMicroserviceToActivateTermination(Matter matter) {
        log.info("requestParentMicroserviceToActicateTermination: request to activate termination started");
        String CUSTOMER_PROCESSING_URL = "http://localhost:7091" + matter.getTerminationCallBackUrl();
        log.info("CUSTOMER_PROCESSING_URL: " + CUSTOMER_PROCESSING_URL);
        ResponseEntity<?> response = sendMessage(matter.convertToMatterMessage(), DCM_ROLE, CUSTOMER_PROCESSING_URL, HttpMethod.POST);

        return response.getStatusCode().value() == 200;
    }
}
