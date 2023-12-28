package pl.rationalworks.cryptorecommendationservicetest.model;

import jakarta.persistence.*;
import lombok.*;
import pl.rationalworks.cryptorecommendationservicetest.repository.DailyEvaluationRecord;
import pl.rationalworks.cryptorecommendationservicetest.repository.NormalizedFactor;

import java.math.BigDecimal;
import java.time.LocalDate;

@NamedNativeQueries({
    @NamedNativeQuery(name = "evaluateDailyFactorsGroupBySymbol",
        query = """
            select symbol,
                   min(price) as min_price,
                   max(price) as max_price,
                   (max(price) - min(price)) / min(price) as normalized_factor
            from crypto_currencies
            where date = :date
            group by symbol""",
        resultSetMapping = "dailyEvaluationFactorsMapping"),
    @NamedNativeQuery(name = "selectDailyOldestPrice",
        query = """
            WITH OLDEST(oldest_date, symbol) AS
                     (select min(timestamp) as oldest_date, symbol
                      from crypto_currencies
                      where date = :date
                      group by symbol)
            select cc.*
            from crypto_currencies cc,
                 OLDEST o
            where cc.timestamp = o.oldest_date
              AND cc.symbol = o.symbol;
            """,
        resultClass = CryptoCurrency.class),
    @NamedNativeQuery(name = "selectDailyNewestPrice",
        query = """
            WITH NEWEST(newest_date, symbol) AS
                     (select max(timestamp) as newest_date, symbol
                      from crypto_currencies
                      where date = :date
                      group by symbol)
            select cc.*
            from crypto_currencies cc,
                 NEWEST n
            where cc.timestamp = n.newest_date
              AND cc.symbol = n.symbol;
            """,
        resultClass = CryptoCurrency.class),
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
                    @ColumnResult(name = "max_price", type = BigDecimal.class),
                    @ColumnResult(name = "normalized_factor", type = BigDecimal.class)
                },
                targetClass = DailyEvaluationRecord.class
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
