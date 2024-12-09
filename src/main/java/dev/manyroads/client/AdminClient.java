package dev.manyroads.client;

import dev.manyroads.model.entity.Charge;
import dev.manyroads.model.entity.Matter;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "Decom-Admin", url = "http://localhost:7090")
public interface AdminClient {

    @GetMapping("/vehicles/{matterNr}")
    String searchVehicleType(@PathVariable("matterNr") String matterNr);

    @GetMapping("/vehicles")
    String searchVehicleType();

    @PostMapping("/terminate-matter")
    String terminateMatter(@RequestBody Matter matter);

    @PostMapping("/applied")
    String startDCMApplied(@RequestBody Charge charge);

    @PostMapping("/executable")
    String startExecutable(@RequestBody Charge charge);


}
