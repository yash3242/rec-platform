package com.platform.recs.dto;

import com.platform.recs.entity.RenewableEnergyCertificate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record RecResponse(
    Long id,
    String recCode,
    Long producerId,
    String producerName,
    String energySource,
    LocalDate generationStartDate,
    LocalDate generationEndDate,
    BigDecimal energyQuantityMwh,
    Integer certificateQuantity,
    String status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static RecResponse from(RenewableEnergyCertificate rec) {
        return new RecResponse(
            rec.getId(),
            rec.getRecCode(),
            rec.getProducer().getId(),
            rec.getProducer().getFullName(),
            rec.getEnergySource().name(),
            rec.getGenerationStartDate(),
            rec.getGenerationEndDate(),
            rec.getEnergyQuantityMwh(),
            rec.getCertificateQuantity(),
            rec.getStatus().name(),
            rec.getCreatedAt(),
            rec.getUpdatedAt()
        );
    }
}
