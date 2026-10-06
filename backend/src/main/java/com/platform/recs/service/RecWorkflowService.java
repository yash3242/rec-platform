package com.platform.recs.service;

import com.platform.recs.enumtype.RecStatus;
import com.platform.recs.enumtype.RoleName;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Service
public class RecWorkflowService {
    private static final Map<RecStatus, Map<RecStatus, Set<RoleName>>> TRANSITIONS = new EnumMap<>(RecStatus.class);

    static {
        TRANSITIONS.put(RecStatus.CREATED, Map.of(RecStatus.SUBMITTED, EnumSet.of(RoleName.PRODUCER, RoleName.ADMIN)));
        TRANSITIONS.put(RecStatus.SUBMITTED, Map.of(RecStatus.UNDER_REVIEW, EnumSet.of(RoleName.REVIEWER, RoleName.ADMIN)));
        TRANSITIONS.put(RecStatus.UNDER_REVIEW, Map.of(
            RecStatus.APPROVED, EnumSet.of(RoleName.REVIEWER, RoleName.ADMIN),
            RecStatus.REJECTED, EnumSet.of(RoleName.REVIEWER, RoleName.ADMIN)
        ));
        TRANSITIONS.put(RecStatus.APPROVED, Map.of(RecStatus.ISSUED, EnumSet.of(RoleName.MANAGER, RoleName.ADMIN)));
        TRANSITIONS.put(RecStatus.ISSUED, Map.of(RecStatus.RETIRED, EnumSet.of(RoleName.MANAGER, RoleName.ADMIN)));
        TRANSITIONS.put(RecStatus.REJECTED, Map.of(RecStatus.CREATED, EnumSet.of(RoleName.PRODUCER, RoleName.ADMIN)));
    }

    public void validate(RecStatus current, RecStatus next, RoleName role) {
        Map<RecStatus, Set<RoleName>> nextMap = TRANSITIONS.getOrDefault(current, Map.of());
        Set<RoleName> allowedRoles = nextMap.get(next);
        if (allowedRoles == null) {
            throw new com.platform.recs.exception.BadRequestException("Invalid status transition from " + current + " to " + next);
        }
        if (!allowedRoles.contains(role)) {
            throw new com.platform.recs.exception.ForbiddenOperationException("Role " + role + " cannot move from " + current + " to " + next);
        }
    }
}
