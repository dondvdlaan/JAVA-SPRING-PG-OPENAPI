package dev.manyroads.miscommunication;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MiscommunicationEventListner {


    @EventListener
    public void onApplicationEvent(MisCommunicationEvent misCommunicationEvent) {
        log.info("onApplicationEvent: misCommunicationEvent received: {}", misCommunicationEvent);


    }
}
