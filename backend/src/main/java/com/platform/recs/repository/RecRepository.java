package com.platform.recs.repository;

import com.platform.recs.entity.Rec;
import com.platform.recs.enumtype.EnergySource;
import com.platform.recs.enumtype.RecStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecRepository extends JpaRepository<Rec, Long> {
    boolean existsByRecCode(String recCode);
    Page<Rec> findByOwnerId(Long ownerId, Pageable pageable);
    Page<Rec> findByStatus(RecStatus status, Pageable pageable);
    long countByStatus(RecStatus status);
    long countByOwnerId(Long ownerId);
    Page<Rec> findByEnergySource(EnergySource energySource, Pageable pageable);
    Page<Rec> findByVintageYear(Integer vintageYear, Pageable pageable);
    Page<Rec> findByEnergySourceAndVintageYear(EnergySource energySource, Integer vintageYear, Pageable pageable);
    Page<Rec> findByEnergySourceAndVintageYearAndStatus(EnergySource energySource, Integer vintageYear, RecStatus status, Pageable pageable);
}
