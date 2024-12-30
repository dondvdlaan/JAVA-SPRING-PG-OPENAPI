package dev.manyroads.scheduler;

import dev.manyroads.matterreception.MatterReceptionService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class CustomerStandByJob implements Job {

    private final MatterReceptionService matterReceptionService;

    public CustomerStandByJob(MatterReceptionService matterReceptionService) {
        this.matterReceptionService = matterReceptionService;
    }

    @Override
    @Transactional
    public void execute(JobExecutionContext jobExecutionContext) {
        long customerNr = jobExecutionContext.getJobDetail().getJobDataMap().getLong("customerNr");
        log.info(String.format("CustomerStandByJob: executing job for customer %d", customerNr));
        matterReceptionService.sendCustomerDataToCustomerProcessing(customerNr);
    }
}
