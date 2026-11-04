package com.evs.electricvehiclestore.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.evs.electricvehiclestore.dto.UsageReportResponse;
import com.evs.electricvehiclestore.dto.UsageReportResponse.DailyUsageView;
import com.evs.electricvehiclestore.entity.UsageEvent;
import com.evs.electricvehiclestore.repository.UsageEventRepository;

@Service
public class UsageTrackingService {

    private final UsageEventRepository usageEventRepository;

    public UsageTrackingService(UsageEventRepository usageEventRepository) {
        this.usageEventRepository = usageEventRepository;
    }

    @Transactional
    public void record(Long userId, String eventType, String path) {
        usageEventRepository.save(new UsageEvent(userId, eventType, path, Instant.now()));
    }

    @Transactional(readOnly = true)
    public UsageReportResponse report() {
        List<UsageEvent> allEvents = usageEventRepository.findAll();
        Instant last24Hours = Instant.now().minusSeconds(24 * 60 * 60);

        Map<String, Long> eventsByType = allEvents.stream()
                .collect(Collectors.groupingBy(
                        UsageEvent::getEventType,
                        TreeMap::new,
                        Collectors.counting()
                ));

        Map<LocalDate, Long> byDate = allEvents.stream()
                .collect(Collectors.groupingBy(
                        event -> event.getOccurredAt().atZone(ZoneOffset.UTC).toLocalDate(),
                        TreeMap::new,
                        Collectors.counting()
                ));

        List<DailyUsageView> dailyActivity = byDate.entrySet().stream()
                .map(entry -> new DailyUsageView(entry.getKey().toString(), entry.getValue()))
                .toList();

        Set<Long> uniqueUsers = allEvents.stream()
                .map(UsageEvent::getUserId)
                .filter(userId -> userId != null)
                .collect(Collectors.toSet());

        long recentEvents = allEvents.stream()
                .filter(event -> !event.getOccurredAt().isBefore(last24Hours))
                .count();

        return new UsageReportResponse(
                Instant.now(),
                allEvents.size(),
                recentEvents,
                uniqueUsers.size(),
                new LinkedHashMap<>(eventsByType),
                dailyActivity,
                allEvents.isEmpty() ? "No application usage has been recorded yet" : "Usage report generated successfully"
        );
    }
}
