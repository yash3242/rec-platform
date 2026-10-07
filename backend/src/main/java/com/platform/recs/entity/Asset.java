package com.platform.recs.entity;

import com.platform.recs.enumtype.AssetStatus;
import com.platform.recs.enumtype.EnergySource;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "assets")
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String assetCode;

    @Column(nullable = false, length = 120)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "energy_source", nullable = false, length = 40)
    private EnergySource energySource;

    @Column(name = "location", length = 160)
    private String location;

    @Column(name = "capacity_mw")
    private Double capacityMw;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AssetStatus status = AssetStatus.PENDING_VERIFICATION;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() { this.updatedAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public String getAssetCode() { return assetCode; }
    public void setAssetCode(String assetCode) { this.assetCode = assetCode; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public EnergySource getEnergySource() { return energySource; }
    public void setEnergySource(EnergySource energySource) { this.energySource = energySource; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Double getCapacityMw() { return capacityMw; }
    public void setCapacityMw(Double capacityMw) { this.capacityMw = capacityMw; }
    public AssetStatus getStatus() { return status; }
    public void setStatus(AssetStatus status) { this.status = status; }
    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
