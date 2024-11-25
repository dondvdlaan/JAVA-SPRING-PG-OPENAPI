package dev.manyroads.client;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@AllArgsConstructor
@Slf4j
public class CustomerProcessingClient extends RESTConnector {

public boolean sendMessageToCustomerProcessing(){

    return true;
}

}
