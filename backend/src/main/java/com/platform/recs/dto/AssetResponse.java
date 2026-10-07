package com.platform.recs.dto;

import com.platform.recs.entity.Asset;
import java.time.LocalDateTime;

public record AssetResponse(
    Long id,
    String assetCode,
    String name,
    String energySource,
    String location,
    Double capacityMw,
    String status,
    Long ownerId,
    String ownerName,
    LocalDateTime createdAt
) {
    public static AssetResponse from(Asset asset) {
        return new AssetResponse(
            asset.getId(),
            asset.getAssetCode(),
            asset.getName(),
            asset.getEnergySource().name(),
            asset.getLocation(),
            asset.getCapacityMw(),
            asset.getStatus().name(),
            asset.getOwner().getId(),
            asset.getOwner().getFullName(),
            asset.getCreatedAt()
        );
    }
}
