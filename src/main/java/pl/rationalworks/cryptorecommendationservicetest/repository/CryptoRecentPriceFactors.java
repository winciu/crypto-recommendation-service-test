package pl.rationalworks.cryptorecommendationservicetest.repository;

import pl.rationalworks.cryptorecommendationservicetest.model.FactorPeriod;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CryptoRecentPriceFactors(String symbol, LocalDate referenceDate, BigDecimal minPrice, BigDecimal maxPrice,
                                       BigDecimal oldestPrice, BigDecimal newestPrice, FactorPeriod period) {
    public static CryptoRecentPriceFactors empty(String symbol, LocalDate referenceDate, FactorPeriod period) {
        return new CryptoRecentPriceFactors(symbol, referenceDate, null, null, null, null, period);
    }
}
