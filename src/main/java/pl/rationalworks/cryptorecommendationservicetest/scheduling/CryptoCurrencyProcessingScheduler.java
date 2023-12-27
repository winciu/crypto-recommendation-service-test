package pl.rationalworks.cryptorecommendationservicetest.scheduling;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.rationalworks.cryptorecommendationservicetest.properties.SchedulingProperties;
import pl.rationalworks.cryptorecommendationservicetest.service.CryptoCurrencyService;

import java.time.LocalDate;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "service.scheduling.enabled", matchIfMissing = true)
public class CryptoCurrencyProcessingScheduler {

    private final CryptoCurrencyService service;
    private final SchedulingProperties schedulingProperties;

    /**
     * By default, scheduler should process all entries from the day before (yesterday).
     * However, due to the fact that the input data has totally different dates we need to add some logic
     * for processing those 'additional'/'not up-to-date' entries as well.<br/>
     * For that purpose we provide some predefined dates which corresponds with the dates in the input data.
     */
    @Scheduled(cron = "${service.scheduling.cron}", zone = "${service.scheduling.timezone}")
    public void startCryptoProcessing() {
        LocalDate date = LocalDate.now().minusDays(1); // yesterday (by default)
        List<LocalDate> predefinedDates = schedulingProperties.getPredefinedDates();
        log.info("Predefined date list has {} remaining items", predefinedDates.size());
        if (!predefinedDates.isEmpty()) {
            date = predefinedDates.get(0);
            predefinedDates.remove(0);
        }
        updateMinMaxFactors(date);
        updateDailyOldestPriceFactors(date);
        updateDailyNewestPriceFactors(date);
    }

    /**
     *
     *
     * @param date a {@link LocalDate} instance for which data processing should take place
     */
    private void updateMinMaxFactors(LocalDate date) {
        log.info("Scheduled task UPDATE_MIN_MAX_FACTORS for {} started ...", date);
        service.updateMinMaxFactorValuesForGivenDay(date);
        log.info("Scheduled task UPDATE_MIN_MAX_FACTORS for {} finished.", date);
    }

    private void updateDailyOldestPriceFactors(LocalDate date) {
        log.info("Scheduled task UPDATE_DAILY_OLDEST_PRICE for {} started ...", date);
        service.updateDailyOldestPriceFactors(date);
        log.info("Scheduled task UPDATE_DAILY_OLDEST_PRICE for {} finished.", date);
    }

    private void updateDailyNewestPriceFactors(LocalDate date) {
        log.info("Scheduled task UPDATE_DAILY_NEWEST_PRICE for {} started ...", date);
        service.updateDailyNewestPriceFactors(date);
        log.info("Scheduled task UPDATE_DAILY_NEWEST_PRICE for {} finished.", date);
    }
}
