package com.platform.recs.controller;

import com.platform.recs.dto.AssetRequest;
import com.platform.recs.dto.AssetResponse;
import com.platform.recs.dto.StatusChangeRequest;
import com.platform.recs.dto.StatusHistoryResponse;
import com.platform.recs.security.SecurityUtils;
import com.platform.recs.service.AssetService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/assets")
public class AssetController {
    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('GENERATOR','ADMIN')")
    public AssetResponse create(@Valid @RequestBody AssetRequest request) {
        return assetService.create(request, SecurityUtils.currentUser());
    }

    @GetMapping
    public Page<AssetResponse> search(@RequestParam(required = false) String energySource,
                                      @RequestParam(required = false) String status,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "20") int size) {
        return assetService.search(energySource, status, page, size, SecurityUtils.currentUser());
    }

    @GetMapping("/{id}")
    public AssetResponse get(@PathVariable Long id) {
        return assetService.get(id, SecurityUtils.currentUser());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('GENERATOR','ADMIN')")
    public AssetResponse update(@PathVariable Long id, @Valid @RequestBody AssetRequest request) {
        return assetService.update(id, request, SecurityUtils.currentUser());
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public AssetResponse changeStatus(@PathVariable Long id, @Valid @RequestBody StatusChangeRequest request) {
        return assetService.changeStatus(id, request, SecurityUtils.currentUser());
    }

    @GetMapping("/{id}/history")
    public List<StatusHistoryResponse> history(@PathVariable Long id) {
        return assetService.history(id, SecurityUtils.currentUser());
    }
}
