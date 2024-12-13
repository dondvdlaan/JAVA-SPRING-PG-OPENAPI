package dev.manyroads.debug;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

//@Service
public class StartTests implements CommandLineRunner {

    TestDBService testDBService;

    public StartTests(TestDBService testDBService) {
        this.testDBService = testDBService;
    }

    @Override
    public void run(String... args) throws Exception {

         /*
        System.out.println("StartTests: Starting tests");

        Customer customerSaved = testDBService.savingOneToManyManyToOne();

        System.out.println("Yesss: " + customerSaved.getCustomerID());
        System.out.println("Nr. Charges: " + testDBService.addNewChargeToCustomerAndSave(customerSaved));
         */
    }
}
