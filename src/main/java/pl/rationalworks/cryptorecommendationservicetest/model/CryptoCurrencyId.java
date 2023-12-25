package pl.rationalworks.cryptorecommendationservicetest.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.time.Instant;

@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
@Embeddable
@Getter
public class CryptoCurrencyId implements Serializable {
    @Column(name = "timestamp", nullable = false, updatable = false)
    private Instant timestamp;
    @Column(name = "symbol", nullable = false, updatable = false)
    private String symbol;

}
