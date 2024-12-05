package dev.manyroads.matterreception;

import dev.manyroads.client.AdminClient;
import dev.manyroads.client.CustomerProcessingClient;
import dev.manyroads.decomreception.exception.AdminClientException;
import dev.manyroads.decomreception.exception.InternalException;
import dev.manyroads.matterreception.exception.VehicleTypeNotCoincideWithDomainException;
import dev.manyroads.decomreception.exception.VehicleTypeNotFoundException;
import dev.manyroads.model.ChargeStatusEnum;
import dev.manyroads.model.MatterRequest;
import dev.manyroads.model.MatterResponse;
import dev.manyroads.model.VehicleTypeEnum;
import dev.manyroads.model.entity.Charge;
import dev.manyroads.model.entity.Customer;
import dev.manyroads.model.entity.Matter;
import dev.manyroads.model.enums.MatterStatus;
import dev.manyroads.model.repository.ChargeRepository;
import dev.manyroads.model.repository.CustomerRepository;
import dev.manyroads.model.repository.MatterRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatterReceptionService {

    private final AdminClient adminClient;
    private final CustomerRepository customerRepository;
    private final ChargeRepository chargeRepository;
    private final MatterRepository matterRepository;
    private final CustomerProcessingClient customerProcessingClient;

    public MatterResponse processIncomingMatterRequest(MatterRequest matterRequest) {
        log.info("processIncomingMatterRequest: started to process incoming matter Request");
        MatterResponse matterResponse = new MatterResponse();
        matterResponse.setCustomerNr(matterRequest.getCustomerNr());
        Charge charge = new Charge();

        // Retrieve vehicle type from admin microservice
        String vehicleType = retrieveVehicleType(matterRequest.getMatterNr());

        // Verify vehicle type in DCM domain
        VehicleTypeEnum vehicleTypeConfirmed;
        try {
            vehicleTypeConfirmed = VehicleTypeEnum.fromValue(vehicleType);
        } catch (IllegalArgumentException ex) {
            throw new VehicleTypeNotCoincideWithDomainException();
        }

        // Check if customer exists, otherwise create new account and save
        Customer oCustomer = customerRepository.findByCustomerNr(matterRequest.getCustomerNr());
        Customer customer = Optional.ofNullable(oCustomer)
                .orElseGet(
                        () -> {
                            Customer newCustomer = new Customer();
                            newCustomer.setCustomerNr(matterRequest.getCustomerNr());
                            Customer savedCustomer = customerRepository.save(newCustomer);
                            log.info(String.format("New customer with ID: %s is saved", savedCustomer.getCustomerID()));
                            return savedCustomer;
                        });

        // Check if charges for customer exists, if so, check if matter can be added, otherwise create new charge
        Optional<List<Charge>> oListCharges = chargeRepository.findByCustomerNrAndChargeStatus(
                ChargeStatusEnum.DCM_APPLIED,
                ChargeStatusEnum.BOOKED,
                matterRequest.getCustomerNr());
        if (oListCharges.isPresent()) {
            List<Charge> filteredListCharges = oListCharges.get().stream()
                    .filter(c -> c.getVehicleType().equals(vehicleTypeConfirmed)).toList();
            if (!filteredListCharges.isEmpty()) {
                charge = filteredListCharges.get(0);
                Matter newMatter = mapMatterRequest(matterRequest, charge);
                matterRepository.save(newMatter);
                charge.getMatters().add(newMatter);
                chargeRepository.save(charge);
                log.info("Vehicle type coincides, matter added to existing charge: {}", charge.getChargeID());
            } else {
                charge = createNewCharge(matterRequest, vehicleTypeConfirmed, customer);
                log.info("New charge created {} for existing customer nr: {}", charge.getChargeID(), charge.getCustomer().getCustomerID());
            }
        } else {
            charge = createNewCharge(matterRequest, vehicleTypeConfirmed, customer);
            log.info("New charge created {} for new customer nr: {}", charge.getChargeID(), charge.getCustomer().getCustomerID());
        }
        matterResponse.setChargeID(charge.getChargeID());

        // Pass on data to customer processing
        if (!customerProcessingClient.sendMessageToCustomerProcessing(charge)) {
            log.info("Failed to send message to customerProcessingClient for customer: {} ", customer.getCustomerNr());
            throw (new InternalException("DCM 101: customerProcessingClient not responsive"));
        }

        return matterResponse;
    }

    // Submethods
    private Charge createNewCharge(MatterRequest matterRequest, VehicleTypeEnum vehicleTypeConfirmed, Customer customer) {
        Charge newCharge = new Charge();
        newCharge.setChargeStatus(ChargeStatusEnum.BOOKED);
        newCharge.setCustomerNr(matterRequest.getCustomerNr());
        newCharge.setVehicleType(vehicleTypeConfirmed);
        newCharge.setCustomer(customer);
        chargeRepository.save(newCharge);
        Matter newMatter = mapMatterRequest(matterRequest, newCharge);
        matterRepository.save(newMatter);
        newCharge.getMatters().add(newMatter);
        return chargeRepository.save(newCharge);
    }

    private String retrieveVehicleType(String matterID) {
        String vehicleType;
        try {
            Optional<String> oVehicleType = getVehicleTypeEnum(matterID);
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
     * @param matterID
     * @return
     */
    private Optional<String> getVehicleTypeEnum(String matterID) {
        return Optional.ofNullable(adminClient.searchVehicleType(matterID));
    }

    private Matter mapMatterRequest(MatterRequest matterRequest, Charge charge) {
        Matter newMatter = new Matter();
        newMatter.setCustomerNr(matterRequest.getCustomerNr());
        newMatter.setMatterStatus(MatterStatus.EXECUTABLE);
        newMatter.setCharge(charge);
        return newMatter;
    }

}
