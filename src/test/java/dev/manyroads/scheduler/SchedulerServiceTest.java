package dev.manyroads.scheduler;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class SchedulerServiceTest {

    @Autowired
    SchedulerService schedulerService;

    @Test
    public void happyFlowCustomerStandByScheduleTest() throws Exception {
        // activate
        schedulerService.scheduleCustomerStandby(12345L);
        // Verify
        // check log messages

        Thread.sleep(30000); // Sleep 30 s
    }
}
