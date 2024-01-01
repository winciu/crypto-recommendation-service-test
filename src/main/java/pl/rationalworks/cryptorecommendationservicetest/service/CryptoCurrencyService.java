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
import pl.rationalworks.cryptorecommendationservicetest.repository.CryptoDailyPriceFactors;
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
    public void evaluateDailyFactors(LocalDate date) {
        evaluateDailyFactors(cryptoCurrencyRepository.evaluateDailyFactors(date),
            record -> new DailyRecentFactorId(record.symbol(), date),
            CryptoDailyAggregatedFactors::setupDailyEvaluationFactors,
            f -> dailyRecentFactorRepository.updateMinMaxPriceFactors(f.getId(), f.getMinPrice(), f.getMinPriceDate(),
                f.getMaxPrice(), f.getMaxPriceDate(), f.getOldestPrice(), f.getOldestPriceDate(),
                f.getNewestPrice(), f.getNewestPriceDate(), f.getDailyNormalizedFactor())
        );
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void evaluateNormalizedFactors(LocalDate date, FactorPeriod period) {
        evaluateDailyFactors(cryptoCurrencyRepository.fetchNormalizedFactors(date, period.getDaysBack()),
            nf -> new DailyRecentFactorId(nf.symbol(), date),
            (fid, nf) -> {
                if (FactorPeriod.WEEK.equals(period)) {
                    return CryptoDailyAggregatedFactors.setupNormalizedWeeklyFactors(fid, nf.factorValue());
                }
                if (FactorPeriod.MONTH.equals(period)) {
                    return CryptoDailyAggregatedFactors.setupNormalizedMonthlyFactors(fid, nf.factorValue());
                }
                return new CryptoDailyAggregatedFactors(); // an empty factor (be default, just in case)
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

    private <IN> void evaluateDailyFactors(List<IN> cryptoCurrencies,
                                           Function<IN, DailyRecentFactorId> factorIdKeyMapperFunction,
                                           BiFunction<DailyRecentFactorId, IN, CryptoDailyAggregatedFactors> cryptoFactorSupplier,
                                           Consumer<CryptoDailyAggregatedFactors> updateMethodSupplier) {
        //attach factor ids
        Map<DailyRecentFactorId, IN> cryptosWithFactorIds = cryptoCurrencies.stream()
            .collect(toMap(factorIdKeyMapperFunction, Function.identity()));
        // fetch existing factors
        Map<DailyRecentFactorId, CryptoDailyAggregatedFactors> existingFactors = fetchExistingFactors(cryptosWithFactorIds.keySet());

        List<CryptoDailyAggregatedFactors> newFactors = new ArrayList<>();
        List<CryptoDailyAggregatedFactors> toBeUpdatedFactors = new ArrayList<>();

        cryptosWithFactorIds.forEach((fid, cc) -> {
            CryptoDailyAggregatedFactors factors = cryptoFactorSupplier.apply(fid, cc);
            if (existingFactors.containsKey(fid)) { // it's an update
                toBeUpdatedFactors.add(factors);
            } else { // it's an insert
                newFactors.add(factors);
            }
        });
        toBeUpdatedFactors.forEach(updateMethodSupplier);
        dailyRecentFactorRepository.saveAll(newFactors);
    }

    private Map<DailyRecentFactorId, CryptoDailyAggregatedFactors> fetchExistingFactors(Iterable<DailyRecentFactorId> factorIds) {
        Iterable<CryptoDailyAggregatedFactors> factorsById = dailyRecentFactorRepository.findAllById(factorIds);
        return StreamSupport
            .stream(factorsById.spliterator(), false)
            .collect(toMap(CryptoDailyAggregatedFactors::getId, Function.identity()));
    }

    public List<CryptoCurrencyDto> cryptoRanking(LocalDate date, FactorPeriod period) {
        int noLimit = Integer.MAX_VALUE;
        List<String> symbols = dailyRecentFactorRepository.selectBestCryptosByNormalizedFactor(date, period.name(), noLimit);
        return symbols.stream().map(CryptoCurrencyDto::new).toList();
    }

    public Optional<CryptoDailyPriceFactors> getCryptoPriceFactors(@NotBlank @Pattern(regexp = "[A-Z]{3,6}") String symbol,
                                                                   LocalDate date, FactorPeriod period) {
        return switch (period) {
            case DAY -> {
                Optional<CryptoDailyAggregatedFactors> dailyFactors = dailyRecentFactorRepository.findById(new DailyRecentFactorId(symbol, date));
                if (dailyFactors.isPresent()) {
                    yield dailyFactors
                        .map(df -> new CryptoDailyPriceFactors(df.getId().getSymbol(),
                            df.getMinPrice(), df.getMinPriceDate(), df.getMaxPrice(), df.getMaxPriceDate(),
                            df.getOldestPrice(), df.getOldestPriceDate(),
                            df.getNewestPrice(), df.getNewestPriceDate(), df.getDailyNormalizedFactor()));
                } else {
                    yield Optional.empty();
                }
            }
            case WEEK, MONTH ->
                dailyRecentFactorRepository.evaluateAggregatedMinMaxPriceFactors(symbol, date, period.getDaysBack());
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
