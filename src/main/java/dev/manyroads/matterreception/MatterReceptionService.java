package dev.manyroads.matterreception;

import dev.manyroads.client.AdminClient;
import dev.manyroads.client.CustomerProcessingClient;
import dev.manyroads.matterreception.exception.AdminClientException;
import dev.manyroads.matterreception.exception.InternalException;
import dev.manyroads.matterreception.exception.VehicleTypeNotCoincideWithDomainException;
import dev.manyroads.matterreception.exception.VehicleTypeNotFoundException;
import dev.manyroads.model.ChargeStatus;
import dev.manyroads.model.MatterRequest;
import dev.manyroads.model.MatterResponse;
import dev.manyroads.model.VehicleTypeEnum;
import dev.manyroads.model.entity.Charge;
import dev.manyroads.model.entity.Customer;
import dev.manyroads.model.entity.Matter;
import dev.manyroads.model.repository.ChargeRepository;
import dev.manyroads.model.repository.CustomerRepository;
import dev.manyroads.model.repository.MatterRepository;
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
    private final MatterRepository matterRepository;
    private final CustomerProcessingClient customerProcessingClient;

    public MatterResponse processIncomingMatterRequest(MatterRequest matterRequest) {

        MatterResponse matterResponse = new MatterResponse();
        matterResponse.setCustomerNr(matterRequest.getCustomerNr());
        Charge charge;

        // Retrieve vehicle type from admin microservice
        String vehicleType = retrieveVehicleType(matterRequest.getMatterID());

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

        // Check if charge for customer exists, if so, check if matter can be added. Otherwise create new charge
        Optional<Charge> oCharge = chargeRepository.findByCustomerNrAndChargeStatus(
                ChargeStatus.APPLIED,
                ChargeStatus.BOOKED,
                matterRequest.getCustomerNr());
        if (oCharge.isPresent() && oCharge.get().getVehicleType().equals(vehicleTypeConfirmed)) {
            charge = oCharge.get();
            Matter newMatter = mapMatterRequest(matterRequest, charge);
            matterRepository.save(newMatter);
            charge.getMatters().add(newMatter);
            chargeRepository.save(charge);
            log.info("Vehicle type coincides, matter added to existing charge: {}", charge.getChargeID());
        } else {
            charge = createNewCharge(matterRequest, vehicleTypeConfirmed, customer);
            log.info("New charge created {} for new customer nr: {}", charge.getChargeID(), charge.getCustomer().getCustomerID());
        }
        matterResponse.setChargeID(charge.getChargeID());

        // Pass on data to customer processing
        if (!customerProcessingClient.sendMessageToCustomerProcessing()) {
            log.info("Failed to send message to customerProcessingClient for customer: {} ", customer.getCustomerNr());
            throw (new InternalException("DCM 101: customerProcessingClient not responsive"));
        }

        return matterResponse;
    }

    // Submethods
    private Charge createNewCharge(MatterRequest matterRequest, VehicleTypeEnum vehicleTypeConfirmed, Customer customer) {
        Charge newCharge = new Charge();
        newCharge.setChargeStatus(ChargeStatus.BOOKED);
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
        newMatter.setCharge(charge);
        return newMatter;
    }

}
