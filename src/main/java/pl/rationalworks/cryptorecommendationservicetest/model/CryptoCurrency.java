package pl.rationalworks.cryptorecommendationservicetest.model;

import jakarta.persistence.*;
import lombok.*;
import pl.rationalworks.cryptorecommendationservicetest.repository.DailyMinMaxRecord;

import java.math.BigDecimal;

@NamedNativeQueries({
    @NamedNativeQuery(name = "selectMinMaxPricesByDayGroupBySymbol",
        query = """
            select min(price) as minPrice, max(price) as maxPrice, symbol
            from crypto_currencies
            where FORMATDATETIME(timestamp,'yyyy-MM-dd') = ':date' 
            group by symbol""",
        resultSetMapping = "minMaxValuesGroupBySymbolMapping"),
    @NamedNativeQuery(name = "selectDailyMinPrice",
        query = """
            select * 
            from crypto_currencies
            where timestamp in (
                 select
                    min(timestamp)
                from
                    crypto_currencies
                where
                    FORMATDATETIME(timestamp, 'yyyy-MM-dd') = ':date'
                 group by symbol);
            """)
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

    @Column(name = "price", nullable = false, precision = 14, scale = 5)
    private BigDecimal price;

}
