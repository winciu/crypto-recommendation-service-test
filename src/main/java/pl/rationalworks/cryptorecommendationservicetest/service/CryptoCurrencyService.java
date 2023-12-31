package pl.rationalworks.cryptorecommendationservicetest.service;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pl.rationalworks.cryptorecommendationservicetest.data.CsvDataRecord;
import pl.rationalworks.cryptorecommendationservicetest.model.*;
import pl.rationalworks.cryptorecommendationservicetest.model.dto.CryptoCurrencyDto;
import pl.rationalworks.cryptorecommendationservicetest.repository.CryptoCurrencyRepository;
import pl.rationalworks.cryptorecommendationservicetest.repository.CryptoRecentPriceFactors;
import pl.rationalworks.cryptorecommendationservicetest.repository.DailyRecentFactorRepository;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
                    dailyRecentFactorRepository.updateWeeklyNormalizedFactor(f.getId(), f.getWeeklyNormalizedFactor());
                }
                if (FactorPeriod.MONTH.equals(period)) {
                    dailyRecentFactorRepository.updateMonthlyNormalizedFactor(f.getId(), f.getMonthlyNormalizedFactor());
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

    public List<CryptoCurrencyDto> cryptoRanking(LocalDate date, FactorPeriod period) {
        int noLimit = Integer.MAX_VALUE;
        List<String> symbols = dailyRecentFactorRepository.selectBestCryptosByNormalizedFactor(date, period.name(), noLimit);
        return symbols.stream().map(CryptoCurrencyDto::new).toList();
    }

    public Optional<CryptoRecentPriceFactors> getCryptoPriceFactors(@NotBlank @Pattern(regexp = "[A-Z]{3,6}") String symbol,
                                                          LocalDate date, FactorPeriod period) {
        return switch (period) {
            case DAY -> {
                Optional<CryptoDailyRecentFactors> dailyFactors = dailyRecentFactorRepository.findById(new DailyRecentFactorId(symbol, date));
                if (dailyFactors.isPresent()) {
                    yield dailyFactors
                        .map(df -> new CryptoRecentPriceFactors(df.getId().getSymbol(),
                            df.getMinPrice(), df.getMaxPrice(), df.getOldestPrice(), df.getOldestPriceDate(),
                            df.getNewestPrice(), df.getNewestPriceDate()));
                } else {
                    yield Optional.empty();
                }
            }
            case WEEK, MONTH -> {
                // we need to execute 2 SQL queries to an issue with handling aggregated WITH statement by Spring/Hibernate
                // if the issue is fixed it's enough to just call one service method (1 SQL query to get all the aggregated values)
                // for details, see CryptoDailyRecentFactors class for the @NamedNativeQuery(name = "evaluateAggregatedPriceFactors")
                Optional<CryptoRecentPriceFactors> firstFactors = dailyRecentFactorRepository.evaluateAggregatedMinMaxPriceFactors(symbol, date, period.getDaysBack());
                if (firstFactors.isPresent()) {
                    CryptoRecentPriceFactors factors = firstFactors.get();
                    Optional<CryptoRecentPriceFactors> remainingFactors =
                        dailyRecentFactorRepository.evaluateRestOfAggregatedPriceFactors(symbol, factors.oldestPriceDate(), factors.newestPriceDate());
                    yield remainingFactors.map(rf -> new CryptoRecentPriceFactors(symbol, factors.minPrice(),
                        factors.maxPrice(), rf.oldestPrice(), factors.oldestPriceDate(), rf.newestPrice(), factors.newestPriceDate()));
                } else {
                    yield Optional.empty();
                }
            }
            default -> Optional.empty();
        };
    }

    public Optional<CryptoCurrencyDto> getBestCrypto(LocalDate date, FactorPeriod period) {
        List<String> symbol = dailyRecentFactorRepository.selectBestCryptosByNormalizedFactor(date, period.name(), 1);
        if (!symbol.isEmpty()) {
            return Optional.of(new CryptoCurrencyDto(symbol.get(0)));
        } else {
            return Optional.empty();
        }
    }
}
