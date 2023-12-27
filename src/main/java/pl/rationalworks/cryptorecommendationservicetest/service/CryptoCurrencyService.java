package pl.rationalworks.cryptorecommendationservicetest.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pl.rationalworks.cryptorecommendationservicetest.data.CsvDataRecord;
import pl.rationalworks.cryptorecommendationservicetest.model.CryptoCurrency;
import pl.rationalworks.cryptorecommendationservicetest.model.CryptoCurrencyId;
import pl.rationalworks.cryptorecommendationservicetest.model.CryptoDailyRecentFactors;
import pl.rationalworks.cryptorecommendationservicetest.model.DailyRecentFactorId;
import pl.rationalworks.cryptorecommendationservicetest.repository.CryptoCurrencyRepository;
import pl.rationalworks.cryptorecommendationservicetest.repository.DailyMinMaxRecord;
import pl.rationalworks.cryptorecommendationservicetest.repository.DailyRecentFactorRepository;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@Slf4j
@RequiredArgsConstructor
public class CryptoCurrencyService {

    private final CryptoCurrencyRepository cryptoCurrencyRepository;
    private final DailyRecentFactorRepository dailyRecentFactorRepository;

    public void saveCryptos(List<CsvDataRecord> dataRecords) {
        List<CryptoCurrency> cryptoCurrencies = dataRecords.stream()
                .map(r -> {
                    CryptoCurrencyId id = new CryptoCurrencyId(r.timestamp(), r.symbol());
                    return new CryptoCurrency(id, LocalDate.ofInstant(r.timestamp(), ZoneId.of("GMT")), r.price());
                })
                .toList();
        cryptoCurrencyRepository.saveAll(cryptoCurrencies);
    }

    private Map<DailyRecentFactorId, CryptoDailyRecentFactors> calculateMinMaxValuesForGivenDay(LocalDate date) {
        List<DailyMinMaxRecord> records = cryptoCurrencyRepository.fetchMinMaxValuesForGivenDay(date);
        log.info("fetched {} new daily factors", records.size());
        return records.stream()
            .map(record -> {
                DailyRecentFactorId factorId = new DailyRecentFactorId(record.symbol(), date);
                return new CryptoDailyRecentFactors(factorId, record.minPrice(), record.maxPrice());
            })
            .collect(Collectors.toMap(CryptoDailyRecentFactors::getId, Function.identity()));
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void updateMinMaxFactorValuesForGivenDay(LocalDate date) {
        Map<DailyRecentFactorId, CryptoDailyRecentFactors> newDailyRecentFactors = calculateMinMaxValuesForGivenDay(date);

        Iterable<CryptoDailyRecentFactors> factorsById = dailyRecentFactorRepository.findAllById(newDailyRecentFactors.keySet());
        Map<DailyRecentFactorId, CryptoDailyRecentFactors> existingFactors = StreamSupport
            .stream(factorsById.spliterator(), false)
            .collect(Collectors.toMap(CryptoDailyRecentFactors::getId, Function.identity()));

        List<CryptoDailyRecentFactors> newFactors = new ArrayList<>();
        List<CryptoDailyRecentFactors> updatedFactors = new ArrayList<>();
        newDailyRecentFactors.forEach((newId, newFactor) -> {
            if (existingFactors.containsKey(newId)) { // it's an update
                updatedFactors.add(newFactor);
            } else { // it's an insert
                newFactors.add(newFactor);
            }
        });
        updatedFactors.forEach(f -> dailyRecentFactorRepository.updateMinMaxFactorsByDate(f.getId(), f.getMinPrice(), f.getMaxPrice()));
        dailyRecentFactorRepository.saveAll(newFactors);
    }

}
