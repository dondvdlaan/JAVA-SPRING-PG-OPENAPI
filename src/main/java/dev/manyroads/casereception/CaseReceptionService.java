package dev.manyroads.casereception;

import dev.manyroads.client.AdminClient;
import dev.manyroads.exception.AdminClientException;
import dev.manyroads.exception.VehicleTypeNotCoincideWithDomainException;
import dev.manyroads.exception.VehicleTypeNotFoundException;
import dev.manyroads.model.CaseRequest;
import dev.manyroads.model.CaseResponse;
import dev.manyroads.model.VehicleTypeEnum;
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

    public CaseResponse processIncomingCaseRequest(CaseRequest caseRequest) {

        // Retrieve vehicle type from admin microservice
        CaseResponse caseResponse = new CaseResponse();
        String vehicleType = retrieveVehicleType(caseRequest.getCaseID());

        // Verify vehicle type in DCM domain
        VehicleTypeEnum vehicleTypeConfirmed;
        try{
            vehicleTypeConfirmed = VehicleTypeEnum.fromValue(vehicleType);
        }catch(IllegalArgumentException ex){
            throw new VehicleTypeNotCoincideWithDomainException();
        }

        caseResponse.setVehicleType(vehicleTypeConfirmed);

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
     * @param caseID
     * @return
     */
    private Optional<String> getVehicleTypeEnum(String caseID) {
        return Optional.ofNullable(adminClient.searchVehicleType(caseID));
    }
}
