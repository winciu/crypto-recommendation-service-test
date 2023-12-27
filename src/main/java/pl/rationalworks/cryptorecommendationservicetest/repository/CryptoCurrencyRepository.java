package pl.rationalworks.cryptorecommendationservicetest.repository;

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
     * @return list of records with selected min/max value for particular day and cryptocurrency
     */
    @Query(name = "selectMinMaxPricesByDayGroupBySymbol", nativeQuery = true)
    List<DailyMinMaxRecord> fetchMinMaxPricesForGivenDay(@Param("date") LocalDate date);

    @Query(name = "selectDailyOldestPrice", nativeQuery = true)
    List<CryptoCurrency> fetchDailyOldestPrice(@Param("date") LocalDate date);

    @Query(name = "selectDailyNewestPrice", nativeQuery = true)
    List<CryptoCurrency> fetchDailyNewestPrice(@Param("date") LocalDate date);
}



