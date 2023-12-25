package pl.rationalworks.cryptorecommendationservicetest.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDate;

@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
@Embeddable
@Getter
public class DailyRecentFactorId implements Serializable {

    @Column(name = "symbol")
    private String symbol;
    @Column(name = "reference_date")
    private LocalDate referenceDate;
}
