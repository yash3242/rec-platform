package com.platform.recs.service;

import com.platform.recs.dto.AssetRequest;
import com.platform.recs.dto.AssetResponse;
import com.platform.recs.dto.StatusChangeRequest;
import com.platform.recs.dto.StatusHistoryResponse;
import com.platform.recs.entity.Asset;
import com.platform.recs.entity.StatusHistory;
import com.platform.recs.entity.User;
import com.platform.recs.enumtype.AssetStatus;
import com.platform.recs.enumtype.RoleName;
import com.platform.recs.exception.BadRequestException;
import com.platform.recs.exception.ForbiddenOperationException;
import com.platform.recs.exception.ResourceNotFoundException;
import com.platform.recs.repository.AssetRepository;
import com.platform.recs.repository.StatusHistoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AssetService {
    private final AssetRepository assetRepository;
    private final StatusHistoryRepository statusHistoryRepository;
    private final AssetWorkflowService workflowService;

    public AssetService(AssetRepository assetRepository, StatusHistoryRepository statusHistoryRepository, AssetWorkflowService workflowService) {
        this.assetRepository = assetRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.workflowService = workflowService;
    }

    @Transactional
    public AssetResponse create(AssetRequest request, User currentUser) {
        if (assetRepository.existsByAssetCode(request.assetCode())) throw new BadRequestException("Asset code already exists");
        Asset asset = new Asset();
        asset.setAssetCode(request.assetCode());
        asset.setName(request.name());
        asset.setEnergySource(request.energySource());
        asset.setLocation(request.location());
        asset.setCapacityMw(request.capacityMw());
        asset.setStatus(AssetStatus.PENDING_VERIFICATION);
        asset.setOwner(currentUser);
        assetRepository.save(asset);
        recordHistory(asset.getId(), null, AssetStatus.PENDING_VERIFICATION.name(), currentUser, "Asset created");
        return AssetResponse.from(asset);
    }

    public Page<AssetResponse> search(String energySource, String status, int page, int size, User currentUser) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        if (currentUser.getRole().getName() == RoleName.GENERATOR) {
            return assetRepository.findByOwnerId(currentUser.getId(), pageable).map(AssetResponse::from);
        }
        if (energySource != null && !energySource.isBlank() && status != null && !status.isBlank()) {
            return assetRepository.findByEnergySourceAndStatus(Enum.valueOf(com.platform.recs.enumtype.EnergySource.class, energySource), AssetStatus.valueOf(status), pageable).map(AssetResponse::from);
        }
        if (energySource != null && !energySource.isBlank()) {
            return assetRepository.findByEnergySource(Enum.valueOf(com.platform.recs.enumtype.EnergySource.class, energySource), pageable).map(AssetResponse::from);
        }
        if (status != null && !status.isBlank()) {
            return assetRepository.findByStatus(AssetStatus.valueOf(status), pageable).map(AssetResponse::from);
        }
        return assetRepository.findAll(pageable).map(AssetResponse::from);
    }

    public AssetResponse get(Long id, User currentUser) {
        Asset asset = findAsset(id);
        if (currentUser.getRole().getName() == RoleName.GENERATOR && !asset.getOwner().getId().equals(currentUser.getId())) {
            throw new ForbiddenOperationException("You do not have access to this asset");
        }
        return AssetResponse.from(asset);
    }

    @Transactional
    public AssetResponse update(Long id, AssetRequest request, User currentUser) {
        Asset asset = findAsset(id);
        if (currentUser.getRole().getName() == RoleName.GENERATOR && !asset.getOwner().getId().equals(currentUser.getId())) {
            throw new ForbiddenOperationException("You can only edit your own assets");
        }
        if (asset.getStatus() == AssetStatus.SUSPENDED) throw new BadRequestException("Suspended assets cannot be edited");
        asset.setAssetCode(request.assetCode());
        asset.setName(request.name());
        asset.setEnergySource(request.energySource());
        asset.setLocation(request.location());
        asset.setCapacityMw(request.capacityMw());
        return AssetResponse.from(asset);
    }

    @Transactional
    public AssetResponse changeStatus(Long id, StatusChangeRequest request, User currentUser) {
        Asset asset = findAsset(id);
        AssetStatus next = AssetStatus.valueOf(request.status());
        workflowService.validateTransition(asset.getStatus(), next, currentUser.getRole().getName());
        AssetStatus old = asset.getStatus();
        asset.setStatus(next);
        recordHistory(asset.getId(), old.name(), next.name(), currentUser, request.comment());
        return AssetResponse.from(asset);
    }

    public List<StatusHistoryResponse> history(Long id, User currentUser) {
        Asset asset = findAsset(id);
        if (currentUser.getRole().getName() == RoleName.GENERATOR && !asset.getOwner().getId().equals(currentUser.getId())) {
            throw new ForbiddenOperationException("You do not have access to this asset");
        }
        return statusHistoryRepository.findByResourceTypeAndResourceIdOrderByChangedAtAsc("ASSET", id)
            .stream().map(StatusHistoryResponse::from).collect(Collectors.toList());
    }

    private Asset findAsset(Long id) {
        return assetRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Asset not found"));
    }

    private void recordHistory(Long resourceId, String oldStatus, String newStatus, User user, String comment) {
        StatusHistory h = new StatusHistory();
        h.setResourceType("ASSET");
        h.setResourceId(resourceId);
        h.setOldStatus(oldStatus);
        h.setNewStatus(newStatus);
        h.setChangedBy(user);
        h.setComment(comment);
        statusHistoryRepository.save(h);
    }
}
