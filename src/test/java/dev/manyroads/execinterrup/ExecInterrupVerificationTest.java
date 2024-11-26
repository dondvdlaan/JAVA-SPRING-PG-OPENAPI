package dev.manyroads.execinterrup;

import dev.manyroads.execinterrup.exception.CustomerNrIsMissingException;
import dev.manyroads.execinterrup.exception.ExecInterrupEmptyOrNullException;
import dev.manyroads.execinterrup.exception.ExecInterrupTypeMissingException;
import dev.manyroads.model.ExecInterrupEnum;
import dev.manyroads.model.ExecInterrupRequest;
import dev.manyroads.verification.Verification;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class ExecInterrupVerificationTest {

    static Stream<ExecInterrupEnum> differentTypes() {
        return Arrays.stream(ExecInterrupEnum.values());
    }

    Verification verification;

    @BeforeEach
    void setUp() {
        verification = new Verification();

    }

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

    @ParameterizedTest
    @MethodSource("differentTypes")
    void execInterrupIsNull(ExecInterrupEnum execInterrupEnum) {

        Long customerNr = (long) (Math.random() * 999999);

        ExecInterrupRequest nullExecInterrupRequest = null;

        // Verify
        assertThatThrownBy(() -> verification.verifyExecInterrupRequest(nullExecInterrupRequest))
                .isInstanceOf(ExecInterrupEmptyOrNullException.class);
    }

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

    @ParameterizedTest
    @MethodSource("differentTypes")
    void checkHappyFlowExecInterruptTest(ExecInterrupEnum execInterrupEnum) {
        Long customerNr = (long) (Math.random() * 999999);
        ExecInterrupRequest correctExecInterrupRequest = new ExecInterrupRequest();
        correctExecInterrupRequest.setCustomerNr(customerNr);
        correctExecInterrupRequest.setExecInterrupType(execInterrupEnum);

        // Verify
        Assertions.assertDoesNotThrow(() -> verification.verifyExecInterrupRequest(correctExecInterrupRequest));
    }

}
