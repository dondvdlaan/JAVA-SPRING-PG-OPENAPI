package dev.manyroads.scheduler;

import dev.manyroads.miscommunication.exception.MiscommunicationNotFoundException;
import dev.manyroads.model.entity.MisCommunication;
import dev.manyroads.model.repository.MiscommunicationRepository;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@Slf4j
public class MisCommunicationRetryJob implements Job {

    private final MiscommunicationRepository miscommunicationRepository;

    public MisCommunicationRetryJob(MiscommunicationRepository miscommunicationRepository) {
        this.miscommunicationRepository = miscommunicationRepository;
    }

    @Override
    @Transactional
    public void execute(JobExecutionContext jobExecutionContext) {
        String misCommID = jobExecutionContext.getJobDetail().getJobDataMap().getString("misCommID");
        log.info(String.format("MisCommunicationRetryJob: executing job for retry %s", misCommID));
        MisCommunication misCommunication = miscommunicationRepository.findById(UUID.fromString(misCommID)).orElse(null);
        if (misCommunication == null) throw new MiscommunicationNotFoundException(UUID.fromString(misCommID));
        log.info("MisCommunicationRetryJob: MisCommunication: " + misCommunication);
    }
}
