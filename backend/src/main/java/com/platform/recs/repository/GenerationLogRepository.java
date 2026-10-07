package com.platform.recs.repository;

import com.platform.recs.entity.GenerationLog;
import com.platform.recs.enumtype.EnergySource;
import com.platform.recs.enumtype.GenerationLogStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GenerationLogRepository extends JpaRepository<GenerationLog, Long> {
    Page<GenerationLog> findByCreatedById(Long userId, Pageable pageable);
    Page<GenerationLog> findByStatus(GenerationLogStatus status, Pageable pageable);
    long countByStatus(GenerationLogStatus status);
    Page<GenerationLog> findByEnergySource(EnergySource energySource, Pageable pageable);
    Page<GenerationLog> findByVintageYear(Integer vintageYear, Pageable pageable);
    Page<GenerationLog> findByEnergySourceAndVintageYear(EnergySource energySource, Integer vintageYear, Pageable pageable);
    Page<GenerationLog> findByEnergySourceAndVintageYearAndStatus(EnergySource energySource, Integer vintageYear, GenerationLogStatus status, Pageable pageable);
}
