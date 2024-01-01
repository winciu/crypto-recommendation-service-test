package pl.rationalworks.cryptorecommendationservicetest.repository;

import java.math.BigDecimal;
import java.time.Instant;

public record CryptoDailyPriceFactors(String symbol, BigDecimal minPrice, Instant minPriceDate,
                                      BigDecimal maxPrice, Instant maxPriceDate,
                                      BigDecimal oldestPrice, Instant oldestPriceDate,
                                      BigDecimal newestPrice, Instant newestPriceDate, BigDecimal dailyPriceFactor) {

}
