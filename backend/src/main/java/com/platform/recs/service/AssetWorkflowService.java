package com.platform.recs.service;

import com.platform.recs.enumtype.AssetStatus;
import com.platform.recs.enumtype.RoleName;
import com.platform.recs.exception.BadRequestException;
import com.platform.recs.exception.ForbiddenOperationException;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

@Service
public class AssetWorkflowService {
    private static final Map<AssetStatus, Set<RoleName>> VERIFY_ROLES = Map.of(
        AssetStatus.PENDING_VERIFICATION, Set.of(RoleName.ADMIN)
    );

    private static final Map<AssetStatus, Set<RoleName>> SUSPEND_ROLES = Map.of(
        AssetStatus.ACTIVE, Set.of(RoleName.ADMIN)
    );

    public void validateTransition(AssetStatus current, AssetStatus next, RoleName role) {
        if (current == AssetStatus.PENDING_VERIFICATION && next == AssetStatus.ACTIVE) {
            if (!VERIFY_ROLES.get(AssetStatus.PENDING_VERIFICATION).contains(role)) {
                throw new ForbiddenOperationException("Only ADMIN can activate an asset");
            }
            return;
        }
        if (current == AssetStatus.ACTIVE && next == AssetStatus.SUSPENDED) {
            if (!SUSPEND_ROLES.get(AssetStatus.ACTIVE).contains(role)) {
                throw new ForbiddenOperationException("Only ADMIN can suspend an asset");
            }
            return;
        }
        throw new BadRequestException("Invalid asset status transition from " + current + " to " + next);
    }
}
