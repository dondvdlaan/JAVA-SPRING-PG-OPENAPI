package dev.manyroads.debug;

import dev.manyroads.model.entity.MisCommunication;
import dev.manyroads.model.repository.MiscommunicationRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

//@Service
public class StartTests implements CommandLineRunner {

    TestDBService testDBService;
    MiscommunicationRepository miscommunicationRepository;

    public StartTests(TestDBService testDBService, MiscommunicationRepository miscommunicationRepository) {
        this.testDBService = testDBService;
        this.miscommunicationRepository = miscommunicationRepository;
    }

    @Override
    public void run(String... args) throws Exception {

/*
        System.out.println("StartTests: Starting tests");
        for (int i = 0; i < 1; i++) {

            MisCommunication misCommunication = MisCommunication.builder()
                    .requestURI("http//localhostje/hola" + i)
                    .httpMethod("postje")
                    .messageBody(new byte[1024 * 16])
                    .headersAsJson("headertje")
                    .build();
            miscommunicationRepository.save(misCommunication);
            System.out.println("getMisCommID: " + misCommunication.getMisCommID());
        }

        Customer customerSaved = testDBService.savingOneToManyManyToOne();

        System.out.println("Yesss: " + customerSaved.getCustomerID());
        System.out.println("Nr. Charges: " + testDBService.addNewChargeToCustomerAndSave(customerSaved));
         */
    }
}
