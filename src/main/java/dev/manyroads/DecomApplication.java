package dev.manyroads;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class DecomApplication implements CommandLineRunner {



    public static void main(String[] args) {
        SpringApplication.run(DecomApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        /*
        Customer customerSaved = testDBService.savingOneToManyManyToOne();

        System.out.println("Yesss: " + customerSaved.getCustomerID());
        System.out.println("Nr. Charges: " + testDBService.addNewChargeToCustomerAndSave(customerSaved));
         */
    }
}
