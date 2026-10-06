package com.platform.recs.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record DashboardSummary(
    long totalRecs,
    Map<String, Long> statusCounts,
    Map<String, Long> energySourceCounts,
    BigDecimal totalEnergyMwh,
    Long totalCertificateQuantity,
    List<RecResponse> recentRecs
) {}
