package dev.manyroads.casereception;

import dev.manyroads.client.AdminClient;
import dev.manyroads.exception.AdminClientException;
import dev.manyroads.exception.VehicleTypeNotFoundException;
import dev.manyroads.model.CaseRequest;
import dev.manyroads.model.CaseResponse;
import dev.manyroads.model.VehicleTypeEnum;
import feign.FeignException;
import lombok.AllArgsConstructor;
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

        CaseResponse caseResponse = new CaseResponse();
        VehicleTypeEnum vehicleType = retrieveVehicleType(caseRequest.getCaseID());
        caseResponse.setVehicleType(vehicleType);

        return caseResponse;
    }

    private VehicleTypeEnum retrieveVehicleType(String caseID) {

        VehicleTypeEnum vehicleType = null;
        try {
            Optional<VehicleTypeEnum> oVehicleType = getVehicleTypeEnum(caseID);
            oVehicleType.orElseThrow(VehicleTypeNotFoundException::new);
            vehicleType = oVehicleType.get();
        } catch (FeignException ex) {
            throw new AdminClientException();
        }
        return vehicleType;
    }

    private Optional<VehicleTypeEnum> getVehicleTypeEnum(String caseID) {
        return Optional.ofNullable(adminClient.searchVehicleType(caseID));
    }
}
