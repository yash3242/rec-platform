package com.platform.recs.dto;

import com.platform.recs.entity.GenerationLog;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record GenerationLogResponse(
    Long id,
    Long assetId,
    String assetCode,
    LocalDate generationDate,
    String energySource,
    BigDecimal energyQuantityMwh,
    Integer vintageYear,
    String status,
    Long createdById,
    String createdByName,
    LocalDateTime createdAt
) {
    public static GenerationLogResponse from(GenerationLog log) {
        return new GenerationLogResponse(
            log.getId(),
            log.getAsset().getId(),
            log.getAsset().getAssetCode(),
            log.getGenerationDate(),
            log.getEnergySource().name(),
            log.getEnergyQuantityMwh(),
            log.getVintageYear(),
            log.getStatus().name(),
            log.getCreatedBy().getId(),
            log.getCreatedBy().getFullName(),
            log.getCreatedAt()
        );
    }
}
