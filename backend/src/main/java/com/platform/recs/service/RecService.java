package com.platform.recs.service;

import com.platform.recs.dto.RecResponse;
import com.platform.recs.dto.StatusChangeRequest;
import com.platform.recs.dto.StatusHistoryResponse;
import com.platform.recs.entity.Rec;
import com.platform.recs.entity.StatusHistory;
import com.platform.recs.entity.User;
import com.platform.recs.enumtype.*;
import com.platform.recs.exception.BadRequestException;
import com.platform.recs.exception.ForbiddenOperationException;
import com.platform.recs.exception.ResourceNotFoundException;
import com.platform.recs.repository.RecRepository;
import com.platform.recs.repository.StatusHistoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecService {
    private final RecRepository recRepository;
    private final StatusHistoryRepository statusHistoryRepository;
    private final RecWorkflowService workflowService;

    public RecService(RecRepository recRepository, StatusHistoryRepository statusHistoryRepository, RecWorkflowService workflowService) {
        this.recRepository = recRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.workflowService = workflowService;
    }

    @Transactional(readOnly = true)
    public Page<RecResponse> search(String energySource, Integer vintageYear, String status, int page, int size, User currentUser) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        if (currentUser.getRole().getName() == RoleName.GENERATOR) {
            return recRepository.findByOwnerId(currentUser.getId(), pageable).map(RecResponse::from);
        }
        if (currentUser.getRole().getName() == RoleName.BUYER && status == null && energySource == null && vintageYear == null) {
            return recRepository.findByStatus(RecStatus.LISTED, pageable).map(RecResponse::from);
        }
        if (energySource != null && !energySource.isBlank() && vintageYear != null && status != null && !status.isBlank()) {
            return recRepository.findByEnergySourceAndVintageYearAndStatus(Enum.valueOf(EnergySource.class, energySource), vintageYear, RecStatus.valueOf(status), pageable).map(RecResponse::from);
        }
        if (status != null && !status.isBlank()) return recRepository.findByStatus(RecStatus.valueOf(status), pageable).map(RecResponse::from);
        if (energySource != null && !energySource.isBlank() && vintageYear != null) {
            return recRepository.findByEnergySourceAndVintageYear(Enum.valueOf(EnergySource.class, energySource), vintageYear, pageable).map(RecResponse::from);
        }
        if (energySource != null && !energySource.isBlank()) return recRepository.findByEnergySource(Enum.valueOf(EnergySource.class, energySource), pageable).map(RecResponse::from);
        if (vintageYear != null) return recRepository.findByVintageYear(vintageYear, pageable).map(RecResponse::from);
        return recRepository.findAll(pageable).map(RecResponse::from);
    }

    @Transactional(readOnly = true)
    public RecResponse get(Long id, User currentUser) {
        Rec rec = findRec(id);
        if (currentUser.getRole().getName() == RoleName.GENERATOR && !rec.getOwner().getId().equals(currentUser.getId())) throw new ForbiddenOperationException("Access denied");
        if (currentUser.getRole().getName() == RoleName.BUYER && rec.getStatus() != RecStatus.LISTED && !rec.getOwner().getId().equals(currentUser.getId())) throw new ForbiddenOperationException("Access denied");
        return RecResponse.from(rec);
    }

    @Transactional
    public RecResponse changeStatus(Long id, StatusChangeRequest request, User currentUser) {
        Rec rec = findRec(id);
        RecStatus next = RecStatus.valueOf(request.status());
        workflowService.validateTransition(rec.getStatus(), next, currentUser.getRole().getName());
        if (currentUser.getRole().getName() == RoleName.GENERATOR && !rec.getOwner().getId().equals(currentUser.getId())) throw new ForbiddenOperationException("Only the REC owner can perform this action");
        RecStatus old = rec.getStatus();
        rec.setStatus(next);
        if (next == RecStatus.LISTED) rec.setListedAt(LocalDateTime.now());
        if (next == RecStatus.TRANSFERRED) {
            // In buyer purchase, owner should already be transferred by service method below.
            rec.setTransferredAt(LocalDateTime.now());
        }
        if (next == RecStatus.RETIRED) rec.setRetiredAt(LocalDateTime.now());
        recordHistory("REC", rec.getId(), old.name(), next.name(), currentUser, request.comment());
        return RecResponse.from(rec);
    }

    @Transactional
    public RecResponse purchase(Long id, User buyer) {
        if (buyer.getRole().getName() != RoleName.BUYER && buyer.getRole().getName() != RoleName.ADMIN) throw new ForbiddenOperationException("Only buyers can purchase RECs");
        Rec rec = findRec(id);
        if (rec.getStatus() != RecStatus.LISTED) throw new BadRequestException("Only listed RECs can be purchased");
        workflowService.validateTransition(rec.getStatus(), RecStatus.TRANSFERRED, buyer.getRole().getName());
        rec.setStatus(RecStatus.TRANSFERRED);
        rec.setOwner(buyer);
        rec.setTransferredAt(LocalDateTime.now());
        recordHistory("REC", rec.getId(), "LISTED", "TRANSFERRED", buyer, "Purchased by buyer");
        return RecResponse.from(rec);
    }

    @Transactional(readOnly = true)
    public List<StatusHistoryResponse> history(Long id, User currentUser) {
        Rec rec = findRec(id);
        if (currentUser.getRole().getName() == RoleName.GENERATOR && !rec.getOwner().getId().equals(currentUser.getId())) throw new ForbiddenOperationException("Access denied");
        return statusHistoryRepository.findByResourceTypeAndResourceIdOrderByChangedAtAsc("REC", id).stream().map(StatusHistoryResponse::from).collect(Collectors.toList());
    }

    private Rec findRec(Long id) {
        return recRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("REC not found"));
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
