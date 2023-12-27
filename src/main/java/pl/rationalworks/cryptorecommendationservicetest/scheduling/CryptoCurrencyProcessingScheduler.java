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

    @Scheduled(cron = "${service.scheduling.cron}", zone = "${service.scheduling.timezone}")
    public void startCryptoProcessing() {
        LocalDate date = LocalDate.now().minusDays(1); // yesterday (by default)
        List<LocalDate> predefinedDates = schedulingProperties.getPredefinedDates();
        log.info("Predefined date list has {} items", predefinedDates.size());
        if (!predefinedDates.isEmpty()) {
            date = predefinedDates.get(0);
            predefinedDates.remove(0);
        }
        updateMinMaxFactors(date);
    }

    /**
     * This method takes a {@code date} as an input parameter. In
     *
     * @param date a {@link LocalDate} instance for which data processing should take place
     */
    private void updateMinMaxFactors(LocalDate date) {
        log.info("Scheduled task UPDATE_MIN_MAX_FACTORS with for {} started ...", date);
        service.updateMinMaxFactorValuesForGivenDay(date);
        log.info("Scheduled task UPDATE_MIN_MAX_FACTORS with for {} finished.", date);
    }

    private void updateMinPriceFactors(LocalDate date) {

    }

    private void updateMaxPriceFactors(LocalDate date) {

    }
}
