package dev.manyroads.database;

import dev.manyroads.model.entity.MisCommunication;
import dev.manyroads.model.repository.MiscommunicationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Copied from PostgresSqlTest for individual tests
 */
@SpringBootTest
public class MiscommunicationRepoPostgresSqlTest {

    @Autowired
    MiscommunicationRepository miscommunicationRepository;

    @BeforeEach
    void setUp() {
        miscommunicationRepository.deleteAll();
    }

    @AfterEach
    void simmerDown() throws InterruptedException {
        Thread.sleep(20000);
    }

    @Test
    @Transactional
    @DisplayName("Testing miscommunicationRepository ")
    void miscommunicationRepoTest() throws InterruptedException {
        // prepare
        MisCommunication misCommunication = null;
        MisCommunication saved = null;

        for (int i = 0; i < 10; i++) {
            misCommunication = MisCommunication.builder()
                    .requestURI("http//localhostje/hola" + 1)
                    .httpMethod("postje")
                    .messageBody(new byte[1024 * 16])
                    .headersAsJson("headertje")
                    .build();

            // activate
            saved = miscommunicationRepository.save(misCommunication);
            var found = miscommunicationRepository.findById(saved.getMisCommID()).orElse(null);

            // verify
            assertEquals(saved.getMisCommID(), misCommunication.getMisCommID());
            assert found != null : "found is OK";
            assertEquals(found.getMisCommID(), misCommunication.getMisCommID());
        }

        assertEquals(miscommunicationRepository.count(), 10);
    }
}
