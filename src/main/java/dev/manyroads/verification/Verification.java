package dev.manyroads.verification;

import dev.manyroads.matterreception.exception.MatterIDIsMissingException;
import dev.manyroads.matterreception.exception.MatterRequestEmptyOrNullException;
import dev.manyroads.matterreception.exception.PersonIDIsMissingException;
import dev.manyroads.model.MatterRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class Verification {

    /**
     * Verify incoming request case on null or empty object/fields, if erroneous send back a 400 BAD REQUEST to
     * the requesting microservice
     * @param matterRequest
     */
    public void verifyMatterRequest(MatterRequest matterRequest) {
        Optional.ofNullable(matterRequest)
                .orElseThrow(MatterRequestEmptyOrNullException::new);
        Optional.ofNullable(matterRequest.getCustomerNr())
                .orElseThrow(PersonIDIsMissingException::new);
        Optional.ofNullable(matterRequest.getMatterID())
                .orElseThrow(MatterIDIsMissingException::new);
    }


}
