package com.platform.recs.entity;

import com.platform.recs.enumtype.EnergySource;
import com.platform.recs.enumtype.RecStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "recs")
public class Rec {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rec_code", nullable = false, unique = true, length = 50)
    private String recCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generation_log_id", nullable = false)
    private GenerationLog generationLog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Enumerated(EnumType.STRING)
    @Column(name = "energy_source", nullable = false, length = 40)
    private EnergySource energySource;

    @Column(name = "vintage_year", nullable = false)
    private Integer vintageYear;

    @Column(name = "energy_quantity_mwh", nullable = false, precision = 14, scale = 3)
    private BigDecimal energyQuantityMwh;

    @Column(name = "certificate_quantity", nullable = false)
    private Integer certificateQuantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RecStatus status = RecStatus.ISSUED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "listed_at")
    private LocalDateTime listedAt;

    @Column(name = "transferred_at")
    private LocalDateTime transferredAt;

    @Column(name = "retired_at")
    private LocalDateTime retiredAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() { this.updatedAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public String getRecCode() { return recCode; }
    public void setRecCode(String recCode) { this.recCode = recCode; }
    public GenerationLog getGenerationLog() { return generationLog; }
    public void setGenerationLog(GenerationLog generationLog) { this.generationLog = generationLog; }
    public Asset getAsset() { return asset; }
    public void setAsset(Asset asset) { this.asset = asset; }
    public EnergySource getEnergySource() { return energySource; }
    public void setEnergySource(EnergySource energySource) { this.energySource = energySource; }
    public Integer getVintageYear() { return vintageYear; }
    public void setVintageYear(Integer vintageYear) { this.vintageYear = vintageYear; }
    public BigDecimal getEnergyQuantityMwh() { return energyQuantityMwh; }
    public void setEnergyQuantityMwh(BigDecimal energyQuantityMwh) { this.energyQuantityMwh = energyQuantityMwh; }
    public Integer getCertificateQuantity() { return certificateQuantity; }
    public void setCertificateQuantity(Integer certificateQuantity) { this.certificateQuantity = certificateQuantity; }
    public RecStatus getStatus() { return status; }
    public void setStatus(RecStatus status) { this.status = status; }
    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }
    public LocalDateTime getListedAt() { return listedAt; }
    public void setListedAt(LocalDateTime listedAt) { this.listedAt = listedAt; }
    public LocalDateTime getTransferredAt() { return transferredAt; }
    public void setTransferredAt(LocalDateTime transferredAt) { this.transferredAt = transferredAt; }
    public LocalDateTime getRetiredAt() { return retiredAt; }
    public void setRetiredAt(LocalDateTime retiredAt) { this.retiredAt = retiredAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
