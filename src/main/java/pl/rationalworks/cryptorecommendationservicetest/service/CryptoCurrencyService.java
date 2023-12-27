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
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class CryptoCurrencyService {

    private final CryptoCurrencyRepository cryptoCurrencyRepository;
    private final DailyRecentFactorRepository dailyRecentFactorRepository;

    private static Map<DailyRecentFactorId, CryptoCurrency> attachFactorIds(List<CryptoCurrency> cryptoCurrencies) {
        return cryptoCurrencies.stream()
            .collect(toMap(cc -> new DailyRecentFactorId(cc.getId().getSymbol(), cc.getDate()), Function.identity()));
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void saveCryptos(List<CsvDataRecord> dataRecords) {
        List<CryptoCurrency> cryptoCurrencies = dataRecords.stream()
                .map(r -> {
                    CryptoCurrencyId id = new CryptoCurrencyId(r.timestamp(), r.symbol());
                    return new CryptoCurrency(id, LocalDate.ofInstant(r.timestamp(), ZoneId.of("GMT")), r.price());
                })
                .toList();
        cryptoCurrencyRepository.saveAll(cryptoCurrencies);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void updateMinMaxFactorValuesForGivenDay(LocalDate date) {
        Map<DailyRecentFactorId, CryptoDailyRecentFactors> newDailyRecentFactors = fetchMinMaxPricesForGivenDay(date);
        Map<DailyRecentFactorId, CryptoDailyRecentFactors> existingFactors = fetchExistingFactors(newDailyRecentFactors.keySet());

        List<CryptoDailyRecentFactors> newFactors = new ArrayList<>();
        List<CryptoDailyRecentFactors> toBeUpdatedFactors = new ArrayList<>();
        newDailyRecentFactors.forEach((fid, newFactor) -> {
            if (existingFactors.containsKey(fid)) { // it's an update
                toBeUpdatedFactors.add(newFactor);
            } else { // it's an insert
                newFactors.add(newFactor);
            }
        });
        toBeUpdatedFactors.forEach(f -> dailyRecentFactorRepository.updateMinMaxFactorsByDate(f.getId(), f.getMinPrice(), f.getMaxPrice()));
        dailyRecentFactorRepository.saveAll(newFactors);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void updateDailyOldestPriceFactors(LocalDate date) {
        List<CryptoCurrency> dailyOldestCryptos = cryptoCurrencyRepository.fetchDailyOldestPrice(date);
        updateDailyOldestNewestPriceFactors(dailyOldestCryptos,
            (fid, cc) -> CryptoDailyRecentFactors.setupOldestPriceFactors(fid, cc.getPrice(), cc.getId().getTimestamp()),
            f -> dailyRecentFactorRepository.updateOldestPriceFactors(f.getId(), f.getOldestPrice(), f.getOldestPriceDate()));
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void updateDailyNewestPriceFactors(LocalDate date) {
        List<CryptoCurrency> dailyNewestCryptos = cryptoCurrencyRepository.fetchDailyNewestPrice(date);
        updateDailyOldestNewestPriceFactors(dailyNewestCryptos,
            (fid, cc) -> CryptoDailyRecentFactors.setupNewestPriceFactors(fid, cc.getPrice(), cc.getId().getTimestamp()),
            f -> dailyRecentFactorRepository.updateNewestPriceFactors(f.getId(), f.getNewestPrice(), f.getNewestPriceDate()));
    }

    private void updateDailyOldestNewestPriceFactors(List<CryptoCurrency> cryptoCurrencies,
                                                     BiFunction<DailyRecentFactorId, CryptoCurrency, CryptoDailyRecentFactors> cryptoFactorSupplier,
                                                     Consumer<CryptoDailyRecentFactors> updateMethodSupplier) {
        Map<DailyRecentFactorId, CryptoCurrency> cryptosWithFactorIds = attachFactorIds(cryptoCurrencies);
        // fetch existing factors
        Map<DailyRecentFactorId, CryptoDailyRecentFactors> existingFactors = fetchExistingFactors(cryptosWithFactorIds.keySet());

        List<CryptoDailyRecentFactors> newFactors = new ArrayList<>();
        List<CryptoDailyRecentFactors> toBeUpdatedFactors = new ArrayList<>();

        cryptosWithFactorIds.forEach((fid, cc) -> {
            CryptoDailyRecentFactors oldestPriceFactor = cryptoFactorSupplier.apply(fid, cc);
            if (existingFactors.containsKey(fid)) { // it's an update
                toBeUpdatedFactors.add(oldestPriceFactor);
            } else { // it's an insert
                newFactors.add(oldestPriceFactor);
            }
        });
        toBeUpdatedFactors.forEach(updateMethodSupplier);
        dailyRecentFactorRepository.saveAll(newFactors);
    }

    private Map<DailyRecentFactorId, CryptoDailyRecentFactors> fetchMinMaxPricesForGivenDay(LocalDate date) {
        List<DailyMinMaxRecord> records = cryptoCurrencyRepository.fetchMinMaxPricesForGivenDay(date);
        log.info("fetched {} new daily factors", records.size());
        return records.stream()
            .map(record -> {
                DailyRecentFactorId factorId = new DailyRecentFactorId(record.symbol(), date);
                return CryptoDailyRecentFactors.setupMinMaxPriceFactors(factorId, record.minPrice(), record.maxPrice());
            })
            .collect(toMap(CryptoDailyRecentFactors::getId, Function.identity()));
    }

    private Map<DailyRecentFactorId, CryptoDailyRecentFactors> fetchExistingFactors(Iterable<DailyRecentFactorId> factorIds) {
        Iterable<CryptoDailyRecentFactors> factorsById = dailyRecentFactorRepository.findAllById(factorIds);
        return StreamSupport
            .stream(factorsById.spliterator(), false)
            .collect(toMap(CryptoDailyRecentFactors::getId, Function.identity()));
    }
}
