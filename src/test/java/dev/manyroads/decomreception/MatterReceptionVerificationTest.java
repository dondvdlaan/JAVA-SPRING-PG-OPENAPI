package dev.manyroads.decomreception;

import dev.manyroads.execinterrup.exception.CustomerNrIsMissingException;
import dev.manyroads.execinterrup.exception.ExecInterrupEmptyOrNullException;
import dev.manyroads.execinterrup.exception.ExecInterrupTypeMissingException;
import dev.manyroads.model.ExecInterrupEnum;
import dev.manyroads.model.ExecInterrupRequest;
import dev.manyroads.model.MatterRequest;
import dev.manyroads.verification.Verification;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.mock.web.MockHttpServletRequest;

import java.net.http.HttpHeaders;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class MatterReceptionVerificationTest {

    static Stream<ExecInterrupEnum> differentTypes() {
        return Arrays.stream(ExecInterrupEnum.values());
    }

    Verification verification;

    @BeforeEach
    void setUp() {
        verification = new Verification();
    }

    @Disabled
    @ParameterizedTest
    @MethodSource("differentTypes")
    void execInterrupTypeIsNullAnCustomerIDIsNull(ExecInterrupEnum execInterrupEnum) {
        Long customerNr = (long) (Math.random() * 999999);
        ExecInterrupRequest customerNrAndTypeNullExecInterrupRequest = new ExecInterrupRequest();
        customerNrAndTypeNullExecInterrupRequest.setCustomerNr(null);
        customerNrAndTypeNullExecInterrupRequest.setExecInterrupType(null);

        // Verify
        assertThatThrownBy(() -> verification.verifyExecInterrupRequest(customerNrAndTypeNullExecInterrupRequest))
                .isInstanceOf(CustomerNrIsMissingException.class);
    }

    @Disabled
    @ParameterizedTest
    @MethodSource("differentTypes")
    void execInterrupIsNull(ExecInterrupEnum execInterrupEnum) {

        Long customerNr = (long) (Math.random() * 999999);

        ExecInterrupRequest nullExecInterrupRequest = null;

        // Verify
        assertThatThrownBy(() -> verification.verifyExecInterrupRequest(nullExecInterrupRequest))
                .isInstanceOf(ExecInterrupEmptyOrNullException.class);
    }

    @Disabled
    @ParameterizedTest
    @MethodSource("differentTypes")
    void execInterrupTypeIsNull(ExecInterrupEnum execInterrupEnum) {
        Long customerNr = (long) (Math.random() * 999999);
        ExecInterrupRequest noExecInerrupTypeExecInterrupRequest = new ExecInterrupRequest();
        noExecInerrupTypeExecInterrupRequest.setCustomerNr(customerNr);
        noExecInerrupTypeExecInterrupRequest.setExecInterrupType(null);

        // Verify
        assertThatThrownBy(() -> verification.verifyExecInterrupRequest(noExecInerrupTypeExecInterrupRequest))
                .isInstanceOf(ExecInterrupTypeMissingException.class);
    }

    @Disabled
    @ParameterizedTest
    @MethodSource("differentTypes")
    void execInterrupCustomerNrIsNull(ExecInterrupEnum execInterrupEnum) {

        ExecInterrupRequest noCustomerNrExecInterrupRequest = new ExecInterrupRequest();
        noCustomerNrExecInterrupRequest.setCustomerNr(null);
        noCustomerNrExecInterrupRequest.setExecInterrupType(execInterrupEnum);

        // Verify
        assertThatThrownBy(() -> verification.verifyExecInterrupRequest(noCustomerNrExecInterrupRequest))
                .isInstanceOf(CustomerNrIsMissingException.class);
    }

    @Test
    void checkHappyFlowMatterReceptionVerificationTest() {
        Long customerNr = (long) (Math.random() * 999999);
        String matterNr = "121212";
        MatterRequest matterRequest = new MatterRequest();
        matterRequest.setMatterNr(matterNr);
        matterRequest.setCustomerNr(customerNr);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Termination-Call-Back-Url", "/v1/terminate-matter/");

        // Verify
        Assertions.assertDoesNotThrow(() -> verification.verifyMatterRequest(matterRequest, request));
    }

}
