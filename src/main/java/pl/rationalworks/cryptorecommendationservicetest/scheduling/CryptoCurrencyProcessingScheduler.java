package pl.rationalworks.cryptorecommendationservicetest.scheduling;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.rationalworks.cryptorecommendationservicetest.model.FactorPeriod;
import pl.rationalworks.cryptorecommendationservicetest.service.CryptoCurrencyService;

import java.time.LocalDate;
import java.util.List;

import static java.time.LocalDate.now;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "service.scheduling.enabled", matchIfMissing = true)
public class CryptoCurrencyProcessingScheduler {

    private final CryptoCurrencyService service;

    /**
     * By default, scheduler should process all entries starting from the current day (today).
     */
    @Scheduled(cron = "${service.scheduling.cron}", zone = "${service.scheduling.timezone}")
    public void startCryptoProcessing() {
        LocalDate date = now();
        List<LocalDate> unprocessedDates = service.findUnprocessedDates();
        log.info("Unprocessed dates' list has {} remaining items: {}", unprocessedDates.size(), unprocessedDates);
        if (!unprocessedDates.isEmpty()) {
            date = unprocessedDates.get(0);
        }
        updateMinMaxFactors(date);
        evaluateWeeklyCryptosNormalizedRange(date);
        evaluateMonthlyCryptosNormalizedRange(date);
        setRowsAsProcessed(date);
    }

    private void setRowsAsProcessed(LocalDate date) {
        service.markDataAsProcessed(date);
    }

    private void evaluateWeeklyCryptosNormalizedRange(LocalDate date) {
        log.info("Scheduled task EVALUATE_NORMALIZED_WEEKLY_FACTORS for {} started ...", date);
        service.evaluateNormalizedFactors(date, FactorPeriod.WEEK);
        log.info("Scheduled task EVALUATE_NORMALIZED_WEEKLY_FACTORS for {} finished.", date);
    }

    private void evaluateMonthlyCryptosNormalizedRange(LocalDate date) {
        log.info("Scheduled task EVALUATE_NORMALIZED_MONTHLY_FACTORS for {} started ...", date);
        service.evaluateNormalizedFactors(date, FactorPeriod.MONTH);
        log.info("Scheduled task EVALUATE_NORMALIZED_MONTHLY_FACTORS for {} finished.", date);
    }

    /**
     *
     *
     * @param date a {@link LocalDate} instance for which data processing should take place
     */
    private void updateMinMaxFactors(LocalDate date) {
        log.info("Scheduled task UPDATE_MIN_MAX_FACTORS for {} started ...", date);
        service.evaluateDailyFactors(date);
        log.info("Scheduled task UPDATE_MIN_MAX_FACTORS for {} finished.", date);
    }

}
