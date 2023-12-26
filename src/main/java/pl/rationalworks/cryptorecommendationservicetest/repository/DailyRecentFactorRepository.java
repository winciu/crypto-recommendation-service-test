package pl.rationalworks.cryptorecommendationservicetest.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.rationalworks.cryptorecommendationservicetest.model.CryptoDailyRecentFactors;
import pl.rationalworks.cryptorecommendationservicetest.model.DailyRecentFactorId;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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
}
