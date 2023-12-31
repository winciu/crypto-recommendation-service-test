package pl.rationalworks.cryptorecommendationservicetest.repository;

import java.math.BigDecimal;
import java.time.Instant;

public record CryptoRecentPriceFactors(String symbol, BigDecimal minPrice, BigDecimal maxPrice,
                                       BigDecimal oldestPrice, Instant oldestPriceDate,
                                       BigDecimal newestPrice, Instant newestPriceDate) {

    public CryptoRecentPriceFactors(String symbol, BigDecimal oldestPrice, BigDecimal newestPrice) {
        this(symbol, null, null, oldestPrice, null, newestPrice, null);
    }

    public CryptoRecentPriceFactors(String symbol, BigDecimal minPrice, BigDecimal maxPrice, Instant oldestPriceDate, Instant newestPriceDate) {
        this(symbol, minPrice, maxPrice, null, oldestPriceDate, null, newestPriceDate);
    }
}
