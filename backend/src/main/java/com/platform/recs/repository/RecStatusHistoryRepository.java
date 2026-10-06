package com.platform.recs.repository;

import com.platform.recs.entity.RecStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecStatusHistoryRepository extends JpaRepository<RecStatusHistory, Long> {
    List<RecStatusHistory> findByRecIdOrderByChangedAtAsc(Long recId);
}
