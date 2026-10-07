package com.platform.recs.dto;

import com.platform.recs.enumtype.EnergySource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AssetRequest(
    @NotBlank String assetCode,
    @NotBlank String name,
    @NotNull EnergySource energySource,
    String location,
    Double capacityMw
) {}
