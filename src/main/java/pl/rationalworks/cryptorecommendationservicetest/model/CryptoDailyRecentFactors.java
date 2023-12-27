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
    @Column(name = "min_price")
    private BigDecimal minPrice;
    @Column(name = "max_price")
    private BigDecimal maxPrice;
    /**
     * This field stores the oldest currency price we have since the beginning of the current day (id.referenceDate)
     */
    @Column(name = "oldest_price")
    private BigDecimal oldestPrice;
    @Column(name = "oldest_price_date")
    private Instant oldestPriceDate;
    /**
     * This field stores the newest currency price we have for the current day (id.referenceDate)
     */
    @Column(name = "newest_price")
    private BigDecimal newestPrice;
    @Column(name = "newest_price_date")
    private Instant newestPriceDate;
    /**
     * This field is currently not used since we do not have input data for it.
     * Could be used in the future to hold single, final value of a price at the end of the day.
     */
    @Column(name = "final_price")
    private BigDecimal finalPrice;
    /**
     * This property stores a normalized weekly factor for a given currency. Here 'weekly' means a week period to date.
     * That means that this factor is calculated using a data from a 7 days back up to now (that is, the current date which is an
     * id.referenceDate).
     */
    @Column(name = "week_normalized_factor")
    private BigDecimal weekNormalizedFactor;
    /**
     * This property stores a normalized monthly factor for a given currency. Here 'monthly' means a month period to date.
     * That means that this factor is calculated using a data from a 31 days back up to now (that is, the current date which is an
     * id.referenceDate).
     */
    @Column(name = "month_normalized_factor")
    private BigDecimal monthNormalizedFactor;

    public CryptoDailyRecentFactors(DailyRecentFactorId id, BigDecimal minPrice, BigDecimal maxPrice) {
        this.id = id;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
    }
}
