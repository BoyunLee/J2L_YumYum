package com.ssafy.yumyum.domain.admin.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.yumyum.domain.admin.dao.AdminDao;
import com.ssafy.yumyum.domain.admin.dto.AdminDtos.ApiUsagePoint;
import com.ssafy.yumyum.domain.admin.dto.AdminDtos.ApiUsageResponse;
import com.ssafy.yumyum.domain.admin.dto.ApiUsageAggregate;
import com.ssafy.yumyum.domain.admin.entity.ApiUsageLog;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ApiUsageService {
    public static final String OCR = "OCR";
    public static final String BARCODE = "BARCODE";
    public static final String RECIPE_RECOMMENDATION = "RECIPE_RECOMMENDATION";

    private final AdminDao adminDao;

    public void record(Long userId, String apiType, boolean success, long durationMs, String errorCode) {
        ApiUsageLog log = new ApiUsageLog();
        log.setUserId(userId);
        log.setApiType(apiType);
        log.setSuccess(success);
        log.setDurationMs(Math.max(0, durationMs));
        log.setErrorCode(errorCode == null ? null : errorCode.substring(0, Math.min(50, errorCode.length())));
        adminDao.insertApiUsageLog(log);
    }

    @Transactional(readOnly = true)
    public ApiUsageResponse getHourlyUsage(int requestedHours) {
        int hours = Math.max(6, Math.min(168, requestedHours));
        List<ApiUsageAggregate> aggregates = adminDao.findHourlyApiUsage(hours);
        Map<LocalDateTime, Map<String, Long>> grouped = new HashMap<>();
        long total = 0;
        long successful = 0;
        long ocrCalls = 0;
        long barcodeCalls = 0;
        long recipeRecommendationCalls = 0;
        for (ApiUsageAggregate aggregate : aggregates) {
            long callCount = aggregate.getCallCount() == null ? 0 : aggregate.getCallCount();
            grouped.computeIfAbsent(aggregate.getHour(), ignored -> new HashMap<>())
                    .put(aggregate.getApiType(), callCount);
            total += callCount;
            successful += aggregate.getSuccessCount() == null ? 0 : aggregate.getSuccessCount();
            switch (aggregate.getApiType()) {
                case OCR -> ocrCalls += callCount;
                case BARCODE -> barcodeCalls += callCount;
                case RECIPE_RECOMMENDATION -> recipeRecommendationCalls += callCount;
                default -> {
                }
            }
        }

        LocalDateTime end = currentDatabaseHour();
        List<ApiUsagePoint> points = IntStream.range(0, hours)
                .mapToObj(offset -> end.minusHours(hours - 1L - offset))
                .map(hour -> {
                    Map<String, Long> values = grouped.getOrDefault(hour, Map.of());
                    return new ApiUsagePoint(hour, values.getOrDefault(OCR, 0L),
                            values.getOrDefault(BARCODE, 0L),
                            values.getOrDefault(RECIPE_RECOMMENDATION, 0L));
                })
                .toList();
        return new ApiUsageResponse(
                hours,
                total,
                successful,
                ocrCalls,
                barcodeCalls,
                recipeRecommendationCalls,
                points);
    }

    private LocalDateTime currentDatabaseHour() {
        LocalDateTime databaseHour = adminDao.findCurrentDatabaseHour();
        return databaseHour == null ? LocalDateTime.now().truncatedTo(ChronoUnit.HOURS) : databaseHour;
    }
}
