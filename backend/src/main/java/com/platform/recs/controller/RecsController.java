package com.platform.recs.controller;

import com.platform.recs.dto.RecResponse;
import com.platform.recs.dto.StatusChangeRequest;
import com.platform.recs.dto.StatusHistoryResponse;
import com.platform.recs.security.SecurityUtils;
import com.platform.recs.service.RecService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recs")
public class RecsController {
    private final RecService recService;

    public RecsController(RecService recService) {
        this.recService = recService;
    }

    @GetMapping
    public Page<RecResponse> search(@RequestParam(required = false) String energySource,
                                    @RequestParam(required = false) Integer vintageYear,
                                    @RequestParam(required = false) String status,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "20") int size) {
        return recService.search(energySource, vintageYear, status, page, size, SecurityUtils.currentUser());
    }

    @GetMapping("/{id}")
    public RecResponse get(@PathVariable Long id) {
        return recService.get(id, SecurityUtils.currentUser());
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('GENERATOR','BUYER','ADMIN')")
    public RecResponse changeStatus(@PathVariable Long id, @Valid @RequestBody StatusChangeRequest request) {
        return recService.changeStatus(id, request, SecurityUtils.currentUser());
    }

    @PostMapping("/{id}/purchase")
    @PreAuthorize("hasAnyRole('BUYER','ADMIN')")
    public RecResponse purchase(@PathVariable Long id) {
        return recService.purchase(id, SecurityUtils.currentUser());
    }

    @GetMapping("/{id}/history")
    public List<StatusHistoryResponse> history(@PathVariable Long id) {
        return recService.history(id, SecurityUtils.currentUser());
    }
}
