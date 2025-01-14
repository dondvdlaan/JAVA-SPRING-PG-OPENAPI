package dev.manyroads.miscommunication;

import lombok.Getter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;

@Getter
@ToString
public class MisCommunicationEvent extends ApplicationEvent {

    private final String url;
    private final String method;
    private final byte[] body;
    private final String jsoHeaders;

    public MisCommunicationEvent(Object source, String url, String method, byte[] body, String jsoHeaders) {
        super(source);
        this.url = url;
        this.method = method;
        this.body = body;
        this.jsoHeaders = jsoHeaders;
    }

}