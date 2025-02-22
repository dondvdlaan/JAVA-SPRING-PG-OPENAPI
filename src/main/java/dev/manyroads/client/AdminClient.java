package dev.manyroads.client;

import dev.manyroads.client.configfeign.CustomFeignDecoder;
import dev.manyroads.model.entity.Charge;
import dev.manyroads.model.messages.ChargeMessage;
import dev.manyroads.model.messages.MatterMessage;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "Decom-Admin",
        url = "http://localhost:7090",
        configuration = {CustomFeignDecoder.class})
public interface AdminClient {

    @GetMapping("/vehicles/{matterNr}")
    String searchVehicleType(@PathVariable("matterNr") String matterNr);

    @PostMapping("/terminate-matter")
    String terminateMatter(@RequestBody MatterMessage matterMessage);

    @PostMapping("/applied")
    String startDCMApplied(@RequestBody ChargeMessage chargeMessage);

    @PostMapping("/executable")
    String startExecutable(@RequestBody Charge charge);


}
