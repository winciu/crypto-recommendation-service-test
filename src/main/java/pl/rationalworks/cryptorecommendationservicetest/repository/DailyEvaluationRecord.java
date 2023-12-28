package pl.rationalworks.cryptorecommendationservicetest.repository;

import java.math.BigDecimal;

public record DailyEvaluationRecord(String symbol, BigDecimal minPrice, BigDecimal maxPrice,
                                    BigDecimal normalizedFactor) {
}
