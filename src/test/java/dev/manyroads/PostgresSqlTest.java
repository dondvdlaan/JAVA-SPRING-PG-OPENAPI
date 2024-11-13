package dev.manyroads;

import dev.manyroads.model.ChargeEntity;
import dev.manyroads.repository.ChargeRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

@SpringBootTest
public class PostgresSqlTest {

    @Autowired
    ChargeRepository chargeRepository;

    @Test
    void jobRepoTest() {

        // Prepare
        ChargeEntity job = new ChargeEntity();
        String status = "recorded";
        job.setChargeStatus(status);
        ChargeEntity savedJob = chargeRepository.save(job);

        // Act
        Optional<ChargeEntity> oResult = chargeRepository.findById(savedJob.getChargeId());

        // Verify
        oResult.ifPresent(j -> assertEquals(status, j.getChargeStatus()));

    }
}
