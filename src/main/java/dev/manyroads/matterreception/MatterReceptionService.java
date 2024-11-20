package dev.manyroads.matterreception;

import dev.manyroads.client.AdminClient;
import dev.manyroads.matterreception.exception.AdminClientException;
import dev.manyroads.matterreception.exception.VehicleTypeNotCoincideWithDomainException;
import dev.manyroads.matterreception.exception.VehicleTypeNotFoundException;
import dev.manyroads.model.ChargeStatusEnum;
import dev.manyroads.model.MatterRequest;
import dev.manyroads.model.MatterResponse;
import dev.manyroads.model.VehicleTypeEnum;
import dev.manyroads.model.entity.Charge;
import dev.manyroads.model.entity.Customer;
import dev.manyroads.model.entity.Matter;
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
public class MatterReceptionService {

    private final AdminClient adminClient;
    private final CustomerRepository customerRepository;
    private final ChargeRepository chargeRepository;

    public MatterResponse processIncomingCaseRequest(MatterRequest matterRequest) {

        MatterResponse matterResponse = new MatterResponse();

        // Retrieve vehicle type from admin microservice
        String vehicleType = retrieveVehicleType(matterRequest.getMatterID());

        // Verify vehicle type in DCM domain
        VehicleTypeEnum vehicleTypeConfirmed;
        try {
            vehicleTypeConfirmed = VehicleTypeEnum.fromValue(vehicleType);
        } catch (IllegalArgumentException ex) {
            throw new VehicleTypeNotCoincideWithDomainException();
        }
        matterResponse.setVehicleType(vehicleTypeConfirmed);

        // Check if customer exists, otherwise create new account and save
        Optional<Customer> oCustomer = customerRepository.findByCustomerNr(matterRequest.getCustomerNr());
        oCustomer.orElseGet(() -> {
            Customer newCustomer = new Customer();
            newCustomer.setCustomerNr(matterRequest.getCustomerNr());
            log.info(String.format("New customer with nr: %d is saved", matterRequest.getCustomerNr()));
            return customerRepository.save(newCustomer);
        });
        matterResponse.setCustomerNr(matterRequest.getCustomerNr());

        // Check if charge for customer exists, if so, check if matter can be added. Otherwise create new charge
        Optional<Charge> oCharge = chargeRepository.findByCustomerNrAndChargeStatus(
                ChargeStatusEnum.APPLIED.toString(),
                ChargeStatusEnum.BOOKED.toString(),
                matterRequest.getCustomerNr());
        oCharge.ifPresentOrElse(
                (c) -> {
                    log.info("Existing charge found for customer nr: {}", matterRequest.getCustomerNr());
                    if (c.getVehicleType().equals(vehicleTypeConfirmed)) {
                        log.info("Vehicle type coincides, matter added to charge {}", matterRequest.getCustomerNr());
                        c.getMatters().add(mapMatterRequest(matterRequest));
                        Charge savedCharge =chargeRepository.save(c);
                    }
                    matterResponse.setChargeID(c.getChargeID());
                },
                () -> {
                    log.info("New charge created for customer nr: {}", matterRequest.getCustomerNr());
                    Charge newCharge = Charge.builder()
                            .
                            .build();
                }
        );

        return matterResponse;
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

    private Matter mapMatterRequest(MatterRequest matterRequest) {
        return Matter.builder()
                .customerNr(matterRequest.getCustomerNr())
                .build();
    }

}
