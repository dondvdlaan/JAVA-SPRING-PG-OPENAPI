package dev.manyroads.client;

import dev.manyroads.model.VehicleTypeEnum;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("Decom-Admin")
public interface AdminClient {

    @GetMapping("/vehicles/{caseID}")
    String searchVehicleType(@PathVariable("caseID") String caseID);

}
