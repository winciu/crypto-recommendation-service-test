package pl.rationalworks.cryptorecommendationservicetest.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.rationalworks.cryptorecommendationservicetest.model.CryptoCurrency;
import pl.rationalworks.cryptorecommendationservicetest.model.CryptoCurrencyId;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CryptoCurrencyRepository extends CrudRepository<CryptoCurrency, CryptoCurrencyId> {

    /**
     * @param date date in the following format 'yyyy-MM-dd'
     * @return list of records with evaluate daily factors for particular day and cryptocurrency
     */
    @Query(name = "evaluateDailyFactorsGroupBySymbol", nativeQuery = true)
    List<CryptoDailyPriceFactors> evaluateDailyFactors(@Param("date") LocalDate date);

    /**
     * @param date   a data from which to start aggregating data for evaluating a normalized factor (inclusive)
     * @param daysBack Number of days back to calculate a normalized factor.
     * @return list of {@link NormalizedFactor} instance for each cryptocurrency symbol
     */
    @Query(name = "selectNormalizedFactorsGroupBySymbol", nativeQuery = true)
    List<NormalizedFactor> fetchNormalizedFactors(@Param("date") LocalDate date, @Param("daysBack") int daysBack);

    @Query(name = "findDistinctDatesForUnprocessedEntries")
    List<LocalDate> selectAllDistinctDatesForUnprocessedEntries();

    @Query(value = """
        update CryptoCurrency set processed = true where date = :date
        """)
    @Modifying
    void markDataAsProcessed(@Param("date") LocalDate date);
}



