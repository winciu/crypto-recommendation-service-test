package pl.rationalworks.cryptorecommendationservicetest.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pl.rationalworks.cryptorecommendationservicetest.model.CryptoCurrency;
import pl.rationalworks.cryptorecommendationservicetest.model.CryptoCurrencyId;

@Repository
public interface CryptoCurrencyRepository extends CrudRepository<CryptoCurrency, CryptoCurrencyId> {
}
