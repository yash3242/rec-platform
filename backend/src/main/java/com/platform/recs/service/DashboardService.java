package com.platform.recs.service;

import com.platform.recs.dto.DashboardSummary;
import com.platform.recs.dto.RecResponse;
import com.platform.recs.repository.RecRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {
    private final RecRepository recRepository;

    public DashboardService(RecRepository recRepository) {
        this.recRepository = recRepository;
    }

    @Transactional(readOnly = true)
    public DashboardSummary summary() {
        Map<String, Long> statusCounts = new LinkedHashMap<>();
        recRepository.countByStatusGroup().forEach(row -> statusCounts.put(row[0].toString(), (Long) row[1]));

        Map<String, Long> energySourceCounts = new LinkedHashMap<>();
        recRepository.countByEnergySourceGroup().forEach(row -> energySourceCounts.put(row[0].toString(), (Long) row[1]));

        List<RecResponse> recent = recRepository.findTop10ByOrderByCreatedAtDesc().stream()
            .map(RecResponse::from)
            .collect(Collectors.toList());

        return new DashboardSummary(
            recRepository.count(),
            statusCounts,
            energySourceCounts,
            recRepository.sumEnergyQuantityMwh(),
            recRepository.sumCertificateQuantity(),
            recent
        );
    }
}
