package com.platform.recs.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record GenerationLogRequest(
    @NotNull Long assetId,
    @NotNull LocalDate generationDate,
    @NotNull com.platform.recs.enumtype.EnergySource energySource,
    @NotNull @DecimalMin("0.001") BigDecimal energyQuantityMwh,
    @NotNull @Min(2000) Integer vintageYear
) {}
