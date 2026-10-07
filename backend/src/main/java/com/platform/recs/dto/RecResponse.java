package com.platform.recs.dto;

import com.platform.recs.entity.Rec;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RecResponse(
    Long id,
    String recCode,
    Long assetId,
    String assetCode,
    String energySource,
    Integer vintageYear,
    BigDecimal energyQuantityMwh,
    Integer certificateQuantity,
    String status,
    Long ownerId,
    String ownerName,
    LocalDateTime listedAt,
    LocalDateTime transferredAt,
    LocalDateTime retiredAt,
    LocalDateTime createdAt
) {
    public static RecResponse from(Rec rec) {
        return new RecResponse(
            rec.getId(),
            rec.getRecCode(),
            rec.getAsset().getId(),
            rec.getAsset().getAssetCode(),
            rec.getEnergySource().name(),
            rec.getVintageYear(),
            rec.getEnergyQuantityMwh(),
            rec.getCertificateQuantity(),
            rec.getStatus().name(),
            rec.getOwner().getId(),
            rec.getOwner().getFullName(),
            rec.getListedAt(),
            rec.getTransferredAt(),
            rec.getRetiredAt(),
            rec.getCreatedAt()
        );
    }
}
