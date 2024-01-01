package pl.rationalworks.cryptorecommendationservicetest.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import pl.rationalworks.cryptorecommendationservicetest.repository.CryptoDailyPriceFactors;

import java.math.BigDecimal;
import java.time.Instant;

/**
 *
 */
@NamedNativeQueries({
    @NamedNativeQuery(name = "evaluateAggregatedPriceFactors",
        query = """
            WITH AGG(symbol, min_price, max_price, oldest_price_date, newest_price_date) AS
                     (select symbol,
                             min(min_price)         as min_price,
                             max(max_price)         as max_price,
                             min(oldest_price_date) as oldest_price_date,
                             max(newest_price_date) as newest_price_date
                      from daily_recent_factors
                      where DATEDIFF(DAY, :date, reference_date) > :daysBack
                        AND DATEDIFF(DAY, :date, reference_date) <= 0
                      group by symbol)
            select AGG.*,
                   null                                                as factor,
                   (select f.oldest_price
                    from daily_recent_factors f
                    where f.symbol = AGG.symbol
                      and f.oldest_price_date = AGG.oldest_price_date) as oldest_price,
                   (select f.newest_price
                    from daily_recent_factors f
                    where f.symbol = AGG.symbol
                      and f.newest_price_date = AGG.newest_price_date) as newest_price,
                   (select min(f.min_price_date)
                    from daily_recent_factors f
                    where f.symbol = AGG.symbol
                      and f.min_price = AGG.min_price
                      AND DATEDIFF(DAY, :date, f.reference_date) > :daysBack
                      AND DATEDIFF(DAY, :date, f.reference_date) <= 0) as min_price_date,
                   (select max(f.max_price_date)
                    from daily_recent_factors f
                    where f.symbol = AGG.symbol
                      and f.max_price = AGG.max_price
                      AND DATEDIFF(DAY, :date, f.reference_date) > :daysBack
                      AND DATEDIFF(DAY, :date, f.reference_date) <= 0) as max_price_date
            FROM AGG
            where symbol = :symbol
            """,
        resultSetMapping = "aggregatedPriceFactorsMapping"),
    @NamedNativeQuery(name = "selectCryptosByNormalizedFactorAndPeriod",
        query = """
            select f.symbol
            from daily_recent_factors f
            where reference_date = :date
            order by case
                         when :period = 'DAY' then
                             daily_normalized_factor
                         when :period = 'WEEK' then
                             weekly_normalized_factor
                         when :period = 'MONTH' then
                             monthly_normalized_factor
                         end DESC
            limit :lmt
            """,
        resultClass = String.class
    )
})
@SqlResultSetMappings({
    @SqlResultSetMapping(
        name = "aggregatedPriceFactorsMapping",
        classes = {
            @ConstructorResult(
                columns = {
                    // order of those properties here matters (to match proper constructor)
                    @ColumnResult(name = "symbol", type = String.class),
                    @ColumnResult(name = "min_price", type = BigDecimal.class),
                    @ColumnResult(name = "min_price_date", type = Instant.class),
                    @ColumnResult(name = "max_price", type = BigDecimal.class),
                    @ColumnResult(name = "max_price_date", type = Instant.class),
                    @ColumnResult(name = "oldest_price", type = BigDecimal.class),
                    @ColumnResult(name = "oldest_price_date", type = Instant.class),
                    @ColumnResult(name = "newest_price", type = BigDecimal.class),
                    @ColumnResult(name = "newest_price_date", type = Instant.class),
                    @ColumnResult(name = "factor", type = BigDecimal.class)
                },
                targetClass = CryptoDailyPriceFactors.class
            )
        }
    )
})
@Entity
@Table(name = "daily_recent_factors")
@AllArgsConstructor
@Getter
@EqualsAndHashCode
@Builder
public class CryptoDailyAggregatedFactors {

    @EmbeddedId
    private final DailyRecentFactorId id;
    @Column(name = "min_price", precision = 16, scale = 5)
    private final BigDecimal minPrice;
    @Column(name = "min_price_date")
    private final Instant minPriceDate;
    @Column(name = "max_price", precision = 16, scale = 5)
    private final BigDecimal maxPrice;
    @Column(name = "max_price_date")
    private final Instant maxPriceDate;
    /**
     * This field stores the oldest currency price we have since the beginning of the current day (id.referenceDate)
     */
    @Column(name = "oldest_price", precision = 16, scale = 5)
    private final BigDecimal oldestPrice;
    @Column(name = "oldest_price_date")
    private final Instant oldestPriceDate;
    /**
     * This field stores the newest currency price we have for the current day (id.referenceDate)
     */
    @Column(name = "newest_price", precision = 16, scale = 5)
    private final BigDecimal newestPrice;
    @Column(name = "newest_price_date")
    private final Instant newestPriceDate;
    @Column(name = "daily_normalized_factor", precision = 16, scale = 5)
    private final BigDecimal dailyNormalizedFactor;
    /**
     * This property stores a normalized weekly factor for a given currency. Here 'weekly' means a week period to date.
     * That means that this factor is calculated using a data from a 7 days back up to now (that is, the current date which is an
     * id.referenceDate).
     */
    @Column(name = "weekly_normalized_factor", precision = 16, scale = 5)
    private final BigDecimal weeklyNormalizedFactor;
    /**
     * This property stores a normalized monthly factor for a given currency. Here 'monthly' means a month period to date.
     * That means that this factor is calculated using a data from a 31 days back up to now (that is, the current date which is an
     * id.referenceDate).
     */
    @Column(name = "monthly_normalized_factor", precision = 16, scale = 5)
    private final BigDecimal monthlyNormalizedFactor;

    public CryptoDailyAggregatedFactors() {
        this(null, null, null, null, null, null, null,
            null, null, null, null, null);
    }

    public static CryptoDailyAggregatedFactors setupDailyEvaluationFactors(DailyRecentFactorId id, CryptoDailyPriceFactors factor) {
        return CryptoDailyAggregatedFactors.builder()
            .id(id)
            .minPrice(factor.minPrice())
            .minPriceDate(factor.minPriceDate())
            .maxPrice(factor.maxPrice())
            .maxPriceDate(factor.maxPriceDate())
            .oldestPrice(factor.oldestPrice())
            .oldestPriceDate(factor.oldestPriceDate())
            .newestPrice(factor.newestPrice())
            .newestPriceDate(factor.newestPriceDate())
            .dailyNormalizedFactor(factor.dailyPriceFactor())
            .build();
    }

    public static CryptoDailyAggregatedFactors setupNormalizedWeeklyFactors(DailyRecentFactorId id, BigDecimal factorValue) {
        return CryptoDailyAggregatedFactors.builder()
            .id(id)
            .weeklyNormalizedFactor(factorValue)
            .build();
    }

    public static CryptoDailyAggregatedFactors setupNormalizedMonthlyFactors(DailyRecentFactorId id, BigDecimal factorValue) {
        return CryptoDailyAggregatedFactors.builder()
            .id(id)
            .monthlyNormalizedFactor(factorValue)
            .build();
    }
}
