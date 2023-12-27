package pl.rationalworks.cryptorecommendationservicetest.model;

import jakarta.persistence.*;
import lombok.*;
import pl.rationalworks.cryptorecommendationservicetest.repository.DailyMinMaxRecord;

import java.math.BigDecimal;
import java.time.LocalDate;

@NamedNativeQueries({
    @NamedNativeQuery(name = "selectMinMaxPricesByDayGroupBySymbol",
        query = """
            select min(price) as minPrice, max(price) as maxPrice, symbol
            from crypto_currencies
            where date = :date 
            group by symbol""",
        resultSetMapping = "minMaxValuesGroupBySymbolMapping"),
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
        resultClass = CryptoCurrency.class)
})
@SqlResultSetMappings(
    @SqlResultSetMapping(
        name = "minMaxValuesGroupBySymbolMapping",
        classes = {
            @ConstructorResult(
                columns = {
                    @ColumnResult(name = "minPrice", type = BigDecimal.class),
                    @ColumnResult(name = "maxPrice", type = BigDecimal.class),
                    @ColumnResult(name = "symbol", type = String.class)
                },
                targetClass = DailyMinMaxRecord.class
            )
        }
    )
)
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

    @Column(name = "price", nullable = false, precision = 14, scale = 5)
    private BigDecimal price;

}
