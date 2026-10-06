package com.platform.recs.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.platform.recs.enumtype.RecStatus;

public record StatusChangeRequest(
    @NotNull RecStatus status,
    @Size(max = 500) String comment
) {}
