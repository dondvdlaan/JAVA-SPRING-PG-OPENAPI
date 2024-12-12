package dev.manyroads.debug;

import dev.manyroads.model.entity.Charge;
import dev.manyroads.model.entity.Customer;
import dev.manyroads.model.entity.Matter;
import dev.manyroads.model.repository.ChargeRepository;
import dev.manyroads.model.repository.CustomerRepository;
import dev.manyroads.model.repository.MatterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;

public class TestDBSeviceTest {

    TestDBService testDBService;
    CustomerRepository customerRepository;
    ChargeRepository chargeRepository;
    MatterRepository matterRepository;

    @BeforeEach
    void setUp() {
        customerRepository = mock(CustomerRepository.class);
        chargeRepository = mock(ChargeRepository.class);
        matterRepository = mock(MatterRepository.class);
        this.testDBService = new TestDBService(customerRepository, chargeRepository, matterRepository);
    }

    @Test
    @Transactional
    void saveCustomerTest() {

        // Prepare
        Long customerNr = (long) (Math.random() * 999999);
        String matterNr = "258963";
        Matter newMatter = new Matter();
        newMatter.setMatterNr(matterNr);

        Customer newCustomer = new Customer();
        newCustomer.setCustomerNr(customerNr);
        when(customerRepository.save(any())).thenReturn(newCustomer);

        Charge charge = new Charge();
        charge.setCustomerNr(customerNr);
        charge.setCustomer(newCustomer);
        when(chargeRepository.save(any())).thenReturn(charge);

        // When
        testDBService.savingCustomer(newMatter);
        // Verify
        verify(customerRepository, times(2)).save(any());
        verify(chargeRepository, times(1)).save(any());
        assertEquals(customerNr, newCustomer.getCustomerNr());
    }

}
