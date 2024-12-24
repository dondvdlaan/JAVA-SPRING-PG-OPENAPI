package dev.manyroads.client;

import dev.manyroads.decomreception.exception.InternalException;
import dev.manyroads.model.entity.Charge;
import dev.manyroads.model.messages.CustomerProcessingClientMessage;
import dev.manyroads.model.messages.MatterMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientService {
    CustomerProcessingClient customerProcessingClient;


}
