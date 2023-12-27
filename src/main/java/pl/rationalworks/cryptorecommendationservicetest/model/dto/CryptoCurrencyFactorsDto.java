package pl.rationalworks.cryptorecommendationservicetest.model.dto;

import java.math.BigDecimal;

public record CryptoCurrencyFactorsDto(String symbol, BigDecimal minPrice, BigDecimal maxPrice, BigDecimal oldestPrice,
                                       BigDecimal newestPrice) {
}
