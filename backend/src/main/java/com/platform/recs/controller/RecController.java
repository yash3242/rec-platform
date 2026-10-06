package com.platform.recs.controller;

import com.platform.recs.dto.RecRequest;
import com.platform.recs.dto.RecResponse;
import com.platform.recs.dto.StatusChangeRequest;
import com.platform.recs.dto.StatusHistoryResponse;
import com.platform.recs.entity.User;
import com.platform.recs.enumtype.EnergySource;
import com.platform.recs.enumtype.RecStatus;
import com.platform.recs.security.SecurityUtils;
import com.platform.recs.service.RecService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/recs")
public class RecController {
    private final RecService recService;

    public RecController(RecService recService) {
        this.recService = recService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('PRODUCER','ADMIN')")
    public ResponseEntity<RecResponse> create(@Valid @RequestBody RecRequest request) {
        User currentUser = SecurityUtils.currentUser();
        return ResponseEntity.status(HttpStatus.CREATED).body(recService.create(request, currentUser));
    }

    @GetMapping
    public Page<RecResponse> search(
        @RequestParam(required = false) String recCode,
        @RequestParam(required = false) Long producerId,
        @RequestParam(required = false) EnergySource energySource,
        @RequestParam(required = false) RecStatus status,
        @RequestParam(required = false) LocalDate startFrom,
        @RequestParam(required = false) LocalDate endTo,
        @RequestParam(required = false) Integer minCertQty,
        @RequestParam(required = false) Integer maxCertQty,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "createdAt") String sort
    ) {
        User currentUser = SecurityUtils.currentUser();
        return recService.search(recCode, producerId, energySource, status, startFrom, endTo, minCertQty, maxCertQty, page, size, sort, currentUser);
    }

    @GetMapping("/{id}")
    public RecResponse get(@PathVariable Long id) {
        return recService.get(id, SecurityUtils.currentUser());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PRODUCER','ADMIN')")
    public RecResponse update(@PathVariable Long id, @Valid @RequestBody RecRequest request) {
        return recService.update(id, request, SecurityUtils.currentUser());
    }

    @PatchMapping("/{id}/status")
    public RecResponse changeStatus(@PathVariable Long id, @Valid @RequestBody StatusChangeRequest request) {
        return recService.changeStatus(id, request, SecurityUtils.currentUser());
    }

    @GetMapping("/{id}/history")
    public List<StatusHistoryResponse> history(@PathVariable Long id) {
        return recService.history(id, SecurityUtils.currentUser());
    }
}
