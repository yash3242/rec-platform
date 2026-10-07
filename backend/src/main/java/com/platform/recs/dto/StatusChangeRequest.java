package com.platform.recs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StatusChangeRequest(
    @NotBlank String status,
    @Size(max = 500) String comment
) {}
