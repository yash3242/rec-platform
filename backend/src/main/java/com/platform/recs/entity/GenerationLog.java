package com.platform.recs.entity;

import com.platform.recs.enumtype.EnergySource;
import com.platform.recs.enumtype.GenerationLogStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "generation_logs")
public class GenerationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Column(name = "generation_date", nullable = false)
    private LocalDate generationDate;

    @Column(name = "energy_source", nullable = false, length = 40)
    @Enumerated(EnumType.STRING)
    private EnergySource energySource;

    @Column(name = "energy_quantity_mwh", nullable = false, precision = 14, scale = 3)
    private BigDecimal energyQuantityMwh;

    @Column(name = "vintage_year", nullable = false)
    private Integer vintageYear;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private GenerationLogStatus status = GenerationLogStatus.SUBMITTED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() { this.updatedAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public Asset getAsset() { return asset; }
    public void setAsset(Asset asset) { this.asset = asset; }
    public LocalDate getGenerationDate() { return generationDate; }
    public void setGenerationDate(LocalDate generationDate) { this.generationDate = generationDate; }
    public EnergySource getEnergySource() { return energySource; }
    public void setEnergySource(EnergySource energySource) { this.energySource = energySource; }
    public BigDecimal getEnergyQuantityMwh() { return energyQuantityMwh; }
    public void setEnergyQuantityMwh(BigDecimal energyQuantityMwh) { this.energyQuantityMwh = energyQuantityMwh; }
    public Integer getVintageYear() { return vintageYear; }
    public void setVintageYear(Integer vintageYear) { this.vintageYear = vintageYear; }
    public GenerationLogStatus getStatus() { return status; }
    public void setStatus(GenerationLogStatus status) { this.status = status; }
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
