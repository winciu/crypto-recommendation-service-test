package pl.rationalworks.cryptorecommendationservicetest.repository;

import java.math.BigDecimal;

public record DailyMinMaxRecord(BigDecimal minPrice, BigDecimal maxPrice, String symbol) {
}
