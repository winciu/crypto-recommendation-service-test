package pl.rationalworks.cryptorecommendationservicetest.model.dto;

import pl.rationalworks.cryptorecommendationservicetest.model.FactorPeriod;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record CryptoCurrencyFactorsDto(String symbol, LocalDate referenceDate, BigDecimal minPrice, BigDecimal maxPrice,
                                       BigDecimal oldestPrice, Instant oldestPriceDate,
                                       BigDecimal newestPrice, Instant newestPriceDate, FactorPeriod period) {
}
