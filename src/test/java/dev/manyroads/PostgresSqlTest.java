package dev.manyroads;

import dev.manyroads.controller.CaseController;
import dev.manyroads.model.entity.Charge;
import dev.manyroads.model.repository.ChargeRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

@SpringBootTest
public class PostgresSqlTest {

    @Autowired
    ChargeRepository chargeRepository;
    @Autowired
    CaseController caseController;

    @Test
    void chargeRepoTest() {

        // Prepare
        Charge job = new Charge();
        String status = "recorded";
        job.setChargeStatus(status);
        Charge savedJob = chargeRepository.save(job);

        // Act
        Optional<Charge> oResult = chargeRepository.findById(savedJob.getId());

        // Verify
        oResult.ifPresent(j -> assertEquals(status, j.getChargeStatus()));

    }
    @Test
    void smokeTest(){
        // Verify
        assertThat(caseController).isNotNull();
    }
}
