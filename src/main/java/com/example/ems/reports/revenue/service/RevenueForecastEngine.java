package com.example.ems.reports.revenue.service;

import com.example.ems.reports.revenue.dto.RevenueForecastResponse;
import com.example.ems.reports.revenue.repository.RevenueForecastRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class RevenueForecastEngine {

    @Autowired
    private RevenueForecastRepository forecastRepository;

    public RevenueForecastResponse calculateForecast(int horizonMonths, BigDecimal currentActiveMrr) {
        Instant sixMonthsAgo = Instant.now().minus(180, ChronoUnit.DAYS);
        List<Object[]> history = forecastRepository.getHistoricalMonthlyRevenue(sixMonthsAgo);

        List<BigDecimal> monthlyValues = new ArrayList<>();
        if (history != null) {
            for (Object[] row : history) {
                if (row.length > 1 && row[1] != null) {
                    monthlyValues.add(new BigDecimal(row[1].toString()));
                }
            }
        }

        double confidenceScore;
        BigDecimal nextProjectedRevenue;

        if (monthlyValues.size() >= 3) {
            BigDecimal weightedSum = BigDecimal.ZERO;
            int weightSum = 0;
            for (int i = 0; i < monthlyValues.size(); i++) {
                int weight = i + 1;
                weightedSum = weightedSum.add(monthlyValues.get(i).multiply(BigDecimal.valueOf(weight)));
                weightSum += weight;
            }
            nextProjectedRevenue = weightedSum.divide(BigDecimal.valueOf(weightSum), 2, RoundingMode.HALF_UP);

            if (monthlyValues.size() >= 6) {
                confidenceScore = 95.0;
            } else if (monthlyValues.size() == 5) {
                confidenceScore = 85.0;
            } else if (monthlyValues.size() == 4) {
                confidenceScore = 75.0;
            } else {
                confidenceScore = 60.0;
            }
        } else {
            nextProjectedRevenue = currentActiveMrr != null && currentActiveMrr.compareTo(BigDecimal.ZERO) > 0 
                ? currentActiveMrr 
                : new BigDecimal("50000.00");
            confidenceScore = 30.0;
        }

        List<RevenueForecastResponse.ForecastDataPoint> dataPoints = new ArrayList<>();
        LocalDate baseDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYYY-MM");

        BigDecimal monthlyGrowthFactor = new BigDecimal("1.015");
        BigDecimal runningProjection = nextProjectedRevenue;

        for (int m = 1; m <= horizonMonths; m++) {
            LocalDate targetMonth = baseDate.plusMonths(m);
            runningProjection = runningProjection.multiply(monthlyGrowthFactor).setScale(2, RoundingMode.HALF_UP);
            dataPoints.add(new RevenueForecastResponse.ForecastDataPoint(
                targetMonth.format(formatter),
                runningProjection
            ));
        }

        return new RevenueForecastResponse(horizonMonths, confidenceScore, dataPoints);
    }
}
