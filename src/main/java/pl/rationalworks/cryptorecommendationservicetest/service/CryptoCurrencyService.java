package pl.rationalworks.cryptorecommendationservicetest.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pl.rationalworks.cryptorecommendationservicetest.data.CsvDataRecord;
import pl.rationalworks.cryptorecommendationservicetest.model.*;
import pl.rationalworks.cryptorecommendationservicetest.repository.CryptoCurrencyRepository;
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
import static pl.rationalworks.cryptorecommendationservicetest.model.CryptoDailyRecentFactors.setupDailyEvaluationFactors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CryptoCurrencyService {

    private final CryptoCurrencyRepository cryptoCurrencyRepository;
    private final DailyRecentFactorRepository dailyRecentFactorRepository;

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
        updateRecentFactors(cryptoCurrencyRepository.evaluateDailyFactors(date),
            record -> new DailyRecentFactorId(record.symbol(), date),
            (fid, r) -> setupDailyEvaluationFactors(fid, r.minPrice(), r.maxPrice(), r.normalizedFactor()),
            f -> dailyRecentFactorRepository.updateMinMaxFactorsByDate(f.getId(), f.getMinPrice(), f.getMaxPrice())
        );
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void updateDailyOldestPriceFactors(LocalDate date) {
        updateRecentFactors(cryptoCurrencyRepository.fetchDailyOldestPrice(date),
            cc -> new DailyRecentFactorId(cc.getId().getSymbol(), cc.getDate()),
            (fid, cc) -> CryptoDailyRecentFactors.setupOldestPriceFactors(fid, cc.getPrice(), cc.getId().getTimestamp()),
            f -> dailyRecentFactorRepository.updateOldestPriceFactors(f.getId(), f.getOldestPrice(), f.getOldestPriceDate()));
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void updateDailyNewestPriceFactors(LocalDate date) {
        updateRecentFactors(cryptoCurrencyRepository.fetchDailyNewestPrice(date),
            cc -> new DailyRecentFactorId(cc.getId().getSymbol(), cc.getDate()),
            (fid, cc) -> CryptoDailyRecentFactors.setupNewestPriceFactors(fid, cc.getPrice(), cc.getId().getTimestamp()),
            f -> dailyRecentFactorRepository.updateNewestPriceFactors(f.getId(), f.getNewestPrice(), f.getNewestPriceDate()));
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void evaluateNormalizedFactors(LocalDate date, FactorPeriod period) {
        updateRecentFactors(cryptoCurrencyRepository.fetchNormalizedFactors(date, period.getDaysBack()),
            nf -> new DailyRecentFactorId(nf.symbol(), date),
            (fid, nf) -> {
                if (FactorPeriod.WEEK.equals(period)) {
                    return CryptoDailyRecentFactors.setupNormalizedWeeklyFactors(fid, nf.factorValue());
                }
                if (FactorPeriod.MONTH.equals(period)) {
                    return CryptoDailyRecentFactors.setupNormalizedMonthlyFactors(fid, nf.factorValue());
                }
                return new CryptoDailyRecentFactors(); // an empty factor (be default, just in case)
            },
            f -> {
                if (FactorPeriod.WEEK.equals(period)) {
                    dailyRecentFactorRepository.updateWeeklyNormalizedFactor(f.getId(), f.getWeekNormalizedFactor());
                }
                if (FactorPeriod.MONTH.equals(period)) {
                    dailyRecentFactorRepository.updateMonthlyNormalizedFactor(f.getId(), f.getMonthNormalizedFactor());
                }
            }
        );
    }

    private <IN> void updateRecentFactors(List<IN> cryptoCurrencies,
                                          Function<IN, DailyRecentFactorId> factorIdKeyMapperFunction,
                                          BiFunction<DailyRecentFactorId, IN, CryptoDailyRecentFactors> cryptoFactorSupplier,
                                          Consumer<CryptoDailyRecentFactors> updateMethodSupplier) {
        //attach factor ids
        Map<DailyRecentFactorId, IN> cryptosWithFactorIds = cryptoCurrencies.stream()
            .collect(toMap(factorIdKeyMapperFunction, Function.identity()));
        // fetch existing factors
        Map<DailyRecentFactorId, CryptoDailyRecentFactors> existingFactors = fetchExistingFactors(cryptosWithFactorIds.keySet());

        List<CryptoDailyRecentFactors> newFactors = new ArrayList<>();
        List<CryptoDailyRecentFactors> toBeUpdatedFactors = new ArrayList<>();

        cryptosWithFactorIds.forEach((fid, cc) -> {
            CryptoDailyRecentFactors factors = cryptoFactorSupplier.apply(fid, cc);
            if (existingFactors.containsKey(fid)) { // it's an update
                toBeUpdatedFactors.add(factors);
            } else { // it's an insert
                newFactors.add(factors);
            }
        });
        toBeUpdatedFactors.forEach(updateMethodSupplier);
        dailyRecentFactorRepository.saveAll(newFactors);
    }

    private Map<DailyRecentFactorId, CryptoDailyRecentFactors> fetchExistingFactors(Iterable<DailyRecentFactorId> factorIds) {
        Iterable<CryptoDailyRecentFactors> factorsById = dailyRecentFactorRepository.findAllById(factorIds);
        return StreamSupport
            .stream(factorsById.spliterator(), false)
            .collect(toMap(CryptoDailyRecentFactors::getId, Function.identity()));
    }
}
