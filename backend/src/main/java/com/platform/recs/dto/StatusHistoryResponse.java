package com.platform.recs.dto;

import com.platform.recs.entity.StatusHistory;
import java.time.LocalDateTime;

public record StatusHistoryResponse(
    Long id,
    String resourceType,
    Long resourceId,
    String oldStatus,
    String newStatus,
    Long changedById,
    String changedByName,
    String comment,
    LocalDateTime changedAt
) {
    public static StatusHistoryResponse from(StatusHistory h) {
        return new StatusHistoryResponse(h.getId(), h.getResourceType(), h.getResourceId(), h.getOldStatus(), h.getNewStatus(), h.getChangedBy().getId(), h.getChangedBy().getFullName(), h.getComment(), h.getChangedAt());
    }
}
