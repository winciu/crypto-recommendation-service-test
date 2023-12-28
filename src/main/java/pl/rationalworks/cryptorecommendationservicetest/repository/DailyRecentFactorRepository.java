package pl.rationalworks.cryptorecommendationservicetest.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.rationalworks.cryptorecommendationservicetest.model.CryptoDailyRecentFactors;
import pl.rationalworks.cryptorecommendationservicetest.model.DailyRecentFactorId;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyRecentFactorRepository extends CrudRepository<CryptoDailyRecentFactors, DailyRecentFactorId> {
    @Modifying
    @Query(value = """
        UPDATE CryptoDailyRecentFactors f
        SET f.minPrice = :minPrice, f.maxPrice = :maxPrice
        WHERE f.id = :id
        """)
    void updateMinMaxFactorsByDate(@Param("id") DailyRecentFactorId id,
                                   @Param("minPrice") BigDecimal minPrice,
                                   @Param("maxPrice") BigDecimal maxPrice);

    @Modifying
    @Query(value = """
        UPDATE CryptoDailyRecentFactors f
        SET f.oldestPrice = :oldestPrice, f.oldestPriceDate = :oldestPriceDate
        WHERE f.id = :id
        """)
    void updateOldestPriceFactors(@Param("id") DailyRecentFactorId id,
                                  @Param("oldestPrice") BigDecimal oldestPrice,
                                  @Param("oldestPriceDate") Instant oldestPriceDate);

    @Modifying
    @Query(value = """
        UPDATE CryptoDailyRecentFactors f
        SET f.newestPrice = :newestPrice, f.newestPriceDate = :newestPriceDate
        WHERE f.id = :id
        """)
    void updateNewestPriceFactors(@Param("id") DailyRecentFactorId id,
                                  @Param("newestPrice") BigDecimal newestPrice,
                                  @Param("newestPriceDate") Instant newestPriceDate);

    @Modifying
    @Query(value = """
        UPDATE CryptoDailyRecentFactors f
        SET f.weekNormalizedFactor = :factor
        WHERE f.id = :id
        """)
    void updateWeeklyNormalizedFactor(@Param("id") DailyRecentFactorId id,
                                      @Param("factor") BigDecimal weekNormalizedFactor);

    @Modifying
    @Query(value = """
        UPDATE CryptoDailyRecentFactors f
        SET f.monthNormalizedFactor = :factor
        WHERE f.id = :id
        """)
    void updateMonthlyNormalizedFactor(@Param("id") DailyRecentFactorId id,
                                       @Param("factor") BigDecimal monthlyNormalizedFactor);

    @Query(value = """
        SELECT f.id.symbol
        FROM CryptoDailyRecentFactors f
        where f.id.referenceDate = :date
        order by f.dailyNormalizedFactor desc
        """)
    List<String> obtainDailyCryptoRanking(@Param("date") LocalDate date);

    @Query(value = """
        select f
        from CryptoDailyRecentFactors f
        """)
    CryptoRecentPriceFactors evaluateWeeklyPriceFactors(DailyRecentFactorId dailyRecentFactorId);
}
