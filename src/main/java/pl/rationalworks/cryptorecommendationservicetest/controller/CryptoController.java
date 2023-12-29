package pl.rationalworks.cryptorecommendationservicetest.controller;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.rationalworks.cryptorecommendationservicetest.model.FactorPeriod;
import pl.rationalworks.cryptorecommendationservicetest.model.dto.CryptoCurrencyFactorsDto;
import pl.rationalworks.cryptorecommendationservicetest.properties.CryptoProperties;
import pl.rationalworks.cryptorecommendationservicetest.repository.CryptoRecentPriceFactors;
import pl.rationalworks.cryptorecommendationservicetest.service.CryptoCurrencyService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/cryptos")
@RequiredArgsConstructor
@Validated
public class CryptoController {

    private final CryptoCurrencyService cryptoCurrencyService;
    private final CryptoProperties cryptoProperties;

    @GetMapping(value = {"/ranking", "/ranking/{date}", "/ranking/{date}/{period}"})
    public ResponseEntity<List<String>> cryptoRanking(@PathVariable("date")
                                                      @DateTimeFormat(pattern = "yyyy-MM-dd") Optional<LocalDate> date,
                                                      @PathVariable("period") Optional<FactorPeriod> period) {
        LocalDate referenceDate = date.orElse(LocalDate.now().minusDays(1)); // yesterday (by default)
        List<String> rankingList = cryptoCurrencyService.cryptoRanking(referenceDate, period.orElse(FactorPeriod.DAY));
        return ResponseEntity.ok(rankingList);
    }

    @GetMapping(value = {"/{symbol}/factors", "/{symbol}/factors/{date}", "/{symbol}/factors/{date}/{period}"})
    public ResponseEntity<CryptoCurrencyFactorsDto> obtainCryptoPriceFactors(
        @PathVariable("symbol")
        @Pattern(regexp = "[A-Z]{3,6}", message = "Cryptocurrency symbol must match '[A-Z]{3,6}'") String symbol,
        @PathVariable("date") @DateTimeFormat(pattern = "yyyy-MM-dd") Optional<LocalDate> date,
        @PathVariable("period") Optional<FactorPeriod> period) {
        if (!cryptoProperties.getSupportedCurrencies().contains(symbol)) {
            return ResponseEntity.badRequest().build();
        }
        LocalDate referenceDate = date.orElse(LocalDate.now().minusDays(1)); // yesterday (by default)
        FactorPeriod factorPeriod = period.orElse(FactorPeriod.DAY);
        CryptoRecentPriceFactors factors = cryptoCurrencyService.getCryptoPriceFactors(symbol, referenceDate,
            factorPeriod);
        CryptoCurrencyFactorsDto dto = new CryptoCurrencyFactorsDto(factors.symbol(), referenceDate,
            factors.minPrice(), factors.maxPrice(), factors.oldestPrice(), factors.oldestPriceDate(),
            factors.newestPrice(), factors.newestPriceDate(), factorPeriod);
        return ResponseEntity.ok(dto);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ResponseEntity<?> handleConstraintViolationException(ConstraintViolationException e) {
        return new ResponseEntity<>("Validation Error: " + e.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
