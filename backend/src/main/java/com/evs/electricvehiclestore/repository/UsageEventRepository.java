package com.evs.electricvehiclestore.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.evs.electricvehiclestore.entity.UsageEvent;

public interface UsageEventRepository extends JpaRepository<UsageEvent, Long> {
    List<UsageEvent> findByOccurredAtGreaterThanEqualOrderByOccurredAtAsc(Instant since);
}
