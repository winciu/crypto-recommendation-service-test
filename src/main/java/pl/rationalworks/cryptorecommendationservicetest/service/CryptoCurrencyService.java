package pl.rationalworks.cryptorecommendationservicetest.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.rationalworks.cryptorecommendationservicetest.data.CsvDataRecord;
import pl.rationalworks.cryptorecommendationservicetest.model.CryptoCurrency;
import pl.rationalworks.cryptorecommendationservicetest.model.CryptoCurrencyId;
import pl.rationalworks.cryptorecommendationservicetest.repository.CryptoCurrencyRepository;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CryptoCurrencyService {

    private final CryptoCurrencyRepository cryptoCurrencyRepository;

    public void saveCryptos(List<CsvDataRecord> dataRecords) {
        List<CryptoCurrency> cryptoCurrencies = dataRecords.stream()
                .map(r -> {
                    CryptoCurrencyId id = new CryptoCurrencyId(r.timestamp(), r.symbol());
                    return new CryptoCurrency(id, r.price());
                })
                .toList();
        cryptoCurrencyRepository.saveAll(cryptoCurrencies);
    }
}
