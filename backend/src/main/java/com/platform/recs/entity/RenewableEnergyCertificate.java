package com.platform.recs.entity;

import com.platform.recs.enumtype.EnergySource;
import com.platform.recs.enumtype.RecStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "recs")
public class RenewableEnergyCertificate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rec_code", nullable = false, unique = true, length = 40)
    private String recCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producer_id", nullable = false)
    private User producer;

    @Enumerated(EnumType.STRING)
    @Column(name = "energy_source", nullable = false, length = 40)
    private EnergySource energySource;

    @Column(name = "generation_start_date", nullable = false)
    private LocalDate generationStartDate;

    @Column(name = "generation_end_date", nullable = false)
    private LocalDate generationEndDate;

    @Column(name = "energy_quantity_mwh", nullable = false, precision = 14, scale = 3)
    private BigDecimal energyQuantityMwh;

    @Column(name = "certificate_quantity", nullable = false)
    private Integer certificateQuantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RecStatus status;

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
    public String getRecCode() { return recCode; }
    public void setRecCode(String recCode) { this.recCode = recCode; }
    public User getProducer() { return producer; }
    public void setProducer(User producer) { this.producer = producer; }
    public EnergySource getEnergySource() { return energySource; }
    public void setEnergySource(EnergySource energySource) { this.energySource = energySource; }
    public LocalDate getGenerationStartDate() { return generationStartDate; }
    public void setGenerationStartDate(LocalDate generationStartDate) { this.generationStartDate = generationStartDate; }
    public LocalDate getGenerationEndDate() { return generationEndDate; }
    public void setGenerationEndDate(LocalDate generationEndDate) { this.generationEndDate = generationEndDate; }
    public BigDecimal getEnergyQuantityMwh() { return energyQuantityMwh; }
    public void setEnergyQuantityMwh(BigDecimal energyQuantityMwh) { this.energyQuantityMwh = energyQuantityMwh; }
    public Integer getCertificateQuantity() { return certificateQuantity; }
    public void setCertificateQuantity(Integer certificateQuantity) { this.certificateQuantity = certificateQuantity; }
    public RecStatus getStatus() { return status; }
    public void setStatus(RecStatus status) { this.status = status; }
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
