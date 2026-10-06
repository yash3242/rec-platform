package com.platform.recs.service;

import com.platform.recs.dto.RecRequest;
import com.platform.recs.dto.RecResponse;
import com.platform.recs.dto.StatusChangeRequest;
import com.platform.recs.dto.StatusHistoryResponse;
import com.platform.recs.entity.RenewableEnergyCertificate;
import com.platform.recs.entity.RecStatusHistory;
import com.platform.recs.entity.User;
import com.platform.recs.enumtype.EnergySource;
import com.platform.recs.enumtype.RecStatus;
import com.platform.recs.enumtype.RoleName;
import com.platform.recs.exception.BadRequestException;
import com.platform.recs.exception.ForbiddenOperationException;
import com.platform.recs.exception.ResourceNotFoundException;
import com.platform.recs.repository.RecRepository;
import com.platform.recs.repository.RecStatusHistoryRepository;
import com.platform.recs.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecService {
    private final RecRepository recRepository;
    private final RecStatusHistoryRepository historyRepository;
    private final UserRepository userRepository;
    private final RecWorkflowService workflowService;

    public RecService(RecRepository recRepository, RecStatusHistoryRepository historyRepository, UserRepository userRepository, RecWorkflowService workflowService) {
        this.recRepository = recRepository;
        this.historyRepository = historyRepository;
        this.userRepository = userRepository;
        this.workflowService = workflowService;
    }

    @Transactional
    public RecResponse create(RecRequest request, User currentUser) {
        if (recRepository.existsByRecCode(request.recCode())) {
            throw new BadRequestException("REC code already exists");
        }
        if (request.generationEndDate().isBefore(request.generationStartDate())) {
            throw new BadRequestException("Generation end date must be on or after start date");
        }
        User producer = userRepository.findById(request.producerId())
            .orElseThrow(() -> new ResourceNotFoundException("Producer not found"));
        if (producer.getRole().getName() != RoleName.PRODUCER) {
            throw new BadRequestException("Producer must have PRODUCER role");
        }
        if (currentUser.getRole().getName() == RoleName.PRODUCER && !currentUser.getId().equals(producer.getId())) {
            throw new ForbiddenOperationException("Producers can only create records for themselves");
        }

        RenewableEnergyCertificate rec = new RenewableEnergyCertificate();
        rec.setRecCode(request.recCode());
        rec.setProducer(producer);
        rec.setEnergySource(request.energySource());
        rec.setGenerationStartDate(request.generationStartDate());
        rec.setGenerationEndDate(request.generationEndDate());
        rec.setEnergyQuantityMwh(request.energyQuantityMwh());
        rec.setCertificateQuantity(request.certificateQuantity());
        rec.setStatus(RecStatus.CREATED);
        rec.setCreatedBy(currentUser);
        recRepository.save(rec);
        recordHistory(rec, null, RecStatus.CREATED, currentUser, "Created");
        return RecResponse.from(rec);
    }

    @Transactional(readOnly = true)
    public Page<RecResponse> search(String recCode, Long producerId, EnergySource energySource, RecStatus status,
                                    LocalDate startFrom, LocalDate endTo, Integer minCertQty, Integer maxCertQty,
                                    int page, int size, String sort, User currentUser) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort == null || sort.isBlank() ? "createdAt" : sort).descending());
        Long effectiveProducerId = producerId;
        if (currentUser.getRole().getName() == RoleName.PRODUCER) {
            effectiveProducerId = currentUser.getId();
        }
        return recRepository.search(recCode, effectiveProducerId, energySource, status, startFrom, endTo, minCertQty, maxCertQty, pageable)
            .map(RecResponse::from);
    }

    @Transactional(readOnly = true)
    public RecResponse get(Long id, User currentUser) {
        RenewableEnergyCertificate rec = findRec(id);
        checkReadAccess(rec, currentUser);
        return RecResponse.from(rec);
    }

    @Transactional
    public RecResponse update(Long id, RecRequest request, User currentUser) {
        RenewableEnergyCertificate rec = findRec(id);
        if (currentUser.getRole().getName() == RoleName.PRODUCER && !rec.getProducer().getId().equals(currentUser.getId())) {
            throw new ForbiddenOperationException("Producers can only edit their own records");
        }
        if (currentUser.getRole().getName() != RoleName.ADMIN && currentUser.getRole().getName() != RoleName.PRODUCER) {
            throw new ForbiddenOperationException("Only producers or admins can edit RECs");
        }
        if (rec.getStatus() != RecStatus.CREATED && rec.getStatus() != RecStatus.REJECTED) {
            throw new BadRequestException("Only created or rejected RECs can be edited");
        }
        if (request.generationEndDate().isBefore(request.generationStartDate())) {
            throw new BadRequestException("Generation end date must be on or after start date");
        }
        User producer = userRepository.findById(request.producerId())
            .orElseThrow(() -> new ResourceNotFoundException("Producer not found"));
        if (producer.getRole().getName() != RoleName.PRODUCER) {
            throw new BadRequestException("Producer must have PRODUCER role");
        }
        if (currentUser.getRole().getName() == RoleName.PRODUCER && !currentUser.getId().equals(producer.getId())) {
            throw new ForbiddenOperationException("Producers cannot reassign records to another producer");
        }
        rec.setRecCode(request.recCode());
        rec.setProducer(producer);
        rec.setEnergySource(request.energySource());
        rec.setGenerationStartDate(request.generationStartDate());
        rec.setGenerationEndDate(request.generationEndDate());
        rec.setEnergyQuantityMwh(request.energyQuantityMwh());
        rec.setCertificateQuantity(request.certificateQuantity());
        return RecResponse.from(rec);
    }

    @Transactional
    public RecResponse changeStatus(Long id, StatusChangeRequest request, User currentUser) {
        RenewableEnergyCertificate rec = findRec(id);
        if (currentUser.getRole().getName() == RoleName.PRODUCER && !rec.getProducer().getId().equals(currentUser.getId())) {
            throw new ForbiddenOperationException("Producers can only change status of their own records");
        }
        workflowService.validate(rec.getStatus(), request.status(), currentUser.getRole().getName());
        RecStatus oldStatus = rec.getStatus();
        rec.setStatus(request.status());
        recordHistory(rec, oldStatus, request.status(), currentUser, request.comment());
        return RecResponse.from(rec);
    }

    @Transactional(readOnly = true)
    public List<StatusHistoryResponse> history(Long id, User currentUser) {
        RenewableEnergyCertificate rec = findRec(id);
        checkReadAccess(rec, currentUser);
        return historyRepository.findByRecIdOrderByChangedAtAsc(id).stream()
            .map(StatusHistoryResponse::from)
            .collect(Collectors.toList());
    }

    private RenewableEnergyCertificate findRec(Long id) {
        return recRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("REC not found"));
    }

    private void checkReadAccess(RenewableEnergyCertificate rec, User currentUser) {
        if (currentUser.getRole().getName() == RoleName.PRODUCER && !rec.getProducer().getId().equals(currentUser.getId())) {
            throw new ForbiddenOperationException("You do not have access to this REC");
        }
    }

    private void recordHistory(RenewableEnergyCertificate rec, RecStatus oldStatus, RecStatus newStatus, User user, String comment) {
        RecStatusHistory history = new RecStatusHistory();
        history.setRec(rec);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setChangedBy(user);
        history.setComment(comment);
        historyRepository.save(history);
    }
}
