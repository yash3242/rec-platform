package com.platform.recs.repository;

import com.platform.recs.entity.Asset;
import com.platform.recs.enumtype.AssetStatus;
import com.platform.recs.enumtype.EnergySource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AssetRepository extends JpaRepository<Asset, Long> {
    boolean existsByAssetCode(String assetCode);
    Optional<Asset> findByAssetCode(String assetCode);
    Page<Asset> findByOwnerId(Long ownerId, Pageable pageable);
    Page<Asset> findByStatus(AssetStatus status, Pageable pageable);
    Page<Asset> findByEnergySource(EnergySource energySource, Pageable pageable);
    Page<Asset> findByEnergySourceAndStatus(EnergySource energySource, AssetStatus status, Pageable pageable);
    long countByStatus(AssetStatus status);
    long countByOwnerId(Long ownerId);
}
