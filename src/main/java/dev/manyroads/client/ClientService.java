package dev.manyroads.client;

import dev.manyroads.decomreception.exception.InternalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientService {
    CustomerProcessingClient customerProcessingClient;

    public void sendCustomerDataToCustomerProcessing(int customerNr){

        // Pass on data to customer processing
        if (!customerProcessingClient.sendMessageToCustomerProcessing(getCustomerProcessingClientMessage(charge))) {
            log.info("Failed to send message to customerProcessingClient for customer: {} ", customer.getCustomerNr());
            throw (new InternalException("DCM 101: customerProcessingClient not responsive"));
        }
    }
}
