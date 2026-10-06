package com.platform.recs;

import com.platform.recs.enumtype.RecStatus;
import com.platform.recs.enumtype.RoleName;
import com.platform.recs.service.RecWorkflowService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RecWorkflowServiceTest {
    private final RecWorkflowService service = new RecWorkflowService();

    @Test
    void allowsValidTransition() {
        assertDoesNotThrow(() -> service.validate(RecStatus.CREATED, RecStatus.SUBMITTED, RoleName.PRODUCER));
    }

    @Test
    void rejectsUnauthorizedTransition() {
        assertThrows(RuntimeException.class, () -> service.validate(RecStatus.CREATED, RecStatus.SUBMITTED, RoleName.REVIEWER));
    }

    @Test
    void rejectsInvalidTransition() {
        assertThrows(RuntimeException.class, () -> service.validate(RecStatus.CREATED, RecStatus.APPROVED, RoleName.PRODUCER));
    }
}
