package com.platform.recs.service;

import com.platform.recs.enumtype.RecStatus;
import com.platform.recs.enumtype.RoleName;
import com.platform.recs.exception.BadRequestException;
import com.platform.recs.exception.ForbiddenOperationException;
import org.springframework.stereotype.Service;

@Service
public class RecWorkflowService {
    public void validateTransition(RecStatus current, RecStatus next, RoleName role) {
        if (current == RecStatus.ISSUED && next == RecStatus.LISTED) {
            if (role != RoleName.GENERATOR && role != RoleName.ADMIN) throw new ForbiddenOperationException("Only GENERATOR or ADMIN can list a REC");
            return;
        }
        if (current == RecStatus.LISTED && next == RecStatus.TRANSFERRED) {
            if (role != RoleName.BUYER && role != RoleName.ADMIN) throw new ForbiddenOperationException("Only BUYER or ADMIN can purchase a listed REC");
            return;
        }
        if (current == RecStatus.TRANSFERRED && next == RecStatus.RETIRED) {
            if (role != RoleName.BUYER && role != RoleName.ADMIN) throw new ForbiddenOperationException("Only BUYER or ADMIN can retire a REC");
            return;
        }
        throw new BadRequestException("Invalid REC status transition from " + current + " to " + next);
    }
}
