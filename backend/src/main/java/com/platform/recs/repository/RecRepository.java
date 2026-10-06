package com.platform.recs.repository;

import com.platform.recs.entity.RenewableEnergyCertificate;
import com.platform.recs.enumtype.EnergySource;
import com.platform.recs.enumtype.RecStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RecRepository extends JpaRepository<RenewableEnergyCertificate, Long> {
    boolean existsByRecCode(String recCode);
    Optional<RenewableEnergyCertificate> findByRecCode(String recCode);
    Page<RenewableEnergyCertificate> findByProducerId(Long producerId, Pageable pageable);
    long countByStatus(RecStatus status);
    long countByEnergySource(EnergySource energySource);
    List<RenewableEnergyCertificate> findTop10ByOrderByCreatedAtDesc();

    @Query("SELECT r.status, COUNT(r) FROM RenewableEnergyCertificate r GROUP BY r.status")
    List<Object[]> countByStatusGroup();

    @Query("SELECT SUM(r.energyQuantityMwh) FROM RenewableEnergyCertificate r")
    java.math.BigDecimal sumEnergyQuantityMwh();

    @Query("SELECT SUM(r.certificateQuantity) FROM RenewableEnergyCertificate r")
    Long sumCertificateQuantity();

    @Query("SELECT r.energySource, COUNT(r) FROM RenewableEnergyCertificate r GROUP BY r.energySource")
    List<Object[]> countByEnergySourceGroup();

    @Query("""
        SELECT r FROM RenewableEnergyCertificate r
        WHERE (:recCode IS NULL OR LOWER(r.recCode) LIKE LOWER(CONCAT('%', :recCode, '%')))
          AND (:producerId IS NULL OR r.producer.id = :producerId)
          AND (:energySource IS NULL OR r.energySource = :energySource)
          AND (:status IS NULL OR r.status = :status)
          AND (:startFrom IS NULL OR r.generationStartDate >= :startFrom)
          AND (:endTo IS NULL OR r.generationEndDate <= :endTo)
          AND (:minCertQty IS NULL OR r.certificateQuantity >= :minCertQty)
          AND (:maxCertQty IS NULL OR r.certificateQuantity <= :maxCertQty)
        """)
    Page<RenewableEnergyCertificate> search(
        @Param("recCode") String recCode,
        @Param("producerId") Long producerId,
        @Param("energySource") EnergySource energySource,
        @Param("status") RecStatus status,
        @Param("startFrom") LocalDate startFrom,
        @Param("endTo") LocalDate endTo,
        @Param("minCertQty") Integer minCertQty,
        @Param("maxCertQty") Integer maxCertQty,
        Pageable pageable
    );
}
