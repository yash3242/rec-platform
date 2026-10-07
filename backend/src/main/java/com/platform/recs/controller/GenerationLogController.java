package com.platform.recs.controller;

import com.platform.recs.dto.GenerationLogRequest;
import com.platform.recs.dto.GenerationLogResponse;
import com.platform.recs.dto.StatusChangeRequest;
import com.platform.recs.dto.StatusHistoryResponse;
import com.platform.recs.security.SecurityUtils;
import com.platform.recs.service.GenerationLogService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/generation-logs")
public class GenerationLogController {
    private final GenerationLogService service;

    public GenerationLogController(GenerationLogService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('GENERATOR','ADMIN')")
    public GenerationLogResponse create(@Valid @RequestBody GenerationLogRequest request) {
        return service.create(request, SecurityUtils.currentUser());
    }

    @GetMapping
    public Page<GenerationLogResponse> search(@RequestParam(required = false) String energySource,
                                              @RequestParam(required = false) Integer vintageYear,
                                              @RequestParam(required = false) String status,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "20") int size) {
        return service.search(energySource, vintageYear, status, page, size, SecurityUtils.currentUser());
    }

    @GetMapping("/{id}")
    public GenerationLogResponse get(@PathVariable Long id) {
        return service.get(id, SecurityUtils.currentUser());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('GENERATOR','ADMIN')")
    public GenerationLogResponse update(@PathVariable Long id, @Valid @RequestBody GenerationLogRequest request) {
        return service.update(id, request, SecurityUtils.currentUser());
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public GenerationLogResponse changeStatus(@PathVariable Long id, @Valid @RequestBody StatusChangeRequest request) {
        return service.changeStatus(id, request, SecurityUtils.currentUser());
    }

    @GetMapping("/{id}/history")
    public List<StatusHistoryResponse> history(@PathVariable Long id) {
        return service.history(id, SecurityUtils.currentUser());
    }
}
