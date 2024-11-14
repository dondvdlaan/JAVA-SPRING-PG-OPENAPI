package dev.manyroads.casereception;

import dev.manyroads.model.CaseRequest;
import dev.manyroads.model.CaseResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CaseControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    void caseRequestShouldReturnCaseResponse() throws Exception {
        // Prepare
        CaseRequest caseRequest = new CaseRequest();
        CaseResponse caseResponse = new CaseResponse();
        String expected = caseResponse.toString();

        // Activate
        CaseResponse result  = testRestTemplate
                .postForObject(
                        "http://localhost:" + port + "/v1/cases",
                        caseRequest, CaseResponse.class);

        // Verify
        assertEquals(result.toString(), expected);
    }
}
