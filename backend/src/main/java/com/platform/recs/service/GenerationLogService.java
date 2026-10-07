package com.platform.recs.service;

import com.platform.recs.dto.GenerationLogRequest;
import com.platform.recs.dto.GenerationLogResponse;
import com.platform.recs.dto.StatusChangeRequest;
import com.platform.recs.dto.StatusHistoryResponse;
import com.platform.recs.entity.Asset;
import com.platform.recs.entity.GenerationLog;
import com.platform.recs.entity.Rec;
import com.platform.recs.entity.StatusHistory;
import com.platform.recs.entity.User;
import com.platform.recs.enumtype.*;
import com.platform.recs.exception.BadRequestException;
import com.platform.recs.exception.ForbiddenOperationException;
import com.platform.recs.exception.ResourceNotFoundException;
import com.platform.recs.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GenerationLogService {
    private final GenerationLogRepository logRepository;
    private final AssetRepository assetRepository;
    private final RecRepository recRepository;
    private final StatusHistoryRepository statusHistoryRepository;
    private final GenerationLogWorkflowService workflowService;

    public GenerationLogService(GenerationLogRepository logRepository, AssetRepository assetRepository, RecRepository recRepository, StatusHistoryRepository statusHistoryRepository, GenerationLogWorkflowService workflowService) {
        this.logRepository = logRepository;
        this.assetRepository = assetRepository;
        this.recRepository = recRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.workflowService = workflowService;
    }

    @Transactional
    public GenerationLogResponse create(GenerationLogRequest request, User currentUser) {
        Asset asset = assetRepository.findById(request.assetId()).orElseThrow(() -> new ResourceNotFoundException("Asset not found"));
        if (currentUser.getRole().getName() == RoleName.GENERATOR && !asset.getOwner().getId().equals(currentUser.getId())) {
            throw new ForbiddenOperationException("You can only submit logs for your own assets");
        }
        if (asset.getStatus() != AssetStatus.ACTIVE) throw new BadRequestException("Only active assets can submit generation logs");
        GenerationLog log = new GenerationLog();
        log.setAsset(asset);
        log.setGenerationDate(request.generationDate());
        log.setEnergySource(request.energySource());
        log.setEnergyQuantityMwh(request.energyQuantityMwh());
        log.setVintageYear(request.vintageYear());
        log.setStatus(GenerationLogStatus.SUBMITTED);
        log.setCreatedBy(currentUser);
        logRepository.save(log);
        recordHistory("GENERATION_LOG", log.getId(), null, GenerationLogStatus.SUBMITTED.name(), currentUser, "Submitted");
        return GenerationLogResponse.from(log);
    }

    public Page<GenerationLogResponse> search(String energySource, Integer vintageYear, String status, int page, int size, User currentUser) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        if (currentUser.getRole().getName() == RoleName.GENERATOR) {
            return logRepository.findByCreatedById(currentUser.getId(), pageable).map(GenerationLogResponse::from);
        }
        if (energySource != null && !energySource.isBlank() && vintageYear != null && status != null && !status.isBlank()) {
            return logRepository.findByEnergySourceAndVintageYearAndStatus(Enum.valueOf(EnergySource.class, energySource), vintageYear, GenerationLogStatus.valueOf(status), pageable).map(GenerationLogResponse::from);
        }
        if (status != null && !status.isBlank()) return logRepository.findByStatus(GenerationLogStatus.valueOf(status), pageable).map(GenerationLogResponse::from);
        if (energySource != null && !energySource.isBlank() && vintageYear != null) {
            return logRepository.findByEnergySourceAndVintageYear(Enum.valueOf(EnergySource.class, energySource), vintageYear, pageable).map(GenerationLogResponse::from);
        }
        if (energySource != null && !energySource.isBlank()) {
            return logRepository.findByEnergySource(Enum.valueOf(EnergySource.class, energySource), pageable).map(GenerationLogResponse::from);
        }
        if (vintageYear != null) {
            return logRepository.findByVintageYear(vintageYear, pageable).map(GenerationLogResponse::from);
        }
        return logRepository.findAll(pageable).map(GenerationLogResponse::from);
    }

    public GenerationLogResponse get(Long id, User currentUser) {
        GenerationLog log = findLog(id);
        if (currentUser.getRole().getName() == RoleName.GENERATOR && !log.getCreatedBy().getId().equals(currentUser.getId())) throw new ForbiddenOperationException("Access denied");
        return GenerationLogResponse.from(log);
    }

    @Transactional
    public GenerationLogResponse update(Long id, GenerationLogRequest request, User currentUser) {
        GenerationLog log = findLog(id);
        if (currentUser.getRole().getName() == RoleName.GENERATOR && !log.getCreatedBy().getId().equals(currentUser.getId())) throw new ForbiddenOperationException("You can only edit your own logs");
        if (log.getStatus() != GenerationLogStatus.SUBMITTED) throw new BadRequestException("Only submitted logs can be edited");
        Asset asset = assetRepository.findById(request.assetId()).orElseThrow(() -> new ResourceNotFoundException("Asset not found"));
        log.setAsset(asset);
        log.setGenerationDate(request.generationDate());
        log.setEnergySource(request.energySource());
        log.setEnergyQuantityMwh(request.energyQuantityMwh());
        log.setVintageYear(request.vintageYear());
        return GenerationLogResponse.from(log);
    }

    @Transactional
    public GenerationLogResponse changeStatus(Long id, StatusChangeRequest request, User currentUser) {
        GenerationLog log = findLog(id);
        GenerationLogStatus next = GenerationLogStatus.valueOf(request.status());
        workflowService.validateTransition(log.getStatus(), next, currentUser.getRole().getName());
        GenerationLogStatus old = log.getStatus();
        log.setStatus(next);
        recordHistory("GENERATION_LOG", log.getId(), old.name(), next.name(), currentUser, request.comment());
        if (next == GenerationLogStatus.MINTED) {
            Rec rec = new Rec();
            rec.setRecCode("REC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            rec.setGenerationLog(log);
            rec.setAsset(log.getAsset());
            rec.setEnergySource(log.getEnergySource());
            rec.setVintageYear(log.getVintageYear());
            rec.setEnergyQuantityMwh(log.getEnergyQuantityMwh());
            rec.setCertificateQuantity(log.getEnergyQuantityMwh().intValue());
            rec.setStatus(RecStatus.ISSUED);
            rec.setOwner(log.getAsset().getOwner());
            recRepository.save(rec);
            recordHistory("REC", rec.getId(), null, RecStatus.ISSUED.name(), currentUser, "Minted from generation log " + log.getId());
        }
        return GenerationLogResponse.from(log);
    }

    public List<StatusHistoryResponse> history(Long id, User currentUser) {
        GenerationLog log = findLog(id);
        if (currentUser.getRole().getName() == RoleName.GENERATOR && !log.getCreatedBy().getId().equals(currentUser.getId())) throw new ForbiddenOperationException("Access denied");
        return statusHistoryRepository.findByResourceTypeAndResourceIdOrderByChangedAtAsc("GENERATION_LOG", id).stream().map(StatusHistoryResponse::from).collect(Collectors.toList());
    }

    private GenerationLog findLog(Long id) {
        return logRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Generation log not found"));
    }

    private void recordHistory(String type, Long id, String oldStatus, String newStatus, User user, String comment) {
        StatusHistory h = new StatusHistory();
        h.setResourceType(type);
        h.setResourceId(id);
        h.setOldStatus(oldStatus);
        h.setNewStatus(newStatus);
        h.setChangedBy(user);
        h.setComment(comment);
        statusHistoryRepository.save(h);
    }
}
