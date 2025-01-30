package dev.manyroads.client.parent;

import dev.manyroads.decomreception.exception.InternalTechnicalException;
import dev.manyroads.model.entity.Matter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class ParentMicroserviceClient extends ParentOAuth2RESTConnector {

    ParentOAuth2Service parentOAuth2Service;
    @Value("${parent.host}")
    String host;
    String parentProcessingUri;
    private int port = 7090;

    public ParentMicroserviceClient(RestTemplate oAUth2restTemplate,
                                    ParentOAuth2Service parentOAuth2Service) {
        super(oAUth2restTemplate);
        this.parentOAuth2Service = parentOAuth2Service;
    }

    private final static String DCM_ROLE = "DCM-SUPER";

    public boolean requestParentMicroserviceToActivateTermination(Matter matter) {
        log.info("requestParentMicroserviceToActivateTermination: request to activate termination started");
        //this.parentProcessingUri += matter.getTerminationCallBackUrl();
        String parentProcessingUri = "http://" + host + ":" + port + matter.getTerminationCallBackUrl();
        log.info("PARENT_PROCESSING_URI: " + parentProcessingUri);

        ResponseEntity<?> response = sendMessage
                (matter.convertToMatterMessage(), parentOAuth2Service.getTransmissionTypeHeaders(), parentProcessingUri, HttpMethod.POST);
        log.info("requestParentMicroserviceToActivateTermination: response.getStatusCode() {}",response.getStatusCode());

        if (response.getStatusCode().isSameCodeAs(HttpStatus.FORBIDDEN) || response.getStatusCode().isSameCodeAs(HttpStatus.UNAUTHORIZED)) {
            log.info("requestParentMicroserviceToActivateTermination: failed with code {}, refreshing accesscode", response.getStatusCode());
            parentOAuth2Service.getAccessToken(true);
            // applied recursion
            return requestParentMicroserviceToActivateTermination(matter);
        }
        if (!response.getStatusCode().is2xxSuccessful())
            throw new InternalTechnicalException(String.format("requestParentMicroserviceToActivateTermination failed %s",response.getStatusCode()));

        return response.getStatusCode().value() == 200;
    }

}
