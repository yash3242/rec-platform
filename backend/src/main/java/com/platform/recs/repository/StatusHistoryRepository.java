package com.platform.recs.repository;

import com.platform.recs.entity.StatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StatusHistoryRepository extends JpaRepository<StatusHistory, Long> {
    List<StatusHistory> findByResourceTypeAndResourceIdOrderByChangedAtAsc(String resourceType, Long resourceId);
}
