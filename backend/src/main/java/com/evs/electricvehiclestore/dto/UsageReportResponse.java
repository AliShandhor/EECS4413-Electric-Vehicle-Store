package com.evs.electricvehiclestore.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record UsageReportResponse(
        Instant generatedAt,
        long totalEvents,
        long eventsLast24Hours,
        long uniqueAuthenticatedUsers,
        Map<String, Long> eventsByType,
        List<DailyUsageView> dailyActivity,
        String message
) {
    public record DailyUsageView(String date, long events) {
    }
}
