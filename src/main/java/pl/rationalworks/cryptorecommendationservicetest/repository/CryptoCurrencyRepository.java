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
    List<DailyMinMaxRecord> fetchMinMaxValuesForGivenDay(@Param("date") LocalDate date);

    @Query(name = "selectDailyMinPrice", nativeQuery = true)
    List<CryptoCurrency> fetchDailyMinPrice(@Param("date") LocalDate date);

    @Query(name = "selectDailyMaxPrice", nativeQuery = true)
    List<CryptoCurrency> fetchDailyMaxPrice(@Param("date") LocalDate date);
}



