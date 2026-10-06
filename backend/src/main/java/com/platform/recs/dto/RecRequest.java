package com.platform.recs.dto;

import com.platform.recs.enumtype.EnergySource;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record RecRequest(
    @NotBlank String recCode,
    @NotNull Long producerId,
    @NotNull EnergySource energySource,
    @NotNull LocalDate generationStartDate,
    @NotNull LocalDate generationEndDate,
    @NotNull @DecimalMin("0.001") BigDecimal energyQuantityMwh,
    @NotNull @Min(1) Integer certificateQuantity
) {}
