package pl.rationalworks.cryptorecommendationservicetest.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 *
 */
@Entity
@Table(name = "daily_recent_factors")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class CryptoDailyRecentFactors {

    @EmbeddedId
    private DailyRecentFactorId id;
    @Column(name = "min_price", precision = 16, scale = 5)
    private BigDecimal minPrice;
    @Column(name = "max_price", precision = 16, scale = 5)
    private BigDecimal maxPrice;
    /**
     * This field stores the oldest currency price we have since the beginning of the current day (id.referenceDate)
     */
    @Column(name = "oldest_price", precision = 16, scale = 5)
    private BigDecimal oldestPrice;
    @Column(name = "oldest_price_date")
    private Instant oldestPriceDate;
    /**
     * This field stores the newest currency price we have for the current day (id.referenceDate)
     */
    @Column(name = "newest_price", precision = 16, scale = 5)
    private BigDecimal newestPrice;
    @Column(name = "newest_price_date")
    private Instant newestPriceDate;

    @Column(name = "daily_normalized_factor", precision = 16, scale = 5)
    private BigDecimal dailyNormalizedFactor;
    /**
     * This property stores a normalized weekly factor for a given currency. Here 'weekly' means a week period to date.
     * That means that this factor is calculated using a data from a 7 days back up to now (that is, the current date which is an
     * id.referenceDate).
     */
    @Column(name = "week_normalized_factor", precision = 16, scale = 5)
    private BigDecimal weekNormalizedFactor;
    /**
     * This property stores a normalized monthly factor for a given currency. Here 'monthly' means a month period to date.
     * That means that this factor is calculated using a data from a 31 days back up to now (that is, the current date which is an
     * id.referenceDate).
     */
    @Column(name = "month_normalized_factor", precision = 16, scale = 5)
    private BigDecimal monthNormalizedFactor;

    public static CryptoDailyRecentFactors setupDailyEvaluationFactors(DailyRecentFactorId id, BigDecimal minPrice,
                                                                       BigDecimal maxPrice,
                                                                       BigDecimal normalizedFactor) {
        return new CryptoDailyRecentFactors(id, minPrice, maxPrice, null, null,
            null, null, normalizedFactor, null, null);
    }

    public static CryptoDailyRecentFactors setupOldestPriceFactors(DailyRecentFactorId id,
                                                                   BigDecimal oldestPrice, Instant oldestPriceDate) {
        return new CryptoDailyRecentFactors(id, null, null, oldestPrice, oldestPriceDate, null,
            null, null, null, null);
    }

    public static CryptoDailyRecentFactors setupNewestPriceFactors(DailyRecentFactorId id,
                                                                   BigDecimal newestPrice, Instant newestPriceDate) {
        return new CryptoDailyRecentFactors(id, null, null, null, null,
            newestPrice, newestPriceDate, null, null, null);
    }

    public static CryptoDailyRecentFactors setupNormalizedWeeklyFactors(DailyRecentFactorId id, BigDecimal factorValue) {
        return new CryptoDailyRecentFactors(id, null,null,null,null,
            null, null, null, factorValue, null);
    }
}
