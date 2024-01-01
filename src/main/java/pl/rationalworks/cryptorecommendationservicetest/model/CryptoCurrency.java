package pl.rationalworks.cryptorecommendationservicetest.model;

import jakarta.persistence.*;
import lombok.*;
import pl.rationalworks.cryptorecommendationservicetest.repository.CryptoDailyPriceFactors;
import pl.rationalworks.cryptorecommendationservicetest.repository.NormalizedFactor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@NamedNativeQueries({
    @NamedNativeQuery(name = "evaluateDailyFactorsGroupBySymbol",
        query = """
            WITH AGG(symbol, min_price, max_price, oldest_price_date, newest_price_date, normalized_factor) AS
                     (select symbol,
                             min(price)                             as min_price,
                             max(price)                             as max_price,
                             min(timestamp)                         as oldest_price_date,
                             max(timestamp)                         as newest_price_date,
                             (max(price) - min(price)) / min(price) as normalized_factor
                      from crypto_currencies
                      where date = :date
                      group by symbol)
            select AGG.*,
                   (select min(timestamp)
                    from crypto_currencies cc
                    where date = :date
                      and cc.symbol = AGG.symbol
                      and price = AGG.min_price)       min_price_date,
                   (select max(timestamp)
                    from crypto_currencies cc
                    where date = :date
                      and cc.symbol = AGG.symbol
                      and price = AGG.max_price)       max_price_date,
                   (select price
                    from crypto_currencies cc
                    where date = :date
                      and cc.symbol = AGG.symbol
                      and timestamp = AGG.oldest_price_date) oldest_price,
                   (select price
                    from crypto_currencies cc
                    where date = :date
                      and cc.symbol = AGG.symbol
                      and timestamp = AGG.newest_price_date) newest_price
            from AGG;
            """,
        resultSetMapping = "dailyEvaluationFactorsMapping"),
    @NamedNativeQuery(name = "selectNormalizedFactorsGroupBySymbol",
        query = """
            WITH AGG(min_price, max_price, symbol, date) AS
                     (select min(price) as min_price, max(price) as max_price, symbol, date
                      from crypto_currencies
                      where DATEDIFF(DAY, :date, date) > :daysBack
                        AND DATEDIFF(DAY, :date, date) <= 0
                      group by symbol, date)
            select symbol, (sum(AGG.max_price) - sum(AGG.min_price)) / sum(AGG.min_price) as normalized_factor
            from AGG
            group by symbol;
            """,
        resultSetMapping = "normalizedFactorMapping")
})
@SqlResultSetMappings({
    @SqlResultSetMapping(
        name = "dailyEvaluationFactorsMapping",
        classes = {
            @ConstructorResult(
                columns = {
                    @ColumnResult(name = "symbol", type = String.class),
                    @ColumnResult(name = "min_price", type = BigDecimal.class),
                    @ColumnResult(name = "min_price_date", type = Instant.class),
                    @ColumnResult(name = "max_price", type = BigDecimal.class),
                    @ColumnResult(name = "max_price_date", type = Instant.class),
                    @ColumnResult(name = "oldest_price", type = BigDecimal.class),
                    @ColumnResult(name = "oldest_price_date", type = Instant.class),
                    @ColumnResult(name = "newest_price", type = BigDecimal.class),
                    @ColumnResult(name = "newest_price_date", type = Instant.class),
                    @ColumnResult(name = "normalized_factor", type = BigDecimal.class)
                },
                targetClass = CryptoDailyPriceFactors.class
            )
        }
    ),
    @SqlResultSetMapping(
        name = "normalizedFactorMapping",
        classes = {
            @ConstructorResult(
                columns = {
                    @ColumnResult(name = "symbol", type = String.class),
                    @ColumnResult(name = "normalized_factor", type = BigDecimal.class)
                },
                targetClass = NormalizedFactor.class
            )
        }
    )
})
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

    @Column(name = "date", nullable = false, updatable = false)
    private LocalDate date;

    @Column(name = "price", nullable = false, precision = 16, scale = 5)
    private BigDecimal price;

}
