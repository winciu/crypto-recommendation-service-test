package pl.rationalworks.cryptorecommendationservicetest.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDate;

@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Embeddable
@Getter
public class DailyRecentFactorId implements Serializable {

    @Column(name = "symbol", nullable = false, updatable = false)
    private final String symbol;
    @Column(name = "reference_date", nullable = false, updatable = false)
    private final LocalDate referenceDate;

    public DailyRecentFactorId() {
        this(null, null);
    }
}
