package dev.manyroads.scheduler;

import dev.manyroads.decomreception.exception.InternalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DateBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

import static org.quartz.DateBuilder.futureDate;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulerService {

    private final Scheduler scheduler;

    @Value("${customerStandByDuration}")
    Integer customerStandByDuration;
    @Value("${misCommunicationRetryDelay}")
    Integer misCommunicationRetryDelay;
    @Value("${misCommunicationMaxRetries}")
    Integer misCommunicationMaxRetries;

    public void scheduleCustomerStandby(Long customerNr) {

        JobDetail job = newJob(CustomerStandByJob.class)
                .withIdentity("customer-job-" + customerNr)
                .usingJobData("customerNr", customerNr)
                .build();
        Trigger trigger = newTrigger()
                .withIdentity("trigger-customer-job-" + customerNr)
                .startAt(futureDate(customerStandByDuration, DateBuilder.IntervalUnit.SECOND))
                .build();
        try {
            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException ex) {
            throw new InternalException(String.format("Scheduler failure for customer: %d", customerNr));
        }
    }

    public void scheduleMiscommunicationRetry(UUID misCommID) {

        Date startTime = DateBuilder.nextGivenSecondDate(null, misCommunicationRetryDelay);
        JobDetail job = newJob(MisCommunicationRetryJob.class)
                .withIdentity("retry-job-" + misCommID)
                .usingJobData("misCommID", misCommID.toString())
                .build();
        Trigger trigger = newTrigger()
                .withIdentity("trigger-retry-job-" + misCommID)
                .startAt(startTime)
                .build();
        try {
            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException ex) {
            throw new InternalException(String.format("Scheduler failure for retry-job: %s", misCommID));
        }
    }
}
