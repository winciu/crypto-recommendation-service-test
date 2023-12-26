package pl.rationalworks.cryptorecommendationservicetest.service;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.rationalworks.cryptorecommendationservicetest.data.CsvDataRecord;
import pl.rationalworks.cryptorecommendationservicetest.model.CryptoCurrency;
import pl.rationalworks.cryptorecommendationservicetest.repository.CryptoCurrencyRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CryptoCurrencyServiceTest {

    @InjectMocks
    private CryptoCurrencyService service;
    @Mock
    private CryptoCurrencyRepository cryptoCurrencyRepository;

    @Test
    void shouldSaveAllGivenDataRecords() {
        List<CsvDataRecord> dataRecords = List.of(
                new CsvDataRecord(Instant.parse("2022-01-25T22:00:00Z"), "DOGE", new BigDecimal("0.141600")),
                new CsvDataRecord(Instant.parse("2022-01-26T01:00:00Z"), "DOGE", new BigDecimal("0.141800")),
                new CsvDataRecord(Instant.parse("2022-01-26T15:00:00Z"), "DOGE", new BigDecimal("0.150200")),
                new CsvDataRecord(Instant.parse("2022-01-26T17:00:00Z"), "DOGE", new BigDecimal("0.150500")),
                new CsvDataRecord(Instant.parse("2022-01-27T12:00:00Z"), "DOGE", new BigDecimal("0.142700")),
                new CsvDataRecord(Instant.parse("2022-01-28T04:00:00Z"), "DOGE", new BigDecimal("0.141100"))
        );

        service.saveCryptos(dataRecords);

        verify(cryptoCurrencyRepository, times(1))
                .saveAll(assertArg(records -> {
                    List<CryptoCurrency> cryptoCurrencies = StreamSupport.stream(records.spliterator(), false).toList();
                    cryptoCurrencies.forEach(cc -> assertEquals("DOGE", cc.getId().getSymbol()));
                }));
        verifyNoMoreInteractions(cryptoCurrencyRepository);
    }
}