package pl.rationalworks.cryptorecommendationservicetest.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.rationalworks.cryptorecommendationservicetest.model.CryptoCurrency;
import pl.rationalworks.cryptorecommendationservicetest.model.CryptoCurrencyId;

import java.util.List;

@Repository
public interface CryptoCurrencyRepository extends CrudRepository<CryptoCurrency, CryptoCurrencyId> {

    /**
     * @param date date in the following format 'yyyy-MM-dd'
     * @return list of records with selected min/max value for particular day and cryptocurrency
     */
    @Query(name = "selectMinMaxPricesByDayGroupBySymbol", nativeQuery = true)
    List<DailyMinMaxRecord> fetchMinMaxValuesForGivenDay(@Param("date") String date);

    @Query(name = "selectDailyMinPrice", nativeQuery = true)
    List<CryptoCurrency> fetchDailyMinPrice(@Param("date") String date);
}



