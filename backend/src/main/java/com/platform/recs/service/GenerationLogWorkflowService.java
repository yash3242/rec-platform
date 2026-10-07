package com.platform.recs.service;

import com.platform.recs.enumtype.GenerationLogStatus;
import com.platform.recs.enumtype.RoleName;
import com.platform.recs.exception.BadRequestException;
import com.platform.recs.exception.ForbiddenOperationException;
import org.springframework.stereotype.Service;

@Service
public class GenerationLogWorkflowService {
    public void validateTransition(GenerationLogStatus current, GenerationLogStatus next, RoleName role) {
        if (current == GenerationLogStatus.SUBMITTED && next == GenerationLogStatus.VERIFIED) {
            if (role != RoleName.ADMIN) throw new ForbiddenOperationException("Only ADMIN can verify a generation log");
            return;
        }
        if (current == GenerationLogStatus.SUBMITTED && next == GenerationLogStatus.REJECTED) {
            if (role != RoleName.ADMIN) throw new ForbiddenOperationException("Only ADMIN can reject a generation log");
            return;
        }
        if (current == GenerationLogStatus.VERIFIED && next == GenerationLogStatus.MINTED) {
            if (role != RoleName.ADMIN) throw new ForbiddenOperationException("Only ADMIN can mint a REC");
            return;
        }
        throw new BadRequestException("Invalid generation log status transition from " + current + " to " + next);
    }
}
