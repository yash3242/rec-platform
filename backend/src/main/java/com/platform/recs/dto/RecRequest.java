package com.platform.recs.dto;

import com.platform.recs.enumtype.EnergySource;
import jakarta.validation.constraints.*;

public record RecRequest(
    @NotBlank String recCode,
    @NotNull Long generationLogId,
    @NotNull Long assetId,
    @NotNull EnergySource energySource,
    @NotNull Integer vintageYear,
    @NotNull @DecimalMin("0.001") java.math.BigDecimal energyQuantityMwh,
    @NotNull @Min(1) Integer certificateQuantity
) {}
