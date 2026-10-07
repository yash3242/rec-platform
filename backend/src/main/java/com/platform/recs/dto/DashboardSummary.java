package com.platform.recs.dto;

import java.util.Map;

public record DashboardSummary(
    long totalAssets,
    long totalGenerationLogs,
    long totalRecs,
    Map<String, Long> assetStatusCounts,
    Map<String, Long> logStatusCounts,
    Map<String, Long> recStatusCounts,
    Map<String, Long> energySourceCounts,
    long mintedThisVintageYear,
    long transferredCount,
    long retiredCount
) {}
