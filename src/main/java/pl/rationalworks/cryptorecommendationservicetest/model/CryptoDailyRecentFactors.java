package pl.rationalworks.cryptorecommendationservicetest.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 *
 */
@Entity
@Table(name = "daily_recent_factors")
public class CryptoDailyRecentFactors {

    @EmbeddedId
    private DailyRecentFactorId id;
    @Column(name = "min_price")
    private BigDecimal minPrice;
    @Column(name = "max_price")
    private BigDecimal maxPrice;
    @Column(name = "final_price")
    private BigDecimal finalPrice;
    @Column(name = "week_normalized_factor")
    private BigDecimal weekNormalizedFactor;
    @Column(name = "month_normalized_factor")
    private BigDecimal monthNormalizedFactor;

}
