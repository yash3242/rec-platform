package com.platform.recs;

import com.platform.recs.enumtype.AssetStatus;
import com.platform.recs.enumtype.GenerationLogStatus;
import com.platform.recs.enumtype.RecStatus;
import com.platform.recs.enumtype.RoleName;
import com.platform.recs.service.AssetWorkflowService;
import com.platform.recs.service.GenerationLogWorkflowService;
import com.platform.recs.service.RecWorkflowService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WorkflowServiceTest {
    private final AssetWorkflowService assetWorkflowService = new AssetWorkflowService();
    private final GenerationLogWorkflowService logWorkflowService = new GenerationLogWorkflowService();
    private final RecWorkflowService recWorkflowService = new RecWorkflowService();

    @Test
    void assetWorkflow() {
        assertDoesNotThrow(() -> assetWorkflowService.validateTransition(AssetStatus.PENDING_VERIFICATION, AssetStatus.ACTIVE, RoleName.ADMIN));
        assertDoesNotThrow(() -> assetWorkflowService.validateTransition(AssetStatus.ACTIVE, AssetStatus.SUSPENDED, RoleName.ADMIN));
        assertThrows(RuntimeException.class, () -> assetWorkflowService.validateTransition(AssetStatus.PENDING_VERIFICATION, AssetStatus.ACTIVE, RoleName.GENERATOR));
    }

    @Test
    void generationLogWorkflow() {
        assertDoesNotThrow(() -> logWorkflowService.validateTransition(GenerationLogStatus.SUBMITTED, GenerationLogStatus.VERIFIED, RoleName.ADMIN));
        assertDoesNotThrow(() -> logWorkflowService.validateTransition(GenerationLogStatus.VERIFIED, GenerationLogStatus.MINTED, RoleName.ADMIN));
        assertThrows(RuntimeException.class, () -> logWorkflowService.validateTransition(GenerationLogStatus.SUBMITTED, GenerationLogStatus.MINTED, RoleName.GENERATOR));
    }

    @Test
    void recWorkflow() {
        assertDoesNotThrow(() -> recWorkflowService.validateTransition(RecStatus.ISSUED, RecStatus.LISTED, RoleName.GENERATOR));
        assertDoesNotThrow(() -> recWorkflowService.validateTransition(RecStatus.LISTED, RecStatus.TRANSFERRED, RoleName.BUYER));
        assertDoesNotThrow(() -> recWorkflowService.validateTransition(RecStatus.TRANSFERRED, RecStatus.RETIRED, RoleName.BUYER));
        assertThrows(RuntimeException.class, () -> recWorkflowService.validateTransition(RecStatus.ISSUED, RecStatus.RETIRED, RoleName.BUYER));
    }
}
