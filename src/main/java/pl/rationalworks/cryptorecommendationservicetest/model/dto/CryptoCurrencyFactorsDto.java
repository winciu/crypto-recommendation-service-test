package pl.rationalworks.cryptorecommendationservicetest.model.dto;

import pl.rationalworks.cryptorecommendationservicetest.model.FactorPeriod;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record CryptoCurrencyFactorsDto(String symbol, LocalDate referenceDate,
                                       BigDecimal minPrice, Instant minPriceDate,
                                       BigDecimal maxPrice, Instant maxPriceDate,
                                       BigDecimal oldestPrice, Instant oldestPriceDate,
                                       BigDecimal newestPrice, Instant newestPriceDate, FactorPeriod period) {
}
