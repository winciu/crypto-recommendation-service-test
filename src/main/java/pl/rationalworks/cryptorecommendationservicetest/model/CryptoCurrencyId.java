package pl.rationalworks.cryptorecommendationservicetest.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.time.Instant;

@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Embeddable
@Getter
public class CryptoCurrencyId implements Serializable {
    @Column(name = "timestamp", nullable = false, updatable = false)
    private final Instant timestamp;
    @Column(name = "symbol", nullable = false, updatable = false)
    private final String symbol;

    public CryptoCurrencyId() {
        this(null, null);
    }
}
