package pl.rationalworks.cryptorecommendationservicetest.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "crypto_currencies")
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CryptoCurrency {

    @EmbeddedId
    private CryptoCurrencyId id;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

}
