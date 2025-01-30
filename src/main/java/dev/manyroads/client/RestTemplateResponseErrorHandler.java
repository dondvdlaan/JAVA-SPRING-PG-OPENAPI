package dev.manyroads.client;

import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;

/**
 * Goal of this RestTemplateResponseErrorHandler class is that exception messages are sent within the response
 * back to RestTemplate method for handling and are not thrown nor caught in the try/catch block
 */
public class RestTemplateResponseErrorHandler implements ResponseErrorHandler {
    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return false;
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {

    }
}
