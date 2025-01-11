package dev.manyroads.miscommunication;

import lombok.Getter;

@Getter
public class MisCommunicationEvent<T> {

    private T source;
    private String url;
    private String method;
    private byte[] body;
    private String jsoHeaders;

    public MisCommunicationEvent(T source, String url, String method, byte[] body, String jsoHeaders) {
        this.source = source;
        this.url = url;
        this.method = method;
        this.body = body;
        this.jsoHeaders = jsoHeaders;
    }

}