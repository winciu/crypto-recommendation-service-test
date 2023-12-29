package pl.rationalworks.cryptorecommendationservicetest.repository;

import java.math.BigDecimal;
import java.time.Instant;

public record CryptoRecentPriceFactors(String symbol, BigDecimal minPrice, BigDecimal maxPrice,
                                       BigDecimal oldestPrice, Instant oldestPriceDate,
                                       BigDecimal newestPrice, Instant newestPriceDate) {
}
