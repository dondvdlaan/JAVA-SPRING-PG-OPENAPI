package dev.manyroads.miscommunication;

import dev.manyroads.model.entity.MisCommunication;
import dev.manyroads.model.repository.MiscommunicationRepository;
import dev.manyroads.scheduler.SchedulerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MiscommunicationEventListner  {

    private final MiscommunicationRepository miscommunicationRepository;
    private final SchedulerService schedulerService;

    public MiscommunicationEventListner(MiscommunicationRepository miscommunicationRepository, SchedulerService schedulerService) {
        this.miscommunicationRepository = miscommunicationRepository;
        this.schedulerService = schedulerService;
    }

    @EventListener
    public void onApplicationEvent(MisCommunicationEvent misCommunicationEvent) {
        log.info("onApplicationEvent: misCommunicationEvent received: {}", misCommunicationEvent);

            MisCommunication misCommunication = convertMisCommunicationEvent(misCommunicationEvent);
            var saved = miscommunicationRepository.save(misCommunication);
            log.info("onApplicationEvent: saved misCommunication " + saved);
            log.info("onApplicationEvent: misCommunication db count " + miscommunicationRepository.count());
            var found = miscommunicationRepository.findById(saved.getMisCommID());
            log.info("onApplicationEvent: FOUND misCommunication " + found);

        //schedulerService.scheduleMiscommunicationRetry(saved.getMisCommID());
    }

    private MisCommunication convertMisCommunicationEvent(MisCommunicationEvent misCommunicationEvent) {
        return MisCommunication.builder()
                .requestURI(misCommunicationEvent.getUrl())
                .httpMethod(misCommunicationEvent.getMethod())
                .messageBody(misCommunicationEvent.getBody())
                .headersAsJson(misCommunicationEvent.getJsoHeaders())
                .build();
    }
}
