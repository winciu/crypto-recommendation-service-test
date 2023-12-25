package pl.rationalworks.cryptorecommendationservicetest.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pl.rationalworks.cryptorecommendationservicetest.model.CryptoDailyRecentFactors;
import pl.rationalworks.cryptorecommendationservicetest.model.DailyRecentFactorId;

@Repository
public interface DailyRecentFactorRepository extends CrudRepository<CryptoDailyRecentFactors, DailyRecentFactorId> {
}
