package dev.manyroads.casereception;

import dev.manyroads.client.AdminClient;
import dev.manyroads.casereception.exception.AdminClientException;
import dev.manyroads.casereception.exception.VehicleTypeNotCoincideWithDomainException;
import dev.manyroads.casereception.exception.VehicleTypeNotFoundException;
import dev.manyroads.model.CaseRequest;
import dev.manyroads.model.CaseResponse;
import dev.manyroads.model.ChargeStatusEnum;
import dev.manyroads.model.VehicleTypeEnum;
import dev.manyroads.model.entity.Charge;
import dev.manyroads.model.entity.Customer;
import dev.manyroads.model.repository.ChargeRepository;
import dev.manyroads.model.repository.CustomerRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CaseReceptionService {

    private final AdminClient adminClient;
    private final CustomerRepository customerRepository;
    private final ChargeRepository chargeRepository;

    public CaseResponse processIncomingCaseRequest(CaseRequest caseRequest) {

        CaseResponse caseResponse = new CaseResponse();

        // Retrieve vehicle type from admin microservice
        String vehicleType = retrieveVehicleType(caseRequest.getCaseID());

        // Verify vehicle type in DCM domain
        VehicleTypeEnum vehicleTypeConfirmed;
        try {
            vehicleTypeConfirmed = VehicleTypeEnum.fromValue(vehicleType);
        } catch (IllegalArgumentException ex) {
            throw new VehicleTypeNotCoincideWithDomainException();
        }
        caseResponse.setVehicleType(vehicleTypeConfirmed);

        // Check if customer exists, otherwise create new account
        Optional<Customer> oCustomer = customerRepository.findByCustomerNr(caseRequest.getCustomerNr());
        oCustomer.orElseGet(() -> {
            Customer newCustomer = new Customer();
            newCustomer.setCustomerNr(caseRequest.getCustomerNr());
            log.info(String.format("New customer with nr: %d is saved", caseRequest.getCustomerNr()));
            return customerRepository.save(newCustomer);
        });
        caseResponse.setCustomerNr(caseRequest.getCustomerNr());

        // Check if charge for customer exist and if so see if case can be added
        Optional<Charge> oCharge = chargeRepository.findByCustomerNrAndChargeStatus(
                ChargeStatusEnum.APPLIED.toString(),
                ChargeStatusEnum.BOOKED.toString(),
                caseRequest.getCustomerNr());
        oCharge.ifPresentOrElse(
                (p)-> System.out.println("ChargeStaus : " + p.getChargeStatus()),
                ()->log.info("no such value"));

        return caseResponse;
    }

    private String retrieveVehicleType(String caseID) {

        String vehicleType;
        try {
            Optional<String> oVehicleType = getVehicleTypeEnum(caseID);
            oVehicleType.orElseThrow(VehicleTypeNotFoundException::new);
            vehicleType = oVehicleType.get();
        } catch (FeignException ex) {
            throw new AdminClientException();
        }
        return vehicleType;
    }

    /**
     * Check if return value is null, if so, return an empty object
     *
     * @param caseID
     * @return
     */
    private Optional<String> getVehicleTypeEnum(String caseID) {
        return Optional.ofNullable(adminClient.searchVehicleType(caseID));
    }
}
