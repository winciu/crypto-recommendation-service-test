package pl.rationalworks.cryptorecommendationservicetest.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.rationalworks.cryptorecommendationservicetest.model.CryptoDailyAggregatedFactors;
import pl.rationalworks.cryptorecommendationservicetest.model.DailyRecentFactorId;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyRecentFactorRepository extends CrudRepository<CryptoDailyAggregatedFactors, DailyRecentFactorId> {
    @Modifying
    @Query(value = """
        UPDATE CryptoDailyAggregatedFactors f
        SET f.minPrice = :minPrice, f.minPriceDate = :minPriceDate,
            f.maxPrice = :maxPrice, f.maxPriceDate = :maxPriceDate,
            f.oldestPrice = :oldestPrice, f.oldestPriceDate = :oldestPriceDate,
            f.newestPrice = :newestPrice, f.newestPriceDate = :newestPriceDate,
            f.dailyNormalizedFactor = :dailyNormalizedFactor
        WHERE f.id = :id
        """)
    void updateMinMaxPriceFactors(@Param("id") DailyRecentFactorId id,
                                  @Param("minPrice") BigDecimal minPrice, @Param("minPriceDate") Instant minPriceDate,
                                  @Param("maxPrice") BigDecimal maxPrice, @Param("maxPriceDate") Instant maxPriceDate,
                                  @Param("oldestPrice") BigDecimal oldestPrice, @Param("oldestPriceDate") Instant oldestPriceDate,
                                  @Param("newestPrice") BigDecimal newestPrice, @Param("newestPriceDate") Instant newestPriceDate,
                                  @Param("dailyNormalizedFactor") BigDecimal dailyNormalizedFactor);

    @Modifying
    @Query(value = """
        UPDATE CryptoDailyAggregatedFactors f
        SET f.weeklyNormalizedFactor = :factor
        WHERE f.id = :id
        """)
    void updateWeeklyNormalizedFactor(@Param("id") DailyRecentFactorId id,
                                      @Param("factor") BigDecimal weekNormalizedFactor);

    @Modifying
    @Query(value = """
        UPDATE CryptoDailyAggregatedFactors f
        SET f.monthlyNormalizedFactor = :factor
        WHERE f.id = :id
        """)
    void updateMonthlyNormalizedFactor(@Param("id") DailyRecentFactorId id,
                                       @Param("factor") BigDecimal monthlyNormalizedFactor);

    @Query(name = "evaluateAggregatedPriceFactors", nativeQuery = true)
    Optional<CryptoDailyPriceFactors> evaluateAggregatedMinMaxPriceFactors(@Param("symbol") String symbol,
                                                                           @Param("date") LocalDate date,
                                                                           @Param("daysBack") int daysBack);


    @Query(name = "selectCryptosByNormalizedFactorAndPeriod", nativeQuery = true)
    List<String> selectBestCryptosByNormalizedFactor(@Param("date") LocalDate date,
                                                     @Param("period") String period,
                                                     @Param("lmt") int limit);
}
