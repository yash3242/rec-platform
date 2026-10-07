package com.platform.recs.service;

import com.platform.recs.dto.DashboardSummary;
import com.platform.recs.entity.User;
import com.platform.recs.enumtype.*;
import com.platform.recs.repository.AssetRepository;
import com.platform.recs.repository.GenerationLogRepository;
import com.platform.recs.repository.RecRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class DashboardService {
    private final AssetRepository assetRepository;
    private final GenerationLogRepository logRepository;
    private final RecRepository recRepository;

    public DashboardService(AssetRepository assetRepository, GenerationLogRepository logRepository, RecRepository recRepository) {
        this.assetRepository = assetRepository;
        this.logRepository = logRepository;
        this.recRepository = recRepository;
    }

    @Transactional(readOnly = true)
    public DashboardSummary summary(User user) {
        Map<String, Long> assetStatusCounts = new LinkedHashMap<>();
        for (AssetStatus s : AssetStatus.values()) assetStatusCounts.put(s.name(), assetRepository.countByStatus(s));

        Map<String, Long> logStatusCounts = new LinkedHashMap<>();
        for (GenerationLogStatus s : GenerationLogStatus.values()) logStatusCounts.put(s.name(), logRepository.countByStatus(s));

        Map<String, Long> recStatusCounts = new LinkedHashMap<>();
        for (RecStatus s : RecStatus.values()) recStatusCounts.put(s.name(), recRepository.countByStatus(s));

        Map<String, Long> energySourceCounts = new LinkedHashMap<>();
        for (EnergySource s : EnergySource.values()) {
            // simple counts via full scan not ideal for large data; acceptable for MVP
            long count = recRepository.findAll().stream().filter(r -> r.getEnergySource() == s).count();
            energySourceCounts.put(s.name(), count);
        }

        long mintedThisYear = logRepository.findAll().stream().filter(l -> l.getStatus() == GenerationLogStatus.MINTED && l.getVintageYear() == java.time.Year.now().getValue()).count();
        long transferred = recRepository.countByStatus(RecStatus.TRANSFERRED);
        long retired = recRepository.countByStatus(RecStatus.RETIRED);

        long totalAssets = assetRepository.count();
        long totalLogs = logRepository.count();
        long totalRecs = recRepository.count();
        if (user.getRole().getName() == RoleName.GENERATOR) {
            totalAssets = assetRepository.countByOwnerId(user.getId());
            totalLogs = logRepository.findAll().stream().filter(l -> l.getCreatedBy().getId().equals(user.getId())).count();
            totalRecs = recRepository.countByOwnerId(user.getId());
        } else if (user.getRole().getName() == RoleName.BUYER) {
            totalAssets = 0;
            totalLogs = 0;
            totalRecs = recRepository.countByOwnerId(user.getId());
        }

        return new DashboardSummary(totalAssets, totalLogs, totalRecs, assetStatusCounts, logStatusCounts, recStatusCounts, energySourceCounts, mintedThisYear, transferred, retired);
    }
}
