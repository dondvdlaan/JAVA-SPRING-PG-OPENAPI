package dev.manyroads.miscommunication;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class InterceptedResponse implements ClientHttpResponse {

    private HttpStatusCode httpStatusCode;
    private String statusText;
    private byte[] body;
    private HttpHeaders httpHeaders;

    public InterceptedResponse(HttpStatusCode httpStatusCode, String statusText, byte[] body, HttpHeaders httpHeaders) {
        this.httpStatusCode = httpStatusCode;
        this.statusText = statusText;
        this.body = body;
        this.httpHeaders = httpHeaders;
    }

    @Override
    public HttpStatusCode getStatusCode() throws IOException {
        return this.httpStatusCode;
    }

    @Override
    public String getStatusText() throws IOException {
        return "";
    }

    @Override
    public void close() {

    }

    @Override
    public InputStream getBody() throws IOException {
        return new ByteArrayInputStream(this.body);
    }

    @Override
    public HttpHeaders getHeaders() {
        return null;
    }
}
