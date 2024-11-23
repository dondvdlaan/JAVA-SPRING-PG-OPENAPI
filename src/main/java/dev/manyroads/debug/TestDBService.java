package dev.manyroads.debug;

import dev.manyroads.model.entity.Charge;
import dev.manyroads.model.entity.Customer;
import dev.manyroads.model.entity.Matter;
import dev.manyroads.model.repository.ChargeRepository;
import dev.manyroads.model.repository.CustomerRepository;
import dev.manyroads.model.repository.MatterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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


}
