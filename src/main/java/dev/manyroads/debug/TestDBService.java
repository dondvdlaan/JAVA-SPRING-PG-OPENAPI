package dev.manyroads.debug;

import dev.manyroads.model.entity.Charge;
import dev.manyroads.model.entity.Customer;
import dev.manyroads.model.entity.Matter;
import dev.manyroads.model.enums.ChargeStatus;
import dev.manyroads.model.repository.ChargeRepository;
import dev.manyroads.model.repository.CustomerRepository;
import dev.manyroads.model.repository.MatterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TestDBService {

    private final CustomerRepository customerRepository;
    private final ChargeRepository chargeRepository;
    private final MatterRepository matterRepository;

    public void savingCustomer(Matter matter) {


        Customer newCustomer = new Customer();
        newCustomer.setCustomerNr(matter.getCustomerNr());
        Customer savedCustomer = customerRepository.save(newCustomer);
        log.info("savedCustomer: {}", savedCustomer);
        log.debug("savedCustomer: {}", savedCustomer);

        matterRepository.save(matter);

        Charge charge = new Charge();
        charge.setCustomerNr(matter.getCustomerNr());
        charge.setCustomer(newCustomer);
        Charge savedCharge = chargeRepository.save(charge);
        log.info("savedCharge: {}", savedCharge);
        log.info("charge: {}", charge);

        newCustomer.getCharge().add(charge);
        customerRepository.save(newCustomer);
    }

    public Customer savingOneToManyManyToOne() {

        Long customerNr = (long) (Math.random() * 999999);
        UUID customerId = UUID.randomUUID();
        UUID chargeId = UUID.randomUUID();

        Customer customer = Customer.builder()
                .customerNr(customerNr)
                .build();
        customerRepository.save(customer);
        Charge charge = Charge.builder()
                .chargeID(chargeId)
                .customerNr(customerNr)
                .chargeStatus(ChargeStatus.IN_PROCESS)
                .startDate(Instant.now())
                .customer(customer)
                .build();
        chargeRepository.save(charge);
        List<Charge> listCjages = new ArrayList<>() ;
        listCjages.add(charge);
        customer.setCharge(listCjages);
        customerRepository.save(customer);

        return customer;
    }

    @Transactional
    public long addNewChargeToCustomerAndSave(Customer customer){

        Long customerNr = (long) (Math.random() * 999999);
        UUID customerId = UUID.randomUUID();
        UUID chargeId = UUID.randomUUID();

        Charge newCharge = Charge.builder()
                .chargeID(chargeId)
                .customerNr(customerNr)
                .chargeStatus(ChargeStatus.REJECTED)
                .startDate(Instant.now())
                .customer(customer)
                .build();
        Optional<Customer> oExistingCustomer = customerRepository.findById(customer.getCustomerID());
        oExistingCustomer.orElseThrow();
        Customer existingCustomer = oExistingCustomer.get();
        // Update charge entity, ManyToOne
        newCharge.setCustomer(existingCustomer);
        chargeRepository.save(newCharge);
        // Update customer entity, OneToMany
        existingCustomer.getCharge().add(newCharge);
        customerRepository.save(existingCustomer);

        return existingCustomer.getCharge().size();
    }




}
