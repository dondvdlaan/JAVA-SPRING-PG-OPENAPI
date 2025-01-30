package dev.manyroads.client.parent;

import dev.manyroads.decomreception.exception.InternalTechnicalException;
import dev.manyroads.model.OAuth2ResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class ParentOAuth2Service extends ParentOAuth2RESTConnector {

    private final static String AUTHORIZATION_CODE = "Authorization Code";
    private final static String DCM_ROLE = "DCM-SUPER";
    @Value("${OAuth2rest.user}")
    private String oAuth2RestUser;
    @Value("${OAuth2rest.password}")
    private String oAuth2RestPassword;
    private int port = 7090;
    private String authPath = "/auth";
    private String resourceServerPath = "/vehicle";
    private String authUri;
    private final static String RESOURCE_SERVER_URI = "http://localhost:7090/vehicle/";
    private String authorizationGrant;
    private String redirectionURI;
    private String accessToken;

    public ParentOAuth2Service(RestTemplate oAUth2restTemplate, @Value("${parent.host}") String host) {
        super(oAUth2restTemplate);
        this.authUri = "http://" + host + ":" + port + authPath;
    }

    public String getAccessToken(boolean byForce) {
        if (this.accessToken == null || byForce) retrieveAccessToken();
        return this.accessToken;
    }

    private void retrieveAccessToken() {
        // retrieve authorization grant
        log.info("retrieveAccessToken(): authorization grant: authUri-> " + authUri);
        ResponseEntity<OAuth2ResponseDTO> response = null;
        try {
            response = sendMessage(null, getOAuth2Headers(), authUri, HttpMethod.GET);
            log.info("retrieveAccessToken(): authorization grant: response-> " + response.getStatusCode());
        } catch (Exception e) {
            log.info("retrieveAccessToken(): authorization grant: Exception message: {}", e.getMessage());
            throw new InternalTechnicalException(String.format("Getting authorization grant failed with message: %s", e.getMessage()));
        }
        log.info("getAuthorizationGrant(): response.getBody() " + response.getBody());

        if (response.getBody() != null &&
                response.getBody().getAuthorizationGrant() != null &&
                response.getBody().getRedirectionURI() != null) {
            authorizationGrant = response.getBody().getAuthorizationGrant();
            redirectionURI = response.getBody().getRedirectionURI();
        }

        try {
            response = sendMessage(null, this.getAccessTokenHeaders(authorizationGrant), redirectionURI, HttpMethod.GET);
            log.info("getAccessToken(): response-> " + response.getStatusCode());
        } catch (Exception e) {
            log.info("sendMessageToOAuth2Client: Exception message: {}", e.getMessage());
            throw new InternalTechnicalException(String.format("getAccessToken() failed with message: %s", e.getMessage()));
        }
        log.info("getAccessToken(): response.getBody() " + response.getBody());
        this.accessToken = response.getHeaders().getFirst("Access-Token");
        log.info("getAccessToken(): this.accessToken-> " + this.accessToken);
    }

    // sub methods
    private HttpHeaders getOAuth2Headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Grant-Type", AUTHORIZATION_CODE);
        headers.add("x-user-role", DCM_ROLE);
        headers.setBasicAuth(oAuth2RestUser, oAuth2RestPassword);
        return headers;
    }

    private HttpHeaders getAccessTokenHeaders(String authorizationGrant) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Grant-Type", AUTHORIZATION_CODE);
        headers.add("Authorization-Grant", authorizationGrant);
        headers.add("x-user-role", DCM_ROLE);
        return headers;
    }

    public HttpHeaders getTransmissionTypeHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Grant-Type", AUTHORIZATION_CODE);
        headers.add("Access-Token", getAccessToken(false));
        headers.add("x-user-role", DCM_ROLE);
        return headers;
    }
}
