package dev.manyroads.matterreception;

import dev.manyroads.client.AdminClient;
import dev.manyroads.client.CustomerProcessingClient;
import dev.manyroads.decomreception.exception.AdminClientException;
import dev.manyroads.decomreception.exception.InternalTechnicalException;
import dev.manyroads.matterreception.exception.CustomerNotFoundException;
import dev.manyroads.matterreception.exception.NoChargesFoundForCustomerException;
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
import dev.manyroads.model.messages.CustomerProcessingClientMessage;
import dev.manyroads.model.messages.MatterMessage;
import dev.manyroads.model.repository.ChargeRepository;
import dev.manyroads.model.repository.CustomerRepository;
import dev.manyroads.model.repository.MatterRepository;
import dev.manyroads.scheduler.SchedulerService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static dev.manyroads.model.ChargeStatusEnum.BOOKED;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatterReceptionService {

    private final AdminClient adminClient;
    private final CustomerRepository customerRepository;
    private final ChargeRepository chargeRepository;
    private final MatterRepository matterRepository;
    private final CustomerProcessingClient customerProcessingClient;
    private final SchedulerService schedulerService;

    public MatterResponse processIncomingMatterRequest(MatterRequest matterRequest) {
        log.info("processIncomingMatterRequest: started to process incoming matter Request");
        MatterResponse matterResponse = new MatterResponse();
        matterResponse.setCustomerNr(matterRequest.getCustomerNr());
        Charge charge = new Charge();

        // Retrieve vehicle type from admin microservice
        String vehicleType = retrieveVehicleType(matterRequest.getMatterNr());

        // Verify vehicle type is in DCM domain
        VehicleTypeEnum vehicleTypeConfirmed;
        try {
            vehicleTypeConfirmed = VehicleTypeEnum.fromValue(vehicleType);
        } catch (IllegalArgumentException ex) {
            throw new VehicleTypeNotCoincideWithDomainException();
        }

        // Check if customer exists, otherwise create new account and save
        Customer customer = customerRepository.findByCustomerNr(matterRequest.getCustomerNr())
                .orElseGet(
                        () -> {
                            log.info("Optional.ofNullable(oCustomer): creating new customer");
                            Customer newCustomer = new Customer();
                            newCustomer.setCustomerNr(matterRequest.getCustomerNr());
                            Customer savedCustomer = customerRepository.save(newCustomer);
                            log.info(String.format("New customer with ID: %s is saved", savedCustomer.getCustomerID()));
                            return savedCustomer;
                        });

        log.info("findByCustomerNrAndChargeStatus");
        Optional<List<Charge>> oListCharges = chargeRepository.findByCustomerNrAndChargeStatus(
                ChargeStatusEnum.DCM_APPLIED,
                BOOKED,
                matterRequest.getCustomerNr());
        // Check if charges for customer exist
        if (oListCharges.isPresent() && !oListCharges.get().isEmpty()) {
            log.info("oListCharges.isPresent() : {}", oListCharges.get());
            List<Charge> listChargesSameVehicleType = oListCharges.get().stream()
                    .filter(c -> c.getVehicleType().equals(vehicleTypeConfirmed)).toList();
            // Check if matter can be added
            if (!listChargesSameVehicleType.isEmpty()) {
                log.info("!listChargesSameVehicleType.isEmpty(): matter {} to be added to existing charge: {}", matterRequest.getMatterNr(), charge.getChargeID());
                charge = listChargesSameVehicleType.get(0);
                Matter newMatter = mapMatterRequest(matterRequest);
                matterRepository.save(newMatter);
                charge.getMatters().add(newMatter);
                chargeRepository.save(charge);
                customer.getCharges().add(charge);
                customerRepository.save(customer);
                log.info("Vehicle type coincides, matter added to existing charge: {}", charge.getChargeID());
            } else {
                log.info("Create new charge for new vehicle type existing customer");
                charge = createNewCharge(matterRequest, vehicleTypeConfirmed, customer);
                log.info("New charge created {} for existing customer nr: {}", charge.getChargeID(), customer.getCustomerNr());
            }
            // Creating new charge
        } else {
            log.info("About to createNewCharge for new customer: matterRequest {}, vehicleTypeConfirmed {}, customer {}",
                    matterRequest, vehicleTypeConfirmed, customer);
            charge = createNewCharge(matterRequest, vehicleTypeConfirmed, customer);
            log.info("New charge created {} for new customer nr: {}", charge.getChargeID(), customer.getCustomerNr());
        }
        matterResponse.setChargeID(charge.getChargeID());

        // Start customer standby period
        if (!customer.isStandByFlag()) {
            log.info("isStandByFlag(): Customer {} in standby", customer.getCustomerNr());
            customer.setStandByFlag(true);
            customerRepository.save(customer);

            // kick off customer scheduler
            schedulerService.scheduleCustomerStandby(customer.getCustomerNr());
        }
        return matterResponse;
    }

    public void sendCustomerDataToCustomerProcessing(long customerNr) {
        log.info("sendCustomerDataToCustomerProcessing: starts sending customer charges to customer processing client");

        Optional<List<Charge>> oCharges = chargeRepository.findByCustomerNrAndChargeStatuss(customerNr, BOOKED);
        if (oCharges.isPresent() && oCharges.get().isEmpty()) throw new NoChargesFoundForCustomerException(customerNr);

        oCharges.ifPresent(c -> c.forEach(charge -> {
            log.info("forEach(charge -> processing customer: {}", charge.getCustomer().getCustomerNr());
            var message = convertToCustomerProcessingClientMessage(charge);
            // Pass on data to customer processing
            if (!customerProcessingClient.sendMessageToCustomerProcessing(message)) {
                log.info("Failed to send message to customerProcessingClient for customer: {} ", customerNr);
                throw (new InternalTechnicalException("DCM 101: customerProcessingClient not responsive"));
            }
        }));
        // Reset customer standby flag
        Optional<Customer> oProcessedCustomer = customerRepository.findByCustomerNr(customerNr);
        if (oProcessedCustomer.isPresent()) {
            oProcessedCustomer.get().setStandByFlag(false);
            customerRepository.save(oProcessedCustomer.get());
        } else throw new CustomerNotFoundException(customerNr);
    }

    // Sub methods
    private static CustomerProcessingClientMessage convertToCustomerProcessingClientMessage(Charge charge) {
        log.info("convertToCustomerProcessingClientMessage: charge.getCustomerNr() {}", charge.getCustomer().getCustomerNr());
        List<MatterMessage> listMatterMessage = new ArrayList<>();
        charge.getMatters().forEach(matter -> {
            MatterMessage matterMessage = new MatterMessage(matter.getMatterNr(), matter.getMatterStatus());
            listMatterMessage.add(matterMessage);
        });
        return new CustomerProcessingClientMessage(charge.getChargeID(), listMatterMessage);
    }

    private Charge createNewCharge(MatterRequest matterRequest, VehicleTypeEnum vehicleTypeConfirmed, Customer customer) {
        Matter newMatter = mapMatterRequest(matterRequest);
        matterRepository.save(newMatter);
        Charge newCharge = new Charge();
        newCharge.setChargeStatus(BOOKED);
        newCharge.setVehicleType(vehicleTypeConfirmed);
        //newCharge.setCustomer(customer);
        newCharge.getMatters().add(newMatter);
        chargeRepository.save(newCharge);
        System.out.println("customer.getCharge(): " + customer.getCharges());
        customer.getCharges().add(newCharge);
        customerRepository.save(customer);
        return newCharge;
    }

    private String retrieveVehicleType(String matterID) {
        String vehicleType;
        try {
            Optional<String> oVehicleType = getVehicleTypeEnum(matterID);
            oVehicleType.orElseThrow(VehicleTypeNotFoundException::new);
            vehicleType = oVehicleType.get();
        } catch (FeignException ex) {
            log.info("FeignException: {}", ex.getMessage());
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
        log.info("getVehicleTypeEnum: sending to adminClient.searchVehicleType ");
        return Optional.ofNullable(adminClient.searchVehicleType(matterID));
    }

    private Matter mapMatterRequest(MatterRequest matterRequest) {
        Matter newMatter = new Matter();
        newMatter.setMatterNr(matterRequest.getMatterNr());
        newMatter.setMatterStatus(MatterStatus.EXECUTABLE);
        newMatter.setTerminationCallBackUrl(matterRequest.getCallback().getTerminationCallBackUrl());
        return newMatter;
    }
}
