package com.platform.recs.dto;

import com.platform.recs.entity.RecStatusHistory;
import java.time.LocalDateTime;

public record StatusHistoryResponse(
    Long id,
    String oldStatus,
    String newStatus,
    Long changedById,
    String changedByName,
    String comment,
    LocalDateTime changedAt
) {
    public static StatusHistoryResponse from(RecStatusHistory history) {
        return new StatusHistoryResponse(
            history.getId(),
            history.getOldStatus() == null ? null : history.getOldStatus().name(),
            history.getNewStatus().name(),
            history.getChangedBy().getId(),
            history.getChangedBy().getFullName(),
            history.getComment(),
            history.getChangedAt()
        );
    }
}
