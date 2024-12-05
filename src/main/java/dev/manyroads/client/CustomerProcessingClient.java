package dev.manyroads.client;

import dev.manyroads.model.entity.Charge;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@AllArgsConstructor
@Slf4j
public class CustomerProcessingClient extends RESTConnector {

public boolean sendMessageToCustomerProcessing(Charge charge){


    return true;
}

}
