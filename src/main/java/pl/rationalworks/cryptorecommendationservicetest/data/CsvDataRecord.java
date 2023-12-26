package pl.rationalworks.cryptorecommendationservicetest.data;

import java.math.BigDecimal;
import java.time.Instant;

public record CsvDataRecord(Instant timestamp, String symbol, BigDecimal price) {
}
