package dev.manyroads.scheduler;

import dev.manyroads.matterreception.MatterReceptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomerStandByJob implements Job {

    private final MatterReceptionService matterReceptionService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        long customerNr = jobExecutionContext.getJobDetail().getJobDataMap().getLong("customerNr");
        log.info(String.format("CustomerStandByJob: executing job for customer %d", customerNr));
        matterReceptionService.sendCustomerDataToCustomerProcessing(customerNr);
    }
}
