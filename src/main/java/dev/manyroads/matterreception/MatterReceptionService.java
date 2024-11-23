package dev.manyroads.matterreception;

import dev.manyroads.client.AdminClient;
import dev.manyroads.matterreception.exception.AdminClientException;
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

    public MatterResponse processIncomingMatterRequest(MatterRequest matterRequest) {

        MatterResponse matterResponse = new MatterResponse();
        matterResponse.setCustomerNr(matterRequest.getCustomerNr());

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
        Charge Charge = chargeRepository.findByCustomerNrAndChargeStatus(
                ChargeStatus.APPLIED,
                ChargeStatus.BOOKED,
                matterRequest.getCustomerNr());
        Optional.ofNullable(Charge).ifPresentOrElse(
                (c) -> {
                    log.info("Existing charge found for customer nr: {}", c.getCustomerNr());
                    if (c.getVehicleType().equals(vehicleTypeConfirmed)) {
                        log.info("Vehicle type coincides, matter added to existing charge: {}", c.getChargeID());
                        Matter newMatter = mapMatterRequest(matterRequest, c);
                        matterRepository.save(newMatter);
                        c.getMatters().add(newMatter);
                        chargeRepository.save(c);
                        matterResponse.setChargeID(c.getChargeID());
                    } else {
                        Charge savedNewCharge = createNewCharge(matterRequest, vehicleTypeConfirmed, customer);
                        matterResponse.setChargeID(savedNewCharge.getChargeID());
                        log.info("New charge created {} for existing customer nr: {}", savedNewCharge.getChargeID(), matterRequest.getCustomerNr());
                    }
                },
                () -> {
                    Charge savedNewCharge = createNewCharge(matterRequest, vehicleTypeConfirmed, customer);
                    matterResponse.setChargeID(savedNewCharge.getChargeID());
                    log.info("New charge created {} for new customer nr: {}", savedNewCharge.getChargeID(), savedNewCharge.getCustomer().getCustomerID());
                }
        );
        return matterResponse;
    }

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
