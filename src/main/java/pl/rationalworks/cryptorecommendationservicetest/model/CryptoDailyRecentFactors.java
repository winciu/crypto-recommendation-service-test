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
     * This field is currently not used since we do not have input data for it.
     * Could be used in the future to hold single, final value of a price at the end of the day.
     */
    @Column(name = "final_price")
    private BigDecimal finalPrice;
    @Column(name = "week_normalized_factor")
    private BigDecimal weekNormalizedFactor;
    @Column(name = "month_normalized_factor")
    private BigDecimal monthNormalizedFactor;

    public CryptoDailyRecentFactors(DailyRecentFactorId id, BigDecimal minPrice, BigDecimal maxPrice) {
        this.id = id;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
    }
}
